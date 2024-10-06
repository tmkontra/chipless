package com.tylerkontra.chipless.model

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

import org.junit.jupiter.api.Assertions.*
import java.util.*

class BettingRoundTest {

    @Test
    fun getCurrentActionPlayer() {
        val game = GameInfo(1, ShortCode("xyz"), Money(10.0.toBigDecimal()), 100)
        val alice = Player(1, ShortCode("abc"), "alice", 1, listOf(), game)
        val brian = Player(2, ShortCode("def"), "brian", 1, listOf(), game)
        val charlie = Player(3, ShortCode("ghi"), "charlie", 1, listOf(), game)
        var round = BettingRound(
            UUID.randomUUID(),
            72,
            listOf(
                alice,
                brian,
                charlie
            ),
            listOf(
                BettingAction(
                    UUID.randomUUID(),
                    1,
                    alice,
                    PlayerAction.Bet(5),
                ),
                BettingAction(
                    UUID.randomUUID(),
                    2,
                    brian,
                    PlayerAction.Call(5),
                ),
                BettingAction(
                    UUID.randomUUID(),
                    3,
                    charlie,
                    PlayerAction.Raise(10)
                ),
                BettingAction(
                    UUID.randomUUID(),
                    4,
                    alice,
                    PlayerAction.Call(10),
                ),
                BettingAction(
                    UUID.randomUUID(),
                    5,
                    brian,
                    PlayerAction.Fold,
                ),
            ),
        )
        assertThat(round.getCurrentActionPlayer()).matches { it.name == "charlie" }
        assertThat(round.isClosed())
    }
}