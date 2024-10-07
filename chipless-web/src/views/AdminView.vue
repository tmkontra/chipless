<script setup lang="ts">
import { apiClient } from '@/api/client'
import { type GameAdminView } from '@/api/gen'
import { onBeforeMount, type Ref, ref } from 'vue'
import { useRoute } from 'vue-router'

const route = useRoute()

const game: Ref<GameAdminView | null> = ref(null)

const confirmBuyPlayerCode = ref('')

const cashoutModalPlayerCode = ref('')

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
      <template v-for="player in game.players" :key="player.shortCode">
        <div
          class="w-full p-6 bg-white border border-gray-200 flex flex-row justify-between items-center"
        >
          <div>
            <p>{{ player.player.name }}</p>
            <p>{{ player.player.buyCount }} buys</p>
            <p>{{ player.outstandingChips }} outstanding chips</p>
          </div>
          <div class="flex flex-row gap-2">
            <button
              v-if="confirmBuyPlayerCode == player.shortCode"
              class="btn-success"
              @click="
                () => {
                  confirmBuyPlayerCode = ''
                  doPlayerBuy(player.shortCode)
                }
              "
            >
              Confirm Buy
            </button>
            <button
              v-else
              class="btn-primary"
              @click="
                () => {
                  confirmBuyPlayerCode = player.shortCode
                }
              "
            >
              + Buy
            </button>
            <button class="btn-primary" @click="cashoutModalPlayerCode = player.shortCode">
              Cashout
            </button>
          </div>
        </div>
      </template>
    </div>

    <v-modal
      v-if="cashoutModalPlayerCode != ''"
      title="Confirm Action"
      width="sm"
      v-on:close="cashoutModalPlayerCode = ''"
    >
      <p class="text-gray-800">
        Record cashout for
        {{ game.players.find((p) => p.shortCode === cashoutModalPlayerCode).name }}
      </p>

      <div class="text-right mt-4">
        <button
          @click="showModal = false"
          class="px-4 py-2 text-sm text-gray-600 focus:outline-none hover:underline"
        >
          Cancel
        </button>
        <button
          class="mr-2 px-4 py-2 text-sm rounded text-white bg-red-500 focus:outline-none hover:bg-red-400"
        >
          Delete
        </button>
      </div>
    </v-modal>
  </main>
</template>

<style></style>
