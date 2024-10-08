<script setup lang="ts">
import type { PlayerAction, PlayerHandView } from '@/api/gen'
import ChipStack from './chips/ChipStack.vue'
import { ref } from 'vue'
import CPlayerAction from './table/PlayerAction.vue'

const props = defineProps<{
  hand: PlayerHandView
}>()

const emit = defineEmits<{
  (e: 'submitAction', action: PlayerAction): void
}>()

const currentWager = ref(0)

const handleAction = (action: PlayerAction) => {
  switch (action.actionType) {
    case 'CHECK':
    case 'FOLD':
    case 'CALL':
      emit('submitAction', action)
      return
    case 'BET':
    case 'RAISE':
      emit('submitAction', {
        actionType: action.actionType,
        chipCount: currentWager.value
      })
      return
    default:
      // todo: TOAST
      return
  }
}
</script>

<template>
  <div class="flex flex-col gap-4">
    <ChipStack
      :is-turn="props.hand.isTurn"
      :available-chips="props.hand.availableChips"
      v-model:current-wager="currentWager"
    />
    <div class="flex flex-row justify-around">
      <template v-for="action in hand.availableActions" :key="action.actionType">
        <CPlayerAction :action="action" :hand="hand" @click="handleAction" />
      </template>
    </div>
  </div>
</template>

<style scoped></style>
