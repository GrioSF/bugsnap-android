package com.grio.lib.features.reporter

import android.util.Log
import androidx.lifecycle.MutableLiveData
import com.grio.lib.core.platform.BaseViewModel
import com.grio.lib.features.editor.cases.AddAttachment
import com.grio.lib.features.editor.cases.CreateIssue
import okhttp3.MultipartBody
import java.io.File
import javax.inject.Inject

class ReporterViewModel
@Inject constructor(private val createIssue: CreateIssue, private val addAttachment: AddAttachment) : BaseViewModel() {

    var isLoading: MutableLiveData<Boolean> = MutableLiveData()
    var log: String = ""

    private val files = ArrayList<MultipartBody.Part>()

    /**
     * Invoked when the "Add Ticket"
     * button is clicked.
     */
    fun sendReportClicked(summary: String = "Default summary.",
                          description: String = "Default Description.",
                          file: File,
                          logString: String = "",
                          isVideo: Boolean = false) {
        // Start loading
        isLoading.value = true

        // Create issue.
        createIssue(CreateIssue.Params(summary, description)) { it.either({
            isLoading.value = false
            Log.e("BugSnap", "Failed to create issue: $it")

        }, {

            Log.i("BugSnap", "Successfully create issue.")

            // If successful, add attachment.
            if (file.isFile) {
                val mimeType = if (isVideo) "video/*" else "image/*"
                files.add(AddAttachment.prepareFilePart(file, mimeType))
            }

            if (logString.isNotEmpty()) {
                files.add(AddAttachment.prepareFilePart("logcat.log", logString, "text/plain"))
            }

            addAttachment(AddAttachment.Params(it.id, files)) { it.either({
                isLoading.value = false
                Log.e("BugSnap", "Failed to upload attachment: $it")
            }, {
                isLoading.value = false
                Log.i("BugSnap", "Attachment uploaded successfully! $it")
            })}
        }) }
    }
}