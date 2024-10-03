package com.tylerkontra.chipless.storage.player

import org.springframework.data.repository.CrudRepository

interface PlayerRepository: CrudRepository<Player, Long> {
}