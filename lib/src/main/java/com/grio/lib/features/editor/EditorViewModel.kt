package com.grio.lib.features.editor

import android.util.Log
import androidx.lifecycle.MutableLiveData
import com.grio.lib.core.platform.BaseViewModel
import com.grio.lib.features.editor.cases.AddAttachment
import com.grio.lib.features.editor.cases.CreateIssue
import okhttp3.MultipartBody
import java.io.File
import javax.inject.Inject

class EditorViewModel
@Inject constructor(private val createIssue: CreateIssue, private val addAttachment: AddAttachment) : BaseViewModel() {


    var toolOptionsShown: MutableLiveData<Boolean> = MutableLiveData()
    var isLoading: MutableLiveData<Boolean> = MutableLiveData()
    var log: String = ""

    private val files = ArrayList<MultipartBody.Part>()

    fun toggleToolOptionsDrawer() {
        toolOptionsShown.value?.let {
            toolOptionsShown.value = !it
        }
    }


    /**
     * Invoked when the "Add Ticket"
     * button is clicked.
     */
    fun addButtonClicked(summary: String, description: String, file: File, logString: String = "") {
        // Start loading
        isLoading.value = true

        // Create issue.
        createIssue(CreateIssue.Params(summary, description)) { it.either({
            isLoading.value = false
            Log.e("BugSnap", "Failed to create issue: $it")

        }, {

            Log.i("BugSnap", "Successfully create issue.")

            if (file.isFile) {
                files.add(AddAttachment.prepareFilePart(file, "image/*"))
            }


            if (logString.isNotEmpty()) {
                files.add(AddAttachment.prepareFilePart("logcat.log", logString, "text/plain"))
            }

            addAttachment(AddAttachment.Params(it.id, files)) { it.either({
                isLoading.value = false
                Log.e("BugSnap", "Failed to upload attachments: $it")
            }, {
                isLoading.value = false
                Log.i("BugSnap", "Attachments uploaded successfully! $it")
            })}
        })}
    }
}