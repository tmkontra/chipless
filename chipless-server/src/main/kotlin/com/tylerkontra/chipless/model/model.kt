package com.tylerkontra.chipless.model

import java.math.BigDecimal
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
        fun fromStorage(created: com.tylerkontra.chipless.storage.game.Game): Game {
            return Game(
                created.id,
                ShortCode(created.shortCode),
                ShortCode(created.adminCode),
                created.name,
                Money.fromInt(created.buyinCents),
                created.buyinChips,
                created.players.map { Player.fromStorage(it) },
                created.hands.map { Hand.fromStorage(it) },
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
    val players: List<Player>,
    val sittingOut: List<Player>,
    val rounds: List<BettingRound>,
    val winners: List<PlayerWin>
) {
    val isFinished: Boolean = winners.isNotEmpty()

    companion object {
        fun fromStorage(hand: com.tylerkontra.chipless.storage.hand.Hand): Hand {
            return Hand(
                hand.id,
                hand.sequence,
                hand.players.map { Player.fromStorage(it.player) },
                hand.sittingOut.map { Player.fromStorage(it) },
                hand.rounds.map { BettingRound.fromStorage(it) },
                listOf(),
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

    companion object {
        fun fromStorage(it: com.tylerkontra.chipless.storage.hand.BettingRound): BettingRound {
            return BettingRound(it.id, it.sequence, it.players.map(Player::fromStorage), listOf())
        }
    }
}

data class BettingAction(
    val id: UUID,
    val sequence: Int,
    val player: Player,
    val action: PlayerAction,
)

sealed class PlayerAction {
    object Check: PlayerAction()
    object Fold: PlayerAction()
    object Call: PlayerAction()
    data class Bet(val amount: Int): PlayerAction()
    data class Raise(val to: Int): PlayerAction()
}

data class PlayerWin(
    val player: Player,
    val chipCount: Int,
)

data class PlayerHandView(
    val player: Player,
    val hand: Hand,
) {
    fun isFinished(): Boolean = hand.isFinished

    fun isPlayerTurn(): Boolean {
        if (isFinished()) return false
        var r = currentRound() ?: throw ChiplessErrror.InvalidStateError("no current betting round")
        var p = r.getCurrentActionPlayer() ?: throw ChiplessErrror.InvalidStateError("no action player in current betting round")
        return p.id == this.player.id
    }

    fun currentRound(): BettingRound? {
        if (hand.rounds.isEmpty()) throw ChiplessErrror.InvalidStateError("no betting round")
        return hand.rounds.lastOrNull()
    }

    fun availableActions(): List<PlayerAction> {
        var actions: MutableList<PlayerAction> = mutableListOf(PlayerAction.Fold)
        var r = currentRound() ?: throw ChiplessErrror.InvalidStateError("no current betting round")
        if (player.outstandingChips <= 0) return actions
        if (r.actions.any { when (it.action) {
            is PlayerAction.Bet -> true
            else -> false
        }}) {
            actions.addAll(listOf(PlayerAction.Call, PlayerAction.Raise(player.outstandingChips)))
        } else {
            actions.add(PlayerAction.Bet(player.outstandingChips))
        }
        return actions
    }
}