package com.tylerkontra.chipless.http

import com.tylerkontra.chipless.http.protocol.CreateGame
import com.tylerkontra.chipless.service.GameService
import com.tylerkontra.chipless.http.protocol.Game as ProtocolGame
import com.tylerkontra.chipless.http.protocol.Player as ProtocolPlayer
import com.tylerkontra.chipless.storage.game.Game
import com.tylerkontra.chipless.storage.game.GameRepository
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController

@RestController
class Controller(
    var gameService: GameService
) {
    @GetMapping("/")
    fun index(): String {
        return "Welcome to Chipless"
    }

    @PostMapping("/start")
    fun startGame(@RequestBody game: CreateGame): ProtocolGame {
        var g = gameService.createGame(game)
        return ProtocolGame.fromModel(g)
    }

    @PostMapping("/join")
    fun joinGame(@RequestParam code: String, @RequestParam playerName: String): ProtocolPlayer {
        var p = gameService.addPlayer(code, playerName)
        return ProtocolPlayer.fromModel(p)
    }

    @GetMapping("/game/{code}")
    fun viewGame(@PathVariable code: String): ProtocolGame {
        var g: com.tylerkontra.chipless.model.Game =
            gameService.findGameByCode(code) ?: throw Exception("game not found")
        return ProtocolGame.fromModel(g)
    }
}