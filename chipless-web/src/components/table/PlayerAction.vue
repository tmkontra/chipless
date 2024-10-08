<script setup lang="ts">
import type { actionType, PlayerAction, PlayerHandView } from '@/api/gen'

const props = defineProps<{
  hand: PlayerHandView
  action: PlayerAction
}>()

const emit = defineEmits<{
  (e: 'click', actionType: PlayerAction): void
}>()

const buttonText = (() => {
  switch (props.action.actionType) {
    case 'FOLD':
      return 'Fold'
    case 'CHECK':
      return 'Check'
    case 'BET':
      return 'Bet'
    case 'RAISE':
      return 'Raise'
    case 'CALL':
      return 'Call ' + props.action.chipCount
    default:
      return ''
  }
})()
</script>

<template>
  <button
    class="btn-primary px-8 py-2"
    :disabled="!props.hand.isTurn"
    @click="$emit('click', action)"
  >
    {{ buttonText }}
  </button>
</template>

<style scoped></style>
