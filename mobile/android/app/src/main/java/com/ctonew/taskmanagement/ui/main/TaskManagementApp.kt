package com.ctonew.taskmanagement.ui.main

import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.ctonew.taskmanagement.R
import com.ctonew.taskmanagement.ui.features.PlaceholderScreen
import com.ctonew.taskmanagement.ui.settings.SettingsRoute
import com.ctonew.taskmanagement.ui.task.TaskDetailSheetRoute
import kotlinx.coroutines.launch

@Composable
fun TaskManagementApp() {
  val navController = rememberNavController()
  val navBackStackEntry by navController.currentBackStackEntryAsState()
  val currentDestination = navBackStackEntry?.destination

  var isTaskDetailOpen by rememberSaveable { mutableStateOf(false) }
  val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
  val scope = rememberCoroutineScope()

  val openTaskDetail: () -> Unit = remember {
    {
      isTaskDetailOpen = true
    }
  }

  val closeTaskDetail: () -> Unit = {
    scope.launch {
      sheetState.hide()
      isTaskDetailOpen = false
    }
  }

  if (isTaskDetailOpen) {
    ModalBottomSheet(
      onDismissRequest = closeTaskDetail,
      sheetState = sheetState,
    ) {
      TaskDetailSheetRoute(onClose = closeTaskDetail)
    }
  }

  Scaffold(
    modifier = Modifier.fillMaxSize(),
    contentWindowInsets = WindowInsets.systemBars,
    floatingActionButton = {
      FloatingActionButton(
        onClick = openTaskDetail,
        containerColor = MaterialTheme.colorScheme.primary,
        contentColor = MaterialTheme.colorScheme.onPrimary,
      ) {
        Icon(
          imageVector = Icons.Filled.Add,
          contentDescription = stringResource(id = R.string.new_task),
        )
      }
    },
    bottomBar = {
      NavigationBar {
        TopLevelDestination.all.forEach { destination ->
          val selected = currentDestination?.hierarchy?.any { it.route == destination.route } == true
          val label = stringResource(id = destination.labelResId)

          NavigationBarItem(
            selected = selected,
            onClick = {
              navController.navigate(destination.route) {
                popUpTo(navController.graph.findStartDestination().id) {
                  saveState = true
                }
                launchSingleTop = true
                restoreState = true
              }
            },
            icon = { Icon(imageVector = destination.icon, contentDescription = label) },
            label = { Text(text = label) },
          )
        }
      }
    },
  ) { paddingValues ->
    NavHost(
      navController = navController,
      startDestination = TopLevelDestination.Dashboard.route,
      modifier = Modifier
        .fillMaxSize()
        .padding(paddingValues),
    ) {
      composable(TopLevelDestination.Dashboard.route) {
        com.ctonew.taskmanagement.ui.dashboard.DashboardRoute(
          onOpenTaskDetail = openTaskDetail,
        )
      }

      composable(TopLevelDestination.Categories.route) {
        com.ctonew.taskmanagement.ui.categories.CategoriesRoute()
      }

      composable(TopLevelDestination.Calendar.route) {
        PlaceholderScreen(titleResId = R.string.calendar)
      }

      composable(TopLevelDestination.Productivity.route) {
        PlaceholderScreen(titleResId = R.string.productivity)
      }

      composable(TopLevelDestination.Settings.route) {
        SettingsRoute(onOpenTaskDetail = openTaskDetail)
      }
    }
  }
}
