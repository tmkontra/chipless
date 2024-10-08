<script setup lang="ts">
import { actionTypeName } from '@/api/extension'
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
  <div class="player flex flex-col justify-center items-center" :class="{ active: isTurn }">
    <p>{{ player.player.name }}</p>
    <p v-if="player.isDealer">D</p>
    <p>
      <span v-if="lastAction">{{ actionTypeName(lastAction) }}</span
      >{{ ' ' }} {{ player.wager }}
    </p>
  </div>
</template>

<style scoped>
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
</style>
