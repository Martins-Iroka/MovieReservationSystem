package com.martdev.shared.util

import com.martdev.shared.domain.exception.BadRequestException
import com.martdev.shared.domain.exception.InternalServerException
import com.martdev.shared.domain.exception.NotFoundException
import com.martdev.shared.domain.model.DataResult

fun <T> DataResult<T>.returnValue() = when (this) {
    is DataResult.Failure.NotFound -> throw NotFoundException(errorMessage)
    DataResult.Failure.UniqueViolation, DataResult.Failure.ForeignKeyViolation -> throw BadRequestException()
    is DataResult.Failure.UnknownError -> throw InternalServerException()
    is DataResult.Success -> value
}