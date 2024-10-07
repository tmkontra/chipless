package com.tylerkontra.chipless.http

import com.tylerkontra.chipless.model.ChiplessError
import com.tylerkontra.chipless.model.ErrorCode
import org.apache.coyote.Response
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.ControllerAdvice
import org.springframework.web.bind.annotation.ExceptionHandler

@ControllerAdvice
class ExceptionHandler {

    @ExceptionHandler(ChiplessError.InvalidStateError::class)
    fun handleInvalidStateError(ex: ChiplessError.InvalidStateError): ResponseEntity<ErrorResponse> {
        val error = ErrorResponse(ex.message ?: "request would result in invalid state", ex.errorCode)
        return ResponseEntity(error, HttpStatus.CONFLICT)
    }

    @ExceptionHandler(ChiplessError.ResourceNotFoundError::class)
    fun handleResourceNotFoundError(ex: ChiplessError.ResourceNotFoundError): ResponseEntity<ErrorResponse> {
        return ResponseEntity(HttpStatus.NOT_FOUND)
    }

    companion object {
        data class ErrorResponse(val error: Error, val errorCode: ErrorCode? = null) {
            constructor(error: String, errorCode: ErrorCode? = null) : this(Error(error, errorCode))
        }
        data class Error(val message: String, val errorCode: ErrorCode? = null)
    }
}