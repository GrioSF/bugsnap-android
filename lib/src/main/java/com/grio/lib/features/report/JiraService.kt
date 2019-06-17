package com.grio.lib.features.report

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

    override fun createIssue() = jiraApi.createIssue()

}