package com.grio.lib.core.di.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.grio.lib.features.editor.EditorViewModel
import com.grio.lib.features.recorder.ReporterViewModel
import dagger.Binds
import dagger.Module
import dagger.multibindings.IntoMap

@Module
abstract class ViewModelModule {

    @Binds
    internal abstract fun bindViewModelFactory(factory: ViewModelFactory): ViewModelProvider.Factory

    @Binds
    @IntoMap
    @ViewModelKey(EditorViewModel::class)
    abstract fun bindsAnnotationViewModel(editorViewModel: EditorViewModel): ViewModel

    @Binds
    @IntoMap
    @ViewModelKey(ReporterViewModel::class)
    abstract fun bindsReporterViewModel(editorViewModel: ReporterViewModel): ViewModel

}