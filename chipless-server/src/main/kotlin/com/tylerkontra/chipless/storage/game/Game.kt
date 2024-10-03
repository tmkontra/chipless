package com.tylerkontra.chipless.storage.game

import com.tylerkontra.chipless.storage.player.Player
import jakarta.persistence.*
import java.time.Instant

@Entity
class Game(
    var name: String,
    var buyinCents: Int,
    var buyinChips: Int,
    var shortCode: String = newGameCode(),
    @OneToMany(mappedBy = "game", cascade = [CascadeType.ALL], orphanRemoval = true, fetch = FetchType.EAGER)
        var players: List<Player> = listOf(),
    var addedAt: Instant = Instant.now(),
    @Id @GeneratedValue var id: Long = 0L,
) {
    companion object Utils {
        const val shortcodeChars = "abcdefghijklmnopqrstuvwxyz"
        const val shortcodeLength = 9

        fun newGameCode(): String =
            (1..shortcodeLength)
                .map { shortcodeChars.random() }
                .joinToString("")
    }
}
