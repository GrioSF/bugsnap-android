package com.grio.lib.features.reporter

import com.grio.lib.core.exception.Failure
import com.grio.lib.core.functional.Either
import com.grio.lib.core.platform.NetworkHandler
import com.grio.lib.features.BugSnap
import com.grio.lib.features.reporter.models.CreateIssueRequest
import com.grio.lib.features.reporter.models.CreateIssueResponse
import com.grio.lib.features.reporter.models.CreationMeta
import okhttp3.MediaType
import okhttp3.MultipartBody
import okhttp3.ResponseBody
import retrofit2.Call
import javax.inject.Inject

interface JiraRepository {

    fun createIssue(summary: String, description: String): Either<Failure, CreateIssueResponse>
    fun getCreationMeta(): Either<Failure, CreationMeta>
    fun addAttachment(issueId: String, files: List<MultipartBody.Part>) : Either<Failure, ResponseBody>

    /**
     * A [JiraRepository] implementation for network calls.
     */
    class Network
    @Inject constructor(private val networkHandler: NetworkHandler,
                        private val service: JiraService
    ) : JiraRepository {

        override fun addAttachment(issueId: String, files: List<MultipartBody.Part>): Either<Failure, ResponseBody> {
            return when (networkHandler.isConnected) {
                true -> request(service.addAttachment(issueId, files), { it }, ResponseBody.create(MediaType.parse(""), ""))
                false, null -> Either.Left(Failure.NetworkConnection())
            }
        }

        /**
         * Create issue.
         */
        override fun createIssue(summary: String, description: String): Either<Failure, CreateIssueResponse> {

            // Bugs have an id of 1.
            val req = CreateIssueRequest.create(BugSnap.jiraProjectKey, summary, description, 1)

            return when (networkHandler.isConnected) {
                true -> request(service.createIssue(req), { it }, CreateIssueResponse("","",""))
                false, null -> Either.Left(Failure.NetworkConnection())
            }
        }

        override fun getCreationMeta(): Either<Failure, CreationMeta> {
            return when (networkHandler.isConnected) {
                true -> request(service.getCreationMeta(), { it },
                    CreationMeta("", mutableListOf())
                )
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