package com.example.expensecalculator.TripManager

import android.content.ContentValues
import android.content.Context
import android.graphics.Paint
import android.graphics.pdf.PdfDocument
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import android.util.Log
import androidx.annotation.RequiresApi
import com.example.expensecalculator.tripData.CompleteTripDetails
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.apache.poi.xssf.usermodel.XSSFWorkbook

class TripExportManager(private val context: Context) {

    @RequiresApi(Build.VERSION_CODES.Q)
    suspend fun exportTripToCSV(tripDetails: CompleteTripDetails): Uri? = withContext(Dispatchers.IO) {
        try {
            val fileName = "${tripDetails.trip.title}_expenses_${System.currentTimeMillis()}.csv"
            val contentValues = ContentValues().apply {
                put(MediaStore.MediaColumns.DISPLAY_NAME, fileName)
                put(MediaStore.MediaColumns.MIME_TYPE, "text/csv")
                put(MediaStore.MediaColumns.RELATIVE_PATH, Environment.DIRECTORY_DOWNLOADS)
            }

            val uri = context.contentResolver.insert(
                MediaStore.Downloads.EXTERNAL_CONTENT_URI,
                contentValues
            )

            uri?.let {
                context.contentResolver.openOutputStream(it)?.use { outputStream ->
                    val writer = outputStream.bufferedWriter()

                    // Write trip header
                    writer.write("Trip: ${tripDetails.trip.title}\n")
                    writer.write("Days: ${tripDetails.trip.days ?: "N/A"}\n")
                    writer.write("Total Expenditure: ₹${tripDetails.trip.expenditure ?: 0.0}\n")
                    writer.write("\n")

                    // Write participants
                    writer.write("Participants:\n")
                    tripDetails.participants.forEach { participant ->
                        writer.write("${participant.participantName},${participant.contactNumber ?: ""},${participant.email ?: ""}\n")
                    }
                    writer.write("\n")

                    // Write expenses header
                    writer.write("Date,Expense Name,Amount,Paid By,Split Among\n")

                    // Write expenses
                    tripDetails.expensesWithSplits.forEach { expenseWithSplits ->
                        val expense = expenseWithSplits.expense
                        val splitParticipants = expenseWithSplits.splits.joinToString(";") {
                            "${it.participantName}:₹${it.shareAmount}"
                        }
                        writer.write("${expense.date ?: "N/A"},${expense.expenseName},₹${expense.amount},${expense.paidBy},\"$splitParticipants\"\n")
                    }

                    writer.flush()
                }
            }
            uri
        } catch (e: Exception) {
            Log.e("TripExportManager", "CSV Export failed", e)
            null
        }
    }

    @RequiresApi(Build.VERSION_CODES.Q)
    suspend fun exportTripToPDF(tripDetails: CompleteTripDetails): Uri? = withContext(Dispatchers.IO) {
        try {
            val fileName = "${tripDetails.trip.title}_expenses_${System.currentTimeMillis()}.pdf"
            val contentValues = ContentValues().apply {
                put(MediaStore.MediaColumns.DISPLAY_NAME, fileName)
                put(MediaStore.MediaColumns.MIME_TYPE, "application/pdf")
                put(MediaStore.MediaColumns.RELATIVE_PATH, Environment.DIRECTORY_DOWNLOADS)
            }

            val uri = context.contentResolver.insert(
                MediaStore.Downloads.EXTERNAL_CONTENT_URI,
                contentValues
            )

            uri?.let {
                context.contentResolver.openOutputStream(it)?.use { outputStream ->
                    val pdfDocument = PdfDocument()
                    val pageInfo = PdfDocument.PageInfo.Builder(595, 842, 1).create() // A4 size
                    val page = pdfDocument.startPage(pageInfo)

                    val canvas = page.canvas
                    val paintTitle = Paint().apply {
                        textSize = 18f
                        isFakeBoldText = true
                    }
                    val paintHeader = Paint().apply {
                        textSize = 14f
                        isFakeBoldText = true
                    }
                    val paintNormal = Paint().apply {
                        textSize = 12f
                    }

                    var yPos = 50f

                    // Title
                    canvas.drawText("Trip Expense Report", 50f, yPos, paintTitle)
                    yPos += 30f

                    // Trip details
                    canvas.drawText("Trip: ${tripDetails.trip.title}", 50f, yPos, paintHeader)
                    yPos += 20f
                    canvas.drawText("Days: ${tripDetails.trip.days ?: "N/A"}", 50f, yPos, paintNormal)
                    yPos += 20f
                    canvas.drawText("Total Expenditure: ₹${tripDetails.trip.expenditure ?: 0.0}", 50f, yPos, paintNormal)
                    yPos += 30f

                    // Participants
                    canvas.drawText("Participants:", 50f, yPos, paintHeader)
                    yPos += 20f
                    tripDetails.participants.forEach { participant ->
                        canvas.drawText("• ${participant.participantName}", 70f, yPos, paintNormal)
                        yPos += 18f
                        if (yPos > 800f) return@forEach
                    }
                    yPos += 20f

                    // Expenses
                    canvas.drawText("Expenses:", 50f, yPos, paintHeader)
                    yPos += 20f

                    tripDetails.expensesWithSplits.forEach { expenseWithSplits ->
                        val expense = expenseWithSplits.expense
                        canvas.drawText("${expense.date ?: "N/A"} - ${expense.expenseName}", 50f, yPos, paintNormal)
                        yPos += 18f
                        canvas.drawText("Amount: ₹${expense.amount} (Paid by: ${expense.paidBy})", 70f, yPos, paintNormal)
                        yPos += 22f
                        if (yPos > 800f) return@forEach
                    }

                    pdfDocument.finishPage(page)
                    pdfDocument.writeTo(outputStream)
                    pdfDocument.close()
                }
            }
            uri
        } catch (e: Exception) {
            Log.e("TripExportManager", "PDF Export failed", e)
            null
        }
    }

    @RequiresApi(Build.VERSION_CODES.Q)
    suspend fun exportTripToExcel(tripDetails: CompleteTripDetails): Uri? = withContext(Dispatchers.IO) {
        try {
            val fileName = "${tripDetails.trip.title}_expenses_${System.currentTimeMillis()}.xlsx"
            val contentValues = ContentValues().apply {
                put(MediaStore.MediaColumns.DISPLAY_NAME, fileName)
                put(MediaStore.MediaColumns.MIME_TYPE, "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet")
                put(MediaStore.MediaColumns.RELATIVE_PATH, Environment.DIRECTORY_DOWNLOADS)
            }

            val uri = context.contentResolver.insert(
                MediaStore.Downloads.EXTERNAL_CONTENT_URI,
                contentValues
            )

            uri?.let {
                context.contentResolver.openOutputStream(it)?.use { outputStream ->
                    val workbook = XSSFWorkbook()

                    // Trip Info Sheet
                    val infoSheet = workbook.createSheet("Trip Info")
                    var rowNum = 0

                    infoSheet.createRow(rowNum++).apply {
                        createCell(0).setCellValue("Trip Name")
                        createCell(1).setCellValue(tripDetails.trip.title)
                    }
                    infoSheet.createRow(rowNum++).apply {
                        createCell(0).setCellValue("Days")
                        createCell(1).setCellValue(tripDetails.trip.days?.toDouble() ?: 0.0)
                    }
                    infoSheet.createRow(rowNum++).apply {
                        createCell(0).setCellValue("Total Expenditure")
                        createCell(1).setCellValue(tripDetails.trip.expenditure ?: 0.0)
                    }

                    // Participants Sheet
                    val participantsSheet = workbook.createSheet("Participants")
                    val headerRow = participantsSheet.createRow(0)
                    headerRow.createCell(0).setCellValue("Name")
                    headerRow.createCell(1).setCellValue("Contact")
                    headerRow.createCell(2).setCellValue("Email")

                    tripDetails.participants.forEachIndexed { index, participant ->
                        val row = participantsSheet.createRow(index + 1)
                        row.createCell(0).setCellValue(participant.participantName)
                        row.createCell(1).setCellValue(participant.contactNumber ?: "")
                        row.createCell(2).setCellValue(participant.email ?: "")
                    }

                    // Expenses Sheet
                    val expensesSheet = workbook.createSheet("Expenses")
                    val expenseHeaderRow = expensesSheet.createRow(0)
                    expenseHeaderRow.createCell(0).setCellValue("Date")
                    expenseHeaderRow.createCell(1).setCellValue("Expense Name")
                    expenseHeaderRow.createCell(2).setCellValue("Amount")
                    expenseHeaderRow.createCell(3).setCellValue("Paid By")
                    expenseHeaderRow.createCell(4).setCellValue("Split Type")

                    tripDetails.expensesWithSplits.forEachIndexed { index, expenseWithSplits ->
                        val expense = expenseWithSplits.expense
                        val row = expensesSheet.createRow(index + 1)
                        row.createCell(0).setCellValue(expense.date ?: "")
                        row.createCell(1).setCellValue(expense.expenseName)
                        row.createCell(2).setCellValue(expense.amount)
                        row.createCell(3).setCellValue(expense.paidBy)
                        row.createCell(4).setCellValue(expense.splitType)
                    }

                    // Expense Splits Sheet
                    val splitsSheet = workbook.createSheet("Expense Splits")
                    val splitHeaderRow = splitsSheet.createRow(0)
                    splitHeaderRow.createCell(0).setCellValue("Expense Name")
                    splitHeaderRow.createCell(1).setCellValue("Participant")
                    splitHeaderRow.createCell(2).setCellValue("Share Amount")

                    var splitRowNum = 1
                    tripDetails.expensesWithSplits.forEach { expenseWithSplits ->
                        val expenseName = expenseWithSplits.expense.expenseName
                        expenseWithSplits.splits.forEach { split ->
                            val row = splitsSheet.createRow(splitRowNum++)
                            row.createCell(0).setCellValue(expenseName)
                            row.createCell(1).setCellValue(split.participantName)
                            row.createCell(2).setCellValue(split.shareAmount)
                        }
                    }

                    workbook.write(outputStream)
                    workbook.close()
                }
            }
            uri
        } catch (e: Exception) {
            Log.e("TripExportManager", "Excel Export failed", e)
            null
        }
    }
}
