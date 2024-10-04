package com.tylerkontra.chipless.http

import com.tylerkontra.chipless.http.protocol.Game
import com.tylerkontra.chipless.http.protocol.Hand
import com.tylerkontra.chipless.http.protocol.Player
import com.tylerkontra.chipless.model.ChiplessErrror
import com.tylerkontra.chipless.model.ShortCode
import com.tylerkontra.chipless.service.GameService
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/game/{code}")
class GameController(val gameService: GameService) {

    @GetMapping("")
    fun viewGame(@PathVariable code: ShortCode): Game {
        val g: com.tylerkontra.chipless.model.Game =
            gameService.findGameByCode(code) ?: throw ChiplessErrror.ResourceNotFoundError.ofEntity("game")
        return Game.fromModel(g)
    }

    @PostMapping("/join")
    fun joinGame(@PathVariable code: ShortCode, @RequestParam playerName: String): Player {
        val p = gameService.addPlayer(code, playerName)
        return Player.fromModel(p)
    }


    @GetMapping("/hand")
    fun viewCurrentHand(@PathVariable code: ShortCode): Hand? {
        return null
    }

}