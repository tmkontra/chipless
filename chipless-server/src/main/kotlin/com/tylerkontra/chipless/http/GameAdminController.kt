package com.tylerkontra.chipless.http

import com.tylerkontra.chipless.http.protocol.GameAdminView
import com.tylerkontra.chipless.http.protocol.Player
import com.tylerkontra.chipless.model.ShortCode
import com.tylerkontra.chipless.model.SmallBlindSeatOrder
import com.tylerkontra.chipless.service.GameService
import io.swagger.v3.oas.annotations.Parameter
import jakarta.transaction.Transactional
import org.springframework.web.bind.annotation.*
import java.util.*

@RestController
@RequestMapping("/gameAdmin/{adminCode}")
class GameAdminController(val gameService: GameService) {
    @GetMapping("")
    fun viewAdmin(@PathVariable adminCode: ShortCode): GameAdminView {
        val g = getGame(adminCode)
        return GameAdminView.fromModel(g)
    }

    @PostMapping("/player/{code}/buy")
    @Transactional
    fun playerBuy(@PathVariable adminCode: ShortCode, @PathVariable code: ShortCode): Player {
        val g = getGame(adminCode)
        val p = gameService.findPlayerByCode(code) ?: throw Exception("player not found")
        val updated = gameService.playerBuy(g, p)
        return Player.fromModel(updated)
    }

    @PostMapping("/player/{code}/cashout")
    @Transactional
    fun playerCashout(@PathVariable adminCode: ShortCode, @PathVariable code: ShortCode, @RequestParam chipCount: Int): Player {
        val g = getGame(adminCode)
        val p = gameService.findPlayerByCode(code) ?: throw Exception("player not found")
        val updated = gameService.playerCashout(g, p, chipCount)
        return Player.fromModel(updated)
    }

    @PostMapping("/hand")
    @Transactional
    fun startHand(
        @PathVariable adminCode: ShortCode,
        @Parameter(description = "the players for this hand, beginning with the small blind (player seated left of the dealer)")
        @RequestParam() seatOrderPlayerIds: List<Long>?,
    ): GameAdminView {
        val g = getGame(adminCode)
        val seatOrder = SmallBlindSeatOrder(seatOrderPlayerIds.orEmpty())
        val updated = gameService.startHand(g, GameService.Companion.HandInput(
            seatOrderPlayerIds = seatOrder
        ))
        return GameAdminView.fromModel(updated)
    }

    @PostMapping("/hand/advance")
    @Transactional
    fun nextBettingRound(
        @PathVariable adminCode: ShortCode,
    ): GameAdminView {
        var game = getGame(adminCode)
        var hand = gameService.nextBettingRound(game)
        var updated = getGame(adminCode)
        return GameAdminView.fromModel(updated)
    }

    private fun getGame(adminCode: ShortCode) =
        gameService.findGameByAdminCode(adminCode) ?: throw Exception("game not found")
}