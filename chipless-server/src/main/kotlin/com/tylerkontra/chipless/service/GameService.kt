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
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import java.util.Collections
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

    private fun createHand(game: Game, input: HandInput): Hand {
        val sequence = input.sequence
        val excludePlayerIds = input.excludePlayerIds
        var (sittingOut, playing) = game.playerChips().partition { excludePlayerIds.contains(it.id) || it.availableChips <= 0 }
        var previousHand = game.latestHand()
        var hand =  handRepository.save(
            Hand(
                sequence,
                entityManager.getReference(com.tylerkontra.chipless.storage.game.Game::class.java, game.id),
                mutableListOf(),
                sittingOut.map { playerRef(it.player) }.toMutableList(),
            )
        )
        if (input.hasValidSeatOrder(playing.map { it.id })) {
            playing = playing.sortedWith(input.compareBySeatOrder { it.player })
        } else if (previousHand != null) {
            val players = previousHand.players.filterNot { excludePlayerIds.contains(it.player.id) }.toMutableList()
            Collections.rotate(players, -1)
            val withNewSeatOrder = input.copy(seatOrderPlayerIds = players.map { it.player.id })
            playing = playing.sortedWith(withNewSeatOrder.compareBySeatOrder { it.player })
        }
        val handPlayers = playing.mapIndexed { index, player ->
            HandPlayer(
                playerRef(player.player),
                hand,
                index + 1,
                player.availableChips,
            )
        }
        hand.players.addAll(handPlayers.toMutableList())
        val bettingRound = newBettingRound(hand)
        hand.rounds.add(bettingRound)
        hand = handRepository.save(hand)
        return hand
    }

    private fun newBettingRound(hand: Hand) = BettingRound(
        hand.rounds.size+1,
        entityManager.getReference(Hand::class.java, hand.id),
        hand.nextRoundPlayers().map { p ->
            entityManager.getReference(
                com.tylerkontra.chipless.storage.player.Player::class.java,
                p.player.id
            )
        }.toMutableList(), // TODO: exclude fold
        mutableListOf(),
    )

    private fun playerRef(it: Player): com.tylerkontra.chipless.storage.player.Player =
        entityManager
            .getReference(
                com.tylerkontra.chipless.storage.player.Player::class.java,
                it.id
            )

    fun startHand(g: Game, input: HandInput): Game {
        var game = gameRepository.findById(g.id).get()
        g.latestHand()?.let {
            if (it.isFinished) {
                val h = createHand(g, input.copy(sequence = it.sequence + 1))
                game.hands.add(h)
            } else {
                throw ChiplessErrror.InvalidStateError("cannot start hand, previous hand not finished")
            }
        } ?: apply {
            val h = createHand(g, input.copy(sequence = 1))
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
            throw ChiplessErrror.InvalidStateError("the requested action (${action}) is not allowed: ${hand.availableActions()}")
        }
        var handRecord = handRepository.findById(hand.hand.id).orElseThrow { ChiplessErrror.ResourceNotFoundError.ofEntity("hand") }
        var newAction = BettingAction(
            hand.nextActionSequence(),
            playerRef(hand.player),
            handRecord.rounds.last(),
            action.actionType,
        )
        if (action is PlayerAction.ChipAction) {
            newAction.chipCount = action.chipCount
        }
        handRecord.rounds.last().actions.add(newAction)
        if (handRecord.isComplete()) {
            handRecord.uncontestedWin()
        }
        var updatedHand = handRepository.save(handRecord)
        var updatedHandView = com.tylerkontra.chipless.model.Hand.fromStorage(updatedHand)
        return PlayerHandView(hand.player, updatedHandView)
    }

    fun nextBettingRound(game: Game): com.tylerkontra.chipless.model.Hand {
        game.latestHand()?.let { hand ->
            var hand = handRepository.findById(hand.id).getOrNull() ?: throw ChiplessErrror.ResourceNotFoundError.ofEntity("hand")
            if (com.tylerkontra.chipless.model.Hand.fromStorage(hand).currentRound()?.isClosed() != true) {
                throw ChiplessErrror.InvalidStateError("current betting round is not closed")
            }
            var newRound = newBettingRound(hand)
            hand.rounds.add(newRound)
            val updatedHand = handRepository.save(hand)
            val updatedHandView = com.tylerkontra.chipless.model.Hand.fromStorage(updatedHand)
            return updatedHandView
        } ?: throw ChiplessErrror.InvalidStateError("game has no current hand")
    }

    companion object {
        val logger = LoggerFactory.getLogger(GameService::class.java)

        interface CreateGame {
            val name: String
            fun getBuyinAmount(): Money
            val buyinChips: Int
        }

        data class HandInput private constructor(
            val excludePlayerIds: List<Long> = listOf(),
            // dealer id first
            val seatOrderPlayerIds: List<Long> = listOf(),
            val sequence: Int = 0,
        ) {
            constructor(excludePlayerIds: List<Long> = listOf(), seatOrderPlayerIds: List<Long> = listOf()): this(excludePlayerIds, seatOrderPlayerIds, 0) {}
            private val seatOrder = compareBy<Player> { seatOrderPlayerIds.indexOf(it.id) }

            fun <E> compareBySeatOrder(selector: (E) -> Player) = compareBy(seatOrder, selector)

            fun hasValidSeatOrder(playerIds: List<Long>): Boolean {
                if (seatOrderPlayerIds.isEmpty()) return false
                if (seatOrderPlayerIds.toSet().intersect(playerIds.toSet()).size < playerIds.size)
                    throw IllegalArgumentException("seat order must specify all players")
                return true
            }
        }
    }
}