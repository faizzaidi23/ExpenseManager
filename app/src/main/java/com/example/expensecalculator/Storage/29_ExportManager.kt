package com.example.expensecalculator.Storage

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
import com.example.expensecalculator.Data.Expense
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.apache.poi.xssf.usermodel.XSSFWorkbook
import java.text.SimpleDateFormat
import java.util.*

class ExportManager(private val context: Context) {

    private val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())

    @RequiresApi(Build.VERSION_CODES.O)
    suspend fun exportToCSV(expenses: List<Expense>): Uri? = withContext(Dispatchers.IO) {
        try {
            val fileName = "expenses_${System.currentTimeMillis()}.csv"
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
                    writer.write("Date,Description,Amount,Account ID\n")
                    expenses.forEach { expense ->
                        writer.write("${dateFormat.format(expense.date)},\"${expense.description}\",${expense.amount},${expense.accountId}\n")
                    }
                    writer.flush()
                }
            }
            uri
        } catch (e: Exception) {
            Log.e("ExportManager", "CSV Export failed", e)
            null
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    suspend fun exportToPDF(expenses: List<Expense>): Uri? = withContext(Dispatchers.IO) {
        try {
            val fileName = "expenses_${System.currentTimeMillis()}.pdf"
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
                    val paint = Paint()
                    paint.textSize = 12f

                    var yPos = 50f
                    canvas.drawText("Expense Report", 50f, yPos, paint)
                    yPos += 30f

                    expenses.forEach { expense ->
                        canvas.drawText(
                            "${dateFormat.format(expense.date)} - ${expense.description}: ₹${expense.amount}",
                            50f, yPos, paint
                        )
                        yPos += 20f
                        if (yPos > 800f) return@forEach // Prevent overflow (add pagination for production)
                    }

                    pdfDocument.finishPage(page)
                    pdfDocument.writeTo(outputStream)
                    pdfDocument.close()
                }
            }
            uri
        } catch (e: Exception) {
            Log.e("ExportManager", "PDF Export failed", e)
            null
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    suspend fun exportToExcel(expenses: List<Expense>): Uri? = withContext(Dispatchers.IO) {
        try {
            val fileName = "expenses_${System.currentTimeMillis()}.xlsx"
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
                    val sheet = workbook.createSheet("Expenses")

                    // Header row
                    val headerRow = sheet.createRow(0)
                    headerRow.createCell(0).setCellValue("Date")
                    headerRow.createCell(1).setCellValue("Description")
                    headerRow.createCell(2).setCellValue("Amount")
                    headerRow.createCell(3).setCellValue("Account ID")

                    // Data rows
                    expenses.forEachIndexed { index, expense ->
                        val row = sheet.createRow(index + 1)
                        row.createCell(0).setCellValue(dateFormat.format(expense.date))
                        row.createCell(1).setCellValue(expense.description)
                        row.createCell(2).setCellValue(expense.amount)
                        row.createCell(3).setCellValue(expense.accountId.toDouble())
                    }

                    workbook.write(outputStream)
                    workbook.close()
                }
            }
            uri
        } catch (e: Exception) {
            Log.e("ExportManager", "Excel Export failed", e)
            null
        }
    }
}
