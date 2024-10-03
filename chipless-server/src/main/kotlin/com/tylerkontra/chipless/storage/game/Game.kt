package com.tylerkontra.chipless.storage.game

import com.tylerkontra.chipless.storage.player.Player
import jakarta.persistence.CascadeType
import jakarta.persistence.Entity
import jakarta.persistence.GeneratedValue
import jakarta.persistence.Id
import jakarta.persistence.OneToMany
import java.time.Instant

@Entity
class Game(
    var name: String,
    var buyinCents: Int,
    var buyinChips: Int,
    @OneToMany(mappedBy = "game", cascade = [CascadeType.ALL], orphanRemoval = true)
        var players: List<Player> = listOf(),
    var addedAt: Instant = Instant.now(),
    @Id @GeneratedValue var id: Long = 0L,
)
