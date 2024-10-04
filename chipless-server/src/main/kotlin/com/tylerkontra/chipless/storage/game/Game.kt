package com.tylerkontra.chipless.storage.game

import com.tylerkontra.chipless.storage.hand.Hand
import com.tylerkontra.chipless.storage.player.Player
import jakarta.persistence.*
import java.time.Instant

@Entity
class Game(
    var name: String,
    var buyinCents: Int,
    var buyinChips: Int,
    var shortCode: String = newGameCode(),
    var adminCode: String = newAdminCode(),
    @OneToMany(mappedBy = "game", cascade = [CascadeType.ALL], orphanRemoval = true, fetch = FetchType.EAGER)
        var players: List<Player> = listOf(),
    @OneToMany(mappedBy = "game", cascade = [CascadeType.ALL], orphanRemoval = true, fetch = FetchType.LAZY)
        var hands: MutableList<Hand> = mutableListOf(),
    var addedAt: Instant = Instant.now(),
    @Id @GeneratedValue var id: Long = 0L,
) {
    companion object Utils {
        const val shortcodeChars = "abcdefghijklmnopqrstuvwxyz"
        const val shortcodeLength = 9
        const val adminCodeLength = 12

        fun newGameCode(): String =
            (1..shortcodeLength)
                .map { shortcodeChars.random() }
                .joinToString("")

        fun newAdminCode(): String =
            (1..adminCodeLength)
                .map { shortcodeChars.random() }
                .joinToString("")
    }
}
