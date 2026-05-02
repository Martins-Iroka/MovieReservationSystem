package com.martdev.infrastructure.db

import com.martdev.domain.DataResult
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import org.jetbrains.exposed.v1.core.SqlLogger
import org.jetbrains.exposed.v1.core.StdOutSqlLogger
import org.jetbrains.exposed.v1.core.Transaction
import org.jetbrains.exposed.v1.core.statements.StatementContext
import org.jetbrains.exposed.v1.core.statements.expandArgs
import org.jetbrains.exposed.v1.exceptions.ExposedSQLException
import org.jetbrains.exposed.v1.jdbc.JdbcTransaction
import org.jetbrains.exposed.v1.jdbc.transactions.inTopLevelSuspendTransaction
import org.jetbrains.exposed.v1.jdbc.transactions.suspendTransaction
import kotlin.time.Clock

suspend fun <T> withTopLevelSuspendTransaction(block: suspend JdbcTransaction.() -> DataResult<T>): DataResult<T> =
    withContext(Dispatchers.IO) {
        try {
            inTopLevelSuspendTransaction {
                addLogger(StdOutSqlLogger)
                try {
                    block()
                } catch (e: ExposedSQLException) {
                    handleDbException(e)
                } catch (e: Exception) {
                    DataResult.Failure.UnknownError(e.stackTraceToString())
                }
            }
        } catch (_: ExposedSQLException) {
            // UniqueViolation shouldn't be the db error type.
            // an exception thrown was BatchUpdateException(23514)
            DataResult.Failure.UniqueViolation
        } catch (e: Exception) {
            DataResult.Failure.UnknownError(e.stackTraceToString())
        }
    }

suspend fun <T> withSuspendTransaction(block: suspend JdbcTransaction.() -> DataResult<T>): DataResult<T> =
    withContext(Dispatchers.IO) {
        try {
            suspendTransaction {
                addLogger(CustomLogger)
                try {
                    block()
                } catch (e: ExposedSQLException) {
                    handleDbException(e)
                } catch (e: Exception) {
                    DataResult.Failure.UnknownError(e.stackTraceToString())
                }
            }
        } catch (_: ExposedSQLException) {
            // UniqueViolation shouldn't be the db error type.
            // an exception thrown was BatchUpdateException(23514)
            DataResult.Failure.UniqueViolation
        } catch (e: Exception) {
            DataResult.Failure.UnknownError(e.stackTraceToString())
        }
    }

fun handleDbException(e: ExposedSQLException): DataResult.Failure {
    return when (e.sqlState) {
        "23505" -> DataResult.Failure.UniqueViolation
        "23503" -> DataResult.Failure.ForeignKeyViolation
        else -> DataResult.Failure.UnknownError(e.stackTraceToString())
    }
}

object CustomLogger : SqlLogger {
    override fun log(
        context: StatementContext,
        transaction: Transaction
    ) {
        println("${Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault())} SQL: ${context.expandArgs(transaction)}")
    }
}