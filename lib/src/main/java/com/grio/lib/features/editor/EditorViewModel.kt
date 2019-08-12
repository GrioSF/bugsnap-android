package com.grio.lib.features.editor

import androidx.lifecycle.MutableLiveData
import com.grio.lib.core.platform.BaseViewModel
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