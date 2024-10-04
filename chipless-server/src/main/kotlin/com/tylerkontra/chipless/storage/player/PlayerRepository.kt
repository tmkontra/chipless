package com.tylerkontra.chipless.storage.player

import org.springframework.data.jpa.repository.Modifying
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.CrudRepository
import org.springframework.data.repository.query.Param
import java.util.Optional

interface PlayerRepository: CrudRepository<Player, Long> {
    fun findPlayerByShortCode(code: String): Optional<Player>
    @Modifying(clearAutomatically = true)
    @Query("update Player set buyCount = buyCount + 1 where shortCode = :code")
    fun incrementPlayerBuyCount(@Param("code") code: String): Int
}