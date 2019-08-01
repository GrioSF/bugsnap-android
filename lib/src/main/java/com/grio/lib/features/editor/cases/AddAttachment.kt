package com.grio.lib.features.editor.cases

import com.grio.lib.core.interactor.UseCase
import com.grio.lib.features.editor.JiraRepository
import okhttp3.MediaType
import okhttp3.MultipartBody
import okhttp3.RequestBody
import okhttp3.ResponseBody
import java.io.File
import javax.inject.Inject

class AddAttachment
@Inject constructor(private val repository: JiraRepository) : UseCase<ResponseBody, AddAttachment.Params>() {

    val fileKey: String = "file"

    override suspend fun run(params: Params?) = repository.addAttachment(params!!.issueId, params.attachment)

    data class Params(val issueId: String, val attachment: List<MultipartBody.Part>)

    fun prepareFilePart(file: File, mimetype: String): MultipartBody.Part {
        return MultipartBody.Part.createFormData(fileKey, file.name, RequestBody.create(MediaType.parse(mimetype), file))
    }

    fun prepareFilePart(name: String, string: String, mimetype: String): MultipartBody.Part {
        return MultipartBody.Part.createFormData(fileKey, name, RequestBody.create(MediaType.parse(mimetype), string))
    }

}