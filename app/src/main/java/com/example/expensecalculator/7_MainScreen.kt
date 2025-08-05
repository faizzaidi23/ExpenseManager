package com.example.expensecalculator

import android.graphics.drawable.Icon
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Paid
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.expensecalculator.Data.Detail

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(navController: NavController, viewModel: viewModel) {

    val detailsList by viewModel.details.collectAsState()
    var addDetailDialog by remember { mutableStateOf(false) }
    var editDialog by remember { mutableStateOf(false) }
    var currentAccount by remember { mutableStateOf<Detail?>(null) }
    var accountCopy = currentAccount

    if (addDetailDialog) {
        AddDetail(
            onDismissRequest = { addDetailDialog = false },
            onSave = { detail ->
                viewModel.addDetail(detail)
                addDetailDialog = false
            }
        )
    }

    if (editDialog && accountCopy != null) {
        EditAccount(
            account = accountCopy,
            onDismissRequest = { editDialog = false },
            onSave = { updatedAccount ->
                viewModel.updateDetail(updatedAccount)
                editDialog = false // Close dialog after saving
            }
        )
    }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        topBar = {
            TopAppBar(
                title = { Text("Expense Accounts", fontSize = 28.sp, fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Go back")
                    }
                },
                // CHANGE: Added specific colors for better theming and contrast
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    titleContentColor = MaterialTheme.colorScheme.onPrimary,
                    navigationIconContentColor = MaterialTheme.colorScheme.onPrimary
                )
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = {
                    addDetailDialog = true
                },
                // CHANGE: Styled the FAB with tertiary colors to make it stand out
                containerColor = MaterialTheme.colorScheme.tertiaryContainer,
                contentColor = MaterialTheme.colorScheme.onTertiaryContainer
            ) {
                Icon(imageVector = Icons.Default.Add, contentDescription = "Add new Expense Account")
            }
        },
        floatingActionButtonPosition = FabPosition.End
    ) { innerpadding ->
        LazyColumn(
            modifier = Modifier
                .padding(innerpadding)
                .fillMaxSize()
                .padding(horizontal = 8.dp) // Add horizontal padding for the list
        ) {
            items(detailsList) { detail ->
                AccountCard(
                    id = detail.id,
                    navController = navController,
                    detail = detail,
                    viewModel = viewModel,
                    onEditDialog = {
                        currentAccount = detail
                        editDialog = true
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
fun AccountCard(
    id: Int?,
    navController: NavController,
    detail: Detail,
    viewModel: viewModel,
    onEditDialog: () -> Unit,
    onDeleteDialog: () -> Unit
) {
    // We get the total amount for this specific account
    val totalAmount by viewModel.getTotalForAccount(detail.id!!).collectAsState(initial = 0.0)

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
            .clickable { navController.navigate("expense_screen/$id") },
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        // CHANGE: Using a subtle background color for the card
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // CHANGE: Added a prominent leading icon
            Box(
                modifier = Modifier
                    .size(50.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.primaryContainer),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.AccountBalanceWallet,
                    contentDescription = "Account Icon",
                    tint = MaterialTheme.colorScheme.onPrimaryContainer,
                    modifier = Modifier.size(30.dp)
                )
            }

            Spacer(modifier = Modifier.width(16.dp))

            // This column holds all the text and the action buttons
            Column(modifier = Modifier.weight(1f)) {
                Text(text = detail.name, fontSize = 20.sp, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(4.dp))

                if (!detail.description.isNullOrEmpty()) {
                    Text(
                        text = detail.description,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Normal,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                }

                // CHANGE: Row for Total Spent to include an icon
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Paid,
                        contentDescription = "Amount Icon",
                        modifier = Modifier.size(18.dp),
                        tint = MaterialTheme.colorScheme.tertiary
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "Total Spent: ₹${totalAmount ?: 0.0}",
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.tertiary
                    )
                }
            }

            // This column holds the action buttons, aligned to the right
            Column(horizontalAlignment = Alignment.End) {
                IconButton(
                    onClick = onEditDialog,
                ) {
                    // CHANGE: Colored the Edit icon
                    Icon(
                        Icons.Default.Edit,
                        contentDescription = "Edit Account info",
                        tint = MaterialTheme.colorScheme.secondary
                    )
                }

                IconButton(
                    onClick = onDeleteDialog
                ) {
                    // CHANGE: Colored the Delete icon to indicate a destructive action
                    Icon(
                        Icons.Default.Delete,
                        contentDescription = "Delete Account info",
                        tint = MaterialTheme.colorScheme.error
                    )
                }
            }
        }
    }
}