package com.martdev.shared.domain.exception

data class ConflictException(val error: String = "conflict") : Exception(error)
