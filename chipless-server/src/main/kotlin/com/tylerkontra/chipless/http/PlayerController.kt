package com.tylerkontra.chipless.http

import com.tylerkontra.chipless.http.protocol.Player
import com.tylerkontra.chipless.http.protocol.PlayerHandView
import com.tylerkontra.chipless.model.ChiplessErrror
import com.tylerkontra.chipless.model.ShortCode
import com.tylerkontra.chipless.service.GameService
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/player/{playerCode}")
class PlayerController(
    private val gameService: GameService
) {
    @GetMapping("")
    fun viewPlayer(@PathVariable playerCode: ShortCode): Player {
        val p = gameService.findPlayerByCode(playerCode) ?: throw ChiplessErrror.ResourceNotFoundError.ofEntity("player")
        return Player.fromModel(p)
    }

    @GetMapping("/hand")
    fun viewCurrentHand(@PathVariable playerCode: ShortCode): PlayerHandView {
        var v = gameService.getPlayerHandViewByCode(playerCode)
        return PlayerHandView.fromModel(v)
    }
}