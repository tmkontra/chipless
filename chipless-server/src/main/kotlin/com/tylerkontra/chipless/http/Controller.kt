package com.tylerkontra.chipless.http

import com.tylerkontra.chipless.storage.game.Game
import com.tylerkontra.chipless.storage.game.GameRepository
import jakarta.persistence.EntityManager
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RestController

@RestController
class Controller(
    var gameRepository: GameRepository
) {
    @GetMapping("/")
    fun index(): String {
        return "Welcome to Chipless"
    }

    @GetMapping("/game")
    fun game(): String {
        var g = Game("test game!", 100, 100)
        gameRepository.save(g)
        return g.toString()
    }
}