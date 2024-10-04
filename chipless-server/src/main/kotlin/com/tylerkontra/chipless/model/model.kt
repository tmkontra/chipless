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

data class GameInfo(val shortCode: ShortCode,
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
                hand.players.map { Player.fromStorage(it) },
                hand.sittingOut.map { Player.fromStorage(it) },
                listOf(),
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
)

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