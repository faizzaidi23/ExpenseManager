package com.example.expensecalculator

import android.R.attr.description
import android.R.attr.name
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.AlertDialog
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
    onDismissRequest:()-> Unit,
    onSave:(Detail)-> Unit
){

    var updatedName by remember{mutableStateOf(account.name)}
    var updatedDescription by remember{mutableStateOf(account.description?:"")}

    AlertDialog(
        onDismissRequest = onDismissRequest,
        confirmButton = {
            TextButton(
                onClick = {
                    val updatedAccount=account.copy(
                        name=updatedName,
                        description = updatedDescription,
                        amount = account.amount
                    )
                    onSave(updatedAccount)
                }
            ) {
                Text("Save")
            }
        },
        title={Text("Edit Account")},
        text={
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ){
                OutlinedTextField(
                    value=updatedName,
                    onValueChange = {updatedName=it},
                    modifier=Modifier.fillMaxWidth(),
                    colors= OutlinedTextFieldDefaults.colors(
                        unfocusedBorderColor = MaterialTheme.colorScheme.primary,
                        focusedBorderColor = MaterialTheme.colorScheme.secondary
                    )

                )

                Spacer(modifier=Modifier.height(8.dp))


                OutlinedTextField(
                    value=updatedDescription,
                    onValueChange = {updatedDescription=it},
                    modifier=Modifier.fillMaxWidth(),
                    colors= OutlinedTextFieldDefaults.colors(
                        unfocusedBorderColor = MaterialTheme.colorScheme.primary,
                        focusedBorderColor = MaterialTheme.colorScheme.secondary
                    )

                )


            }


        }
    )

}
