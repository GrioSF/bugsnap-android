package com.grio.lib.features.editor

import android.util.Log
import androidx.lifecycle.MutableLiveData
import com.grio.lib.core.platform.BaseViewModel
import com.grio.lib.features.editor.cases.AddAttachment
import com.grio.lib.features.editor.cases.CreateIssue
import okhttp3.MediaType
import okhttp3.MultipartBody
import okhttp3.RequestBody
import java.io.File
import javax.inject.Inject

class EditorViewModel
@Inject constructor(private val createIssue: CreateIssue, private val addAttachment: AddAttachment) : BaseViewModel() {


    var toolOptionsShown: MutableLiveData<Boolean> = MutableLiveData()
    var isLoading: MutableLiveData<Boolean> = MutableLiveData()

    fun toggleToolOptionsDrawer() {
        toolOptionsShown.value?.let {
            toolOptionsShown.value = !it
        }
    }


    /**
     * Invoked when the "Add Ticket"
     * button is clicked.
     */
    fun addButtonClicked(summary: String, description: String, file: File) {
        // Start loading
        isLoading.value = true

        // Create issue.
        createIssue(CreateIssue.Params(summary, description)) { it.either({
            isLoading.value = false
            Log.e("BugSnap", "Failed to create issue: $it")

        }, {

            Log.i("BugSnap", "Successfully create issue.")

            // If successful, add attachment.
            val filePart = MultipartBody.Part.createFormData("file", file.name, RequestBody.create(MediaType.parse("image/*"), file))

            addAttachment(AddAttachment.Params(it.id, filePart)) { it.either({
                isLoading.value = false
                Log.e("BugSnap", "Failed to upload attachment: $it")
            }, {
                isLoading.value = false
                Log.i("BugSnap", "Attachment uploaded successfully! $it")
            })}
        }) }
    }
}