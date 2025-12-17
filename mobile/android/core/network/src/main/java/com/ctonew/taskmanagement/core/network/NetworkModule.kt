package com.ctonew.taskmanagement.core.network

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton
import okhttp3.HttpUrl.Companion.toHttpUrl

@Module
@InstallIn(SingletonComponent::class)
object NetworkModule {
  @Provides
  @Singleton
  fun provideMoshi() = NetworkClientFactory.createMoshi()

  @Provides
  @Singleton
  fun provideOkHttpClient() = NetworkClientFactory.createOkHttpClient(
    apiKey = BuildConfig.API_KEY,
    enableLogging = false,
  )

  @Provides
  @Singleton
  fun provideRetrofit(
    moshi: com.squareup.moshi.Moshi,
    okHttpClient: okhttp3.OkHttpClient,
  ): retrofit2.Retrofit {
    return retrofit2.Retrofit.Builder()
      .baseUrl(BuildConfig.BASE_URL.toHttpUrl())
      .client(okHttpClient)
      .addConverterFactory(
        retrofit2.converter.moshi.MoshiConverterFactory.create(moshi),
      )
      .build()
  }

  @Provides
  @Singleton
  fun provideTasksApi(retrofit: retrofit2.Retrofit): TasksApi {
    return retrofit.create(TasksApi::class.java)
  }

  @Provides
  @Singleton
  fun provideCategoriesApi(retrofit: retrofit2.Retrofit): CategoriesApi {
    return retrofit.create(CategoriesApi::class.java)
  }

  @Provides
  @Singleton
  fun provideTimeBlocksApi(retrofit: retrofit2.Retrofit): TimeBlocksApi {
    return retrofit.create(TimeBlocksApi::class.java)
  }

  @Provides
  @Singleton
  fun provideProductivityApi(retrofit: retrofit2.Retrofit): ProductivityApi {
    return retrofit.create(ProductivityApi::class.java)
  }

  @Provides
  @Singleton
  fun provideNotificationsApi(retrofit: retrofit2.Retrofit): NotificationsApi {
    return retrofit.create(NotificationsApi::class.java)
  }

  @Provides
  @Singleton
  fun provideSyncApi(retrofit: retrofit2.Retrofit): SyncApi {
    return retrofit.create(SyncApi::class.java)
  }
}
