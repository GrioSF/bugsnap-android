package com.grio.lib.features.report

import retrofit2.Call
import retrofit2.http.POST

internal interface JiraApi {


    /**
     * Create JIRA issue
     */
    @POST("rest/api/2/issue/")
    fun createIssue() : Call<String>
}