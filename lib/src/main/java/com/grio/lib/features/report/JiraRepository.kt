package com.grio.lib.features.report

import com.grio.lib.core.exception.Failure
import com.grio.lib.core.functional.Either
import com.grio.lib.core.platform.NetworkHandler
import com.grio.lib.core.extension.empty
import retrofit2.Call
import javax.inject.Inject

interface JiraRepository {

    fun createIssue(str: String): Either<Failure, String>

    /**
     * A [JiraRepository] implementation for network calls.
     */
    class Network
    @Inject constructor(private val networkHandler: NetworkHandler,
                        private val service: JiraService
    ) : JiraRepository {

        /**
         * Create issue.
         */
        override fun createIssue(str: String): Either<Failure, String> {
            return when (networkHandler.isConnected) {
                true -> request(service.createIssue(), { String.empty() }, String.empty())
                false, null -> Either.Left(Failure.NetworkConnection())
            }
        }

        /**
         * Executes the request.
         *
         * @param call the API call to execute.
         * @param transform a function to transform the response.
         * @param default the value returned by default.
         */
        private fun <T, R> request(call: Call<T>, transform: (T) -> R, default: T): Either<Failure, R> {
            return try {
                val response = call.execute()
                when (response.isSuccessful) {
                    true -> Either.Right(transform((response.body() ?: default)))
                    false -> Either.Left(Failure.ServerError())
                }
            } catch (exception: Throwable) {
                Either.Left(Failure.ServerError())
            }
        }
    }
}