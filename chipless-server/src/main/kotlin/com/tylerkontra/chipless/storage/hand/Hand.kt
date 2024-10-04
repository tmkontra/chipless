package com.tylerkontra.chipless.storage.hand

import com.tylerkontra.chipless.storage.game.Game
import com.tylerkontra.chipless.storage.player.Player
import jakarta.persistence.*
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
    @OrderBy(value = "sequence ASC")
    var players: MutableList<HandPlayer> = mutableListOf(),
    @ManyToMany(cascade = [CascadeType.ALL], fetch = FetchType.EAGER)
    var sittingOut: MutableList<Player> = mutableListOf(),
    @OneToMany(mappedBy = "hand", cascade = [CascadeType.ALL], fetch = FetchType.EAGER)
    var rounds: MutableList<BettingRound> = mutableListOf(),
    @OneToMany(mappedBy = "hand", cascade = [CascadeType.ALL], fetch = FetchType.EAGER)
    var winners: MutableList<HandWinner> = mutableListOf(),
    @Id var id: UUID = UUID.randomUUID()
)

@Entity
@Table(uniqueConstraints=[
    UniqueConstraint(columnNames = ["hand", "player"]),
    UniqueConstraint(columnNames = ["hand", "sequence"])
])
class HandPlayer(
    @ManyToOne
    var player: Player,
    @ManyToOne
    var hand: Hand,
    var sequence: Int,
    var initialChips: Int,
    @Id var id: UUID = UUID.randomUUID()
)

@Entity
class BettingRound(
    val sequence: Int,
    @ManyToOne(fetch = FetchType.EAGER)
    val hand: Hand,
    @ManyToMany(cascade = [CascadeType.ALL], fetch = FetchType.EAGER)
    val players: MutableList<Player> = mutableListOf(),
    @OneToMany(mappedBy = "round", cascade = [CascadeType.ALL], fetch = FetchType.EAGER)
        val actions: MutableList<BettingAction> = mutableListOf(),
    @Id val id: UUID = UUID.randomUUID(),
)

@Entity
class BettingAction(
    val sequence: Int,
    @ManyToOne(fetch = FetchType.EAGER)
    val player: Player,
    @ManyToOne(fetch = FetchType.EAGER)
    val round: BettingRound,
    @Enumerated(EnumType.STRING)
    val actionType: BettingActionType,
    val chipCount: Int? = null, // when BET or RAISE
    @Id val id: UUID = UUID.randomUUID(),
)

enum class BettingActionType {
    CHECK, FOLD, BET, RAISE, CALL
}

@Entity
class HandWinner(
    @ManyToOne(fetch = FetchType.EAGER)
    val player: Player,
    val chipCount: Int,
    @ManyToOne(fetch = FetchType.EAGER)
    val hand: Hand,
    @Id val id: UUID = UUID.randomUUID(),
)