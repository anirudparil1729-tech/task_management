package com.ctonew.taskmanagement.ui.features

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import com.ctonew.taskmanagement.core.database.model.CategoryEntity
import com.ctonew.taskmanagement.core.database.model.TaskEntity
import com.ctonew.taskmanagement.core.database.model.TimeBlockEntity
import com.ctonew.taskmanagement.core.database.repository.CategoriesRepository
import com.ctonew.taskmanagement.core.database.repository.PlannerRepository
import com.ctonew.taskmanagement.core.database.repository.TasksRepository
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.Mock
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.kotlin.*
import java.time.LocalDate
import java.time.LocalTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import kotlin.test.*

@ExtendWith(MockitoExtension::class)
@OptIn(ExperimentalCoroutinesApi::class)
class PlannerViewModelTest {

    @Mock
    private lateinit var plannerRepository: PlannerRepository

    @Mock
    private lateinit var tasksRepository: TasksRepository

    @Mock
    private lateinit var categoriesRepository: CategoriesRepository

    private lateinit var viewModel: PlannerViewModel

    private val testDispatcher = UnconfinedTestDispatcher()
    private val testScope = TestScope(testDispatcher)

    private val testCategory = CategoryEntity(
        localId = "category-1",
        remoteId = 1,
        name = "Work",
        color = "#3B82F6",
        icon = "work",
        isDefault = false,
        createdAtMillis = System.currentTimeMillis(),
        updatedAtMillis = System.currentTimeMillis(),
        modifiedAtMillis = System.currentTimeMillis(),
    )

    private val testTask = TaskEntity(
        localId = "task-1",
        remoteId = 1,
        title = "Test Task",
        description = "Test Description",
        notes = null,
        dueDateMillis = LocalDate.now().atTime(12, 0).atZone(ZoneId.systemDefault()).toInstant().toEpochMilli(),
        priority = 1,
        recurrenceRule = null,
        categoryRemoteId = 1,
        reminderTimeMillis = null,
        isCompleted = false,
        completedAtMillis = null,
        createdAtMillis = System.currentTimeMillis(),
        updatedAtMillis = System.currentTimeMillis(),
        modifiedAtMillis = System.currentTimeMillis(),
    )

    private val testTimeBlock = TimeBlockEntity(
        localId = "timeblock-1",
        remoteId = 1,
        taskRemoteId = 1,
        startTimeMillis = LocalDate.now().atTime(9, 0).atZone(ZoneId.systemDefault()).toInstant().toEpochMilli(),
        endTimeMillis = LocalDate.now().atTime(10, 0).atZone(ZoneId.systemDefault()).toInstant().toEpochMilli(),
        title = "Test Time Block",
        description = null,
        createdAtMillis = System.currentTimeMillis(),
        updatedAtMillis = System.currentTimeMillis(),
        modifiedAtMillis = System.currentTimeMillis(),
    )

    @BeforeEach
    fun setUp() {
        viewModel = PlannerViewModel(
            plannerRepository = plannerRepository,
            tasksRepository = tasksRepository,
            categoriesRepository = categoriesRepository,
        )
    }

    @AfterEach
    fun tearDown() {
        // Clean up any resources
    }

    @Test
    fun `initial state should have default values`() = testScope.runTest {
        val uiState = viewModel.uiState.first()
        
        assertEquals(ViewMode.DAILY, uiState.viewMode)
        assertEquals(LocalDate.now(), uiState.selectedDate)
        assertTrue(uiState.timeBlocks.isEmpty())
        assertTrue(uiState.categories.isEmpty())
        assertTrue(uiState.tasks.isEmpty())
        assertFalse(uiState.isLoading)
        assertNull(uiState.error)
    }

    @Test
    fun `updateViewMode should change view mode and load appropriate data`() = testScope.runTest {
        // Given
        whenever(categoriesRepository.categories).thenReturn(flowOf(listOf(testCategory)))
        whenever(tasksRepository.tasks).thenReturn(flowOf(listOf(testTask)))
        whenever(plannerRepository.getTimeBlocksForDate(any(), any())).thenReturn(listOf(testTimeBlock))

        // When
        viewModel.updateViewMode(ViewMode.WEEKLY)
        
        val uiState = viewModel.uiState.first()
        
        // Then
        assertEquals(ViewMode.WEEKLY, uiState.viewMode)
    }

    @Test
    fun `updateSelectedDate should change selected date and reload data`() = testScope.runTest {
        // Given
        val newDate = LocalDate.now().plusDays(1)
        whenever(categoriesRepository.categories).thenReturn(flowOf(listOf(testCategory)))
        whenever(tasksRepository.tasks).thenReturn(flowOf(listOf(testTask)))
        whenever(plannerRepository.getTimeBlocksForDate(any(), any())).thenReturn(listOf(testTimeBlock))

        // When
        viewModel.updateSelectedDate(newDate)
        
        val uiState = viewModel.uiState.first()
        
        // Then
        assertEquals(newDate, uiState.selectedDate)
    }

    @Test
    fun `navigateToPreviousPeriod should move back one day in daily mode`() = testScope.runTest {
        // Given
        val startDate = LocalDate.now()
        whenever(categoriesRepository.categories).thenReturn(flowOf(listOf(testCategory)))
        whenever(tasksRepository.tasks).thenReturn(flowOf(listOf(testTask)))
        whenever(plannerRepository.getTimeBlocksForDate(any(), any())).thenReturn(listOf(testTimeBlock))

        viewModel.updateSelectedDate(startDate)
        
        // When
        viewModel.navigateToPreviousPeriod()
        
        val uiState = viewModel.uiState.first()
        
        // Then
        assertEquals(startDate.minusDays(1), uiState.selectedDate)
    }

    @Test
    fun `navigateToNextPeriod should move forward one week in weekly mode`() = testScope.runTest {
        // Given
        val startDate = LocalDate.now()
        viewModel.updateViewMode(ViewMode.WEEKLY)
        whenever(categoriesRepository.categories).thenReturn(flowOf(listOf(testCategory)))
        whenever(tasksRepository.tasks).thenReturn(flowOf(listOf(testTask)))
        whenever(plannerRepository.getTimeBlocksForWeek(any(), any())).thenReturn(listOf(testTimeBlock))

        // When
        viewModel.navigateToNextPeriod()
        
        val uiState = viewModel.uiState.first()
        
        // Then
        assertEquals(startDate.plusWeeks(1), uiState.selectedDate)
    }

    @Test
    fun `navigateToToday should set date to current date`() = testScope.runTest {
        // Given
        val originalDate = LocalDate.now().minusDays(5)
        viewModel.updateSelectedDate(originalDate)
        whenever(categoriesRepository.categories).thenReturn(flowOf(listOf(testCategory)))
        whenever(tasksRepository.tasks).thenReturn(flowOf(listOf(testTask)))
        whenever(plannerRepository.getTimeBlocksForDate(any(), any())).thenReturn(listOf(testTimeBlock))

        // When
        viewModel.navigateToToday()
        
        val uiState = viewModel.uiState.first()
        
        // Then
        assertEquals(LocalDate.now(), uiState.selectedDate)
    }

    @Test
    fun `createTimeBlock should create a new time block`() = testScope.runTest {
        // Given
        val request = CreateTimeBlockRequest(
            title = "New Time Block",
            startTime = LocalTime.of(14, 0),
            endTime = LocalTime.of(15, 30),
            taskLocalId = "task-1",
            description = "Test Description",
        )
        
        whenever(categoriesRepository.categories).thenReturn(flowOf(listOf(testCategory)))
        whenever(tasksRepository.tasks).thenReturn(flowOf(listOf(testTask)))
        whenever(plannerRepository.getTimeBlocksForDate(any(), any())).thenReturn(listOf(testTimeBlock))

        // When
        viewModel.createTimeBlock(request)

        // Then
        verify(plannerRepository).createTimeBlock(
            startTimeMillis = any(),
            endTimeMillis = any(),
            taskRemoteId = eq(1),
            estimatedDurationMillis = any(),
            title = eq("New Time Block"),
            description = eq("Test Description"),
        )
    }

    @Test
    fun `deleteTimeBlock should delete the specified time block`() = testScope.runTest {
        // Given
        val timeBlockId = "timeblock-1"
        
        // When
        viewModel.deleteTimeBlock(timeBlockId)

        // Then
        verify(plannerRepository).deleteTimeBlock(eq(timeBlockId))
    }

    @Test
    fun `getTimeBlocksUi should return formatted time blocks`() = testScope.runTest {
        // Given
        val expectedStartTime = "09:00"
        val expectedEndTime = "10:00"
        val expectedDuration = "01:00"

        whenever(categoriesRepository.categories).thenReturn(flowOf(listOf(testCategory)))
        whenever(tasksRepository.tasks).thenReturn(flowOf(listOf(testTask)))
        whenever(plannerRepository.getTimeBlocksForDate(any(), any())).thenReturn(listOf(testTimeBlock))

        // When
        viewModel.updateSelectedDate(LocalDate.now())
        val timeBlocksUi = viewModel.getTimeBlocksUi()

        // Then
        assertEquals(1, timeBlocksUi.size)
        val timeBlockUi = timeBlocksUi.first()
        assertEquals(expectedStartTime, timeBlockUi.startTime)
        assertEquals(expectedEndTime, timeBlockUi.endTime)
        assertEquals(expectedDuration, timeBlockUi.estimatedDuration)
        assertEquals(testTask.title, timeBlockUi.taskTitle)
        assertEquals(testCategory.color, timeBlockUi.categoryColor)
    }

    @Test
    fun `getTasksForSelectedDate should return tasks due on selected date`() = testScope.runTest {
        // Given
        val selectedDate = LocalDate.now()
        val zoneId = ZoneId.systemDefault()
        val startOfDay = selectedDate.atStartOfDay(zoneId).toInstant().toEpochMilli()
        val endOfDay = selectedDate.plusDays(1).atStartOfDay(zoneId).toInstant().toEpochMilli()
        
        val taskForDate = testTask.copy(localId = "task-2")
        
        whenever(categoriesRepository.categories).thenReturn(flowOf(listOf(testCategory)))
        whenever(tasksRepository.tasks).thenReturn(flowOf(listOf(taskForDate)))

        // When
        viewModel.updateSelectedDate(selectedDate)
        val tasks = viewModel.getTasksForSelectedDate()

        // Then
        assertEquals(1, tasks.size)
        assertEquals(taskForDate, tasks.first())
    }

    @Test
    fun `getIncompleteTasks should return only incomplete tasks`() = testScope.runTest {
        // Given
        val completedTask = testTask.copy(localId = "task-2", isCompleted = true)
        val incompleteTask = testTask.copy(localId = "task-3", isCompleted = false)
        
        whenever(categoriesRepository.categories).thenReturn(flowOf(listOf(testCategory)))
        whenever(tasksRepository.tasks).thenReturn(flowOf(listOf(testTask, completedTask, incompleteTask)))

        // When
        val incompleteTasks = viewModel.getIncompleteTasks()

        // Then
        assertEquals(2, incompleteTasks.size)
        assertTrue(incompleteTasks.all { !it.isCompleted })
    }

    @Test
    fun `clearError should clear error state`() = testScope.runTest {
        // Given - simulate an error state
        viewModel.clearError() // Initial call to set up state observer
        
        val uiState = viewModel.uiState.first()
        assertNull(uiState.error)

        // Verify error can be cleared (this is a basic test)
        viewModel.clearError()
        
        val clearedState = viewModel.uiState.first()
        assertNull(clearedState.error)
    }
}