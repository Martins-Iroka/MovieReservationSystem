package com.martdev.shared.domain.model

sealed interface DataResult<out T> {
    data class Success<T>(val value: T) : DataResult<T>
    sealed interface Failure : DataResult<Nothing> {
        data object NotFound : Failure
        data object UniqueViolation : Failure
        data object ForeignKeyViolation : Failure
        data class UnknownError(val errorMessage: String, val cause: Throwable = Exception()) : Failure
    }
}