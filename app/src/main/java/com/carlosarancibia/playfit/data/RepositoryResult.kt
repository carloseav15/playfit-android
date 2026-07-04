package com.carlosarancibia.playfit.data

import java.io.IOException
import kotlinx.serialization.SerializationException
import retrofit2.HttpException

enum class DataSource { Network, Cache, Local }

sealed interface RepositoryError {
    val message: String
    val retryable: Boolean

    data class Network(override val message: String = "No network connection.") : RepositoryError {
        override val retryable: Boolean = true
    }

    data class Unauthorized(override val message: String = "Your session is no longer valid.") : RepositoryError {
        override val retryable: Boolean = false
    }

    data class Server(
        val statusCode: Int,
        override val message: String = "Playfit is temporarily unavailable.",
    ) : RepositoryError {
        override val retryable: Boolean = statusCode == 408 || statusCode == 429 || statusCode >= 500
    }

    data class InvalidData(override val message: String = "Playfit returned invalid data.") : RepositoryError {
        override val retryable: Boolean = false
    }

    data class Unknown(override val message: String = "Something went wrong.") : RepositoryError {
        override val retryable: Boolean = false
    }
}

sealed interface RepositoryResult<out T> {
    data class Success<T>(
        val data: T,
        val source: DataSource,
        val isStale: Boolean = false,
        val pendingSync: Boolean = false,
    ) : RepositoryResult<T>

    data class Failure(val error: RepositoryError) : RepositoryResult<Nothing>
}

internal fun Throwable.toRepositoryError(): RepositoryError = when (this) {
    is IOException -> RepositoryError.Network(message ?: "No network connection.")
    is HttpException -> when (code()) {
        401, 403 -> RepositoryError.Unauthorized()
        else -> RepositoryError.Server(statusCode = code())
    }
    is SerializationException -> RepositoryError.InvalidData(message ?: "Playfit returned invalid data.")
    else -> RepositoryError.Unknown(message ?: "Something went wrong.")
}
