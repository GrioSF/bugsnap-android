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
@Inject constructor() : BaseViewModel() {

    var toolOptionsShown: MutableLiveData<Boolean> = MutableLiveData()

    fun toggleToolOptionsDrawer() {
        toolOptionsShown.value?.let {
            toolOptionsShown.value = !it
        }
    }
}