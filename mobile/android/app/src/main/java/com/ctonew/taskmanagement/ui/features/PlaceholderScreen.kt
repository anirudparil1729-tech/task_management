package com.ctonew.taskmanagement.ui.features

import androidx.annotation.StringRes
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import com.ctonew.taskmanagement.R

@Composable
fun PlaceholderScreen(
  @StringRes titleResId: Int,
) {
  Box(
    modifier = Modifier
      .fillMaxSize()
      .background(MaterialTheme.colorScheme.background)
      .padding(WindowInsets.safeDrawing.asPaddingValues()),
    contentAlignment = Alignment.Center,
  ) {
    Text(
      text = stringResource(
        id = R.string.placeholder_format,
        stringResource(id = titleResId),
      ),
      style = MaterialTheme.typography.titleLarge,
      color = MaterialTheme.colorScheme.onBackground,
    )
  }
}
