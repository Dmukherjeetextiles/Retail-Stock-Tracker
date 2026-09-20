package com.example.ui.export

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.core.content.FileProvider
import com.example.data.model.EmployeeShift
import com.example.data.model.Product
import com.example.data.model.StockTransaction
import com.example.data.model.StoreStock
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object ExportHelper {

  fun exportInventoryCsv(
    context: Context,
    products: List<Product>,
    stocks: List<StoreStock>,
    storeName: String
  ) {
    val stockMap = stocks.associateBy { it.productId }
    val sb = StringBuilder()
    sb.append("Business: Retail Flow\n")
    sb.append("Store: $storeName\n")
    sb.append("Report: Inventory Stock Summary\n")
    sb.append("Date: ${SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.US).format(Date())}\n\n")
    sb.append("SKU,Barcode,Product Name,Category,Unit,Current Stock,Expired Stock,Min Stock,Purchase Price,Selling Price,Inventory Value\n")

    var totalUnits = 0
    var totalValue = 0.0

    products.forEach { prod ->
      val st = stockMap[prod.id]
      val qty = st?.quantity ?: 0
      val expQty = st?.expiredQuantity ?: 0
      val valItem = qty * prod.sellingPrice
      totalUnits += qty
      totalValue += valItem
      sb.append("\"${prod.sku}\",\"${prod.barcode}\",\"${prod.name}\",\"${prod.category}\",\"${prod.unitType}\",$qty,$expQty,${prod.minStockLevel},${String.format(Locale.US, "%.2f", prod.purchasePrice)},${String.format(Locale.US, "%.2f", prod.sellingPrice)},${String.format(Locale.US, "%.2f", valItem)}\n")
    }

    sb.append("\nTOTALS,,,,,${totalUnits},,,,${String.format(Locale.US, "%.2f", totalValue)}\n")

    shareExportFile(context, "Retail_Inventory_${System.currentTimeMillis()}.csv", sb.toString(), "text/csv")
  }

  fun exportStockMovementsCsv(
    context: Context,
    transactions: List<StockTransaction>,
    storeName: String
  ) {
    val sb = StringBuilder()
    val df = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.US)
    sb.append("Business: Retail Flow\n")
    sb.append("Store: $storeName\n")
    sb.append("Report: Stock Movement Audit Log\n")
    sb.append("Date: ${df.format(Date())}\n\n")
    sb.append("Date & Time,Reference,Product,Type,Quantity,Prev Stock,New Stock,User,Notes\n")

    transactions.forEach { tx ->
      val dateStr = df.format(Date(tx.timestamp))
      sb.append("\"$dateStr\",\"${tx.referenceNumber}\",\"${tx.productName}\",\"${tx.transactionType}\",${tx.quantity},${tx.previousStock},${tx.newStock},\"${tx.userName}\",\"${tx.notes.replace("\"", "'")}\"\n")
    }

    shareExportFile(context, "Stock_Movements_${System.currentTimeMillis()}.csv", sb.toString(), "text/csv")
  }

  fun exportShiftsCsv(
    context: Context,
    shifts: List<EmployeeShift>,
    storeName: String
  ) {
    val sb = StringBuilder()
    sb.append("Business: Retail Flow\n")
    sb.append("Store: $storeName\n")
    sb.append("Report: Work Roster & Shift Schedules\n")
    sb.append("Date: ${SimpleDateFormat("yyyy-MM-dd", Locale.US).format(Date())}\n\n")
    sb.append("Date,Employee,Store,Shift Type,Start Time,End Time,Break (min),Notes\n")

    shifts.forEach { s ->
      sb.append("\"${s.date}\",\"${s.employeeName}\",\"${s.storeName}\",\"${s.shiftType}\",\"${s.startTime}\",\"${s.endTime}\",${s.breakDurationMinutes},\"${s.notes.replace("\"", "'")}\"\n")
    }

    shareExportFile(context, "Work_Roster_${System.currentTimeMillis()}.csv", sb.toString(), "text/csv")
  }

  private fun shareExportFile(context: Context, fileName: String, content: String, mimeType: String) {
    try {
      val exportDir = File(context.cacheDir, "exports").apply { mkdirs() }
      val file = File(exportDir, fileName)
      FileOutputStream(file).use { it.write(content.toByteArray()) }

      val uri: Uri = try {
        FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", file)
      } catch (e: Exception) {
        Uri.fromFile(file)
      }

      val intent = Intent(Intent.ACTION_SEND).apply {
        type = mimeType
        putExtra(Intent.EXTRA_STREAM, uri)
        putExtra(Intent.EXTRA_SUBJECT, fileName.removeSuffix(".csv"))
        putExtra(Intent.EXTRA_TEXT, "Export generated from Retail Flow Management App.\n\n$content")
        addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
      }
      context.startActivity(Intent.createChooser(intent, "Export / Share File").apply {
        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
      })
    } catch (e: Exception) {
      // Fallback: share plain text
      val fallbackIntent = Intent(Intent.ACTION_SEND).apply {
        type = "text/plain"
        putExtra(Intent.EXTRA_SUBJECT, fileName)
        putExtra(Intent.EXTRA_TEXT, content)
        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
      }
      context.startActivity(Intent.createChooser(fallbackIntent, "Export Report").apply {
        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
      })
    }
  }
}
