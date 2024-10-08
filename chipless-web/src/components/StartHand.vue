<script setup lang="ts">
import type { GameAdminView, PlayerAdminView } from '@/api/gen'
import { computed, ref, watch } from 'vue'

const props = defineProps<{
  game: GameAdminView
  sittingOut: Array<PlayerAdminView>
  handPlayers: Array<PlayerAdminView>
}>()

const emit = defineEmits<{
  (e: 'startHand', seatOrderPlayerIds: Array<number>): void
}>()

const hasBankrupt = ref(props.game.playersBankrupt.length > 0)
const playerList = ref([props.sittingOut, props.handPlayers])
const seatOrderPlayerIds = computed(() => playerList.value[1].map(({ player }) => player.id))

watch(
  () => props.sittingOut,
  (newValue) => {
    playerList.value = [newValue, playerList.value[1]]
  }
)
watch(
  () => props.handPlayers,
  (newValue) => {
    playerList.value = [playerList.value[0], newValue]
  }
)
watch(
  () => props.game.playersBankrupt,
  (newValue) => {
    hasBankrupt.value = newValue.length > 0
  }
)
</script>

<template>
  <div class="mb-2">
    <h3>Start Hand</h3>
    <p>
      Select the players that are participating in the hand, and confirm the betting order. The
      player list begins with the player seated left of the dealer.
    </p>
    <p v-if="hasBankrupt" class="underline decoration-dotted">
      Bankrupt players must first buy-in to join the hand.
    </p>
  </div>
  <p-pick-list
    v-model="playerList"
    dataKey="shortCode"
    :showSourceControls="false"
    breakpoint="1024px"
  >
    <template #sourceheader>
      <div class="flex flex-row justify-center p-2 underline">Players Sitting Out</div>
    </template>
    <template #targetheader>
      <div class="flex flex-row justify-center p-2 underline">Players In Hand</div>
    </template>
    <template #option="{ option }">
      {{ option.player.name }}
    </template>
  </p-pick-list>
  <div class="lg:my-6 flex flex-row justify-center lg:justify-end items-center">
    <button class="btn-primary" @click="$emit('startHand', seatOrderPlayerIds)">Start Hand</button>
  </div>
</template>

<style></style>
