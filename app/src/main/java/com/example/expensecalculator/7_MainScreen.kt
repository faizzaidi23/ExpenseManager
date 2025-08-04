package com.example.expensecalculator

import android.graphics.drawable.Icon
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FabPosition
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.FloatingActionButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SegmentedButtonDefaults.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.sp
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.expensecalculator.Data.Detail

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(navController: NavController,viewModel: viewModel){

    val detailsList by viewModel.details.collectAsState()
    var addDetailDialog by remember{mutableStateOf(false)}
    var editDialog by remember{mutableStateOf(false)}
    var currentAccount by remember{mutableStateOf<Detail?>(null)}
    var accountCopy=currentAccount

    if(addDetailDialog){
        AddDetail(
            onDismissRequest = {addDetailDialog=false},
            onSave = {detail->
                viewModel.addDetail(detail)
                addDetailDialog=false
            }
        )
    }

    if(editDialog && accountCopy!=null){
        EditAccount(
            account = accountCopy,
            onDismissRequest = {editDialog=false},
            onSave = {updatedAccount->
                viewModel.updateDetail(updatedAccount)
            }

        )
    }

    Scaffold(
        modifier=Modifier.fillMaxSize(),
        topBar = {TopAppBar(title = {Text("Expense Calculator",fontSize=36.sp, fontWeight = FontWeight.ExtraBold)},
            navigationIcon = {
                IconButton(onClick = {navController.popBackStack()}){
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Go back")
                }
            },
            colors= TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.primary))},
        floatingActionButton={FloatingActionButton(
            onClick = {
                addDetailDialog=true
            }
        ){
            Icon(imageVector = Icons.Default.Add, contentDescription = "Add new Expense")
        }},
        floatingActionButtonPosition = FabPosition.End
    ){innerpadding->
        LazyColumn(
            modifier=Modifier.padding(innerpadding)
                .fillMaxSize()
        ){
            items(detailsList){
                detail->

                AccountCard(id=detail.id,navController = navController, detail=detail,
                    viewModel = viewModel,
                    onEditDialog = {
                        currentAccount=detail
                        editDialog=true
                    },
                    onDeleteDialog = {
                        viewModel.deleteDetail(detail)
                    }
                    )

            }

        }

    }



}


@Composable
fun AccountCard(id:Int?,navController: NavController,detail:Detail,viewModel: viewModel,onEditDialog:()->Unit,onDeleteDialog:()-> Unit){

    // We will get the total amount for this specific account

    // we use the detail.id!! because we know an existing card will have an id

    val totalAmount by viewModel.getTotalForAccount(detail.id!!).collectAsState(initial = 0.0)

    Card(
        modifier=Modifier.fillMaxWidth()
            .padding(vertical=8.dp)
            .clickable{navController.navigate("expense_screen/$id")},
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ){
        Column(
            modifier=Modifier.fillMaxWidth().padding(16.dp)
        ){

            Text(text = detail.name, fontSize = 20.sp, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(4.dp))
            // Only show description if it's not null or empty
            if (!detail.description.isNullOrEmpty()) {
                Text(text = detail.description, fontSize = 14.sp, fontWeight = FontWeight.Normal)
                Spacer(modifier = Modifier.height(8.dp))
            }
            // FIX: Display the dynamically calculated totalAmount
            Text(text = "Total Spent: ₹${totalAmount ?: 0.0}", fontWeight = FontWeight.SemiBold)

            Row(
                modifier=Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ){

                IconButton(
                    onClick = onEditDialog,
                ){
                    Icon(Icons.Default.Edit, contentDescription = "Edit Account info")
                }

                IconButton(
                    onClick = onDeleteDialog
                ){
                    Icon(Icons.Default.Delete, contentDescription = "Delete Account info")
                }

            }

        }
    }

}