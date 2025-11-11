package com.example.expensecalculator

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.EditNote
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.expensecalculator.Data.Account

@Composable
fun EditAccount(
    account: Account,
    onDismissRequest: () -> Unit,
    onSave: (Account) -> Unit
) {
    var updatedName by remember { mutableStateOf(account.name) }
    var updatedDescription by remember { mutableStateOf(account.description ?: "") }
    var showError by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismissRequest,
        containerColor = MaterialTheme.colorScheme.surface,
        shape = MaterialTheme.shapes.large,
        icon = {
            Icon(
                Icons.Default.EditNote,
                "Edit Account Icon",
                tint = MaterialTheme.colorScheme.primary
            )
        },
        title = {
            Text(
                "Edit Account",
                style = MaterialTheme.typography.titleMedium
            )
        },
        text = {
            Column {
                OutlinedTextField(
                    value = updatedName,
                    onValueChange = { updatedName = it; showError = false },
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text("Account Name*") },
                    isError = showError && updatedName.isBlank(),
                    shape = MaterialTheme.shapes.small,
                    singleLine = true
                )
                Spacer(Modifier.height(16.dp))
                OutlinedTextField(
                    value = updatedDescription,
                    onValueChange = { updatedDescription = it },
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text("Description (Optional)") },
                    shape = MaterialTheme.shapes.small,
                    singleLine = true
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (updatedName.isNotBlank()) {
                        onSave(account.copy(name = updatedName, description = updatedDescription))
                    } else {
                        showError = true
                    }
                },
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary
                )
            ) { Text("Save Changes") }
        },
        dismissButton = {
            TextButton(onClick = onDismissRequest) {
                Text("Cancel")
            }
        }
    )
}
