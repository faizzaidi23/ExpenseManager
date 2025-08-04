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
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.BasicAlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
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
    onDismissRequest:()-> Unit,
    onSave:(Detail)-> Unit
){

    var newname by remember{mutableStateOf("")}

    var newdescription by remember{mutableStateOf("")}

    AlertDialog(onDismissRequest = onDismissRequest,
        confirmButton = {
            // FIX: Added a TextButton to make it clickable
            TextButton(
                onClick = {
                    // FIX: Create the Detail object here.
                    // The id is null (Room will generate it).
                    // The amount is 0.0 because it's a new account.
                    val newDetail = Detail(
                        name = newname,
                        description = newdescription,
                        amount = 0.0 // A new account starts with 0 amount
                    )
                    // FIX: Pass the newly created object back through the onSave lambda
                    onSave(newDetail)
                }
            ) {
                Text("Save")
            }
        },
        title={Text("Add Account")},
        text={

            Column(
                //modifier=Modifier.fillMaxSize(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ){
                OutlinedTextField(
                    value=newname,
                    onValueChange = {newname=it},
                    modifier=Modifier.fillMaxWidth().padding(vertical = 5.dp),
                    colors= OutlinedTextFieldDefaults.colors(
                        unfocusedBorderColor = MaterialTheme.colorScheme.primary,
                        focusedBorderColor = MaterialTheme.colorScheme.secondary
                    ),
                    label = {Text("Enter the name")}
                )

                Spacer(Modifier.height(8.dp))

                OutlinedTextField(
                    value=newdescription,
                    onValueChange = {newdescription=it},
                    modifier=Modifier.fillMaxWidth(),
                    colors= OutlinedTextFieldDefaults.colors(
                        unfocusedBorderColor = MaterialTheme.colorScheme.primary,
                        focusedBorderColor = MaterialTheme.colorScheme.secondary
                    ),
                    label={Text("Enter the Description ")}
                )
            }

        }

    )
}