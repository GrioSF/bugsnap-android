package com.grio.lib.features.report

import com.grio.lib.core.interactor.UseCase
import javax.inject.Inject

class CreateIssue
@Inject constructor(private val repository: JiraRepository) : UseCase<String, CreateIssue.Params>() {

    override suspend fun run(params: Params) = repository.createIssue(params.str)

    data class Params(val str: String)
}