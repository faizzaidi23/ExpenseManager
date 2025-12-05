package com.example.expensecalculator.TripManager

import android.widget.Toast
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.PictureAsPdf
import androidx.compose.material.icons.filled.TableChart
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.example.expensecalculator.Storage.ExportFormat

@Composable
fun TripExportDialog(
    onDismiss: () -> Unit,
    onExport: (ExportFormat) -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Exports Trip Expenses") },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                ExportOption(
                    title = "CSV File",
                    description = "It is compatible with Excel, Google Sheets",
                    icon = Icons.Default.Description,
                    onClick = {
                        onExport(ExportFormat.CSV)
                        onDismiss()
                    }
                )

                Divider()

                ExportOption(
                    title = "PDF Document",
                    description = "Easy to share and print",
                    icon = Icons.Default.PictureAsPdf,
                    onClick = {
                        onExport(ExportFormat.PDF)
                        onDismiss()
                    }
                )

                Divider()

                ExportOption(
                    title = "Excel File",
                    description = "Advanced formatting with multiple sheets",
                    icon = Icons.Default.TableChart,
                    onClick = {
                        onExport(ExportFormat.EXCEL)
                        onDismiss()
                    }
                )
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

@Composable
private fun ExportOption(
    title: String,
    description: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(vertical = 9.dp),
        horizontalArrangement = Arrangement.spacedBy(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = title,
            modifier = Modifier.size(40.dp),
            tint = MaterialTheme.colorScheme.primary
        )

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium
            )
            Text(
                text = description,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
fun HandleExportResult(uri: android.net.Uri?) {
    val context = LocalContext.current

    if (uri != null) {
        Toast.makeText(
            context,
            "File exported successfully to the  Downloads folder",
            Toast.LENGTH_LONG
        ).show()
    } else {
        Toast.makeText(
            context,
            "Export failed. Please try again.",
            Toast.LENGTH_SHORT
        ).show()
    }
}

