package com.martdev.shared.domain.exception

data class InternalServerException(val error: String = "the server encountered a problem") : Exception(error)
