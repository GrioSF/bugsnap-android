package com.grio.lib.features.editor

import com.grio.lib.UnitTest
import com.grio.lib.core.exception.Failure
import com.grio.lib.core.functional.Either
import com.grio.lib.core.platform.NetworkHandler
import com.grio.lib.features.BugSnap
import com.grio.lib.features.editor.models.CreateIssueRequest
import com.grio.lib.features.editor.models.CreateIssueResponse
import com.nhaarman.mockitokotlin2.given
import com.nhaarman.mockitokotlin2.verify
import okhttp3.MultipartBody
import org.amshove.kluent.mock
import org.amshove.kluent.shouldBeInstanceOf
import org.amshove.kluent.shouldEqual
import org.junit.Before
import org.junit.Test
import org.mockito.Mock
import org.mockito.Mockito
import retrofit2.Call
import retrofit2.Response


class JiraRepositoryTest : UnitTest() {

    private lateinit var networkRepository: JiraRepository.Network

    @Mock
    private lateinit var networkHandler: NetworkHandler
    @Mock
    private lateinit var service: JiraService

    @Mock
    private lateinit var createIssueCall: Call<CreateIssueResponse>
    @Mock
    private lateinit var createIssueResponse: Response<CreateIssueResponse>


    @Before
    fun setUp() {
        networkRepository = JiraRepository.Network(networkHandler, service)
    }

    @Test
    fun `should return a create issue response model containing an issue id after creating an issue`() {

        BugSnap.jiraProjectKey = "BSP"

        val req = CreateIssueRequest.create("BSP", "summary", "description", 1)
        given { networkHandler.isConnected }.willReturn(true)
        given { createIssueResponse.body() }.willReturn(CreateIssueResponse("123", "BSP-22", "example.com"))
        given { createIssueResponse.isSuccessful }.willReturn(true)
        given { createIssueCall.execute() }.willReturn(createIssueResponse)
        given {

            service.createIssue(req)
        }.willReturn(createIssueCall)

        val res = networkRepository.createIssue("summary", "description")

        res shouldEqual Either.Right(CreateIssueResponse("123", "BSP-22", "example.com"))
        verify(service).createIssue(req)
    }

    @Test
    fun `login service should return network failure when no connection`() {
        given { networkHandler.isConnected }.willReturn(false)

        val res = networkRepository.addAttachment("123", mock(MultipartBody.Part::class))

        res shouldBeInstanceOf Either::class.java
        res.isLeft shouldEqual true
        res.either({ failure -> failure shouldBeInstanceOf Failure.NetworkConnection::class.java }, {})
        Mockito.verifyZeroInteractions(service)
    }

    @Test
    fun `login service should return network failure when undefined connection`() {
        given { networkHandler.isConnected }.willReturn(null)

        val res = networkRepository.addAttachment("123", mock(MultipartBody.Part::class))

        res shouldBeInstanceOf Either::class.java
        res.isLeft shouldEqual true
        res.either({ failure -> failure shouldBeInstanceOf Failure.NetworkConnection::class.java }, {})
        Mockito.verifyZeroInteractions(service)
    }

}