package com.tylerkontra.chipless.storage.game

import org.springframework.data.repository.CrudRepository
import java.util.Optional

interface GameRepository : CrudRepository<Game, Long> {
    fun findGameByShortCode(code: String): Optional<Game>
    fun findGameByAdminCode(adminCode: String): Optional<Game>
}