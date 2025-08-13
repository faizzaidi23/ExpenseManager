package com.example.expensecalculator

import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Paid
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.expensecalculator.Data.Detail
import com.example.expensecalculator.TripManager.AccentBlue
import com.example.expensecalculator.TripManager.DarkPurple
import com.example.expensecalculator.TripManager.ErrorColor
import com.example.expensecalculator.TripManager.MidPurple
import com.example.expensecalculator.TripManager.OffWhite
import com.example.expensecalculator.TripManager.TextPrimary


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(navController: NavController, viewModel: viewModel) {

    val detailsList by viewModel.details.collectAsState()
    var addDetailDialog by remember { mutableStateOf(false) }
    var editDialog by remember { mutableStateOf(false) }
    var currentAccount by remember { mutableStateOf<Detail?>(null) }
    val accountCopy = currentAccount

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
        modifier = Modifier.fillMaxSize().background(OffWhite),
        topBar = {
            TopAppBar(
                title = { Text("Expense Accounts", fontSize = 22.sp, fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Go back", tint = TextPrimary)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.Transparent,
                    titleContentColor = TextPrimary
                ),
                modifier = Modifier.background(
                    Brush.horizontalGradient(
                        colors = listOf(DarkPurple, MidPurple)
                    )
                )
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = {
                    addDetailDialog = true
                },
                containerColor = AccentBlue,
                contentColor = TextPrimary,
                shape = RoundedCornerShape(16.dp)
            ) {
                Icon(imageVector = Icons.Default.Add, contentDescription = "Add new Expense Account")
            }
        },
        floatingActionButtonPosition = FabPosition.End
    ) { innerpadding ->
        if (detailsList.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerpadding),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        imageVector = Icons.Default.AccountBalanceWallet,
                        contentDescription = "No Accounts",
                        tint = MidPurple.copy(alpha = 0.7f),
                        modifier = Modifier.size(64.dp)
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        "No Accounts Yet",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold,
                        color = DarkPurple
                    )
                    Text(
                        "Tap the '+' button to add your first account.",
                        style = MaterialTheme.typography.bodyLarge,
                        color = Color.Gray
                    )
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .padding(innerpadding)
                    .fillMaxSize(),
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
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
    val totalAmount by viewModel.getTotalForAccount(detail.id!!).collectAsState(initial = 0.0)
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.98f else 1f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessLow
        ), label = ""
    )

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .scale(scale)
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                onClick = { navController.navigate("expense_screen/$id") }
            ),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp, pressedElevation = 2.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(56.dp)
                    .clip(CircleShape)
                    .background(MidPurple.copy(alpha = 0.1f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.AccountBalanceWallet,
                    contentDescription = "Account Icon",
                    tint = MidPurple,
                    modifier = Modifier.size(30.dp)
                )
            }

            Spacer(modifier = Modifier.width(16.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(text = detail.name, fontSize = 20.sp, fontWeight = FontWeight.Bold, color = DarkPurple)
                Spacer(modifier = Modifier.height(4.dp))

                if (!detail.description.isNullOrEmpty()) {
                    Text(
                        text = detail.description,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Normal,
                        color = Color.Gray
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Paid,
                        contentDescription = "Amount Icon",
                        modifier = Modifier.size(18.dp),
                        tint = AccentBlue
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "Total Spent: ₹${String.format("%.2f", totalAmount)}",
                        fontWeight = FontWeight.SemiBold,
                        color = AccentBlue
                    )
                }
            }

            Column(horizontalAlignment = Alignment.End) {
                IconButton(onClick = onEditDialog) {
                    Icon(
                        Icons.Default.Edit,
                        contentDescription = "Edit Account info",
                        tint = MidPurple
                    )
                }

                IconButton(onClick = onDeleteDialog) {
                    Icon(
                        Icons.Default.Delete,
                        contentDescription = "Delete Account info",
                        tint = ErrorColor
                    )
                }
            }
        }
    }
}
