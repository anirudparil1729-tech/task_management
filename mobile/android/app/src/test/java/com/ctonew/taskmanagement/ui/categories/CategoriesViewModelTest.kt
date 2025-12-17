package com.ctonew.taskmanagement.ui.categories

import com.ctonew.taskmanagement.core.database.model.CategoryEntity
import com.ctonew.taskmanagement.core.database.repository.CategoriesRepository
import com.ctonew.taskmanagement.core.network.dto.CategoryUpdateDto
import io.mockk.any
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import io.mockk.slot
import io.mockk.withArg
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.runTest

class CategoriesViewModelTest {
  private val categoriesRepository = mockk<CategoriesRepository>(relaxed = true)
  private val testDispatcher = StandardTestDispatcher()

  private fun createViewModel(): CategoriesViewModel {
    return CategoriesViewModel(categoriesRepository)
  }

  @Test
  fun testCategoriesLoaded() = runTest(testDispatcher) {
    val categories = listOf(
      CategoryEntity(
        localId = "1",
        name = "Work",
        color = "#3B82F6",
        isDefault = true,
        createdAtMillis = 0,
        updatedAtMillis = 0,
        modifiedAtMillis = 0,
      ),
      CategoryEntity(
        localId = "2",
        name = "Personal",
        color = "#EC4899",
        isDefault = true,
        createdAtMillis = 0,
        updatedAtMillis = 0,
        modifiedAtMillis = 0,
      ),
    )

    coEvery { categoriesRepository.categories } returns flowOf(categories)

    val viewModel = createViewModel()

    val state = viewModel.uiState.value
    assertEquals(2, state.categories.size)
    assertEquals("Work", state.categories[0].name)
    assertEquals("Personal", state.categories[1].name)
  }

  @Test
  fun testCreateCategory() = runTest(testDispatcher) {
    coEvery { categoriesRepository.categories } returns flowOf(emptyList())
    val nameSlot = slot<String>()
    val colorSlot = slot<String>()
    coEvery { categoriesRepository.createCategory(capture(nameSlot), capture(colorSlot), null, false) } returns Unit

    val viewModel = createViewModel()
    viewModel.createCategory("Shopping", "#F59E0B", null)

    coVerify { categoriesRepository.createCategory("Shopping", "#F59E0B", null, false) }
  }

  @Test
  fun testUpdateCategory() = runTest(testDispatcher) {
    coEvery { categoriesRepository.categories } returns flowOf(emptyList())
    coEvery { categoriesRepository.updateCategory(any(), any()) } returns Unit

    val viewModel = createViewModel()
    viewModel.updateCategory("1", "Updated", "#F59E0B", null)

    coVerify {
      categoriesRepository.updateCategory(
        "1",
        withArg { update ->
          assertEquals("Updated", update.name)
          assertEquals("#F59E0B", update.color)
        },
      )
    }
  }

  @Test
  fun testDeleteCategory() = runTest(testDispatcher) {
    coEvery { categoriesRepository.categories } returns flowOf(emptyList())
    coEvery { categoriesRepository.deleteCategory(any()) } returns Unit

    val viewModel = createViewModel()
    viewModel.deleteCategory("1")

    coVerify { categoriesRepository.deleteCategory("1") }
  }

  @Test
  fun testColorValidation() {
    val validColors = listOf("#3B82F6", "#EC4899", "#8B5CF6", "#F59E0B")
    validColors.forEach { color ->
      assertTrue(color.startsWith("#") && color.length == 7)
    }
  }
}
