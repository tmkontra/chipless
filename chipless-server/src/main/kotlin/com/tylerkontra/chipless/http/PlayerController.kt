package com.tylerkontra.chipless.http

import com.tylerkontra.chipless.http.protocol.Player
import com.tylerkontra.chipless.http.protocol.PlayerAction
import com.tylerkontra.chipless.http.protocol.PlayerHandView
import com.tylerkontra.chipless.model.ChiplessErrror
import com.tylerkontra.chipless.model.ShortCode
import com.tylerkontra.chipless.service.GameService
import jakarta.servlet.http.HttpServletResponse
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpStatus
import org.springframework.http.HttpStatusCode
import org.springframework.http.ResponseEntity
import org.springframework.util.MultiValueMap
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestHeader
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.client.HttpStatusCodeException
import org.springframework.web.server.ResponseStatusException
import org.springframework.web.servlet.function.ServerRequest.Headers

@RestController
@RequestMapping("/player/{playerCode}")
class PlayerController(
    private val gameService: GameService
) {
    @GetMapping("")
    fun viewPlayer(@PathVariable playerCode: ShortCode): Player {
        val p = gameService.findPlayerByCode(playerCode) ?: throw ChiplessErrror.ResourceNotFoundError.ofEntity("player")
        return Player.fromModel(p)
    }

    @GetMapping("/hand")
    fun viewCurrentHand(@PathVariable playerCode: ShortCode): ResponseEntity<PlayerHandView> {
        var v = gameService.getPlayerHandViewByCode(playerCode)
        var h = HttpHeaders()
        h.add(HttpHeaders.ETAG, v.nextActionState())
        return ResponseEntity(PlayerHandView.fromModel(v), h, HttpStatus.OK)
    }

    @PostMapping("/action")
    fun playerAction(
        @PathVariable playerCode: ShortCode,
        @RequestBody action: PlayerAction,
        @RequestHeader(HttpHeaders.IF_MATCH) handState: String,
    ): PlayerHandView {
        val currentHand = gameService.getPlayerHandViewByCode(playerCode)
        if (!currentHand.matches(handState)) {
             throw ResponseStatusException(HttpStatus.PRECONDITION_FAILED)
        }
        val updatedHand = gameService.doPlayerAction(currentHand, action.toModel())
        return PlayerHandView.fromModel(updatedHand)
    }
}