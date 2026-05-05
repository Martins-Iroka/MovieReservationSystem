package com.martdev.shared.domain.exception

data class NotFoundException(val error: String = "not found") : Exception(error)