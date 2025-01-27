package com.tylerkontra.chipless.storage.hand

import com.tylerkontra.chipless.storage.game.Game
import com.tylerkontra.chipless.storage.player.Player
import jakarta.persistence.*
import org.hibernate.annotations.OnDelete
import org.hibernate.annotations.OnDeleteAction
import java.util.*

@Entity
@Table(uniqueConstraints=[
    UniqueConstraint(columnNames = ["game", "sequence"])
])
class Hand(
    var sequence: Int,
    @ManyToOne(fetch = FetchType.EAGER, cascade = [CascadeType.ALL])
    var game: Game,
    @OneToMany(mappedBy = "hand", cascade = [CascadeType.ALL], fetch = FetchType.EAGER)
    @OnDelete(action = OnDeleteAction.CASCADE)
    @OrderBy(value = "sequence ASC")
    var players: MutableList<HandPlayer> = mutableListOf(),
    @ManyToMany(cascade = [CascadeType.ALL], fetch = FetchType.EAGER)
    var sittingOut: MutableList<Player> = mutableListOf(),
    @OneToMany(mappedBy = "hand", cascade = [CascadeType.ALL], fetch = FetchType.EAGER)
    @OnDelete(action = OnDeleteAction.CASCADE)
    @OrderBy("sequence ASC")
    var rounds: MutableList<BettingRound> = mutableListOf(),
    @Id var id: UUID = UUID.randomUUID()
) {
    fun currentPot(): Int =
        rounds.sumOf { r -> r.actions.sumOf { it.chipCount ?: 0 }}

    // All the players who have NOT folded
    fun nextRoundPlayers(): List<HandPlayer> {
        val playerIds: Set<Long>? = rounds.lastOrNull()?.nextRoundPlayers()?.map { it.id }?.toSet()
        if (playerIds != null) {
            return players.filter { playerIds.contains(it.player.id) }
        }
        return players
    }

    fun isComplete(): Boolean {
        // pot uncontested
        return nextRoundPlayers().size < 2
    }

    fun uncontestedWin() {
        if (nextRoundPlayers().size == 1) {
            nextRoundPlayers().first().winnings = currentPot()
        } else {
            throw IllegalArgumentException("pot is not uncontested")
        }
    }

    fun playerActions(handPlayer: HandPlayer): List<BettingAction> =
        rounds.flatMap { r -> r.actions.filter { a -> a.player.id == handPlayer.player.id } }
}

@Entity
@Table(uniqueConstraints=[
    UniqueConstraint(columnNames = ["hand", "player"]),
    UniqueConstraint(columnNames = ["hand", "sequence"])
])
class HandPlayer(
    @ManyToOne(cascade = [CascadeType.ALL])
    var player: Player,
    @ManyToOne
    var hand: Hand,
    var sequence: Int,
    var initialChips: Int,
    var winnings: Int? = null,
    @Id var id: UUID = UUID.randomUUID()
) {
    fun isDealer(): Boolean = sequence == hand.players.size
}

@Entity
class BettingRound(
    val sequence: Int,
    @ManyToOne(fetch = FetchType.EAGER)
    val hand: Hand,
    @ManyToMany(cascade = [CascadeType.ALL], fetch = FetchType.EAGER)
    val players: MutableList<Player> = mutableListOf(),
    @OneToMany(mappedBy = "round", cascade = [CascadeType.ALL], fetch = FetchType.EAGER)
    @OrderBy("sequence ASC")
    val actions: MutableList<BettingAction> = mutableListOf(),
    @Id val id: UUID = UUID.randomUUID(),
) {
    fun nextRoundPlayers(): List<Player> {
        return players.filterNot(::playerHasFolded)
    }

    fun playerHasFolded(player: Player): Boolean {
        return actions.any { act -> act.player.id == player.id && act.actionType == BettingActionType.FOLD }
    }
}

@Entity
class BettingAction(
    var sequence: Int,
    @ManyToOne(fetch = FetchType.EAGER)
    var player: Player,
    @ManyToOne(fetch = FetchType.EAGER)
    var round: BettingRound,
    @Enumerated(EnumType.STRING)
    var actionType: BettingActionType,
    var chipCount: Int? = null, // when BET or RAISE
    @Id var id: UUID = UUID.randomUUID(),
)

enum class BettingActionType {
    CHECK, FOLD, BET, RAISE, CALL
}

fun BettingActionType.isAggression(): Boolean = this in listOf(BettingActionType.BET, BettingActionType.RAISE)
