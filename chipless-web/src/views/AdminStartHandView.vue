<script setup lang="ts">
import { apiClient } from '@/api/client'
import { type GameAdminView } from '@/api/gen'
import { onBeforeMount, type Ref, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'

const route = useRoute()
const router = useRouter()

const game: Ref<GameAdminView | null> = ref(null)

const confirmBuyPlayerCode = ref('')

const cashoutModalPlayerCode = ref('')
const cashoutVisible = ref(false)
const cashoutChipCount = ref(0)

const fetchGame = () => {
  apiClient
    .get(`/gameAdmin/${route.params.code}`)
    .then((resp) => resp.data as GameAdminView)
    .then((gameView) => {
      game.value = gameView
    })
}

onBeforeMount(() => fetchGame())

const doPlayerBuy = (playerCode: string) => {
  apiClient.post(`/gameAdmin/${route.params.code}/player/${playerCode}/buy`).then(() => fetchGame())
}

const submitCashout = (playerCode: string) => {
  apiClient
    .post(
      `/gameAdmin/${route.params.code}/player/${playerCode}/cashout?chipCount=${cashoutChipCount.value}`
    )
    .then(() => (cashoutVisible.value = false))
    .then(() => fetchGame())
}

const confirmStartHand = () => {
  console.log('start hand')
  router.push(window.location + '/start')
}
</script>

<template>
  <main v-if="game" class="w-full px-4 lg:w-1/2 lg:mx-auto lg:px-0 mb-8"></main>
  <h1>{{ game.game.name }}</h1>
  <h3>Start Hand</h3>
</template>

<style></style>
