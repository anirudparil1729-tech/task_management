package com.ctonew.taskmanagement.ui.task

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.ctonew.taskmanagement.R

@Composable
fun TaskDetailSheet(
  onClose: () -> Unit,
) {
  Column(
    modifier = Modifier
      .fillMaxWidth()
      .padding(
        PaddingValues(
          start = 20.dp,
          end = 20.dp,
          top = 8.dp,
          bottom = 20.dp,
        ),
      )
      .padding(WindowInsets.navigationBars.asPaddingValues()),
    verticalArrangement = Arrangement.spacedBy(12.dp),
  ) {
    Text(
      text = stringResource(id = R.string.task_detail),
      style = MaterialTheme.typography.headlineLarge,
    )

    Text(
      text = stringResource(id = R.string.task_detail_placeholder),
      style = MaterialTheme.typography.bodyLarge,
      color = MaterialTheme.colorScheme.onSurfaceVariant,
    )

    Button(onClick = onClose) {
      Text(text = stringResource(id = R.string.close))
    }
  }
}
