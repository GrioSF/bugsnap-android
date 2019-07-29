package com.grio.lib.core.di

import com.grio.lib.core.di.viewmodel.ViewModelModule
import com.grio.lib.core.platform.BaseActivity
import com.grio.lib.features.editor.EditorActivity
import com.grio.lib.features.recorder.ReporterActivity

import dagger.Component
import javax.inject.Singleton

@Singleton
@Component(modules = [ApplicationModule::class, ViewModelModule::class])
interface ApplicationComponent {
    fun inject(baseActivity: BaseActivity)
    fun inject(editorActivity: EditorActivity)
    fun inject(reporterActivity: ReporterActivity)
}
