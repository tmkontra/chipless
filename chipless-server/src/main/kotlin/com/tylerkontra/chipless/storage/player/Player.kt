package com.tylerkontra.chipless.storage.player

import com.tylerkontra.chipless.storage.game.Game
import jakarta.persistence.*
import java.time.Instant

@Entity
class Player(
    var name: String,
    @ManyToOne(fetch = FetchType.EAGER) var game: Game,
    @Column(name = "buy_count")
        var buyCount: Int = 0,
    @Column(name = "short_code")
        var shortCode: String = newPlayerCode(),
    @OneToMany(mappedBy = "player", cascade = [CascadeType.ALL], orphanRemoval = true, fetch = FetchType.EAGER)
        var cashouts: MutableList<Cashout> = mutableListOf(),
    var addedAt: Instant = Instant.now(),
    @Id @GeneratedValue
        var id: Long = 0L,
) {
    companion object Utils {
        const val shortcodeChars = "abcdefghijklmnopqrstuvwxyz"
        const val shortcodeLength = 10

        fun newPlayerCode(): String =
            (1..shortcodeLength)
                .map { shortcodeChars.random() }
                .joinToString("")
    }
}