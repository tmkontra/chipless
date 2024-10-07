<script setup lang="ts">
import { apiClient } from '@/api/client'
import { GameAdminView } from '@/api/gen'
import { onBeforeMount, Ref, ref } from 'vue'
import { useRoute } from 'vue-router'

const route = useRoute()

const game: Ref<GameAdminView | null> = ref(null)

onBeforeMount(() => {
  apiClient
    .get(`/gameAdmin/${route.params.code}`)
    .then((resp) => resp.data as GameAdminView)
    .then((gameView) => {
      console.log('gameView', gameView)
      game.value = gameView
    })
})
</script>

<template>
  <main v-if="game" class="w-full">
    <div class="mb-4">
      <h1>{{ game.game.name }}</h1>
      <div>
        <p>Join Code: {{ game.game.shortCode }}</p>
      </div>
    </div>
    <div class="w-1/2 mx-auto">
      <h3 v-if="game.game.players.length == 0">No Players Yet</h3>
      <template v-for="player in game.game.players" :key="player.id">
        <div class="w-full p-6 bg-white border border-gray-200">
          <p>{{ player.name }}</p>
        </div>
      </template>
    </div>
  </main>
</template>

<style></style>
