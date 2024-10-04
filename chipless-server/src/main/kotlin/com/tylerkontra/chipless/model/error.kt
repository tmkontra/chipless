package com.tylerkontra.chipless.model

sealed class ChiplessErrror(message: String) : Exception(message) {
    class InvalidStateError(message: String) : ChiplessErrror(message)
    class ResourceNotFoundError(message: String) : ChiplessErrror(message) {
        companion object {
            fun ofEntity(name: String) = ResourceNotFoundError("${name} not found")
        }
    }
}