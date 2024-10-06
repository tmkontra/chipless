package com.tylerkontra.chipless.service

import com.tylerkontra.chipless.model.*
import com.tylerkontra.chipless.storage.game.GameRepository
import com.tylerkontra.chipless.storage.hand.*
import com.tylerkontra.chipless.storage.hand.BettingAction
import com.tylerkontra.chipless.storage.hand.BettingRound
import com.tylerkontra.chipless.storage.hand.Hand
import com.tylerkontra.chipless.storage.hand.HandPlayer
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
    private val bettingRoundRepository: BettingRoundRepository,
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

    fun addPlayer(code: ShortCode, playerName: String): Player {
        var game = gameRepository.findGameByShortCode(code.value)
        if (game.isEmpty) {
            throw Exception("no game found")
        }
        var player = playerRepository.save(
            com.tylerkontra.chipless.storage.player.Player(
                playerName, game.get())
        )
        return Player.fromStorage(player)
    }

    fun findGameByCode(code: ShortCode): Game? {
        return gameRepository.findGameByShortCode(code.value).map { Game.fromStorage(it) }.getOrNull()
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
        var (sittingOut, playing) = game.playerChips().partition { excludePlayerIds.contains(it.id) }
        var hand =  handRepository.save(
            Hand(
                sequence,
                entityManager.getReference(com.tylerkontra.chipless.storage.game.Game::class.java, game.id),
                mutableListOf(),
                sittingOut.map { playerRef(it.player) } .toMutableList(),
            )
        )
        hand.players.addAll(
            playing.mapIndexed { index, player ->
                HandPlayer(
                    playerRef(player.player),
                    hand,
                    index+1,
                    player.availableChips,
                )
            }.toMutableList())
        val bettingRound = BettingRound(
            1,
            entityManager.getReference(Hand::class.java, hand.id),
            hand.players.map { p ->
                entityManager.getReference(
                    com.tylerkontra.chipless.storage.player.Player::class.java,
                    p.player.id
                )
            }.toMutableList<com.tylerkontra.chipless.storage.player.Player>(), // TODO: exclude fold
            mutableListOf(),
        )
        hand.rounds.add(bettingRound)
        hand = handRepository.save(hand)
        return hand
    }

    private fun playerRef(it: Player): com.tylerkontra.chipless.storage.player.Player =
        entityManager
            .getReference(
                com.tylerkontra.chipless.storage.player.Player::class.java,
                it.id
            )

    fun startHand(g: Game, excludePlayerIds: List<Long>): Game {
        var game = gameRepository.findById(g.id).get()
        g.latestHand()?.let {
            if (it.isFinished) {
                val h = createHand(g, it.sequence + 1, excludePlayerIds)
                game.hands.add(h)
            } else {
                throw ChiplessErrror.InvalidStateError("cannot start hand, previous hand not finished")
            }
        } ?: apply {
            val h = createHand(g, 1, excludePlayerIds)
            game.hands.add(h)
        }
        val updated = gameRepository.save(game)
        return Game.fromStorage(updated)
    }

    fun getPlayerHandViewByCode(playerCode: ShortCode): PlayerHandView {
        var p = findPlayerByCode(playerCode) ?: throw ChiplessErrror.ResourceNotFoundError.ofEntity("player")
        val g = findGameByCode(p.game.shortCode) ?: throw ChiplessErrror.ResourceNotFoundError.ofEntity("game")
        val hand = g.latestHand() ?: throw ChiplessErrror.InvalidStateError("game has no current hand")
        return PlayerHandView(p, hand)
    }

    fun doPlayerAction(hand: PlayerHandView, action: PlayerAction): PlayerHandView {
        if (!hand.isPlayerTurn()) {
            throw ChiplessErrror.InvalidStateError("it is not your turn to act")
        }
        if (!hand.allowAction(action)) {
            throw ChiplessErrror.InvalidStateError("the requested action is not allowed")
        }
        var row = handRepository.findById(hand.hand.id).orElseThrow { ChiplessErrror.ResourceNotFoundError.ofEntity("hand") }
        var newAction = BettingAction(
            hand.nextActionSequence(),
            playerRef(hand.player),
            row.rounds.last(),
            action.actionType,
        )
        if (action is PlayerAction.ChipAction) {
            newAction.chipCount = action.chipCount
        }
        row.rounds.last().actions.add(newAction)
        if (row.isComplete()) {
            row.uncontestedWin()
        }
        var updatedHand = handRepository.save(row)
        if (row.isComplete()) {
            startHand(Game.fromStorage(row.game), listOf())
        }
        return PlayerHandView(hand.player, com.tylerkontra.chipless.model.Hand.fromStorage(updatedHand))
    }

    companion object {
        interface CreateGame {
            val name: String
            fun getBuyinAmount(): Money
            val buyinChips: Int
        }
    }
}