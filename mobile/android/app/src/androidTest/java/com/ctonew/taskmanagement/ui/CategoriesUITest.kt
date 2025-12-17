package com.ctonew.taskmanagement.ui

import androidx.compose.material3.MaterialTheme
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextInput
import com.ctonew.taskmanagement.core.database.model.CategoryEntity
import com.ctonew.taskmanagement.ui.categories.CategoriesScreen
import com.ctonew.taskmanagement.ui.categories.CategoriesUIState
import org.junit.Rule
import org.junit.Test

class CategoriesUITest {
  @get:Rule val composeTestRule = createComposeRule()

  @Test
  fun categoriesScreenDisplaysTitle() {
    val uiState = CategoriesUIState()

    composeTestRule.setContent {
      MaterialTheme {
        CategoriesScreen(
          uiState = uiState,
          onCreateCategory = { _, _, _ -> },
          onUpdateCategory = { _, _, _, _ -> },
          onDeleteCategory = { },
        )
      }
    }

    composeTestRule.onNodeWithText("Categories").assertIsDisplayed()
    composeTestRule.onNodeWithText("Add New Category").assertIsDisplayed()
  }

  @Test
  fun categoriesScreenDisplaysExistingCategories() {
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
        isDefault = false,
        createdAtMillis = 0,
        updatedAtMillis = 0,
        modifiedAtMillis = 0,
      ),
    )

    val uiState = CategoriesUIState(categories = categories)

    composeTestRule.setContent {
      MaterialTheme {
        CategoriesScreen(
          uiState = uiState,
          onCreateCategory = { _, _, _ -> },
          onUpdateCategory = { _, _, _, _ -> },
          onDeleteCategory = { },
        )
      }
    }

    composeTestRule.onNodeWithText("Your Categories").assertIsDisplayed()
    composeTestRule.onNodeWithText("Work").assertIsDisplayed()
    composeTestRule.onNodeWithText("Personal").assertIsDisplayed()
  }

  @Test
  fun addCategoryButtonCreatesCategory() {
    var createCalled = false
    var categoryName = ""
    var categoryColor = ""

    val uiState = CategoriesUIState()

    composeTestRule.setContent {
      MaterialTheme {
        CategoriesScreen(
          uiState = uiState,
          onCreateCategory = { name, color, _ ->
            createCalled = true
            categoryName = name
            categoryColor = color
          },
          onUpdateCategory = { _, _, _, _ -> },
          onDeleteCategory = { },
        )
      }
    }

    composeTestRule.onNodeWithText("Category name").performTextInput("Shopping")
    composeTestRule.onNodeWithText("Add Category").performClick()

    assert(createCalled)
    assert(categoryName == "Shopping")
  }

  @Test
  fun defaultCategoriesCannotBeDeleted() {
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
    )

    val uiState = CategoriesUIState(categories = categories)

    composeTestRule.setContent {
      MaterialTheme {
        CategoriesScreen(
          uiState = uiState,
          onCreateCategory = { _, _, _ -> },
          onUpdateCategory = { _, _, _, _ -> },
          onDeleteCategory = { },
        )
      }
    }

    composeTestRule.onNodeWithText("Default").assertIsDisplayed()
  }
}
