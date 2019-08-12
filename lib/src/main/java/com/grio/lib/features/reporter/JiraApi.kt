package com.grio.lib.features.reporter

import com.grio.lib.features.reporter.models.CreateIssueRequest
import com.grio.lib.features.reporter.models.CreateIssueResponse
import com.grio.lib.features.reporter.models.CreationMeta
import okhttp3.MultipartBody
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.http.*

internal interface JiraApi {


    /**
     * Create JIRA issue.
     */
    @POST("rest/api/2/issue/")
    fun createIssue(@Body req: CreateIssueRequest) : Call<CreateIssueResponse>

    /**
     * Add one or more attachments to an issue.
     */
    @Multipart
    @Headers("X-Atlassian-Token: no-check")
    @POST("rest/api/2/issue/{issueId}/attachments")
    fun addAttachment(@Path("issueId") issueId: String,
                      @Part filePart: List<MultipartBody.Part>): Call<ResponseBody>


    /**
     * Retrieve meta data.
     */
    @GET("rest/api/3/issue/createmeta")
    fun getCreationMeta(): Call<CreationMeta>
}