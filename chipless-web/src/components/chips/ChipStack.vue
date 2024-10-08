<script setup lang="ts">
import { computed, ref } from 'vue'
import Chip from '@/components/chips/Chip.vue'

const props = defineProps<{
  isTurn: boolean
  availableChips: number
}>()

const currentWager = defineModel<number>('currentWager', { required: true })

const chipsRemaining = computed(() => props.availableChips - currentWager.value)

const addWager = (value: number) => (currentWager.value += value)

const clearWager = () => (currentWager.value = 0)
</script>

<template>
  <div class="flex flex-row justify-center gap-4 py-4 items-center">
    <div class="flex flex-col items-center">
      <p>Available Chips</p>
      <p>{{ availableChips }}</p>
    </div>

    <div class="flex flex-row gap-2 chips">
      <Chip
        v-for="value in [1, 5, 10, 25, 100]"
        :value="value"
        :disabled="!isTurn || chipsRemaining < value"
        :add-wager="addWager"
      />
    </div>

    <div class="flex flex-col items-center">
      <p>To Bet</p>
      <p>{{ currentWager }}</p>
      <button class="btn-secondary" :disabled="!isTurn" @click="clearWager()">Clear</button>
    </div>
  </div>
</template>

<style scoped></style>
