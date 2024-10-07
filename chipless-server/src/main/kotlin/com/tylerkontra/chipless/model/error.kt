package com.tylerkontra.chipless.model

sealed class ChiplessError(message: String, val errorCode: ErrorCode? = null) : Exception(message) {
    class InvalidStateError(message: String, errorCode: ErrorCode? = null) : ChiplessError(message, errorCode) {

    }
    class ResourceNotFoundError(message: String) : ChiplessError(message) {
        companion object {
            fun ofEntity(name: String) = ResourceNotFoundError("${name} not found")
        }
    }
    class CorruptStateError(message: String) : ChiplessError(message)
}

sealed class ErrorCode(val value: String) {
    object NoCurrentHand : ErrorCode("E148")
}