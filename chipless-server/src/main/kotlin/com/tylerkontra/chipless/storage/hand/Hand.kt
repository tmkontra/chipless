package com.tylerkontra.chipless.storage.hand

import com.tylerkontra.chipless.storage.game.Game
import com.tylerkontra.chipless.storage.player.Player
import jakarta.persistence.*
import java.util.*

@Entity
@Table(uniqueConstraints=[
    UniqueConstraint(columnNames = ["game_id", "sequence"])
])
class Hand(
    val sequence: Int,
    @ManyToOne(fetch = FetchType.EAGER, cascade = [CascadeType.ALL])
    val game: Game,
    @ManyToMany(cascade = [CascadeType.ALL], fetch = FetchType.EAGER)
    val players: MutableList<Player> = mutableListOf(),
    @ManyToMany(cascade = [CascadeType.ALL], fetch = FetchType.EAGER)
    val sittingOut: MutableList<Player> = mutableListOf(),
    @OneToMany(mappedBy = "hand", cascade = [CascadeType.ALL], fetch = FetchType.EAGER)
    val rounds: MutableList<BettingRound> = mutableListOf(),
    @OneToMany(mappedBy = "hand", cascade = [CascadeType.ALL], fetch = FetchType.EAGER)
    val winners: MutableList<HandWinner> = mutableListOf(),
    @Id val id: UUID = UUID.randomUUID()
)

@Entity
class BettingRound(
    val sequence: Int,
    @ManyToOne(fetch = FetchType.EAGER)
    val hand: Hand,
    @ManyToMany(cascade = [CascadeType.ALL], fetch = FetchType.EAGER)
    val players: MutableList<Player> = mutableListOf(),
    @Id val id: UUID = UUID.randomUUID(),
)

@Entity
class BettingAction(
    val sequence: Int,
    @ManyToOne(fetch = FetchType.EAGER)
    val player: Player,
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