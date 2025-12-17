package com.ctonew.taskmanagement.core.network

import com.ctonew.taskmanagement.core.network.dto.CategoryCreateDto
import com.ctonew.taskmanagement.core.network.dto.CategoryDto
import com.ctonew.taskmanagement.core.network.dto.CategoryUpdateDto
import com.ctonew.taskmanagement.core.network.dto.CategoryProductivityDto
import com.ctonew.taskmanagement.core.network.dto.NextReminderDto
import com.ctonew.taskmanagement.core.network.dto.ProductivitySummaryDto
import com.ctonew.taskmanagement.core.network.dto.SubTaskDto
import com.ctonew.taskmanagement.core.network.dto.TaskCreateDto
import com.ctonew.taskmanagement.core.network.dto.TaskDto
import com.ctonew.taskmanagement.core.network.dto.TaskUpdateDto
import com.ctonew.taskmanagement.core.network.dto.TaskWithSubTasksDto
import com.ctonew.taskmanagement.core.network.dto.TimeBlockCreateDto
import com.ctonew.taskmanagement.core.network.dto.TimeBlockDto
import com.ctonew.taskmanagement.core.network.dto.TimeBlockUpdateDto
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path
import retrofit2.http.Query

interface TasksApi {
  @POST("api/tasks")
  suspend fun createTask(@Body body: TaskCreateDto): Response<TaskDto>

  @GET("api/tasks")
  suspend fun getTasks(
    @Query("skip") skip: Int = 0,
    @Query("limit") limit: Int = 100,
    @Query("category_id") categoryId: Int? = null,
    @Query("is_completed") isCompleted: Boolean? = null,
  ): Response<List<TaskDto>>

  @GET("api/tasks/{task_id}")
  suspend fun getTask(@Path("task_id") taskId: Int): Response<TaskWithSubTasksDto>

  @PUT("api/tasks/{task_id}")
  suspend fun updateTask(
    @Path("task_id") taskId: Int,
    @Body body: TaskUpdateDto,
  ): Response<TaskDto>

  @DELETE("api/tasks/{task_id}")
  suspend fun deleteTask(@Path("task_id") taskId: Int): Response<Unit>

  @POST("api/tasks/{task_id}/subtasks")
  suspend fun createSubTask(
    @Path("task_id") taskId: Int,
    @Query("title") title: String,
  ): Response<SubTaskDto>

  @GET("api/tasks/{task_id}/subtasks")
  suspend fun getSubTasks(@Path("task_id") taskId: Int): Response<List<SubTaskDto>>

  @PUT("api/tasks/subtasks/{subtask_id}")
  suspend fun updateSubTask(
    @Path("subtask_id") subTaskId: Int,
    @Query("is_completed") isCompleted: Boolean? = null,
    @Query("title") title: String? = null,
    @Query("order") order: Int? = null,
  ): Response<SubTaskDto>

  @DELETE("api/tasks/subtasks/{subtask_id}")
  suspend fun deleteSubTask(@Path("subtask_id") subTaskId: Int): Response<Unit>
}

interface CategoriesApi {
  @POST("api/categories")
  suspend fun createCategory(@Body body: CategoryCreateDto): Response<CategoryDto>

  @GET("api/categories")
  suspend fun getCategories(
    @Query("skip") skip: Int = 0,
    @Query("limit") limit: Int = 100,
  ): Response<List<CategoryDto>>

  @GET("api/categories/{category_id}")
  suspend fun getCategory(@Path("category_id") categoryId: Int): Response<CategoryDto>

  @PUT("api/categories/{category_id}")
  suspend fun updateCategory(
    @Path("category_id") categoryId: Int,
    @Body body: CategoryUpdateDto,
  ): Response<CategoryDto>

  @DELETE("api/categories/{category_id}")
  suspend fun deleteCategory(@Path("category_id") categoryId: Int): Response<Unit>
}

interface TimeBlocksApi {
  @POST("api/time-blocks")
  suspend fun createTimeBlock(@Body body: TimeBlockCreateDto): Response<TimeBlockDto>

  @GET("api/time-blocks")
  suspend fun getTimeBlocks(
    @Query("skip") skip: Int = 0,
    @Query("limit") limit: Int = 100,
    @Query("task_id") taskId: Int? = null,
    @Query("start_date") startDate: String? = null,
    @Query("end_date") endDate: String? = null,
  ): Response<List<TimeBlockDto>>

  @GET("api/time-blocks/{block_id}")
  suspend fun getTimeBlock(@Path("block_id") blockId: Int): Response<TimeBlockDto>

  @PUT("api/time-blocks/{block_id}")
  suspend fun updateTimeBlock(
    @Path("block_id") blockId: Int,
    @Body body: TimeBlockUpdateDto,
  ): Response<TimeBlockDto>

  @DELETE("api/time-blocks/{block_id}")
  suspend fun deleteTimeBlock(@Path("block_id") blockId: Int): Response<Unit>
}

interface ProductivityApi {
  @GET("api/productivity/summary")
  suspend fun getSummary(
    @Query("target_date") targetDate: String? = null,
  ): Response<ProductivitySummaryDto>

  @GET("api/productivity/category/{category_id}")
  suspend fun getCategoryProductivity(
    @Path("category_id") categoryId: Int,
    @Query("target_date") targetDate: String? = null,
  ): Response<CategoryProductivityDto>
}

interface NotificationsApi {
  @GET("api/notifications/next-reminder")
  suspend fun getNextReminder(): Response<NextReminderDto>
}

interface SyncApi {
  @GET("api/sync/tasks")
  suspend fun syncTasks(
    @Query("modified_since") modifiedSince: String? = null,
  ): Response<List<TaskDto>>

  @GET("api/sync/categories")
  suspend fun syncCategories(
    @Query("modified_since") modifiedSince: String? = null,
  ): Response<List<CategoryDto>>

  @GET("api/sync/time-blocks")
  suspend fun syncTimeBlocks(
    @Query("modified_since") modifiedSince: String? = null,
  ): Response<List<TimeBlockDto>>
}
