package com.tylerkontra.chipless.storage.game

import org.springframework.data.repository.CrudRepository

interface GameRepository : CrudRepository<Game, Long> {}