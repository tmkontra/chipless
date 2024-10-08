<script setup lang="ts">
import { apiClient } from '@/api/client'
import { type GameAdminView, type StartHandData } from '@/api/gen'
import { onBeforeMount, type Ref, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import AdminStartHandView from '../components/StartHand.vue'
import TableView from '@/components/table/PokerTable.vue'

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

const startHand = (seatOrderPlayerIds: Array<number>) => {
  const query: StartHandData['query'] = {
    seatOrderPlayerIds
  }
  apiClient
    .post(`/gameAdmin/${route.params.code}/hand`, undefined, {
      params: query,
      paramsSerializer: {
        indexes: null
      }
    })
    .then((resp) => resp.data as GameAdminView)
    .then((gameView) => {
      game.value = gameView
    })
}
</script>

<template>
  <main v-if="game" class="w-full px-4 md:w-4/5 xl:w-3/4 md:mx-auto lg:px-0 mb-8">
    <div class="mb-4">
      <h1>{{ game.game.name }}</h1>
      <div>
        <p>Join Code: {{ game.game.shortCode }}</p>
      </div>
    </div>
    <hr class="mb-4" />
    <div class="mb-4">
      <h3 v-if="game.game.players.length == 0">No Players Yet</h3>
      <h3 v-else class="mb-2">Players</h3>
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
            <div
              v-if="confirmBuyPlayerCode == player.shortCode"
              class="flex flex-row gap-2 items-center"
            >
              <button class="px-2 h-fit border" @click="confirmBuyPlayerCode = ''">x</button>
              <button
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
            </div>
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
    <div class="">
      <h3 class="mb-2">Current Hand</h3>
      <div
        class="w-full p-6 bg-white border border-gray-200 flex flex-row justify-between items-center"
      >
        <div v-if="game.currentHand?.isFinished">
          <p>previous hand</p>
          <button class="btn-primary" @click="startHand([])">Start Next Hand</button>
        </div>
        <TableView v-else-if="game.currentHand" :hand.sync="game.currentHand" />
        <div v-else="game">
          <AdminStartHandView
            :game="game"
            :sitting-out="game.newPlayers"
            :hand-players="game.nextHandOrder"
            @start-hand="startHand"
          />
        </div>
      </div>
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
      <input
        id="chipCount"
        type="number"
        step="1"
        class="flex-auto"
        autocomplete="off"
        v-model="cashoutChipCount"
      />
    </div>
    <div class="flex justify-end items-center gap-4">
      <button type="button" label="Cancel" class="btn-secondary" @click="cashoutVisible = false">
        Cancel
      </button>
      <button
        type="button"
        class="btn-primary"
        @click="() => submitCashout(cashoutModalPlayerCode)"
      >
        Submit
      </button>
    </div>
  </p-dialog>
</template>

<style></style>
