package com.tylerkontra.chipless.service

import com.tylerkontra.chipless.model.Game
import com.tylerkontra.chipless.model.Money
import com.tylerkontra.chipless.model.Player
import com.tylerkontra.chipless.storage.game.GameRepository
import com.tylerkontra.chipless.storage.player.PlayerRepository
import org.springframework.stereotype.Service
import kotlin.jvm.optionals.getOrNull

@Service
class GameService(
    private val gameRepository: GameRepository,
    private val playerRepository: PlayerRepository
) {
    fun createGame(game: CreateGame): Game {
        var g = com.tylerkontra.chipless.storage.game.Game(
            game.name,
            game.getBuyinAmount().toCents(),
            game.buyinChips,
        )
        var created = gameRepository.save(g)
        return Game.fromStorage(created)
    }

    fun addPlayer(code: String, playerName: String): Player {
        var game = gameRepository.findGameByShortCode(code)
        if (game.isEmpty) {
            throw Exception("no game found")
        }
        var player = playerRepository.save(
            com.tylerkontra.chipless.storage.player.Player(
                playerName, game.get())
        )
        return Player.fromStorage(player)
    }

    fun findGameByCode(code: String): Game? {
        return gameRepository.findGameByShortCode(code).map { Game.fromStorage(it) }.getOrNull()
    }

    companion object {
        interface CreateGame {
            val name: String
            fun getBuyinAmount(): Money
            val buyinChips: Int
        }
    }
}