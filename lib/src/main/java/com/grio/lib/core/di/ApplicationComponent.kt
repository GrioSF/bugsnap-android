package com.grio.lib.core.di

import com.grio.lib.core.di.viewmodel.ViewModelModule
import com.grio.lib.core.platform.BaseActivity
import com.grio.lib.features.editor.ReportActivity
import com.grio.lib.features.report.ReportSummaryActivity
import dagger.Component
import javax.inject.Singleton

@Singleton
@Component(modules = [ApplicationModule::class, ViewModelModule::class])
interface ApplicationComponent {
    fun inject(baseActivity: BaseActivity)
    fun inject(reportActivity: ReportActivity)
    fun inject(reportSummaryActivity: ReportSummaryActivity)
}
