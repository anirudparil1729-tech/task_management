package com.ctonew.taskmanagement.ui.task

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ctonew.taskmanagement.core.database.repository.TasksRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.launch

@HiltViewModel
class TaskDetailViewModel @Inject constructor(
  private val tasksRepository: TasksRepository,
) : ViewModel() {
  fun createTask(title: String, description: String? = null) {
    viewModelScope.launch {
      tasksRepository.createTask(title, description)
    }
  }
}
