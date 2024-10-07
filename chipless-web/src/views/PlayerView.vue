<script setup lang="ts">
import { apiClient } from '@/api/client'
import { type PlayerAdminView, type PlayerHandView } from '@/api/gen'
import { HttpStatusCode, type AxiosError } from 'axios'
import { onBeforeMount, Ref, ref } from 'vue'
import { useRoute } from 'vue-router'

const route = useRoute()

const playerError = ref('')
const player: Ref<PlayerAdminView | null> = ref(null)

const hand: Ref<PlayerHandView | null> = ref(null)
const handError = ref('')

onBeforeMount(() => {
  apiClient
    .get(`/player/${route.params.code}`)
    .then((resp) => resp.data as PlayerAdminView)
    .then((playerView) => {
      player.value = playerView
    })

  apiClient
    .get(`player/${route.params.code}/hand`)
    .then((resp) => resp.data as PlayerHandView)
    .then((view) => {
      hand.value = view
    })
    .catch((err: AxiosError) => {
      if (err.response?.status == HttpStatusCode.Conflict) {
        handError.value = err.response?.data?.error.message
      } else if (err.response?.status == HttpStatusCode.NotFound) {
        playerError.value = 'No player found'
      }
    })
})
</script>

<template>
  <main class="w-full px-4 lg:w-1/2 lg:mx-auto lg:px-0">
    <p v-if="playerError != ''">{{ playerError }}</p>
    <div v-if="player" class="w-full border rounded-lg p-4">
      <div class="mb-4">
        <h1>{{ player?.player?.name }}</h1>
        <div>
          <p>Player Code: {{ player?.shortCode }}</p>
        </div>
      </div>
      <p v-if="handError">Waiting For Hand To Start</p>
      <div v-if="hand">
        <p>Hand</p>
      </div>
    </div>
  </main>
</template>

<style></style>
