package com.ctonew.taskmanagement.core.database

import android.content.Context
import androidx.room.Room
import androidx.work.WorkManager
import com.ctonew.taskmanagement.core.database.dao.CategoryDao
import com.ctonew.taskmanagement.core.database.dao.FocusSessionDao
import com.ctonew.taskmanagement.core.database.dao.OutboxDao
import com.ctonew.taskmanagement.core.database.dao.ProductivityLogDao
import com.ctonew.taskmanagement.core.database.dao.ReminderDao
import com.ctonew.taskmanagement.core.database.dao.TaskDao
import com.ctonew.taskmanagement.core.database.dao.TimeBlockDao
import com.ctonew.taskmanagement.core.database.db.TaskManagementDatabase
import com.ctonew.taskmanagement.core.database.repository.CategoriesRepository
import com.ctonew.taskmanagement.core.database.repository.DefaultCategoriesRepository
import com.ctonew.taskmanagement.core.database.repository.DefaultPlannerRepository
import com.ctonew.taskmanagement.core.database.repository.DefaultProductivityRepository
import com.ctonew.taskmanagement.core.database.repository.DefaultTasksRepository
import com.ctonew.taskmanagement.core.database.repository.PlannerRepository
import com.ctonew.taskmanagement.core.database.repository.ProductivityRepository
import com.ctonew.taskmanagement.core.database.repository.TasksRepository
import com.ctonew.taskmanagement.core.database.sync.SyncEngine
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {
  @Provides
  @Singleton
  fun provideDatabase(
    @ApplicationContext context: Context,
  ): TaskManagementDatabase = Room.databaseBuilder(
    context,
    TaskManagementDatabase::class.java,
    "task_management.db",
  ).build()

  @Provides
  fun provideTaskDao(db: TaskManagementDatabase): TaskDao = db.taskDao()

  @Provides
  fun provideCategoryDao(db: TaskManagementDatabase): CategoryDao = db.categoryDao()

  @Provides
  fun provideTimeBlockDao(db: TaskManagementDatabase): TimeBlockDao = db.timeBlockDao()

  @Provides
  fun provideOutboxDao(db: TaskManagementDatabase): OutboxDao = db.outboxDao()

  @Provides
  fun provideReminderDao(db: TaskManagementDatabase): ReminderDao = db.reminderDao()

  @Provides
  fun provideFocusSessionDao(db: TaskManagementDatabase): FocusSessionDao = db.focusSessionDao()

  @Provides
  fun provideProductivityLogDao(db: TaskManagementDatabase): ProductivityLogDao = db.productivityLogDao()

  @Provides
  @Singleton
  fun provideWorkManager(
    @ApplicationContext context: Context,
  ): WorkManager {
    return WorkManager.getInstance(context)
  }

  @Provides
  @Singleton
  fun provideSyncEngine(workManager: WorkManager): SyncEngine = SyncEngine(workManager)
}

@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoriesModule {
  @Binds
  abstract fun bindTasksRepository(impl: DefaultTasksRepository): TasksRepository

  @Binds
  abstract fun bindCategoriesRepository(impl: DefaultCategoriesRepository): CategoriesRepository

  @Binds
  abstract fun bindPlannerRepository(impl: DefaultPlannerRepository): PlannerRepository

  @Binds
  abstract fun bindProductivityRepository(impl: DefaultProductivityRepository): ProductivityRepository
}
