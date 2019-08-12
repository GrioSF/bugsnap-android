package com.grio.lib.features.reporter

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import com.grio.lib.UnitTest
import com.grio.lib.core.functional.Either
import com.grio.lib.features.DeviceInformation
import com.grio.lib.features.reporter.models.CreateIssueResponse
import com.grio.lib.features.reporter.cases.AddAttachment
import com.grio.lib.features.reporter.cases.CreateIssue
import com.nhaarman.mockitokotlin2.given
import kotlinx.coroutines.runBlocking
import okhttp3.MultipartBody
import okhttp3.ResponseBody
import org.amshove.kluent.mock
import org.amshove.kluent.shouldEqualTo
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mock
import org.mockito.junit.MockitoJUnitRunner
import java.io.File

@RunWith(MockitoJUnitRunner.Silent::class)
class ReporterViewModelTest : UnitTest() {

    @get:Rule
    val rule = InstantTaskExecutorRule()

    @Mock
    private lateinit var createIssue: CreateIssue
    @Mock
    private lateinit var addAttachment: AddAttachment

    private val viewModel by lazy { ReporterViewModel(createIssue, addAttachment) }

    @Test
    fun `add ticket request should update live data loading state`() {
        val responseBody = mock(ResponseBody::class)

        given { runBlocking { createIssue.run(CreateIssue.Params("test", "test")) } }.willReturn(
            Either.Right(
                CreateIssueResponse("123", "BSP-22", "example.com/123")
            ))
        given { runBlocking { addAttachment.run(AddAttachment.Params("123", listOf(mock(MultipartBody.Part::class)))) } }.willReturn(
            Either.Right(responseBody))

        viewModel.isLoading.observeForever {
            with(it) {
                this shouldEqualTo true
            }
        }

        runBlocking {
            viewModel.sendReportClicked(
                "",
                "",
                mock(File::class),
                "07-26 13:16:24.786 11328 11328 D BugSnap : shaking!",
                false,
                mock(DeviceInformation::class)
            )
        }
    }
}