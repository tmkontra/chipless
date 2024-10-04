package com.tylerkontra.chipless.http

import com.tylerkontra.chipless.http.protocol.CreateGame
import com.tylerkontra.chipless.http.protocol.GameAdminView
import com.tylerkontra.chipless.http.protocol.Player
import com.tylerkontra.chipless.model.ShortCode
import com.tylerkontra.chipless.service.GameService
import com.tylerkontra.chipless.http.protocol.Game as ProtocolGame
import com.tylerkontra.chipless.http.protocol.Player as ProtocolPlayer
import jakarta.transaction.Transactional
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

    @PostMapping("/game")
    fun startGame(@RequestBody game: CreateGame): ProtocolGame {
        val g = gameService.createGame(game)
        return ProtocolGame.fromModel(g)
    }

}