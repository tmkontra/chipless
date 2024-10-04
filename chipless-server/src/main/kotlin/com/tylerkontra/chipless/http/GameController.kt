package com.tylerkontra.chipless.http

import com.tylerkontra.chipless.http.protocol.Game
import com.tylerkontra.chipless.http.protocol.Player
import com.tylerkontra.chipless.service.GameService
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/game/{code}")
class GameController(val gameService: GameService) {

    @GetMapping("")
    fun viewGame(@PathVariable code: String): Game {
        val g: com.tylerkontra.chipless.model.Game =
            gameService.findGameByCode(code) ?: throw Exception("game not found")
        return Game.fromModel(g)
    }

    @PostMapping("/join")
    fun joinGame(@PathVariable code: String, @RequestParam playerName: String): Player {
        val p = gameService.addPlayer(code, playerName)
        return Player.fromModel(p)
    }
}