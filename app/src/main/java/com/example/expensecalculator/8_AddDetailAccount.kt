package com.example.expensecalculator



import android.R.attr.text
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddCard
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.DriveFileRenameOutline
import androidx.compose.material.icons.filled.Save
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.BasicAlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
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
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.expensecalculator.Data.Detail

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddDetail(
    onDismissRequest: () -> Unit,
    onSave: (Detail) -> Unit
) {

    var newname by remember { mutableStateOf("") }
    var newdescription by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismissRequest,

        // CHANGE: Added a main icon to the dialog
        icon = {
            Icon(Icons.Default.AddCard, contentDescription = "Add Account Icon")
        },

        // CHANGE: Added a more descriptive title
        title = {
            Text("Add New Account")
        },

        text = {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                OutlinedTextField(
                    value = newname,
                    onValueChange = { newname = it },
                    modifier = Modifier.fillMaxWidth().padding(vertical = 5.dp),
                    label = { Text("Account Name*") }, // Added * to indicate required
                    placeholder = { Text("e.g., Groceries, Fuel") },
                    // CHANGE: Added a leading icon for better context
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Default.DriveFileRenameOutline,
                            contentDescription = "Name Icon"
                        )
                    },
                    singleLine = true
                )

                Spacer(Modifier.height(8.dp))

                OutlinedTextField(
                    value = newdescription,
                    onValueChange = { newdescription = it },
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text("Description (Optional)") },
                    placeholder = { Text("e.g., Monthly household food expenses") },
                    // CHANGE: Added a leading icon for better context
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Default.Description,
                            contentDescription = "Description Icon"
                        )
                    }
                )
            }
        },

        // CHANGE: Made this the primary, filled button for a clear call-to-action
        confirmButton = {
            Button(
                // CHANGE: Added validation. Button is disabled if name is blank.
                enabled = newname.isNotBlank(),
                onClick = {
                    val newDetail = Detail(
                        name = newname,
                        description = newdescription,
                        amount = 0.0
                    )
                    onSave(newDetail)
                }
            ) {
                // CHANGE: Added a save icon to the button
                Icon(Icons.Default.Save, contentDescription = "Save", modifier = Modifier.padding(end = 4.dp))
                Text("Save")
            }
        },

        // CHANGE: Added an explicit Cancel button for better UX
        dismissButton = {
            TextButton(
                onClick = onDismissRequest
            ) {
                Text("Cancel")
            }
        }
    )
}