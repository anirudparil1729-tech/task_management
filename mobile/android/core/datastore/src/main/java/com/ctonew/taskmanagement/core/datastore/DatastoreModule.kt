package com.ctonew.taskmanagement.core.datastore

import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
abstract class DatastoreModule {
  @Binds
  abstract fun bindSyncStateStore(impl: DataStoreSyncStateStore): SyncStateStore
}
