<script setup lang="ts">
import type { PlayerHandView } from '@/api/gen'
import ChipStack from './chips/ChipStack.vue'

const props = defineProps<{
  hand: PlayerHandView
}>()
</script>

<template>
  <div class="flex flex-col gap-4">
    <ChipStack :available-chips="props.hand.availableChips" />
    <div class="flex flex-row justify-around">
      <template v-for="action in hand.availableActions">
        <div v-if="action.actionType === 'FOLD'">
          <button class="btn-primary" :disabled="!props.hand.isTurn">Fold</button>
        </div>
        <div v-else-if="action.actionType == 'CHECK'">
          <button class="btn-primary" :disabled="!props.hand.isTurn">Check</button>
        </div>
        <div v-else-if="action.actionType == 'CALL'">
          <button class="btn-primary" :disabled="!props.hand.isTurn">
            Call {{ action.chipCount }}
          </button>
        </div>
        <div v-else-if="action.actionType == 'BET'">
          <button class="btn-primary" :disabled="!props.hand.isTurn">Bet</button>
        </div>
        <div v-else-if="action.actionType == 'RAISE'">
          <button class="btn-primary" :disabled="!props.hand.isTurn">Raise</button>
        </div>
      </template>
    </div>
  </div>
</template>

<style scoped></style>
