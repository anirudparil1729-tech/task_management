package com.ctonew.taskmanagement.ui.categories

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ctonew.taskmanagement.core.database.model.CategoryEntity
import com.ctonew.taskmanagement.core.database.repository.CategoriesRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class CategoriesUIState(
  val categories: List<CategoryEntity> = emptyList(),
  val isLoading: Boolean = false,
)

@HiltViewModel
class CategoriesViewModel @Inject constructor(
  private val categoriesRepository: CategoriesRepository,
) : ViewModel() {
  private val _uiState = MutableStateFlow(CategoriesUIState())
  val uiState: StateFlow<CategoriesUIState> = _uiState.asStateFlow()

  init {
    viewModelScope.launch {
      categoriesRepository.categories
        .stateIn(viewModelScope)
        .collect { categories ->
          _uiState.value = CategoriesUIState(categories = categories)
        }
    }
  }

  fun createCategory(
    name: String,
    color: String = "#3B82F6",
    icon: String? = null,
  ) {
    viewModelScope.launch {
      categoriesRepository.createCategory(name, color, icon, isDefault = false)
    }
  }

  fun updateCategory(
    localId: String,
    name: String? = null,
    color: String? = null,
    icon: String? = null,
  ) {
    viewModelScope.launch {
      val updateDto = com.ctonew.taskmanagement.core.network.dto.CategoryUpdateDto(
        name = name,
        color = color,
        icon = icon,
      )
      categoriesRepository.updateCategory(localId, updateDto)
    }
  }

  fun deleteCategory(localId: String) {
    viewModelScope.launch {
      categoriesRepository.deleteCategory(localId)
    }
  }
}
