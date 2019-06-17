package com.grio.lib.features.report

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import com.grio.lib.core.di.DaggerInjector
import com.grio.lib.core.exception.Failure
import com.grio.lib.core.extension.failure
import com.grio.lib.core.extension.observe
import com.grio.lib.core.extension.viewModels
import javax.inject.Inject


class ReportSummaryActivity : AppCompatActivity() {


    @Inject
    lateinit var viewModelFactory: ViewModelProvider.Factory

    private lateinit var viewModel: ReportSummaryViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        DaggerInjector.getComponent().inject(this)

        viewModel = viewModels(viewModelFactory) {
            observe(issue, ::handleSuccess)
            failure(failure, ::handleFailure)
        }
    }

    fun handleFailure(failure: Failure?) {

    }

    fun handleSuccess(str :String?) {

    }
}