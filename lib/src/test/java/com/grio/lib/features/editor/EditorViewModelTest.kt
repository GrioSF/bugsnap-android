package com.grio.lib.features.editor

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import com.grio.lib.UnitTest
import org.amshove.kluent.shouldBe
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.junit.MockitoJUnitRunner

@RunWith(MockitoJUnitRunner.Silent::class)
class EditorViewModelTest : UnitTest() {

    @get:Rule
    val rule = InstantTaskExecutorRule()

    private val editorViewModel by lazy { EditorViewModel() }

    @Test
    fun `toggleToolOptionsDrawer() should post the opposite value`() {
        editorViewModel.toolOptionsShown.value shouldBe null
        editorViewModel.toolOptionsShown.value = true

        editorViewModel.toggleToolOptionsDrawer()

        editorViewModel.toolOptionsShown.value shouldBe false
    }
}