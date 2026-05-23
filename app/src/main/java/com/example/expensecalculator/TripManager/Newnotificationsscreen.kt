package com.example.expensecalculator.TripManager

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.expensecalculator.firestore.FirestoreNotification
import com.example.expensecalculator.firestore.FirestoreTripViewModel
import com.example.expensecalculator.ui.theme.IconBackground
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NewNotificationsScreen(
    viewModel: FirestoreTripViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val pendingInvites by viewModel.pendingInvites.collectAsState()
    val notifications by viewModel.notifications.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()

    LaunchedEffect(Unit) {
        viewModel.markAllNotificationsRead()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Notifications", style = MaterialTheme.typography.titleMedium) },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.background)
            )
        },
        containerColor = MaterialTheme.colorScheme.background,
        modifier = modifier
    ) { padding ->

        if (pendingInvites.isEmpty() && notifications.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(Icons.Default.Notifications, null, modifier = Modifier.size(60.dp),
                        tint = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.4f))
                    Spacer(modifier = Modifier.height(16.dp))
                    Text("No Notifications", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("Trip activity will appear here", style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f))
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize().padding(padding).padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp),
                contentPadding = PaddingValues(vertical = 16.dp)
            ) {
                // Pending invites
                if (pendingInvites.isNotEmpty()) {
                    item {
                        Text("Pending Invites (${pendingInvites.size})",
                            style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                    }
                    items(pendingInvites) { invite ->
                        InviteCard(
                            invite = invite,
                            isLoading = isLoading,
                            onAccept = {
                                viewModel.acceptInvite(invite,
                                    onSuccess = { Toast.makeText(context, "Joined ${invite.tripTitle}!", Toast.LENGTH_SHORT).show() },
                                    onError = { msg -> Toast.makeText(context, msg, Toast.LENGTH_LONG).show() }
                                )
                            },
                            onDecline = { viewModel.declineInvite(invite) }
                        )
                    }
                }

                // Activity notifications
                if (notifications.isNotEmpty()) {
                    item {
                        Spacer(modifier = Modifier.height(4.dp))
                        Text("Activity", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                    }
                    items(notifications, key = { it.id }) { notif ->
                        ActivityNotificationCard(
                            notification = notif,
                            onDelete = { viewModel.deleteNotification(notif.id) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun ActivityNotificationCard(
    notification: FirestoreNotification,
    onDelete: () -> Unit
) {
    var showDeleteDialog by remember { mutableStateOf(false) }

    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text("Delete Notification") },
            text = { Text("Remove this notification?") },
            confirmButton = {
                Button(onClick = { onDelete(); showDeleteDialog = false },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                ) { Text("Delete") }
            },
            dismissButton = { TextButton(onClick = { showDeleteDialog = false }) { Text("Cancel") } }
        )
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.medium,
        colors = CardDefaults.cardColors(
            containerColor = if (!notification.read)
                MaterialTheme.colorScheme.primary.copy(alpha = 0.07f)
            else MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier.size(42.dp).clip(CircleShape).background(IconBackground),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = when (notification.type) {
                        "expense_added" -> Icons.Default.Add
                        "expense_deleted" -> Icons.Default.DeleteOutline
                        "expense_edited" -> Icons.Default.Edit
                        "category_added" -> Icons.Default.Add
                        "category_deleted" -> Icons.Default.DeleteOutline
                        "member_joined" -> Icons.Default.PersonAdd
                        "settlement_paid" -> Icons.Default.CheckCircle
                        "trip_edited" -> Icons.Default.Edit
                        else -> Icons.Default.Notifications
                    },
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(22.dp)
                )
            }
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(notification.tripTitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.SemiBold)
                Spacer(modifier = Modifier.height(2.dp))
                Text(notification.message,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onBackground)
                Spacer(modifier = Modifier.height(4.dp))
                Text(formatTimeAgo(notification.timestamp),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f))
            }
            IconButton(onClick = { showDeleteDialog = true }, modifier = Modifier.size(32.dp)) {
                Icon(Icons.Default.Close, "Delete notification",
                    tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f),
                    modifier = Modifier.size(18.dp))
            }
        }
    }
}

private fun formatTimeAgo(timestamp: Long): String {
    val diff = System.currentTimeMillis() - timestamp
    return when {
        diff < 60000 -> "Just now"
        diff < 3600000 -> "${diff / 60000}m ago"
        diff < 86400000 -> "${diff / 3600000}h ago"
        diff < 604800000 -> "${diff / 86400000}d ago"
        else -> SimpleDateFormat("MMM dd", Locale.getDefault()).format(Date(timestamp))
    }
}

@Composable
fun InviteCard(
    invite: com.example.expensecalculator.firestore.FirestoreInvite,
    isLoading: Boolean,
    onAccept: () -> Unit,
    onDecline: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.medium,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(modifier = Modifier.fillMaxWidth().padding(14.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier.size(42.dp).clip(CircleShape).background(IconBackground),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        Icons.Default.PersonAdd,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(22.dp)
                    )
                }
                Spacer(modifier = Modifier.width(12.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(invite.tripTitle,
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onBackground)
                    Spacer(modifier = Modifier.height(2.dp))
                    Text("Invited by ${invite.fromName}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f))
                }
            }
            Spacer(modifier = Modifier.height(12.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Button(
                    onClick = onDecline,
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.surface
                    ),
                    enabled = !isLoading
                ) {
                    Text("Decline", color = MaterialTheme.colorScheme.onSurface)
                }
                Button(
                    onClick = onAccept,
                    modifier = Modifier.weight(1f),
                    enabled = !isLoading
                ) {
                    Text("Accept")
                }
            }
        }
    }
}
