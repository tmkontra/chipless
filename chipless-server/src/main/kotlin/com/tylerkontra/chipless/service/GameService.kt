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
            game.buyinMoney().toCents(),
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
        var previousHand = game.latestHand()
        var hand =  handRepository.save(
            Hand(
                sequence,
                entityManager.getReference(com.tylerkontra.chipless.storage.game.Game::class.java, game.id),
                mutableListOf(),
                mutableListOf(),
            )
        )

        var (playing, sittingOut) = game.playerChips().partition { input.seatOrderPlayerIds.playerIds.contains(it.id) }
        if (input.hasValidSeatOrder(playing.map { it.id })) {
            playing = playing.sortedWith(input.compareBySeatOrder { it.player })
        } else if (previousHand != null) {
            val nextRoundPlayers = SmallBlindSeatOrder(previousHand.players.map { it.player.id })
            val withNewSeatOrder = input.copy(seatOrderPlayerIds = nextRoundPlayers.nextHandOrder())
            playing = game.playerChips().filter { it.availableChips > 0 }.sortedWith(withNewSeatOrder.compareBySeatOrder { it.player })
        } else {
            playing = game.playerChips().filter { it.availableChips > 0 }
        }
        val handPlayers = playing.mapIndexed { index, player ->
            HandPlayer(
                playerRef(player.player),
                hand,
                index + 1,
                player.availableChips,
            )
        }
        hand.players.addAll(handPlayers)
        hand.sittingOut.addAll(sittingOut.map { playerRef(it.player) })

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
                throw ChiplessError.InvalidStateError("cannot start hand, previous hand not finished")
            }
        } ?: apply {
            val h = createHand(g, input.copy(sequence = 1))
            game.hands.add(h)
        }
        val updated = gameRepository.save(game)
        return Game.fromStorage(updated)
    }

    fun getPlayerHandViewByCode(playerCode: ShortCode): PlayerHandView {
        var p = findPlayerByCode(playerCode) ?: throw ChiplessError.ResourceNotFoundError.ofEntity("player")
        val g = findGameByCode(p.game.shortCode) ?: throw ChiplessError.ResourceNotFoundError.ofEntity("game")
        val hand = g.latestHand() ?: throw ChiplessError.InvalidStateError("game has no current hand", ErrorCode.NoCurrentHand)
        return PlayerHandView(p, hand)
    }

    fun doPlayerAction(hand: PlayerHandView, action: PlayerAction): PlayerHandView {
        if (!hand.isPlayerTurn()) {
            throw ChiplessError.InvalidStateError("it is not your turn to act")
        }
        if (!hand.allowAction(action)) {
            throw ChiplessError.InvalidStateError("the requested action (${action}) is not allowed: ${hand.availableActions()}")
        }
        var handRecord = handRepository.findById(hand.hand.id).orElseThrow { ChiplessError.ResourceNotFoundError.ofEntity("hand") }
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
            var hand = handRepository.findById(hand.id).getOrNull() ?: throw ChiplessError.ResourceNotFoundError.ofEntity("hand")
            if (com.tylerkontra.chipless.model.Hand.fromStorage(hand).currentRound()?.isClosed() != true) {
                throw ChiplessError.InvalidStateError("current betting round is not closed")
            }
            var newRound = newBettingRound(hand)
            hand.rounds.add(newRound)
            val updatedHand = handRepository.save(hand)
            val updatedHandView = com.tylerkontra.chipless.model.Hand.fromStorage(updatedHand)
            return updatedHandView
        } ?: throw ChiplessError.InvalidStateError("game has no current hand")
    }

    companion object {
        val logger = LoggerFactory.getLogger(GameService::class.java)

        interface CreateGame {
            val name: String
            fun buyinMoney(): Money
            val buyinChips: Int
        }

        data class HandInput private constructor(
            // dealer id first
            val seatOrderPlayerIds: SmallBlindSeatOrder,
            val sequence: Int = 0,
        ) {
            constructor(seatOrderPlayerIds: SmallBlindSeatOrder): this(seatOrderPlayerIds, 0) {}
            companion object {
                fun default(): HandInput = HandInput(SmallBlindSeatOrder.empty())
            }
            private val seatOrder = compareBy<Player> { seatOrderPlayerIds.playerIds.indexOf(it.id) }

            fun <E> compareBySeatOrder(selector: (E) -> Player) = compareBy(seatOrder, selector)

            fun hasValidSeatOrder(playerIds: List<Long>): Boolean {
                if (seatOrderPlayerIds.playerIds.isEmpty()) return false
                if (seatOrderPlayerIds.playerIds.toSet().intersect(playerIds.toSet()).size < playerIds.size)
                    throw IllegalArgumentException("seat order must specify all players")
                return true
            }
        }
    }
}