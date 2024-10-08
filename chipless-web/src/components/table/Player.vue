<script setup lang="ts">
import { actionDisplay } from '@/api/extension'
import type { HandPlayer, PlayerAction, PlayerAdminView } from '@/api/gen'
import { onMounted, ref } from 'vue'

const { player, radius, index, playerCount, parentId, isTurn } = defineProps<{
  player: HandPlayer
  radius: number
  index: number
  playerCount: number
  parentId: string
  isTurn: boolean
  lastAction?: PlayerAction
}>()

const top = ref('')
const left = ref('')

const size = 80
const dimension = size + 'px'

const calc = () => {
  const div = 360 / playerCount
  const parent = document.getElementById(parentId)
  const offsetToParentCenter = (parent?.offsetWidth || 0) / 2 //assumes parent is square
  const offsetToChildCenter = size / 2
  const totalOffset = offsetToParentCenter - offsetToChildCenter

  const y = Math.sin(div * (index + 1) * (Math.PI / 180) + (3 / 2) * Math.PI) * radius
  const x = Math.cos(div * (index + 1) * (Math.PI / 180) + (3 / 2) * Math.PI) * radius

  top.value = (y + totalOffset).toString() + 'px'
  left.value = (x + totalOffset).toString() + 'px'
}

onMounted(() => calc())
</script>

<template>
  <div
    class="player flex flex-col justify-center items-center"
    :class="{ active: isTurn, winner: player.winnings ?? 0 > 0 }"
  >
    <p>
      {{ player.player.name }}
      <div v-if="player.isDealer" class="dealer-button">{{ ' ' }}</div>
    </p>
    <p>
      <span v-if="player.winnings">Win {{ player.winnings }}</span>
      <span v-else-if="lastAction">{{ actionDisplay(lastAction) }}</span>
    </p>
  </div>
</template>

<style scoped>
.dealer-button::before {
  content: 'D';
}
.dealer-button {
  border: 3px gray dotted;
  background-color: skyblue;
  border-radius: 50%;
  width: 30px;
  height: 30px;

  display: inline-flex;
  align-items: center;
  justify-content: center;
}
.player.active {
  background-color: var(--p-primary-300);
  border: 3px var(--p-primary-600) solid;
}
.player {
  position: absolute;
  top: v-bind(top);
  left: v-bind(left);
  width: v-bind(dimension);
  height: v-bind(dimension);
  background-color: var(--p-primary-300);
  border-radius: 16px;
}
.player.winner {
  border-color: #059669;
}
</style>
