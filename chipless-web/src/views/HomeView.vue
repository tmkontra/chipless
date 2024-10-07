<script setup lang="ts">
import { apiClient } from '@/api/client'
import { CreateGame, GameAdminView, PlayerAdminView } from '@/api/gen'
import { reactive } from 'vue'
import { useRouter } from 'vue-router'

const router = useRouter()

const newGameInput = reactive<CreateGame>({ name: '', buyinAmount: 0, buyinChips: 0 })

const submitHostGame = (e) => {
  e.preventDefault()
  apiClient
    .post('/game', newGameInput)
    .then((resp) => resp.data as GameAdminView)
    .then((game) => router.push(`/admin/${game.adminCode}`))
    .catch((err) => alert(err))
}

const joinGameInput = reactive({ code: '', playerName: '' })

const submitJoinGame = (e) => {
  e.preventDefault()
  apiClient
    .post(`/game/${joinGameInput.code}/join?playerName=${joinGameInput.playerName}`)
    .then((resp) => resp.data as PlayerAdminView)
    .then((player) => {
      router.push(`/player/${player.shortCode}`)
    })
    .catch((err) => alert(err))
}

const submitReJoinGame = (e) => {}
</script>

<template>
  <main class="w-full flex justify-around">
    <div>
      <form class="h-fit bg-white shadow-md rounded px-8 pt-6 pb-8 mb-4" @submit="submitHostGame">
        <h3 class="mb-2">Host a Game</h3>
        <div class="mb-2">
          <label>Game Name</label>
          <input type="text" v-model="newGameInput.name" required />
        </div>
        <div class="mb-2">
          <label>Chips per Buyin</label>
          <input type="number" step="1" min="1" v-model="newGameInput.buyinChips" required />
        </div>
        <div class="mb-4">
          <label>Dollars per Buyin</label>
          <input type="number" step="0.01" min="0" v-model="newGameInput.buyinAmount" required />
        </div>
        <button class="btn-primary" type="submit">Start</button>
      </form>
    </div>

    <div>
      <form class="h-fit bg-white shadow-md rounded px-8 pt-6 pb-8 mb-4" @submit="submitJoinGame">
        <h3 class="mb-2">Join a Game</h3>
        <div class="mb-2">
          <label>Game Code</label>
          <input type="text" v-model="joinGameInput.code" />
        </div>
        <div class="mb-4">
          <label>Player Name</label>
          <input type="text" v-model="joinGameInput.playerName" />
        </div>
        <button class="btn-primary" type="submit">Join</button>
      </form>
    </div>

    <form class="h-fit bg-white shadow-md rounded px-8 pt-6 pb-8 mb-4" @submit="submitReJoinGame">
      <h3 class="mb-2">Re-Join Game</h3>
      <div class="mb-4">
        <label>Player Code</label>
        <input type="text" v-model="playerCode" />
      </div>
      <button class="btn-primary" type="submit">Re-Join</button>
    </form>
  </main>
</template>
