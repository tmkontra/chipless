package com.tylerkontra.chipless.http.protocol

import com.tylerkontra.chipless.model.Money
import com.tylerkontra.chipless.model.ShortCode
import com.tylerkontra.chipless.service.GameService
import com.tylerkontra.chipless.storage.hand.BettingActionType
import java.math.BigDecimal
import java.util.*

data class CreateGame(
    override val name: String,
    val buyinAmount: BigDecimal,
    override val buyinChips: Int,
): GameService.Companion.CreateGame {
    override fun buyinMoney(): Money {
        return Money(buyinAmount)
    }
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
    val id: Long,
    val name: String,
    val buyCount: Int,
) {
    companion object {
        fun fromModel(p: com.tylerkontra.chipless.model.Player): Player {
            return Player(p.id, p.name, p.buyCount)
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
    val cashouts: List<Cashout>,
    val outstandingChips: Int,
) {
    companion object {
        fun fromModel(player: com.tylerkontra.chipless.model.Player): PlayerAdminView {
            return PlayerAdminView(
                Player.fromModel(player),
                player.shortCode.prettyPrint(),
                player.cashouts.map { Cashout.fromModel(it) },
                player.outstandingChips,
            )
        }
    }
}

data class GameAdminView(
    val game: Game,
    val adminCode: String,
    val players: List<PlayerAdminView>,
    val hands: List<Hand>,
    val currentHand: Hand?,
    var nextHandOrder: List<PlayerAdminView>,
    var playersBankrupt: List<PlayerAdminView>,
    var newPlayers: List<PlayerAdminView>
) {
    companion object {
        fun fromModel(game: com.tylerkontra.chipless.model.Game): GameAdminView {
            val nextHandPlayers = game.nextHandPlayers()
            val playersBankrupt = game.playersBankrupt()
            var existingPlayers = nextHandPlayers.union(playersBankrupt).map { it.id }.toSet()
            var newPlayers = game.players.filter { !existingPlayers.contains(it.id) }
            return GameAdminView(
                Game.fromModel(game),
                game.adminCode.prettyPrint(),
                game.players.map { PlayerAdminView.fromModel(it) },
                game.hands.map { Hand.fromModel(it) },
                game.latestHand()?.let { Hand.fromModel(it) },
                nextHandPlayers.map(PlayerAdminView::fromModel),
                playersBankrupt.map(PlayerAdminView::fromModel),
                newPlayers.map(PlayerAdminView::fromModel)
            )
        }
    }
}

data class Hand(
    val id: UUID,
    val sequence: Int,
    val players: List<HandPlayer>,
    val rounds: List<BettingRound>,
    val currentRound: BettingRound,
    val isFinished: Boolean,
) {
    companion object {
        fun fromModel(hand: com.tylerkontra.chipless.model.Hand) =
            Hand(
                hand.id,
                hand.sequence,
                hand.players.map(HandPlayer::fromModel),
                hand.rounds.map { BettingRound.fromModel(it) },
                hand.currentRound().let(BettingRound::fromModel),
                hand.isFinished,
            )
    }
}

data class HandPlayer(
    val player: Player,
    val winnings: Int?,
    val wager: Int?,
    val initialChips: Int,
    val isDealer: Boolean,
) {
    companion object {
        fun fromModel(player: com.tylerkontra.chipless.model.HandPlayer): HandPlayer {
            return HandPlayer(
                player = Player.fromModel(player.player),
                initialChips = player.initialChips,
                winnings = player.winnings,
                wager = player.wager,
                isDealer = player.isDealer,
            )
        }
    }
}

data class BettingRound(
    val id: UUID,
    var sequence: Int,
    val players: List<Player>,
    val currentPlayer: Player,
    val actions: List<PlayerAction>,
) {
    companion object {
        fun fromModel(it: com.tylerkontra.chipless.model.BettingRound): BettingRound {
            return BettingRound(
                it.id,
                it.sequence,
                it.players.map { Player.fromModel(it) },
                it.getCurrentActionPlayer().let(Player::fromModel),
                it.actions.map { PlayerAction.fromModel(it.action) }
            )
        }
    }
}

data class PlayerHandView(
    val hand: Hand,
    val player: PlayerAdminView,
    val isTurn: Boolean,
    val availableActions: List<PlayerAction?>,
    val availableChips: Int,
) {
    companion object {
        fun fromModel(v: com.tylerkontra.chipless.model.PlayerHandView): PlayerHandView {
            return PlayerHandView(
                hand = Hand.fromModel(v.hand),
                player = PlayerAdminView.fromModel(v.player),
                isTurn = v.isPlayerTurn(),
                availableActions = v.availableActions().map(PlayerAction::fromModel),
                availableChips = v.availableChips(),
            )
        }
    }
}


data class PlayerAction(
    val actionType: BettingActionType,
    val chipCount: Int? = null
) {
    fun toModel(): com.tylerkontra.chipless.model.PlayerAction {
        return when(this.actionType) {
            BettingActionType.FOLD -> com.tylerkontra.chipless.model.PlayerAction.Fold
            BettingActionType.CHECK -> com.tylerkontra.chipless.model.PlayerAction.Check
            else -> chipCount?.let {
                when (this.actionType) {
                    BettingActionType.BET -> com.tylerkontra.chipless.model.PlayerAction.Bet(chipCount)
                    BettingActionType.RAISE -> com.tylerkontra.chipless.model.PlayerAction.Raise(chipCount)
                    BettingActionType.CALL -> com.tylerkontra.chipless.model.PlayerAction.Call(chipCount)
                    else -> throw Exception("unreachable")
                }
            } ?: throw IllegalArgumentException("action type requires chip count")
        }
    }

    companion object {
        fun fromModel(a: com.tylerkontra.chipless.model.PlayerAction): PlayerAction {
            return when (a) {
                is com.tylerkontra.chipless.model.PlayerAction.Fold -> PlayerAction(BettingActionType.FOLD)
                is com.tylerkontra.chipless.model.PlayerAction.Bet -> PlayerAction(BettingActionType.BET, a.amount)
                is com.tylerkontra.chipless.model.PlayerAction.Call -> PlayerAction(BettingActionType.CALL, a.to)
                com.tylerkontra.chipless.model.PlayerAction.Check -> PlayerAction(BettingActionType.CHECK)
                is com.tylerkontra.chipless.model.PlayerAction.Raise -> PlayerAction(BettingActionType.RAISE, a.to)
            }
        }
    }
}
