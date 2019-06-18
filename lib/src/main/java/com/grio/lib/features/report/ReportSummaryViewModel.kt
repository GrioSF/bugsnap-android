package com.grio.lib.features.report

import androidx.lifecycle.MutableLiveData
import com.grio.lib.core.exception.Failure
import com.grio.lib.core.platform.BaseViewModel
import javax.inject.Inject

class ReportSummaryViewModel
@Inject constructor(private val createIssue: CreateIssue) : BaseViewModel() {

    var issue: MutableLiveData<String> = MutableLiveData()

    fun createIssue() {
        createIssue(CreateIssue.Params( "")) { it.either(::onFailure, ::onSuccess) }
    }

    private fun onSuccess(res: String) {
        issue.value = res
    }


    private fun onFailure(failure: Failure) {
        this.failure.value = failure
    }
}