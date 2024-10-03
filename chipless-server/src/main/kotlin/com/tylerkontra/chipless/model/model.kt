package com.tylerkontra.chipless.model

import java.math.BigDecimal

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
    val shortCode: String,
    val name: String,
    val buyinAmount: Money,
    val buyinChips: Int,
    val players: List<Player>,
) {
    companion object {
        fun fromStorage(created: com.tylerkontra.chipless.storage.game.Game): Game {
            return Game(
                created.id,
                created.shortCode,
                created.name,
                Money.fromInt(created.buyinCents),
                created.buyinChips,
                created.players.map { Player.fromStorage(it) }
            )
        }
    }
}

data class Player(
    val id: Long,
    val name: String,
    val buyCount: Int,
) {
    companion object {
        fun fromStorage(player: com.tylerkontra.chipless.storage.player.Player): Player {
            return Player(player.id, player.name, player.buyCount)
        }
    }
}