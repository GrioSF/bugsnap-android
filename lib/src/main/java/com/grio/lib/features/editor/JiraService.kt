package com.grio.lib.features.editor

import com.grio.lib.features.editor.models.CreateIssueRequest

import okhttp3.MultipartBody
import retrofit2.Retrofit
import javax.inject.Inject
import javax.inject.Named
import javax.inject.Singleton


@Singleton
class JiraService
@Inject constructor(@Named("jira") retrofit: Retrofit) : JiraApi {


    private val jiraApi by lazy {
       retrofit.create(JiraApi::class.java)
    }

    override fun createIssue(req: CreateIssueRequest) = jiraApi.createIssue(req)

    override fun getCreationMeta() = jiraApi.getCreationMeta()

    override fun addAttachment(issueId: String, fileParts: List<MultipartBody.Part>) = jiraApi.addAttachment(issueId, fileParts)

}