package com.martdev.shared.domain.exception

data class UnauthorizedException(val error: String = "unauthorized") : Exception(error)
