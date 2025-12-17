package com.ctonew.taskmanagement.core.database

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.ctonew.taskmanagement.core.database.db.TaskManagementDatabase
import com.ctonew.taskmanagement.core.database.sync.SyncOutcome
import com.ctonew.taskmanagement.core.database.sync.SyncProcessor
import com.ctonew.taskmanagement.core.database.model.TaskEntity
import com.ctonew.taskmanagement.core.datastore.DataStoreSyncStateStore
import com.ctonew.taskmanagement.core.network.CategoriesApi
import com.ctonew.taskmanagement.core.network.NetworkClientFactory
import com.ctonew.taskmanagement.core.network.SyncApi
import com.ctonew.taskmanagement.core.network.TasksApi
import com.ctonew.taskmanagement.core.network.TimeBlocksApi
import com.google.common.truth.Truth.assertThat
import java.time.Instant
import kotlinx.coroutines.runBlocking
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import org.junit.After
import org.junit.Before
import org.junit.Test

class SyncConflictResolutionTest {
  private lateinit var db: TaskManagementDatabase
  private lateinit var server: MockWebServer

  @Before
  fun setUp() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    db = Room.inMemoryDatabaseBuilder(context, TaskManagementDatabase::class.java)
      .allowMainThreadQueries()
      .build()

    server = MockWebServer()
    server.start()
  }

  @After
  fun tearDown() {
    db.close()
    server.shutdown()
  }

  @Test
  fun `pull does not overwrite locally newer entity`() = runBlocking {
    val now = Instant.parse("2025-01-01T00:00:10Z").toEpochMilli()
    val older = Instant.parse("2025-01-01T00:00:00Z")

    db.taskDao().upsert(
      TaskEntity(
        localId = "local",
        remoteId = 1,
        title = "Local newer",
        createdAtMillis = now,
        updatedAtMillis = now,
        modifiedAtMillis = now,
      ),
    )

    val body =
      """
      [
        {
          "id": 1,
          "title": "Remote older",
          "description": null,
          "notes": null,
          "due_date": null,
          "priority": 0,
          "recurrence_rule": null,
          "category_id": null,
          "reminder_time": null,
          "is_completed": false,
          "completed_at": null,
          "created_at": "$older",
          "updated_at": "$older"
        }
      ]
      """.trimIndent()

    server.enqueue(
      MockResponse()
        .setBody(body)
        .addHeader("Content-Type", "application/json"),
    )

    server.enqueue(
      MockResponse()
        .setBody("[]")
        .addHeader("Content-Type", "application/json"),
    )

    server.enqueue(
      MockResponse()
        .setBody("[]")
        .addHeader("Content-Type", "application/json"),
    )

    val retrofit = NetworkClientFactory.createRetrofit(server.url("/"), apiKey = "k")
    val syncApi = retrofit.create(SyncApi::class.java)
    val tasksApi = retrofit.create(TasksApi::class.java)
    val categoriesApi = retrofit.create(CategoriesApi::class.java)
    val timeBlocksApi = retrofit.create(TimeBlocksApi::class.java)

    val context = ApplicationProvider.getApplicationContext<Context>()
    val syncStateStore = DataStoreSyncStateStore(context)
    syncStateStore.clear()

    val processor = SyncProcessor(
      tasksApi = tasksApi,
      categoriesApi = categoriesApi,
      timeBlocksApi = timeBlocksApi,
      syncApi = syncApi,
      taskDao = db.taskDao(),
      categoryDao = db.categoryDao(),
      timeBlockDao = db.timeBlockDao(),
      outboxDao = db.outboxDao(),
      syncStateStore = syncStateStore,
      moshi = NetworkClientFactory.createMoshi(),
    )

    val outcome = processor.syncOnce()
    assertThat(outcome).isEqualTo(SyncOutcome.Success)

    val taskAfter = db.taskDao().getByLocalId("local")
    requireNotNull(taskAfter)
    assertThat(taskAfter.title).isEqualTo("Local newer")

    syncStateStore.clear()
  }
}
