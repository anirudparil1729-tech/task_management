package com.ctonew.taskmanagement.core.common.session

import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
abstract class SessionModule {
  @Binds
  abstract fun bindSessionStore(impl: DataStoreSessionStore): SessionStore
}
