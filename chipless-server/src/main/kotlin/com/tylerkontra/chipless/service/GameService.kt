package com.tylerkontra.chipless.service

import com.tylerkontra.chipless.model.Game
import com.tylerkontra.chipless.model.Money
import com.tylerkontra.chipless.model.Player
import com.tylerkontra.chipless.model.ShortCode
import com.tylerkontra.chipless.storage.game.GameRepository
import com.tylerkontra.chipless.storage.hand.Hand
import com.tylerkontra.chipless.storage.hand.HandRepository
import com.tylerkontra.chipless.storage.player.Cashout
import com.tylerkontra.chipless.storage.player.PlayerRepository
import jakarta.persistence.EntityManager
import org.springframework.stereotype.Service
import kotlin.jvm.optionals.getOrNull

@Service
class GameService(
    private val gameRepository: GameRepository,
    private val playerRepository: PlayerRepository,
    private val handRepository: HandRepository,
    private val entityManager: EntityManager,
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

    fun findPlayerByCode(code: ShortCode): Player? {
        return playerRepository.findPlayerByShortCode(code.value).map { Player.fromStorage(it) }.getOrNull()
    }

    fun findGameByAdminCode(adminCode: ShortCode): Game? {
        return gameRepository.findGameByAdminCode(adminCode.value).map { Game.fromStorage(it) }.getOrNull()
    }

    fun playerBuy(g: Game, p: Player): Player {
        val updated = playerRepository.incrementPlayerBuyCount(p.shortCode.value)
        if (updated == 0) {
            throw Exception("player buying failed")
        }
        return Player.fromStorage(playerRepository.findPlayerByShortCode(p.shortCode.value).get())
    }

    fun playerCashout(g: Game, p: Player, chipCount: Int): Player {
        val p = playerRepository.findById(p.id).get()
        p.cashouts.add(Cashout(p, chipCount))
        val updated = playerRepository.save(p)
        return Player.fromStorage(updated)
    }

    private fun createHand(game: Game, sequence: Int, excludePlayerIds: List<Long>): Hand {
        return handRepository.save(
            Hand(
                sequence,
                entityManager.getReference(com.tylerkontra.chipless.storage.game.Game::class.java, game.id),
                game.players.map { playerRef(it) }.toMutableList(),
                game.players.filter { excludePlayerIds.contains(it.id) }.map { playerRef(it) }.toMutableList(),
            )
        )
    }

    private fun playerRef(it: Player): com.tylerkontra.chipless.storage.player.Player =
        entityManager
            .getReference(
                com.tylerkontra.chipless.storage.player.Player::class.java,
                it.id
            )

    fun startHand(g: Game, excludePlayerIds: List<Long>): Game {
        var game = gameRepository.findById(g.id).get()
        g.latestHand()?.apply {
            if (isFinished) {
                val h = createHand(g, sequence + 1, excludePlayerIds)
                game.hands.add(h)
            } else {
                throw Exception("cannot start hand")
            }
        } ?: apply {
            println("first hand!")
            val h = createHand(g, 1, excludePlayerIds)
            game.hands.add(h)
        }
        val updated = gameRepository.save(game)
        return Game.fromStorage(updated)
    }

    companion object {
        interface CreateGame {
            val name: String
            fun getBuyinAmount(): Money
            val buyinChips: Int
        }
    }
}