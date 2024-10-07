package com.tylerkontra.chipless.http

import com.tylerkontra.chipless.http.protocol.Game
import com.tylerkontra.chipless.http.protocol.PlayerAdminView
import com.tylerkontra.chipless.model.ChiplessError
import com.tylerkontra.chipless.model.ShortCode
import com.tylerkontra.chipless.service.GameService
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/game/{code}")
class GameController(val gameService: GameService) {

    @GetMapping("")
    fun viewGame(@PathVariable code: ShortCode): Game {
        val g: com.tylerkontra.chipless.model.Game =
            gameService.findGameByCode(code) ?: throw ChiplessError.ResourceNotFoundError.ofEntity("game")
        return Game.fromModel(g)
    }

    @PostMapping("/join")
    fun joinGame(@PathVariable code: ShortCode, @RequestParam playerName: String): PlayerAdminView {
        val p = gameService.addPlayer(code, playerName)
        return PlayerAdminView.fromModel(p)
    }

}