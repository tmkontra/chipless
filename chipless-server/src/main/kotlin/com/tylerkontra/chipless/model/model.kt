package com.tylerkontra.chipless.model

import com.tylerkontra.chipless.storage.hand.BettingActionType
import java.math.BigDecimal
import java.security.MessageDigest
import java.util.UUID

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
    val totalCashout: Int = cashouts.sumOf { it.amount }
    val outstandingChips: Int = game.buyinChips * this.buyCount - this.totalCashout

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
) {
    val isFinished: Boolean = isComplete

    companion object {
        fun fromStorage(hand: com.tylerkontra.chipless.storage.hand.Hand): Hand {
            return Hand(
                hand.id,
                hand.sequence,
                hand.players.map { HandPlayer.fromStorage(it, hand.playerActions(it)) },
                hand.sittingOut.map { Player.fromStorage(it) },
                hand.rounds.map { BettingRound.fromStorage(it) },
                hand.isComplete(),
            )
        }
    }
}

data class HandPlayer(
    val player: Player,
    val winnings: Int?,
    private val actions: List<com.tylerkontra.chipless.storage.hand.BettingAction>
) {
    val wager: Int = actions.sumOf { it.chipCount ?: 0 }

    companion object {
        fun fromStorage(
            player: com.tylerkontra.chipless.storage.hand.HandPlayer,
            playerActions: List<com.tylerkontra.chipless.storage.hand.BettingAction>
        ): HandPlayer {
            return HandPlayer(
                Player.fromStorage(player.player),
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
    // TODO: ensure players and actions are in seat-order
    fun getCurrentActionPlayer(): Player?{
        return players.dropWhile { p -> actions.any { act -> act.player.id == p.id } }.firstOrNull()
    }

    fun hasBet(): Boolean {
        return actions.any { when (it.action) {
            is PlayerAction.Bet -> true
            else -> false
        } }
    }

    companion object {
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

    object Check: PlayerAction() {
        override val actionType: BettingActionType = BettingActionType.CHECK
        override fun allowed(other: PlayerAction) = other == Check
    }
    object Fold: PlayerAction() {
        override val actionType: BettingActionType = BettingActionType.FOLD
        override fun allowed(other: PlayerAction)= other == Fold
    }
    object Call: PlayerAction() {
        override val actionType: BettingActionType = BettingActionType.CALL
        override fun allowed(other: PlayerAction) = other == Call
    }
    sealed class ChipAction(val chipCount: Int) : PlayerAction()
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
    companion object {
        fun fromStorage(actionType: BettingActionType, chipCount: Int?): PlayerAction =
            when (actionType) {
                BettingActionType.FOLD -> Fold
                BettingActionType.CALL -> Call
                BettingActionType.CHECK -> Check
                BettingActionType.BET -> Bet(chipCount ?: throw ChiplessErrror.CorruptStateError("bet has no chip count"))
                BettingActionType.RAISE -> Raise(chipCount ?: throw ChiplessErrror.CorruptStateError("raise has no chip count"))
            }
    }
}

data class PlayerHandView(
    val player: Player,
    val hand: Hand,
) {
    fun isFinished(): Boolean = hand.isFinished

    fun isPlayerTurn(): Boolean {
        if (isFinished()) return false
        var r = mustCurrentRound()
        var p = r.getCurrentActionPlayer() ?: return false
        return p.id == this.player.id
    }

    fun currentRound(): BettingRound? {
        if (hand.rounds.isEmpty()) throw ChiplessErrror.InvalidStateError("no betting round")
        return hand.rounds.lastOrNull()
    }

    fun mustCurrentRound(): BettingRound {
        return currentRound() ?: throw ChiplessErrror.InvalidStateError("no current betting round")
    }

    fun availableActions(): List<PlayerAction> {
        var actions: MutableList<PlayerAction> = mutableListOf(PlayerAction.Fold)
        var r = mustCurrentRound()
        if (player.outstandingChips <= 0) return actions
        if (r.hasBet()) {
            actions.addAll(listOf(PlayerAction.Call, PlayerAction.Raise(player.outstandingChips)))
        } else {
            actions.addAll(listOf(PlayerAction.Check, PlayerAction.Bet(player.outstandingChips)))
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
