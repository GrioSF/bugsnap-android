package com.grio.lib.core.di.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.grio.lib.features.editor.AnnotationViewModel
import com.grio.lib.features.report.ReportSummaryViewModel
import dagger.Binds
import dagger.Module
import dagger.multibindings.IntoMap

@Module
abstract class ViewModelModule {

    @Binds
    internal abstract fun bindViewModelFactory(factory: ViewModelFactory): ViewModelProvider.Factory

    @Binds
    @IntoMap
    @ViewModelKey(ReportSummaryViewModel::class)
    abstract fun bindsReportSummaryViewModel(reportSummaryViewModel: ReportSummaryViewModel): ViewModel

    @Binds
    @IntoMap
    @ViewModelKey(AnnotationViewModel::class)
    abstract fun bindsAnnotationViewModel(annotationViewModel: AnnotationViewModel): ViewModel

}