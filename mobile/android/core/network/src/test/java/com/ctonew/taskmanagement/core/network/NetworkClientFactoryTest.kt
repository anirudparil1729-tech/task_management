package com.ctonew.taskmanagement.core.network

import com.google.common.truth.Truth.assertThat
import java.util.concurrent.TimeUnit
import kotlinx.coroutines.runBlocking
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import org.junit.After
import org.junit.Before
import org.junit.Test

class NetworkClientFactoryTest {
  private lateinit var server: MockWebServer

  @Before
  fun setUp() {
    server = MockWebServer()
    server.start()
  }

  @After
  fun tearDown() {
    server.shutdown()
  }

  @Test
  fun `injects X-API-Key header`() = runBlocking {
    server.enqueue(
      MockResponse()
        .setBody("[]")
        .addHeader("Content-Type", "application/json"),
    )

    val retrofit = NetworkClientFactory.createRetrofit(
      baseUrl = server.url("/"),
      apiKey = "test-key",
    )

    val api = retrofit.create(TasksApi::class.java)
    val result = safeApiCall { api.getTasks() }

    assertThat(result).isInstanceOf(NetworkResult.Success::class.java)

    val request = server.takeRequest(1, TimeUnit.SECONDS)
    requireNotNull(request)
    assertThat(request.getHeader("X-API-Key")).isEqualTo("test-key")
  }

  @Test
  fun `surfaces http errors`() = runBlocking {
    server.enqueue(
      MockResponse()
        .setResponseCode(500)
        .setBody("{\"detail\":\"boom\"}")
        .addHeader("Content-Type", "application/json"),
    )

    val retrofit = NetworkClientFactory.createRetrofit(
      baseUrl = server.url("/"),
      apiKey = "test-key",
    )

    val api = retrofit.create(TasksApi::class.java)
    val result = safeApiCall { api.getTasks() }

    assertThat(result).isInstanceOf(NetworkResult.HttpError::class.java)
    val error = result as NetworkResult.HttpError
    assertThat(error.code).isEqualTo(500)
    assertThat(error.errorBody).contains("boom")
  }
}
