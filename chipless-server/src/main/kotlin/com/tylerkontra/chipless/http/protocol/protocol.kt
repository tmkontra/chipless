package com.tylerkontra.chipless.http.protocol

import com.tylerkontra.chipless.model.Money
import com.tylerkontra.chipless.service.GameService
import java.math.BigDecimal

data class CreateGame(
    override val name: String,
    val buyinAmount: BigDecimal,
    override val buyinChips: Int,
): GameService.Companion.CreateGame {
    override fun getBuyinAmount(): Money = Money(buyinAmount)
}

data class Game(
    val id: Long,
    val name: String,
    val shortCode: String,
    val buyinAmount: BigDecimal,
    val buyinChips: Int,
    val players: List<Player>
) {
    companion object {
        fun fromModel(m: com.tylerkontra.chipless.model.Game) = Game(
            m.id, m.name, m.shortCode, m.buyinAmount.value, m.buyinChips,
            m.players.map { Player.fromModel(it) },
        )
    }
}

data class Player(
    val name: String,
    val buyCount: Int,
) {
    companion object {
        fun fromModel(p: com.tylerkontra.chipless.model.Player): Player {
            return Player(p.name, p.buyCount)
        }
    }
}