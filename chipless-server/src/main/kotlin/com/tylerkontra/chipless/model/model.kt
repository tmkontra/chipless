package com.tylerkontra.chipless.model

import com.tylerkontra.chipless.storage.hand.BettingActionType
import com.tylerkontra.chipless.storage.hand.isAggression
import com.tylerkontra.chipless.util.KeySet
import org.slf4j.LoggerFactory
import java.math.BigDecimal
import java.security.MessageDigest
import java.util.*

class ShortCode(input: String) {
    val value: String = input.replace("-", "")

    fun prettyPrint(): String =
        if (value.length % 4 == 0) {
            value.chunked(4).joinToString("-")
        } else if (value.length % 3 == 0) {
            value.chunked(3).joinToString("-")
        } else {
            val rem = value.length % 3
            value.dropLast(3+rem).chunked(3).plus(value.takeLast(3+rem)).joinToString("-")
        }
}

data class Money(val value: BigDecimal) {
    fun toCents(): Int {
        return value.movePointRight(2).intValueExact()
    }

    companion object {
        fun fromInt(value: Int): Money {
            val dollars = value / 100
            val cents = value % 100
            return Money(BigDecimal("${dollars}.${cents}"))
        }
    }
}

data class Game(
    val id: Long,
    val shortCode: ShortCode,
    val adminCode: ShortCode,
    val name: String,
    val buyinAmount: Money,
    val buyinChips: Int,
    val players: List<Player>,
    val hands: List<Hand>,
) {
    fun latestHand(): Hand? =
        hands.maxByOrNull { it.sequence }

    fun playerChips(): List<PlayerChips> =
        players.map { PlayerChips(it, it.availableChips(hands)) }

    companion object {
        fun fromStorage(game: com.tylerkontra.chipless.storage.game.Game): Game {
            return Game(
                game.id,
                ShortCode(game.shortCode),
                ShortCode(game.adminCode),
                game.name,
                Money.fromInt(game.buyinCents),
                game.buyinChips,
                game.players.map { Player.fromStorage(it) },
                game.hands.map { Hand.fromStorage(it) },
            )
        }
    }
}

data class PlayerChips(
    val player: Player,
    val availableChips: Int,
) {
    val id: Long = player.id
}

data class GameInfo(
    val id: Long,
    val shortCode: ShortCode,
    val buyinAmount: Money,
    val buyinChips: Int)

data class Player(
    val id: Long,
    val shortCode: ShortCode,
    val name: String,
    val buyCount: Int,
    val cashouts: List<Cashout>,
    val game: GameInfo,
) {
    fun availableChips(hands: List<Hand>): Int =
        outstandingChips + hands.map { it.players.find { it.player.id == this.id } }.sumOf { it?.net ?: 0 }

    val totalCashout: Int = cashouts.sumOf { it.amount }

    private val outstandingChips: Int = buyCount * game.buyinChips - cashouts.sumOf { it.amount }

    companion object {
        fun fromStorage(player: com.tylerkontra.chipless.storage.player.Player): Player {
            return Player(
                player.id,
                ShortCode(player.shortCode),
                player.name,
                player.buyCount,
                player.cashouts.map { Cashout.fromStorage(it) },
                player.game.let {
                    GameInfo(
                        it.id,
                        ShortCode(it.shortCode),
                        Money.fromInt(it.buyinCents),
                        it.buyinChips,
                    )
                },
            )
        }
    }
}

data class Cashout(val amount: Int) {
    companion object {
        fun fromStorage(cashout: com.tylerkontra.chipless.storage.player.Cashout): Cashout {
            return Cashout(cashout.chipCount)
        }
    }
}

data class Hand(
    val id: UUID,
    val sequence: Int,
    val players: List<HandPlayer>,
    val sittingOut: List<Player>,
    val rounds: List<BettingRound>,
    private val isComplete: Boolean,
    val nextRoundPlayers: List<Player>,
) {
    fun playerWager(player: Player): Int =
        rounds.dropLast(1).flatMap { it.actions.filter { it.player.id == player.id } }.maxOfOrNull { it.action.chipCount } ?: 0

    fun roundWager(player: Player): Int =
        currentRound()?.actions?.filter { it.player.id == player.id }?.maxOfOrNull { it.action.chipCount } ?: 0

    fun currentRound(): BettingRound? {
        if (rounds.isEmpty()) throw ChiplessError.InvalidStateError("no betting round")
        return rounds.lastOrNull()
    }

    val isFinished: Boolean = isComplete

    val winners: List<HandPlayer> = players.filter { it.winnings != null }

    companion object {
        fun fromStorage(hand: com.tylerkontra.chipless.storage.hand.Hand): Hand {
            return Hand(
                hand.id,
                hand.sequence,
                hand.players.map { HandPlayer.fromStorage(it, hand.playerActions(it)) },
                hand.sittingOut.map { Player.fromStorage(it) },
                hand.rounds.map { BettingRound.fromStorage(it) },
                hand.isComplete(),
                hand.nextRoundPlayers().map { Player.fromStorage(it.player) },
            )
        }
    }
}

data class HandPlayer(
    val player: Player,
    val initialChips: Int,
    val winnings: Int?,
    private val actions: List<com.tylerkontra.chipless.storage.hand.BettingAction>
) {
    val wager: Int = actions.sumOf { it.chipCount ?: 0 }

    val net: Int = (winnings ?: 0) - wager

    companion object {
        fun fromStorage(
            player: com.tylerkontra.chipless.storage.hand.HandPlayer,
            playerActions: List<com.tylerkontra.chipless.storage.hand.BettingAction>
        ): HandPlayer {
            return HandPlayer(
                Player.fromStorage(player.player),
                player.initialChips,
                player.winnings,
                playerActions,
            )
        }
    }
}

data class BettingRound (
    val id: UUID,
    val sequence: Int,
    val players: List<Player>,
    val actions: List<BettingAction>,
) {
    val maxWager: Int =
        actions.maxOfOrNull { it.action.chipCount } ?: 0

    fun playerWager(player: Player): Int =
        actions.filter { it.player.id ==  player.id}.maxOfOrNull { it.action.chipCount } ?: 0

    private fun playerSequence(): Sequence<Player> = generateSequence { players }.flatten()

    fun getCurrentActionPlayer(): Player {
        var ix = 0
        val folded: KeySet<Long, Player> = KeySet { it.id }
        var actionIter = actions.iterator()
        while (true) {
            if (folded.contains(playerSequence().elementAt(ix))) {
                ix++
            } else if (actionIter.hasNext()) {
                var act = actionIter.next()
                if (playerSequence().elementAt(ix).id == act.player.id) {
                    ix++
                    if (act.action.actionType == BettingActionType.FOLD) {
                        folded.add(act.player)
                    }
                } else {
                    throw IllegalArgumentException("next action doesn't match player")
                }
            } else {
                return playerSequence().elementAt(ix)
            }
        }
    }

    fun hasBet(): Boolean {
        return actions.any { when (it.action) {
            is PlayerAction.Bet -> true
            else -> false
        } }
    }

    fun isClosed(): Boolean {
        if (actions.count { it.action.actionType == BettingActionType.CHECK } == players.size) {
            return true
        }
        var lastAggressor = actions.lastOrNull { it.action.isAggression() }
        logger.debug("last aggressor: ${lastAggressor?.player?.name}")
        if (actions.dropLastWhile { it.action == PlayerAction.Fold }.lastOrNull()?.action?.actionType == BettingActionType.CALL) {
            logger.debug("last action was check")
            if (getCurrentActionPlayer().id == lastAggressor?.player?.id) {
                logger.debug("aggressor is next")
                return true
            }
        }
        logger.debug("round not closed: ${actions.lastOrNull()?.action}; ${getCurrentActionPlayer().name}")
        return false
    }

    companion object {
        val logger = LoggerFactory.getLogger(BettingRound::class.java)

        fun fromStorage(it: com.tylerkontra.chipless.storage.hand.BettingRound): BettingRound {
            return BettingRound(
                it.id,
                it.sequence,
                it.players.map(Player::fromStorage),
                it.actions.map(BettingAction::fromStorage)
            )
        }
    }
}

data class BettingAction(
    val id: UUID,
    val sequence: Int,
    val player: Player,
    val action: PlayerAction,
) {
    companion object {
        fun fromStorage(action: com.tylerkontra.chipless.storage.hand.BettingAction): BettingAction {
            return BettingAction(
                action.id,
                action.sequence,
                Player.fromStorage(action.player),
                PlayerAction.fromStorage(action.actionType, action.chipCount),
            )
        }
    }
}

sealed class PlayerAction {
    abstract val actionType: BettingActionType
    abstract fun allowed(other: PlayerAction): Boolean
    open val chipCount: Int = 0

    override fun toString(): String {
        return "${actionType}" + if (chipCount > 0) "[${chipCount}]" else ""
    }

    fun isAggression(): Boolean = this.actionType.isAggression()

    object Check: PlayerAction() {
        override val actionType: BettingActionType = BettingActionType.CHECK
        override fun allowed(other: PlayerAction) = other == Check
    }
    object Fold: PlayerAction() {
        override val actionType: BettingActionType = BettingActionType.FOLD
        override fun allowed(other: PlayerAction)= other == Fold
    }
    sealed class ChipAction(override val chipCount: Int) : PlayerAction()
    data class Bet(val amount: Int): ChipAction(amount) {
        override val actionType: BettingActionType = BettingActionType.BET
        override fun allowed(other: PlayerAction) = when (other) {
            is Bet -> this.amount >= other.amount
            else -> false
        }
    }
    data class Raise(val to: Int): ChipAction(to) {
        override val actionType: BettingActionType = BettingActionType.RAISE
        override fun allowed(other: PlayerAction) = when (other) {
            is Raise -> this.to >= other.to
            else -> false
        }
    }
    data class Call(val to: Int) : ChipAction(to) {
        override val actionType: BettingActionType = BettingActionType.CALL
        override fun allowed(other: PlayerAction) = when (other) {
            is Call -> this.to == other.to
            else -> false
        }
    }

    companion object {
        fun fromStorage(actionType: BettingActionType, chipCount: Int?): PlayerAction =
            when (actionType) {
                BettingActionType.FOLD -> Fold
                BettingActionType.CHECK -> Check
                BettingActionType.BET -> Bet(chipCount ?: throw ChiplessError.CorruptStateError("bet has no chip count"))
                BettingActionType.RAISE -> Raise(chipCount ?: throw ChiplessError.CorruptStateError("raise has no chip count"))
                BettingActionType.CALL -> Call(chipCount ?: throw ChiplessError.CorruptStateError("call has no chip count"))
            }
    }
}

data class PlayerHandView(
    val player: Player,
    val hand: Hand,
) {
    val handPlayer =
        hand.players.first { it.player.id == player.id }

    fun availableChips(): Int =
        handPlayer.initialChips - hand.playerWager(player)

    fun isFinished(): Boolean = hand.isFinished

    fun isPlayerTurn(): Boolean {
        if (isFinished()) return false
        var r = mustCurrentRound()
        var p = r.getCurrentActionPlayer()
        return p.id == this.player.id
    }

    fun mustCurrentRound(): BettingRound {
        return hand.currentRound() ?: throw ChiplessError.InvalidStateError("no current betting round")
    }

    val currentRoundWager: Int = mustCurrentRound().playerWager(player)

    fun availableActions(): List<PlayerAction> {
        var actions: MutableList<PlayerAction> = mutableListOf(PlayerAction.Fold)
        var r = mustCurrentRound()
        if (availableChips() <= 0) return actions
        if (r.hasBet()) {
            actions.addAll(listOf(
                PlayerAction.Call(r.maxWager),
                PlayerAction.Raise(handPlayer.initialChips)
            ))
        } else {
            actions.addAll(listOf(
                PlayerAction.Check,
                PlayerAction.Bet(handPlayer.initialChips))
            )
        }
        return actions
    }

    fun matches(handState: String): Boolean {
        return nextActionState().equals(handState)
    }

    @OptIn(ExperimentalStdlibApi::class)
    fun nextActionState(): String {
        var d = MessageDigest.getInstance("SHA-256")
        d.update(this.hand.id.toString().toByteArray())
        val currentRound = mustCurrentRound()
        d.update(currentRound.id.toString().toByteArray())
        currentRound.actions.lastOrNull()?.also {
            d.update(it.id.toString().toByteArray())
        }
        return d.digest().toHexString()
    }

    fun allowAction(action: PlayerAction): Boolean =
        this.availableActions().any { it.allowed(action) }

    fun nextActionSequence(): Int {
        var r = mustCurrentRound()
        return r.actions.maxByOrNull { it.sequence }?.let { it.sequence + 1 } ?: 1
    }
}

