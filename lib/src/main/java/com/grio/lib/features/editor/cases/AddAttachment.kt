package com.grio.lib.features.editor.cases

import com.grio.lib.core.interactor.UseCase
import com.grio.lib.features.editor.JiraRepository
import okhttp3.MultipartBody
import okhttp3.ResponseBody
import javax.inject.Inject

class AddAttachment
@Inject constructor(private val repository: JiraRepository) : UseCase<ResponseBody, AddAttachment.Params>() {

    override suspend fun run(params: Params?) = repository.addAttachment(params!!.issueId, params.attachment)

    data class Params(val issueId: String, val attachment: MultipartBody.Part)
}