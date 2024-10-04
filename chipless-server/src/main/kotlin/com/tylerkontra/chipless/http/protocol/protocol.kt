package com.tylerkontra.chipless.http.protocol

import com.tylerkontra.chipless.model.Money
import com.tylerkontra.chipless.model.ShortCode
import com.tylerkontra.chipless.service.GameService
import org.springframework.core.convert.converter.Converter
import org.springframework.stereotype.Component
import java.math.BigDecimal
import java.util.*

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
            m.id, m.name,
            m.shortCode.prettyPrint(),
            m.buyinAmount.value,
            m.buyinChips,
            m.players.map { Player.fromModel(it) },
        )
    }
}

data class Player(
    val name: String,
    val buyCount: Int,
    val outstandingChips: Int,
) {
    companion object {
        fun fromModel(p: com.tylerkontra.chipless.model.Player): Player {
            val chips = p.game.buyinChips * p.buyCount - p.totalCashout
            return Player(p.name, p.buyCount, chips)
        }
    }
}

data class Cashout(val amount: Int) {
    companion object {
        fun fromModel(model: com.tylerkontra.chipless.model.Cashout): Cashout {
            return Cashout(model.amount)
        }
    }
}

data class PlayerAdminView(
    val player: Player,
    val shortCode: String,
    val cashouts: List<Cashout>
) {
    companion object {
        fun fromModel(player: com.tylerkontra.chipless.model.Player): PlayerAdminView {
            return PlayerAdminView(
                Player.fromModel(player),
                player.shortCode.prettyPrint(),
                player.cashouts.map { Cashout.fromModel(it) }
            )
        }
    }
}

data class GameAdminView(
    val game: Game,
    val players: List<PlayerAdminView>,
    val hands: List<Hand>,
) {
    companion object {
        fun fromModel(game: com.tylerkontra.chipless.model.Game) =
            GameAdminView(
                Game.fromModel(game),
                game.players.map { PlayerAdminView.fromModel(it) },
                game.hands.map { Hand.fromModel(it) }
            )
    }
}

@Component
class ShortCodeDeserializer : Converter<String, ShortCode> {
    override fun convert(source: String): ShortCode {
        return ShortCode(source)
    }
}

data class Hand(
    val id: UUID,
    val sequence: Int,
) {
    companion object {
        fun fromModel(hand: com.tylerkontra.chipless.model.Hand) =
            Hand(hand.id, hand.sequence)
    }
}
