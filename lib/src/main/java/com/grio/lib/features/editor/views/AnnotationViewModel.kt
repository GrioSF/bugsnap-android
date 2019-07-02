package com.grio.lib.features.editor.views

import androidx.lifecycle.MutableLiveData
import com.grio.lib.core.platform.BaseViewModel
import javax.inject.Inject

class AnnotationViewModel
@Inject constructor() : BaseViewModel() {

    var toolOptionsShown: MutableLiveData<Boolean> = MutableLiveData()
}