package com.ctonew.taskmanagement.ui.features

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import com.ctonew.taskmanagement.core.database.model.CategoryEntity
import com.ctonew.taskmanagement.core.database.model.FocusSessionEntity
import com.ctonew.taskmanagement.core.database.model.ReminderEntity
import com.ctonew.taskmanagement.core.database.model.TaskEntity
import com.ctonew.taskmanagement.core.database.repository.CategoriesRepository
import com.ctonew.taskmanagement.core.database.repository.ProductivityRepository
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
import java.time.ZoneId
import kotlin.test.*

@ExtendWith(MockitoExtension::class)
@OptIn(ExperimentalCoroutinesApi::class)
class ProductivityViewModelTest {

    @Mock
    private lateinit var productivityRepository: ProductivityRepository

    @Mock
    private lateinit var tasksRepository: TasksRepository

    @Mock
    private lateinit var categoriesRepository: CategoriesRepository

    private lateinit var viewModel: ProductivityViewModel

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

    private val completedTask = testTask.copy(
        localId = "task-2",
        isCompleted = true,
        completedAtMillis = System.currentTimeMillis()
    )

    private val testFocusSession = FocusSessionEntity(
        localId = "focus-1",
        startedAtMillis = System.currentTimeMillis(),
        endedAtMillis = System.currentTimeMillis() + (30 * 60 * 1000), // 30 minutes later
        modifiedAtMillis = System.currentTimeMillis(),
    )

    private val testReminder = ReminderEntity(
        localId = "reminder-1",
        modifiedAtMillis = System.currentTimeMillis(),
        taskLocalId = "task-1",
        reminderTimeMillis = System.currentTimeMillis() + (60 * 60 * 1000), // 1 hour later
    )

    @BeforeEach
    fun setUp() {
        viewModel = ProductivityViewModel(
            productivityRepository = productivityRepository,
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
        
        assertEquals(LocalDate.now(), uiState.selectedDate)
        assertEquals(0.0, uiState.productivityScore)
        assertEquals(0, uiState.tasksCompleted)
        assertEquals(0, uiState.tasksPlanned)
        assertTrue(uiState.focusSessions.isEmpty())
        assertTrue(uiState.reminders.isEmpty())
        assertTrue(uiState.categories.isEmpty())
        assertTrue(uiState.tasks.isEmpty())
        assertFalse(uiState.isLoading)
        assertNull(uiState.error)
        assertEquals(FocusTimerState.IDLE, uiState.focusTimer)
    }

    @Test
    fun `updateSelectedDate should change selected date and recalculate metrics`() = testScope.runTest {
        // Given
        val newDate = LocalDate.now().plusDays(1)
        val expectedScore = 75.0
        
        whenever(categoriesRepository.categories).thenReturn(flowOf(listOf(testCategory)))
        whenever(tasksRepository.tasks).thenReturn(flowOf(listOf(testTask)))
        whenever(productivityRepository.focusSessions).thenReturn(flowOf(listOf(testFocusSession)))
        whenever(productivityRepository.reminders).thenReturn(flowOf(listOf(testReminder)))
        whenever(productivityRepository.getProductivityScoreForDate(newDate)).thenReturn(expectedScore)

        // When
        viewModel.updateSelectedDate(newDate)
        
        val uiState = viewModel.uiState.first()
        
        // Then
        assertEquals(newDate, uiState.selectedDate)
        assertEquals(expectedScore, uiState.productivityScore)
        verify(productivityRepository).getProductivityScoreForDate(eq(newDate))
    }

    @Test
    fun `startFocusSession should create and start a focus session`() = testScope.runTest {
        // Given
        val sessionId = "new-focus-session"
        val durationMinutes = 25
        
        whenever(productivityRepository.createFocusSession(any(), eq(durationMinutes))).thenReturn(sessionId)

        // When
        viewModel.startFocusSession(durationMinutes)
        
        val uiState = viewModel.uiState.first()
        
        // Then
        assertEquals(FocusTimerState.RUNNING, uiState.focusTimer)
        verify(productivityRepository).createFocusSession(any(), eq(durationMinutes))
    }

    @Test
    fun `stopFocusSession should stop the timer`() = testScope.runTest {
        // Given - Start a session first
        val sessionId = "focus-session"
        whenever(productivityRepository.createFocusSession(any(), eq(25))).thenReturn(sessionId)
        viewModel.startFocusSession(25)
        
        val runningState = viewModel.uiState.first()
        assertEquals(FocusTimerState.RUNNING, runningState.focusTimer)

        // When - Stop the session
        viewModel.stopFocusSession()
        
        val stoppedState = viewModel.uiState.first()
        
        // Then
        assertEquals(FocusTimerState.IDLE, stoppedState.focusTimer)
    }

    @Test
    fun `resetFocusTimer should reset timer to idle state`() = testScope.runTest {
        // Given - Set timer to running state
        viewModel.stopFocusSession() // Ensure we start from a clean state
        
        // When
        viewModel.resetFocusTimer()
        
        val uiState = viewModel.uiState.first()
        
        // Then
        assertEquals(FocusTimerState.IDLE, uiState.focusTimer)
    }

    @Test
    fun `getFocusTimerUi should return correct timer state`() = testScope.runTest {
        // Test idle state
        val idleTimer = viewModel.getFocusTimerUi()
        assertFalse(idleTimer.isRunning)
        assertEquals(0L, idleTimer.elapsedTime)
        assertEquals(0L, idleTimer.targetDuration)
        assertEquals(0L, idleTimer.remainingTime)
        assertNull(idleTimer.sessionLocalId)
    }

    @Test
    fun `createReminder should create a new reminder`() = testScope.runTest {
        // Given
        val request = ReminderRequest(
            title = "Take a break",
            description = "Time for a 5-minute break",
            reminderTime = java.time.LocalTime.of(14, 30),
            taskLocalId = "task-1",
        )

        // When
        viewModel.createReminder(request)

        // Then
        verify(productivityRepository).createReminder(
            title = eq("Take a break"),
            description = eq("Time for a 5-minute break"),
            reminderTimeMillis = any(),
            taskLocalId = eq("task-1"),
        )
    }

    @Test
    fun `deleteReminder should delete the specified reminder`() = testScope.runTest {
        // Given
        val reminderLocalId = "reminder-1"

        // When
        viewModel.deleteReminder(reminderLocalId)

        // Then
        verify(productivityRepository).deleteReminder(eq(reminderLocalId))
    }

    @Test
    fun `getCategoryProgress should return progress for all categories with tasks`() = testScope.runTest {
        // Given
        val workCategory = testCategory.copy(localId = "work-cat", name = "Work")
        val personalCategory = testCategory.copy(localId = "personal-cat", name = "Personal")
        val workTask = testTask.copy(localId = "work-1", categoryRemoteId = 1)
        val completedWorkTask = completedTask.copy(localId = "work-2", categoryRemoteId = 1, remoteId = 2)
        val personalTask = testTask.copy(localId = "personal-1", categoryRemoteId = 2, remoteId = 3)
        
        whenever(categoriesRepository.categories).thenReturn(flowOf(listOf(workCategory, personalCategory)))
        whenever(tasksRepository.tasks).thenReturn(flowOf(listOf(workTask, completedWorkTask, personalTask)))

        // When
        val progressList = viewModel.getCategoryProgress()

        // Then
        assertEquals(2, progressList.size)
        
        val workProgress = progressList.find { it.categoryId == "work-cat" }
        assertNotNull(workProgress)
        assertEquals(2, workProgress.tasksTotal)
        assertEquals(1, workProgress.tasksCompleted)
        assertEquals(50f, workProgress.percentage)
        
        val personalProgress = progressList.find { it.categoryId == "personal-cat" }
        assertNotNull(personalProgress)
        assertEquals(1, personalProgress.tasksTotal)
        assertEquals(0, personalProgress.tasksCompleted)
        assertEquals(0f, personalProgress.percentage)
    }

    @Test
    fun `getEndOfDaySummary should return correct summary data`() = testScope.runTest {
        // Given
        val currentDate = LocalDate.now()
        val summaryTasksCompleted = 3
        val summaryFocusSessions = 2
        val summaryScore = 85.0

        // Mock the focus sessions for today
        val todayFocusSession = testFocusSession.copy(
            localId = "today-focus",
            startedAtMillis = currentDate.atStartOfDay(ZoneId.systemDefault())
                .toInstant().toEpochMilli() + (9 * 60 * 60 * 1000) // 9 AM today
        )
        
        whenever(categoriesRepository.categories).thenReturn(flowOf(listOf(testCategory)))
        whenever(tasksRepository.tasks).thenReturn(flowOf(listOf(testTask, completedTask)))
        whenever(productivityRepository.focusSessions).thenReturn(flowOf(listOf(todayFocusSession)))
        
        viewModel.updateSelectedDate(currentDate)
        viewModel.uiState.first() // Wait for state update

        // When
        val summary = viewModel.getEndOfDaySummary()

        // Then
        assertTrue(summary.containsKey("tasksCompleted"))
        assertTrue(summary.containsKey("focusSessions"))
        assertTrue(summary.containsKey("productivityScore"))
        assertTrue(summary.containsKey("motivationalMessage"))
        
        assertTrue(summary["motivationalMessage"] is String)
        val message = summary["motivationalMessage"] as String
        assertTrue(message.contains("Outstanding work") || message.contains("Great progress"))
    }

    @Test
    fun `getIncompleteTasks should return only incomplete tasks`() = testScope.runTest {
        // Given
        val completedTask = testTask.copy(localId = "task-2", isCompleted = true)
        val incompleteTask1 = testTask.copy(localId = "task-3", isCompleted = false)
        val incompleteTask2 = testTask.copy(localId = "task-4", isCompleted = false)
        
        whenever(categoriesRepository.categories).thenReturn(flowOf(listOf(testCategory)))
        whenever(tasksRepository.tasks).thenReturn(flowOf(listOf(testTask, completedTask, incompleteTask1, incompleteTask2)))

        // When
        val incompleteTasks = viewModel.getIncompleteTasks()

        // Then
        assertEquals(3, incompleteTasks.size) // testTask + incompleteTask1 + incompleteTask2
        assertTrue(incompleteTasks.all { !it.isCompleted })
        assertFalse(incompleteTasks.any { it.localId == "task-2" })
    }

    @Test
    fun `calculateProductivityMetrics should update metrics correctly`() = testScope.runTest {
        // Given
        val currentDate = LocalDate.now()
        val score = 68.5
        
        whenever(productivityRepository.getProductivityScoreForDate(currentDate)).thenReturn(score)
        whenever(categoriesRepository.categories).thenReturn(flowOf(listOf(testCategory)))
        whenever(tasksRepository.tasks).thenReturn(flowOf(listOf(testTask, completedTask)))
        whenever(productivityRepository.focusSessions).thenReturn(flowOf(listOf(testFocusSession)))

        // When
        viewModel.updateSelectedDate(currentDate)
        
        val uiState = viewModel.uiState.first()
        
        // Then
        assertEquals(68.5, uiState.productivityScore)
        assertEquals(1, uiState.tasksCompleted) // completedTask
        assertEquals(2, uiState.tasksPlanned) // Both tasks have due dates
    }

    @Test
    fun `clearError should clear error state`() = testScope.runTest {
        // Given - verify initial state
        val initialState = viewModel.uiState.first()
        assertNull(initialState.error)

        // When - clear error (in a real scenario this would be called after an error occurred)
        viewModel.clearError()
        
        val clearedState = viewModel.uiState.first()
        
        // Then
        assertNull(clearedState.error)
    }

    @Test
    fun `focus sessions are filtered by selected date`() = testScope.runTest {
        // Given
        val today = LocalDate.now()
        val yesterday = LocalDate.now().minusDays(1)
        
        val todaySession = testFocusSession.copy(
            localId = "today",
            startedAtMillis = today.atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli()
        )
        val yesterdaySession = testFocusSession.copy(
            localId = "yesterday",
            startedAtMillis = yesterday.atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli()
        )
        
        whenever(categoriesRepository.categories).thenReturn(flowOf(listOf(testCategory)))
        whenever(tasksRepository.tasks).thenReturn(flowOf(listOf(testTask)))
        whenever(productivityRepository.focusSessions).thenReturn(flowOf(listOf(todaySession, yesterdaySession)))
        whenever(productivityRepository.getProductivityScoreForDate(today)).thenReturn(50.0)

        // When
        viewModel.updateSelectedDate(today)
        val uiState = viewModel.uiState.first()
        
        // The calculation should only count today's sessions
        assertEquals(1, uiState.focusSessions.size)
        assertEquals("today", uiState.focusSessions.first().localId)
    }
}