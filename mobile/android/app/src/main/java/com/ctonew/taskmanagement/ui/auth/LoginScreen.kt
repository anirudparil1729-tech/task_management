package com.ctonew.taskmanagement.ui.auth

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import com.ctonew.taskmanagement.R

@Composable
fun LoginScreen(
  uiState: LoginUiState,
  onPasswordChanged: (String) -> Unit,
  onLogin: () -> Unit,
) {
  Column(
    modifier = Modifier
      .fillMaxSize()
      .background(MaterialTheme.colorScheme.background)
      .padding(WindowInsets.systemBars.asPaddingValues())
      .padding(horizontal = 20.dp, vertical = 24.dp),
    verticalArrangement = Arrangement.spacedBy(16.dp),
  ) {
    Text(
      text = stringResource(id = R.string.welcome),
      style = MaterialTheme.typography.displayMedium,
      color = MaterialTheme.colorScheme.onBackground,
    )

    Text(
      text = stringResource(id = R.string.enter_password_prompt),
      style = MaterialTheme.typography.bodyLarge,
      color = MaterialTheme.colorScheme.onBackground,
    )

    OutlinedTextField(
      value = uiState.password,
      onValueChange = onPasswordChanged,
      label = { Text(stringResource(id = R.string.password_label)) },
      singleLine = true,
      isError = uiState.errorMessageResId != null,
      visualTransformation = PasswordVisualTransformation(),
      keyboardOptions = KeyboardOptions(
        keyboardType = KeyboardType.Password,
        imeAction = ImeAction.Done,
      ),
      keyboardActions = KeyboardActions(
        onDone = { onLogin() },
      ),
      modifier = Modifier.padding(top = 8.dp),
    )

    uiState.errorMessageResId?.let { resId ->
      Text(
        text = stringResource(id = resId),
        style = MaterialTheme.typography.bodyMedium,
        color = MaterialTheme.colorScheme.error,
      )
    }

    Button(
      onClick = onLogin,
      enabled = !uiState.isLoggingIn,
    ) {
      Text(
        text = stringResource(id = if (uiState.isLoggingIn) R.string.signing_in else R.string.sign_in),
      )
    }

    Text(
      text = stringResource(id = R.string.hint_password),
      style = MaterialTheme.typography.bodyMedium,
      color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f),
    )
  }
}
