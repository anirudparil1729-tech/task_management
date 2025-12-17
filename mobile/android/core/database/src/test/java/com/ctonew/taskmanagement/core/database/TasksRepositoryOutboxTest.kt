package com.ctonew.taskmanagement.core.database

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.work.WorkManager
import androidx.work.testing.WorkManagerTestInitHelper
import com.ctonew.taskmanagement.core.database.db.TaskManagementDatabase
import com.ctonew.taskmanagement.core.database.model.OutboxEntityType
import com.ctonew.taskmanagement.core.database.model.OutboxOperation
import com.ctonew.taskmanagement.core.database.repository.DefaultTasksRepository
import com.ctonew.taskmanagement.core.database.sync.SyncEngine
import com.ctonew.taskmanagement.core.network.NetworkClientFactory
import com.google.common.truth.Truth.assertThat
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Before
import org.junit.Test

class TasksRepositoryOutboxTest {
  private lateinit var db: TaskManagementDatabase

  @Before
  fun setUp() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    db = Room.inMemoryDatabaseBuilder(context, TaskManagementDatabase::class.java)
      .allowMainThreadQueries()
      .build()
  }

  @After
  fun tearDown() {
    db.close()
  }

  @Test
  fun `createTask writes to Room and enqueues outbox`() = runBlocking {
    val context = ApplicationProvider.getApplicationContext<Context>()
    runCatching { WorkManagerTestInitHelper.initializeTestWorkManager(context) }

    val workManager = WorkManager.getInstance(context)
    val syncEngine = SyncEngine(workManager)

    val repo = DefaultTasksRepository(
      taskDao = db.taskDao(),
      outboxDao = db.outboxDao(),
      moshi = NetworkClientFactory.createMoshi(),
      syncEngine = syncEngine,
    )

    val localId = repo.createTask(title = "Test", description = "D")

    val task = db.taskDao().getByLocalId(localId)
    requireNotNull(task)
    assertThat(task.title).isEqualTo("Test")

    val pending = db.outboxDao().getPending()
    assertThat(pending).hasSize(1)
    assertThat(pending.first().entityType).isEqualTo(OutboxEntityType.TASK)
    assertThat(pending.first().operation).isEqualTo(OutboxOperation.CREATE)

    val workInfos = workManager.getWorkInfosForUniqueWork(SyncEngine.ONE_TIME_WORK_NAME).get()
    assertThat(workInfos).isNotEmpty()
  }
}
