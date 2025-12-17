package com.ctonew.taskmanagement.ui.settings

import com.ctonew.taskmanagement.core.common.session.SessionStore
import com.ctonew.taskmanagement.core.database.dao.OutboxDao
import com.ctonew.taskmanagement.core.database.model.OutboxEntity
import com.ctonew.taskmanagement.core.database.model.OutboxEntityType
import com.ctonew.taskmanagement.core.database.model.OutboxOperation
import com.ctonew.taskmanagement.core.datastore.SyncStateStore
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.runTest

class SettingsViewModelTest {
  private val sessionStore = mockk<SessionStore>(relaxed = true)
  private val outboxDao = mockk<OutboxDao>()
  private val syncStateStore = mockk<SyncStateStore>()
  private val testDispatcher = StandardTestDispatcher()

  private fun createViewModel(): SettingsViewModel {
    return SettingsViewModel(sessionStore, outboxDao, syncStateStore)
  }

  @Test
  fun testPendingChangesCount() = runTest(testDispatcher) {
    val pendingChanges = listOf(
      OutboxEntity(
        entityType = OutboxEntityType.TASK,
        operation = OutboxOperation.CREATE,
        localId = "1",
        createdAtMillis = System.currentTimeMillis(),
      ),
      OutboxEntity(
        entityType = OutboxEntityType.TASK,
        operation = OutboxOperation.UPDATE,
        localId = "2",
        remoteId = 1,
        createdAtMillis = System.currentTimeMillis(),
      ),
    )

    coEvery { outboxDao.getPending() } returns pendingChanges
    coEvery { syncStateStore.lastTasksSyncMillis } returns flowOf(0L)

    val viewModel = createViewModel()

    val state = viewModel.uiState.value
    assertEquals(2, state.pendingChangesCount)
  }

  @Test
  fun testLastSyncedTime() = runTest(testDispatcher) {
    val lastSyncTime = System.currentTimeMillis() - 3600000
    coEvery { outboxDao.getPending() } returns emptyList()
    coEvery { syncStateStore.lastTasksSyncMillis } returns flowOf(lastSyncTime)

    val viewModel = createViewModel()

    val state = viewModel.uiState.value
    assertEquals(lastSyncTime, state.lastSyncedAt)
  }

  @Test
  fun testLogout() = runTest(testDispatcher) {
    coEvery { outboxDao.getPending() } returns emptyList()
    coEvery { syncStateStore.lastTasksSyncMillis } returns flowOf(0L)

    val viewModel = createViewModel()
    viewModel.logout()

    coVerify { sessionStore.clear() }
  }
}
