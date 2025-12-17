package com.ctonew.taskmanagement.ui.task

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.Checkbox
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.ctonew.taskmanagement.R

@Composable
fun TaskDetailSheetRoute(
  onClose: () -> Unit,
  viewModel: TaskDetailViewModel = hiltViewModel(),
) {
  TaskDetailSheet(
    onClose = onClose,
    onCreateTask = { title, description ->
      viewModel.createTask(title, description)
    },
  )
}

@Composable
fun TaskDetailSheet(
  onClose: () -> Unit,
  onCreateTask: (String, String?) -> Unit,
) {
  var title by remember { mutableStateOf("") }
  var description by remember { mutableStateOf("") }
  var notes by remember { mutableStateOf("") }
  var dueDate by remember { mutableStateOf("") }
  var priority by remember { mutableStateOf(0) }
  var isCompleted by remember { mutableStateOf(false) }

  Column(
    modifier = Modifier
      .fillMaxWidth()
      .verticalScroll(rememberScrollState())
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
    Row(
      modifier = Modifier.fillMaxWidth(),
      horizontalArrangement = Arrangement.SpaceBetween,
      verticalAlignment = Alignment.CenterVertically,
    ) {
      Text(
        text = stringResource(id = R.string.task_detail),
        style = MaterialTheme.typography.headlineLarge,
      )

      IconButton(onClick = onClose) {
        Icon(
          imageVector = Icons.Filled.Close,
          contentDescription = stringResource(id = R.string.close),
        )
      }
    }

    TextField(
      value = title,
      onValueChange = { title = it },
      label = { Text("Task Title") },
      modifier = Modifier.fillMaxWidth(),
      singleLine = true,
      keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
    )

    TextField(
      value = description,
      onValueChange = { description = it },
      label = { Text("Description") },
      modifier = Modifier
        .fillMaxWidth()
        .padding(vertical = 4.dp),
      keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
      minLines = 2,
    )

    TextField(
      value = notes,
      onValueChange = { notes = it },
      label = { Text("Notes") },
      modifier = Modifier
        .fillMaxWidth()
        .padding(vertical = 4.dp),
      keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
      minLines = 2,
    )

    TextField(
      value = dueDate,
      onValueChange = { dueDate = it },
      label = { Text("Due Date (yyyy-MM-dd)") },
      modifier = Modifier
        .fillMaxWidth()
        .padding(vertical = 4.dp),
      singleLine = true,
      keyboardOptions = KeyboardOptions(
        imeAction = ImeAction.Next,
        keyboardType = KeyboardType.Ascii,
      ),
    )

    Card(
      modifier = Modifier
        .fillMaxWidth()
        .padding(vertical = 4.dp),
    ) {
      Column(
        modifier = Modifier.padding(12.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
      ) {
        Text(
          text = "Priority",
          style = MaterialTheme.typography.labelMedium,
        )
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
          listOf("Low", "Medium", "High").forEachIndexed { index, label ->
            OutlinedButton(
              onClick = { priority = index },
              modifier = Modifier
                .weight(1f)
                .then(
                  if (priority == index) Modifier else Modifier
                ),
            ) {
              Text(label)
            }
          }
        }
      }
    }

    Row(
      modifier = Modifier
        .fillMaxWidth()
        .padding(vertical = 4.dp),
      horizontalArrangement = Arrangement.spacedBy(8.dp),
      verticalAlignment = Alignment.CenterVertically,
    ) {
      Checkbox(
        checked = isCompleted,
        onCheckedChange = { isCompleted = it },
      )
      Text(
        text = "Mark as completed",
        style = MaterialTheme.typography.bodyMedium,
      )
    }

    Button(
      onClick = {
        if (title.isNotBlank()) {
          onCreateTask(title, description.takeIf { it.isNotBlank() })
          onClose()
        }
      },
      modifier = Modifier.fillMaxWidth(),
    ) {
      Text(text = "Save Task")
    }

    OutlinedButton(
      onClick = onClose,
      modifier = Modifier.fillMaxWidth(),
    ) {
      Text(text = stringResource(id = R.string.close))
    }
  }
}
