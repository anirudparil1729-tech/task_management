package com.ctonew.taskmanagement.ui.categories

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.ctonew.taskmanagement.R
import com.ctonew.taskmanagement.core.database.model.CategoryEntity

val DEFAULT_COLORS = listOf(
  "#3B82F6", // Blue
  "#EC4899", // Pink
  "#8B5CF6", // Purple
  "#F59E0B", // Amber
  "#10B981", // Emerald
  "#EF4444", // Red
  "#06B6D4", // Cyan
  "#F97316", // Orange
)

@Composable
fun CategoriesRoute(
  viewModel: CategoriesViewModel = hiltViewModel(),
) {
  val uiState by viewModel.uiState.collectAsState()

  CategoriesScreen(
    uiState = uiState,
    onCreateCategory = viewModel::createCategory,
    onUpdateCategory = viewModel::updateCategory,
    onDeleteCategory = viewModel::deleteCategory,
  )
}

@Composable
fun CategoriesScreen(
  uiState: CategoriesUIState,
  onCreateCategory: (String, String, String?) -> Unit,
  onUpdateCategory: (String, String?, String?, String?) -> Unit,
  onDeleteCategory: (String) -> Unit,
) {
  var newCategoryName by remember { mutableStateOf("") }
  var selectedColor by remember { mutableStateOf(DEFAULT_COLORS[0]) }
  var showColorPicker by remember { mutableStateOf(false) }

  Column(
    modifier = Modifier
      .fillMaxSize()
      .background(MaterialTheme.colorScheme.background)
      .verticalScroll(rememberScrollState())
      .padding(WindowInsets.safeDrawing.asPaddingValues())
      .padding(horizontal = 16.dp, vertical = 16.dp),
    verticalArrangement = Arrangement.spacedBy(16.dp),
  ) {
    Text(
      text = stringResource(id = R.string.categories),
      style = MaterialTheme.typography.displaySmall,
      color = MaterialTheme.colorScheme.onBackground,
    )

    Surface(
      shape = MaterialTheme.shapes.medium,
      color = MaterialTheme.colorScheme.surfaceVariant,
      modifier = Modifier.fillMaxWidth(),
    ) {
      Column(
        modifier = Modifier.padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
      ) {
        Text(
          text = "Add New Category",
          style = MaterialTheme.typography.titleMedium,
        )

        TextField(
          value = newCategoryName,
          onValueChange = { newCategoryName = it },
          placeholder = { Text("Category name") },
          modifier = Modifier.fillMaxWidth(),
          singleLine = true,
          keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
        )

        Column(
          modifier = Modifier.fillMaxWidth(),
          verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
          Text(
            text = "Select Color",
            style = MaterialTheme.typography.bodySmall,
          )
          Row(
            modifier = Modifier
              .fillMaxWidth()
              .padding(vertical = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
          ) {
            DEFAULT_COLORS.forEach { color ->
              Surface(
                shape = CircleShape,
                color = Color(android.graphics.Color.parseColor(color)),
                modifier = Modifier
                  .size(40.dp)
                  .clickable { selectedColor = color }
                  .then(
                    if (selectedColor == color) Modifier.padding(2.dp) else Modifier
                  ),
              )
            }
          }
        }

        Button(
          onClick = {
            if (newCategoryName.isNotBlank()) {
              onCreateCategory(newCategoryName, selectedColor, null)
              newCategoryName = ""
              selectedColor = DEFAULT_COLORS[0]
            }
          },
          modifier = Modifier.fillMaxWidth(),
        ) {
          Icon(Icons.Filled.Add, contentDescription = null)
          Text(" Add Category")
        }
      }
    }

    if (uiState.categories.isNotEmpty()) {
      Text(
        text = "Your Categories",
        style = MaterialTheme.typography.titleMedium,
      )

      uiState.categories.forEach { category ->
        CategoryItem(
          category = category,
          onDelete = { onDeleteCategory(category.localId) },
        )
      }
    }
  }
}

@Composable
private fun CategoryItem(
  category: CategoryEntity,
  onDelete: () -> Unit,
) {
  Surface(
    shape = MaterialTheme.shapes.medium,
    color = MaterialTheme.colorScheme.surface,
    modifier = Modifier
      .fillMaxWidth()
      .clickable { },
  ) {
    Row(
      modifier = Modifier
        .fillMaxWidth()
        .padding(16.dp),
      horizontalArrangement = Arrangement.spacedBy(12.dp),
      verticalAlignment = Alignment.CenterVertically,
    ) {
      Surface(
        shape = CircleShape,
        color = Color(android.graphics.Color.parseColor(category.color)),
        modifier = Modifier.size(32.dp),
      )

      Column(
        modifier = Modifier.weight(1f),
        verticalArrangement = Arrangement.spacedBy(4.dp),
      ) {
        Text(
          text = category.name,
          style = MaterialTheme.typography.bodyMedium,
        )
        if (category.isDefault) {
          Text(
            text = "Default",
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
          )
        }
      }

      if (!category.isDefault) {
        IconButton(onClick = onDelete) {
          Icon(
            imageVector = Icons.Filled.Delete,
            contentDescription = "Delete",
            tint = MaterialTheme.colorScheme.error,
          )
        }
      }
    }
  }
}
