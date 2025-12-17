package com.ctonew.taskmanagement.ui.main

import androidx.annotation.StringRes
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Category
import androidx.compose.material.icons.filled.Dashboard
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Insights
import androidx.compose.material.icons.filled.Settings
import androidx.compose.ui.graphics.vector.ImageVector
import com.ctonew.taskmanagement.R

sealed class TopLevelDestination(
  val route: String,
  @StringRes val labelResId: Int,
  val icon: ImageVector,
) {
  data object Dashboard : TopLevelDestination(
    route = "dashboard",
    labelResId = R.string.dashboard,
    icon = Icons.Filled.Dashboard,
  )

  data object Categories : TopLevelDestination(
    route = "categories",
    labelResId = R.string.categories,
    icon = Icons.Filled.Category,
  )

  data object Calendar : TopLevelDestination(
    route = "calendar",
    labelResId = R.string.calendar,
    icon = Icons.Filled.DateRange,
  )

  data object Productivity : TopLevelDestination(
    route = "productivity",
    labelResId = R.string.productivity,
    icon = Icons.Filled.Insights,
  )

  data object Settings : TopLevelDestination(
    route = "settings",
    labelResId = R.string.settings,
    icon = Icons.Filled.Settings,
  )

  companion object {
    val all = listOf(Dashboard, Categories, Calendar, Productivity, Settings)
  }
}
