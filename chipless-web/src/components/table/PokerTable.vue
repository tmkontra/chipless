<script setup lang="ts">
import type { Hand } from '@/api/gen'
import Player from '@/components/table/Player.vue'

const { hand } = defineProps<{
  hand: Hand
}>()

const tableRadius = 150
const tableSize = tableRadius + 'px'
</script>

<template>
  <div class="w-full flex flex-row justify-center items-center overflow-scroll">
    <div class="poker-table-wrapper">
      <div id="poker-table" class="bg-surface-400">
        <Player
          v-for="(player, index) in hand.players"
          :player="player"
          :key="player.player.id"
          :radius="tableRadius"
          :index="index"
          :playerCount="hand.players.length"
          :parent-id="'poker-table'"
          :is-turn="hand.currentRound?.currentPlayer.id == player.player.id"
        />
      </div>
    </div>
  </div>
</template>

<style>
.poker-table-wrapper {
  display: inline-block;
  overflow: auto;
}
#poker-table {
  position: relative;
  min-width: v-bind(tableSize);
  min-height: v-bind(tableSize);
  border-radius: v-bind(tableSize);
  margin: v-bind(tableSize);
}
</style>
