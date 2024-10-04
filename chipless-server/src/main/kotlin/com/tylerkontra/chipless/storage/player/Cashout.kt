package com.tylerkontra.chipless.storage.player

import com.tylerkontra.chipless.storage.game.Game
import jakarta.persistence.*
import java.time.Instant

@Entity
class Cashout(
    @ManyToOne(fetch = FetchType.EAGER) var player: Player,
    var chipCount: Int,
    var addedAt: Instant = Instant.now(),
    @Id @GeneratedValue var id: Long = 0L,
)