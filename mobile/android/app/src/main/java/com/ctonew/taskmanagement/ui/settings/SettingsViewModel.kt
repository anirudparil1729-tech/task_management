package com.ctonew.taskmanagement.ui.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ctonew.taskmanagement.core.common.session.SessionStore
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.launch

@HiltViewModel
class SettingsViewModel @Inject constructor(
  private val sessionStore: SessionStore,
) : ViewModel() {
  fun logout() {
    viewModelScope.launch {
      sessionStore.clear()
    }
  }
}
