package com.tylerkontra.chipless.http

import com.tylerkontra.chipless.model.ChiplessErrror
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.ControllerAdvice
import org.springframework.web.bind.annotation.ExceptionHandler

@ControllerAdvice
class ExceptionHandler {

    @ExceptionHandler(ChiplessErrror.InvalidStateError::class)
    fun handleInvalidStateError(ex: ChiplessErrror.InvalidStateError): ResponseEntity<ErrorResponse> {
        val error = ErrorResponse(ex.message ?: "request would result in invalid state")
        return ResponseEntity(error, HttpStatus.BAD_REQUEST)
    }

    companion object {
        data class ErrorResponse(val error: Error) {
            constructor(error: String) : this(Error(error))
        }
        data class Error(val message: String)
    }
}