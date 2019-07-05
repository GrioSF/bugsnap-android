package com.grio.lib.features.editor.cases

import com.grio.lib.core.interactor.UseCase
import com.grio.lib.features.editor.models.CreateIssueResponse
import com.grio.lib.features.editor.JiraRepository
import javax.inject.Inject

class CreateIssue
@Inject constructor(private val repository: JiraRepository) : UseCase<CreateIssueResponse, CreateIssue.Params>() {

    override suspend fun run(params: Params?) = repository.createIssue(params!!.summary, params.description)

    data class Params(val summary: String, val description: String)
}