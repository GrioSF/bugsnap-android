package com.grio.lib.features.editor

import com.grio.lib.UnitTest
import com.grio.lib.core.functional.Either
import com.grio.lib.features.editor.cases.AddAttachment
import com.grio.lib.features.editor.cases.CreateIssue
import com.grio.lib.features.editor.models.CreateIssueResponse

import com.nhaarman.mockitokotlin2.given
import kotlinx.coroutines.runBlocking
import okhttp3.MultipartBody
import okhttp3.ResponseBody
import org.amshove.kluent.mock
import org.amshove.kluent.shouldEqualTo
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mock
import org.mockito.junit.MockitoJUnitRunner
import java.io.File

@RunWith(MockitoJUnitRunner.Silent::class)
class EditorViewModelTest : UnitTest() {

    private lateinit var editorViewModel: EditorViewModel

    @Mock
    private lateinit var createIssue: CreateIssue
    @Mock
    private lateinit var addAttachment: AddAttachment

    @Before
    fun setUp() {
        editorViewModel = EditorViewModel(createIssue, addAttachment)
    }

    @Test
    fun `add ticket request should update live data loading state`() {
        val responseBody = mock(ResponseBody::class)

        given { runBlocking { createIssue.run(CreateIssue.Params("test", "test")) } }.willReturn(Either.Right(CreateIssueResponse("123", "BSP-22", "example.com/123")))
        given { runBlocking { addAttachment.run(AddAttachment.Params("123", listOf(mock(MultipartBody.Part::class)))) } }.willReturn(Either.Right(responseBody))

        editorViewModel.isLoading.observeForever {
            with(it) {
                this shouldEqualTo true
            }
        }

        runBlocking { editorViewModel.addButtonClicked("", "", mock(File::class), "07-26 13:16:24.786 11328 11328 D BugSnap : shaking!") }
    }
}