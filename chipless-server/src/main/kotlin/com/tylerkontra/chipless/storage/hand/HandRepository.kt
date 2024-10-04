package com.tylerkontra.chipless.storage.hand

import org.springframework.data.repository.CrudRepository
import java.util.*

interface HandRepository : CrudRepository<Hand, UUID>{

}

interface BettingRoundRepository: CrudRepository<BettingRound, UUID>