package com.grio.lib.features.editor.cases

import com.grio.lib.core.interactor.UseCase
import com.grio.lib.features.editor.JiraRepository
import com.grio.lib.features.editor.models.CreationMeta
import javax.inject.Inject

class GetCreationMeta
@Inject constructor(private val repository: JiraRepository) : UseCase<CreationMeta, Unit>() {

    override suspend fun run(params: Unit?) = repository.getCreationMeta()

}