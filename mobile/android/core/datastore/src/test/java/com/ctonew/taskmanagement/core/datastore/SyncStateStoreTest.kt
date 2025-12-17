package com.ctonew.taskmanagement.core.datastore

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.google.common.truth.Truth.assertThat
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.Test

class SyncStateStoreTest {
  @Test
  fun `defaults are zero`() = runBlocking {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val store = DataStoreSyncStateStore(context)
    store.clear()

    assertThat(store.lastTasksSyncMillis.first()).isEqualTo(0L)
    assertThat(store.lastCategoriesSyncMillis.first()).isEqualTo(0L)
    assertThat(store.lastTimeBlocksSyncMillis.first()).isEqualTo(0L)
  }
}
