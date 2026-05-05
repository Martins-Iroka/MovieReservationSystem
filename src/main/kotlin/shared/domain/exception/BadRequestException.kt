package com.martdev.shared.domain.exception

data class BadRequestException(val error: String = "bad request") : Exception(error)