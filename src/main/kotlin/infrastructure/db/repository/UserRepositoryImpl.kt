package com.martdev.infrastructure.db.repository

import com.martdev.domain.DataResult
import com.martdev.domain.model.User
import com.martdev.domain.repository.UserRepository
import com.martdev.infrastructure.db.tables.user.UserEntity
import com.martdev.infrastructure.db.tables.user.UserRefreshTokenEntity
import com.martdev.infrastructure.db.tables.user.UserRefreshTokenTable
import com.martdev.infrastructure.db.tables.user.UserTable
import com.martdev.infrastructure.db.tables.user.UserVerificationEntity
import com.martdev.infrastructure.db.tables.user.UserVerificationTable
import com.martdev.infrastructure.db.tables.user.UserVerificationTable.token
import com.martdev.infrastructure.db.tables.user.toUserModel
import com.martdev.infrastructure.db.withSuspendTransaction
import kotlinx.datetime.LocalDateTime
import org.jetbrains.exposed.v1.core.JoinType
import org.jetbrains.exposed.v1.core.and
import org.jetbrains.exposed.v1.core.dao.id.CompositeID
import org.jetbrains.exposed.v1.core.eq
import org.jetbrains.exposed.v1.core.greater
import org.jetbrains.exposed.v1.core.less
import org.jetbrains.exposed.v1.datetime.CurrentDateTime
import org.jetbrains.exposed.v1.jdbc.deleteWhere
import org.jetbrains.exposed.v1.jdbc.select
import org.koin.core.annotation.Single

@Single
class UserRepositoryImpl : UserRepository {
    override suspend fun activateUser(token: String): DataResult<Unit> {
        return withSuspendTransaction {
            val userId = getUserIdByVerificationToken(token) ?: return@withSuspendTransaction DataResult.Failure.NotFound

            updateIsVerifiedInUser(userId) ?: return@withSuspendTransaction DataResult.Failure.NotFound

            val deletedRow = deleteUserVerificationToken(userId)
            if (deletedRow > 0) {
                DataResult.Success(Unit)
            } else DataResult.Failure.UnknownError("Failed to delete user verification token")
        }
    }

    override suspend fun saveUserAndVerificationToken(
        user: User,
        token: String
    ): DataResult<User> {
        return withSuspendTransaction {
            val userEntity = createUser(user)
            createUserVerificationToken(token, userEntity)
            DataResult.Success(userEntity.toUserModel())
        }
    }

    override suspend fun saveRefreshToken(
        userId: Long,
        tokenHash: String,
        time: LocalDateTime
    ): DataResult<Unit> {
        return withSuspendTransaction {
            val entity = UserEntity.findById(userId) ?: return@withSuspendTransaction DataResult.Failure.NotFound

            val id = UserRefreshTokenEntity.new {
                userEntity = entity
                this.tokenHash = tokenHash
                expiryTime = time
            }.id
            if (id.value > 0) {
                DataResult.Success(Unit)
            } else DataResult.Failure.UnknownError("Failed to save token")
        }
    }

    override suspend fun deleteExpiredRefreshToken(): DataResult<Unit> {
        return withSuspendTransaction {
            UserRefreshTokenTable.deleteWhere {
                expiresAt less CurrentDateTime
            }
            DataResult.Success(Unit)
        }
    }

    override suspend fun getUserByEmail(email: String): DataResult<User> {
        return withSuspendTransaction {
            val entity = UserEntity.find {
                UserTable.email eq email
            }.firstOrNull() ?: return@withSuspendTransaction DataResult.Failure.NotFound

            DataResult.Success(entity.toUserModel())
        }
    }

    override suspend fun getUserById(userId: Long): DataResult<User> {
        return withSuspendTransaction {
            val entity = UserEntity.findById(userId) ?: return@withSuspendTransaction DataResult.Failure.NotFound

            DataResult.Success(entity.toUserModel())
        }
    }

    override suspend fun getUserIdAndRoleByRefreshToken(tokenHash: String): DataResult<User> {
        return withSuspendTransaction {
            val row = UserTable.join(
                otherTable = UserRefreshTokenTable,
                joinType = JoinType.INNER,
                onColumn = UserTable.id,
                otherColumn = UserRefreshTokenTable.userId
            ).select(UserTable.id, UserTable.role).where {
                (UserRefreshTokenTable.tokenHash eq tokenHash) and
                        (UserRefreshTokenTable.expiresAt.greater(CurrentDateTime)) and
                        (UserRefreshTokenTable.revoked eq false)
            }.firstOrNull() ?: return@withSuspendTransaction DataResult.Failure.NotFound

            val userId = row[UserTable.id].value
            val role = row[UserTable.role]

            DataResult.Success(User(id = userId, role = role))
        }
    }

    override suspend fun revokeRefreshToken(tokenHash: String): DataResult<Unit> {
        return withSuspendTransaction {
            UserRefreshTokenEntity.findSingleByAndUpdate(
                UserRefreshTokenTable.tokenHash eq tokenHash
            ) {
                it.revoked = true
            } ?: return@withSuspendTransaction DataResult.Failure.NotFound

            DataResult.Success(Unit)
        }
    }

    override suspend fun deleteAndCreateVerificationToken(token: String, userId: Long): DataResult<Unit> {
        return withSuspendTransaction {
            val deletedRow = deleteUser(userId)
            if (deletedRow == 0) {
                return@withSuspendTransaction DataResult.Failure.NotFound
            }
            val deletedVT = deleteUserVerificationToken(userId)
            if (deletedVT == 0) {
                return@withSuspendTransaction DataResult.Failure.NotFound
            }
            DataResult.Success(Unit)
        }
    }

    private fun getUserIdByVerificationToken(token: String) = UserTable.join(
        otherTable = UserVerificationTable,
        joinType = JoinType.INNER,
        onColumn = UserTable.id,
        otherColumn = UserVerificationTable.userId
    ).select(UserTable.id)
        .where {
            UserVerificationTable.token eq token
        }.firstOrNull()?.get(UserTable.id)?.value

    private fun updateIsVerifiedInUser(userId: Long) = UserEntity.findByIdAndUpdate(userId) {
        it.isVerified = true
    }

    private fun deleteUserVerificationToken(userId: Long) = UserVerificationTable.deleteWhere {
        UserVerificationTable.userId eq userId
    }

    private fun createUser(user: User) = UserEntity.new {
        email = user.email
        password = user.password
        role = user.role
    }

    private fun createUserVerificationToken(tokenParam: String, uid: UserEntity) {
        val userVerificationId = CompositeID {
            it[token] = tokenParam
        }
        UserVerificationEntity.new(userVerificationId) {
            userId = uid
        }
    }

    private fun deleteUser(userId: Long) = UserTable.deleteWhere {
        UserTable.id eq userId
    }
}