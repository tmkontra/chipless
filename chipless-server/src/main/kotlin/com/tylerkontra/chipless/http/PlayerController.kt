package com.tylerkontra.chipless.http

import com.tylerkontra.chipless.http.protocol.PlayerAction
import com.tylerkontra.chipless.http.protocol.PlayerAdminView
import com.tylerkontra.chipless.http.protocol.PlayerHandView
import com.tylerkontra.chipless.model.ChiplessError
import com.tylerkontra.chipless.model.ShortCode
import com.tylerkontra.chipless.service.GameService
import io.swagger.v3.oas.annotations.headers.Header
import io.swagger.v3.oas.annotations.responses.ApiResponse
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import org.springframework.web.server.ResponseStatusException

@RestController
@RequestMapping("/player/{playerCode}")
class PlayerController(
    private val gameService: GameService
) {
    @GetMapping("")
    fun viewPlayer(@PathVariable playerCode: ShortCode): PlayerAdminView {
        val p = gameService.findPlayerByCode(playerCode) ?: throw ChiplessError.ResourceNotFoundError.ofEntity("player")
        return PlayerAdminView.fromModel(p)
    }

    @GetMapping("/hand")
    @ApiResponse(responseCode = "200", headers = [Header(name=HttpHeaders.ETAG)])
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