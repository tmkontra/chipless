package com.tylerkontra.chipless.storage.player

import com.tylerkontra.chipless.storage.game.Game
import jakarta.persistence.Entity
import jakarta.persistence.FetchType
import jakarta.persistence.GeneratedValue
import jakarta.persistence.Id
import jakarta.persistence.ManyToOne
import java.time.Instant

@Entity
class Player(
    var name: String,
    @ManyToOne(fetch = FetchType.EAGER) var game: Game,
    var buyCount: Int = 0,
    var addedAt: Instant = Instant.now(),
    @Id @GeneratedValue var id: Long = 0L,
)