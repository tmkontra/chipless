package com.tylerkontra.chipless.service

import com.tylerkontra.chipless.http.protocol.CreateGame
import com.tylerkontra.chipless.model.ChiplessErrror
import com.tylerkontra.chipless.model.Player
import com.tylerkontra.chipless.model.PlayerAction
import com.tylerkontra.chipless.model.PlayerHandView
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Condition
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertDoesNotThrow
import org.junit.jupiter.api.assertThrows
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.transaction.support.TransactionTemplate
import java.math.RoundingMode
import kotlin.random.Random

@SpringBootTest
class GameServiceTest {

    @Autowired
    lateinit var gameService: GameService

    @Autowired
    lateinit var transactionTemplate: TransactionTemplate

    fun isPlayer(player: Player) = Condition({ p: Player -> p.id == player.id}, "player does not match")

    @Test
    fun game() {
        val create = CreateGame(
            "test game",
            Random.nextDouble(10.0, 100.0).toBigDecimal().setScale(2, RoundingMode.HALF_UP),
            100,
        )
        var game = gameService.createGame(create)
        assertEquals(game.name,  "test game")

        var alice = gameService.addPlayer(game.shortCode, "Alice")
        var brian = gameService.addPlayer(game.shortCode, "Brian")
        var charlie = gameService.addPlayer(game.shortCode, "Charlie")
        var dylan = gameService.addPlayer(game.shortCode, "Dylan")

        fun getGame() =
            gameService.findGameByCode(game.shortCode) ?: throw RuntimeException("Game not found")

        game = getGame()
        assertThat(game.players).hasSize(4)

        fun buys() {
            alice = gameService.playerBuy(game, alice)
            brian = gameService.playerBuy(game, brian)
            repeat(2) {
                charlie = gameService.playerBuy(game, charlie)
            }
        }
        transactionTemplate.execute { status -> buys() }

        game = getGame()
        assertThat(game.players.sumOf { it.buyCount }).isEqualTo(4)
        assertThat(game.hands).isEmpty()

        game = transactionTemplate.execute { s ->
            gameService.startHand(game, GameService.Companion.HandInput())
        } ?: throw RuntimeException("game did not start")
        assertThat(game.hands).hasSize(1)
        assertThat(game.latestHand()).isNotNull()
        assertThat(game.latestHand()?.players).hasSize(3)
        assertThat(game.latestHand()?.currentRound()).isNotNull()
        assertThat(game.latestHand()?.currentRound()?.getCurrentActionPlayer()).has(isPlayer(alice))

        val firstHand = game.latestHand() ?: throw RuntimeException("Hand not found")
        var aliceHand = PlayerHandView(game.players.first(), firstHand)
        var brianHand = PlayerHandView(game.players.elementAt(1), firstHand)
        assertThrows<ChiplessErrror.InvalidStateError> { gameService.doPlayerAction(brianHand, PlayerAction.Check) }

        val betAmount = game.buyinChips / 4
        var aliceView = gameService.doPlayerAction(aliceHand, PlayerAction.Bet(betAmount))

        brianHand = gameService.getPlayerHandViewByCode(brian.shortCode)
        assertThrows<ChiplessErrror.InvalidStateError> { gameService.doPlayerAction(brianHand, PlayerAction.Check) }
        assertThrows<ChiplessErrror.InvalidStateError> { gameService.doPlayerAction(brianHand, PlayerAction.Bet(betAmount)) }
        assertDoesNotThrow { gameService.doPlayerAction(brianHand, PlayerAction.Call(betAmount)) }

        var charlieHand = gameService.getPlayerHandViewByCode(charlie.shortCode)
        assertThrows<ChiplessErrror.InvalidStateError> { gameService.doPlayerAction(charlieHand, PlayerAction.Check) }
        assertThrows<ChiplessErrror.InvalidStateError> { gameService.doPlayerAction(charlieHand, PlayerAction.Bet(betAmount)) }
        assertDoesNotThrow { gameService.doPlayerAction(charlieHand, PlayerAction.Raise(betAmount + 10)) }

        aliceHand = gameService.getPlayerHandViewByCode(alice.shortCode)
        assertThrows<ChiplessErrror.InvalidStateError> { gameService.doPlayerAction(aliceHand, PlayerAction.Check) }
        assertThrows<ChiplessErrror.InvalidStateError> { gameService.doPlayerAction(aliceHand, PlayerAction.Bet(betAmount)) }
        // re-raise
        assertDoesNotThrow { gameService.doPlayerAction(aliceHand, PlayerAction.Raise(betAmount + 20)) }

        brianHand = gameService.getPlayerHandViewByCode(brian.shortCode)
        var brianView = gameService.doPlayerAction(brianHand, PlayerAction.Fold)

        assertThat(brianView.hand.currentRound()?.getCurrentActionPlayer()).has(isPlayer(charlie))

        charlieHand = gameService.getPlayerHandViewByCode(charlie.shortCode)
        var charlieView = gameService.doPlayerAction(charlieHand, PlayerAction.Raise(betAmount + 40))

        assertThat(charlieView.hand.currentRound()?.getCurrentActionPlayer()).has(isPlayer(alice))
        assertThat(charlieView.availableActions().first { it is PlayerAction.Call }).hasFieldOrPropertyWithValue("to", betAmount+40)

        aliceHand = gameService.getPlayerHandViewByCode(alice.shortCode)
        transactionTemplate.execute { status ->
            aliceView = gameService.doPlayerAction(aliceHand, PlayerAction.Call(betAmount + 40))
        }
        assertThat(aliceView.mustCurrentRound().isClosed()).isTrue()
        assertThat(aliceView.hand.nextRoundPlayers).hasSize(2)
    }

    @Test
    fun addPlayer() {
    }

    @Test
    fun playerBuy() {
    }

    @Test
    fun playerCashout() {
    }

    @Test
    fun startHand() {
    }

    @Test
    fun getPlayerHandViewByCode() {
    }

    @Test
    fun doPlayerAction() {
    }
}