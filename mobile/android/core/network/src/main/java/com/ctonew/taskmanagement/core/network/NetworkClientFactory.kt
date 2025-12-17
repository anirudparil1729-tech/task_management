package com.ctonew.taskmanagement.core.network

import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import okhttp3.HttpUrl
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import java.io.IOException

object NetworkClientFactory {
  fun createMoshi(): Moshi = Moshi.Builder()
    .add(InstantJsonAdapter())
    .add(LocalDateJsonAdapter())
    .addLast(KotlinJsonAdapterFactory())
    .build()

  fun createOkHttpClient(
    apiKey: String,
    enableLogging: Boolean = false,
  ): OkHttpClient {
    val builder = OkHttpClient.Builder()
      .addInterceptor(ApiKeyInterceptor(apiKey))

    if (enableLogging) {
      builder.addInterceptor(
        HttpLoggingInterceptor().apply {
          level = HttpLoggingInterceptor.Level.BODY
        },
      )
    }

    return builder.build()
  }

  fun createRetrofit(
    baseUrl: HttpUrl,
    apiKey: String,
    moshi: Moshi = createMoshi(),
    okHttpClient: OkHttpClient = createOkHttpClient(apiKey),
  ): Retrofit = Retrofit.Builder()
    .baseUrl(baseUrl)
    .client(okHttpClient)
    .addConverterFactory(MoshiConverterFactory.create(moshi))
    .build()
}

suspend fun <T : Any> safeApiCall(call: suspend () -> Response<T>): NetworkResult<T> {
  return try {
    val response = call()
    val body = response.body()

    if (response.isSuccessful && body != null) {
      NetworkResult.Success(body)
    } else if (response.isSuccessful) {
      NetworkResult.HttpError(
        code = response.code(),
        errorBody = "Empty body",
      )
    } else {
      NetworkResult.HttpError(
        code = response.code(),
        errorBody = response.errorBody()?.string(),
      )
    }
  } catch (e: IOException) {
    NetworkResult.NetworkError(e)
  } catch (t: Throwable) {
    NetworkResult.UnknownError(t)
  }
}

suspend fun safeEmptyApiCall(call: suspend () -> Response<Unit>): NetworkResult<Unit> {
  return try {
    val response = call()
    if (response.isSuccessful) {
      NetworkResult.Success(Unit)
    } else {
      NetworkResult.HttpError(
        code = response.code(),
        errorBody = response.errorBody()?.string(),
      )
    }
  } catch (e: IOException) {
    NetworkResult.NetworkError(e)
  } catch (t: Throwable) {
    NetworkResult.UnknownError(t)
  }
}
