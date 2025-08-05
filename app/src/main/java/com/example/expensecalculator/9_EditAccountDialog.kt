package com.example.expensecalculator

import android.R.attr.description
import android.R.attr.name
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.DriveFileRenameOutline
import androidx.compose.material.icons.filled.EditNote
import androidx.compose.material.icons.filled.Save
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.expensecalculator.Data.Detail

@Composable
fun EditAccount(
    account: Detail,
    onDismissRequest: () -> Unit,
    onSave: (Detail) -> Unit
) {

    var updatedName by remember { mutableStateOf(account.name) }
    var updatedDescription by remember { mutableStateOf(account.description ?: "") }

    AlertDialog(
        onDismissRequest = onDismissRequest,

        // CHANGE: Added a main icon for editing context
        icon = {
            Icon(Icons.Default.EditNote, contentDescription = "Edit Account Icon")
        },

        title = {
            Text("Edit Account")
        },

        text = {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                OutlinedTextField(
                    value = updatedName,
                    onValueChange = { updatedName = it },
                    modifier = Modifier.fillMaxWidth(),
                    // CHANGE: Added label and leading icon
                    label = { Text("Account Name*") },
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Default.DriveFileRenameOutline,
                            contentDescription = "Name Icon"
                        )
                    },
                    singleLine = true
                )

                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = updatedDescription,
                    onValueChange = { updatedDescription = it },
                    modifier = Modifier.fillMaxWidth(),
                    // CHANGE: Added label and leading icon
                    label = { Text("Description (Optional)") },
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Default.Description,
                            contentDescription = "Description Icon"
                        )
                    }
                )
            }
        },

        // CHANGE: Upgraded to a filled Button for the primary action
        confirmButton = {
            Button(
                // CHANGE: Added validation to prevent saving with a blank name
                enabled = updatedName.isNotBlank(),
                onClick = {
                    val updatedAccount = account.copy(
                        name = updatedName,
                        description = updatedDescription
                    )
                    onSave(updatedAccount)
                }
            ) {
                Icon(Icons.Default.Save, contentDescription = "Save", modifier = Modifier.padding(end = 4.dp))
                Text("Save Changes")
            }
        },

        // CHANGE: Added an explicit Cancel button
        dismissButton = {
            TextButton(
                onClick = onDismissRequest
            ) {
                Text("Cancel")
            }
        }
    )
}