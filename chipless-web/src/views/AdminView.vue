<script setup lang="ts">
import { apiClient } from '@/api/client'
import { type GameAdminView } from '@/api/gen'
import { onBeforeMount, type Ref, ref } from 'vue'
import { useRoute } from 'vue-router'

const route = useRoute()

const game: Ref<GameAdminView | null> = ref(null)

const confirmBuyPlayerCode = ref('')

const cashoutModalPlayerCode = ref('')
const cashoutVisible = ref(false)

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
            <button
              class="btn-primary"
              @click="
                () => {
                  cashoutModalPlayerCode = player.shortCode
                  cashoutVisible = true
                }
              "
            >
              Cashout
            </button>
          </div>
        </div>
      </template>
    </div>
  </main>
  <p-dialog
    v-model:visible="cashoutVisible"
    modal
    header="Confirm Cashout"
    :style="{ width: '25rem' }"
  >
    <div class="flex gap-4 mb-4">
      <label for="chipCount" class="font-semibold w-24">Chip Count</label>
      <input id="chipCount" type="number" step="1" class="flex-auto" autocomplete="off" />
    </div>
    <div class="flex justify-end items-center gap-4">
      <button type="button" label="Cancel" class="btn-secondary" @click="cashoutVisible = false">
        Cancel
      </button>
      <button type="button" class="btn-primary" @click="cashoutVisible = false">Submit</button>
    </div>
  </p-dialog>
</template>

<style></style>
