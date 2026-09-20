package com.example.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.data.model.Product
import com.example.data.model.Store
import com.example.data.model.StoreStock
import com.example.data.model.TransactionType
import com.example.ui.theme.RetailPrimary
import com.example.ui.theme.StatusAmber
import com.example.ui.theme.StatusGreen
import com.example.ui.theme.StatusRed

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StockAdjustmentDialog(
  product: Product,
  stores: List<Store>,
  stocks: List<StoreStock>,
  initialStoreId: String,
  onDismiss: () -> Unit,
  onConfirm: (storeId: String, productId: String, quantityChange: Int, type: TransactionType, notes: String) -> Unit
) {
  var selectedStoreId by remember {
    mutableStateOf(if (initialStoreId != "ALL") initialStoreId else stores.firstOrNull()?.id ?: "store-1")
  }
  var selectedType by remember { mutableStateOf(TransactionType.RECEIVED) }
  var quantityInput by remember { mutableStateOf("10") }
  var notesInput by remember { mutableStateOf("") }
  var storeDropdownExpanded by remember { mutableStateOf(false) }
  var typeDropdownExpanded by remember { mutableStateOf(false) }

  val currentStock = stocks.firstOrNull { it.storeId == selectedStoreId && it.productId == product.id }?.quantity ?: 0
  val qtyVal = quantityInput.toIntOrNull() ?: 0

  // Delta calculation based on type
  val quantityChange = when (selectedType) {
    TransactionType.RECEIVED, TransactionType.RETURNED -> qtyVal
    TransactionType.SOLD, TransactionType.DAMAGED, TransactionType.EXPIRED, TransactionType.DISPOSED -> -qtyVal
    TransactionType.ADJUSTMENT, TransactionType.STOCK_COUNT -> qtyVal - currentStock
    TransactionType.TRANSFERRED -> -qtyVal
  }

  val finalStock = (currentStock + quantityChange).coerceAtLeast(0)

  AlertDialog(
    onDismissRequest = onDismiss,
    title = {
      Column {
        Text(
          text = "Adjust Stock",
          style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
        )
        Text(
          text = product.name,
          style = MaterialTheme.typography.bodySmall,
          color = MaterialTheme.colorScheme.onSurfaceVariant
        )
      }
    },
    text = {
      Column(
        modifier = Modifier
          .fillMaxWidth()
          .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(10.dp)
      ) {
        // Store Selector
        ExposedDropdownMenuBox(
          expanded = storeDropdownExpanded,
          onExpandedChange = { storeDropdownExpanded = !storeDropdownExpanded }
        ) {
          val storeName = stores.firstOrNull { it.id == selectedStoreId }?.name ?: "Select Store"
          OutlinedTextField(
            value = storeName,
            onValueChange = {},
            readOnly = true,
            label = { Text("Store Location") },
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = storeDropdownExpanded) },
            modifier = Modifier
              .menuAnchor()
              .fillMaxWidth()
          )
          ExposedDropdownMenu(
            expanded = storeDropdownExpanded,
            onDismissRequest = { storeDropdownExpanded = false }
          ) {
            stores.forEach { store ->
              DropdownMenuItem(
                text = { Text(store.name) },
                onClick = {
                  selectedStoreId = store.id
                  storeDropdownExpanded = false
                }
              )
            }
          }
        }

        // Transaction Type Selector
        ExposedDropdownMenuBox(
          expanded = typeDropdownExpanded,
          onExpandedChange = { typeDropdownExpanded = !typeDropdownExpanded }
        ) {
          OutlinedTextField(
            value = selectedType.name.replace("_", " "),
            onValueChange = {},
            readOnly = true,
            label = { Text("Transaction Type") },
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = typeDropdownExpanded) },
            modifier = Modifier
              .menuAnchor()
              .fillMaxWidth()
          )
          ExposedDropdownMenu(
            expanded = typeDropdownExpanded,
            onDismissRequest = { typeDropdownExpanded = false }
          ) {
            TransactionType.values().forEach { t ->
              DropdownMenuItem(
                text = {
                  Text(
                    text = when (t) {
                      TransactionType.RECEIVED -> "📦 Stock Received (+)"
                      TransactionType.SOLD -> "🛒 Sold (-)"
                      TransactionType.ADJUSTMENT -> "⚖️ Stock Count / Adjustment"
                      TransactionType.DAMAGED -> "⚠️ Damaged Stock (-)"
                      TransactionType.EXPIRED -> "📅 Expired Batch (-)"
                      TransactionType.RETURNED -> "↩️ Customer Return (+)"
                      TransactionType.TRANSFERRED -> "🔄 Transferred Out (-)"
                      TransactionType.DISPOSED -> "🗑️ Disposed (-)"
                      TransactionType.STOCK_COUNT -> "📋 Physical Stock Count"
                    }
                  )
                },
                onClick = {
                  selectedType = t
                  typeDropdownExpanded = false
                }
              )
            }
          }
        }

        // Quantity Input
        OutlinedTextField(
          value = quantityInput,
          onValueChange = { quantityInput = it },
          label = {
            Text(
              if (selectedType in listOf(TransactionType.ADJUSTMENT, TransactionType.STOCK_COUNT))
                "Actual Physical Count"
              else "Quantity Units"
            )
          },
          singleLine = true,
          modifier = Modifier
            .fillMaxWidth()
            .testTag("adjustment_qty_input")
        )

        // Reason / Notes Input
        OutlinedTextField(
          value = notesInput,
          onValueChange = { notesInput = it },
          label = { Text("Reason / Reference Notes") },
          placeholder = { Text("e.g. PO-8821, Daily POS sales, Damaged box") },
          singleLine = true,
          modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(4.dp))

        // Stock Calculation Preview Card
        Card(
          shape = RoundedCornerShape(10.dp),
          colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
          modifier = Modifier.fillMaxWidth()
        ) {
          Column(modifier = Modifier.padding(12.dp)) {
            Row(
              modifier = Modifier.fillMaxWidth(),
              horizontalArrangement = Arrangement.SpaceBetween
            ) {
              Text(text = "Current Stock:", style = MaterialTheme.typography.bodySmall)
              Text(
                text = "$currentStock ${product.unitType}s",
                style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold)
              )
            }
            Row(
              modifier = Modifier.fillMaxWidth(),
              horizontalArrangement = Arrangement.SpaceBetween
            ) {
              Text(text = "Adjustment Change:", style = MaterialTheme.typography.bodySmall)
              Text(
                text = (if (quantityChange > 0) "+$quantityChange" else "$quantityChange") + " ${product.unitType}s",
                style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold),
                color = if (quantityChange >= 0) StatusGreen else StatusRed
              )
            }
            Spacer(modifier = Modifier.height(4.dp))
            Row(
              modifier = Modifier.fillMaxWidth(),
              horizontalArrangement = Arrangement.SpaceBetween
            ) {
              Text(
                text = "New Resulting Stock:",
                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold)
              )
              Text(
                text = "$finalStock ${product.unitType}s",
                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                color = RetailPrimary
              )
            }
          }
        }
      }
    },
    confirmButton = {
      Button(
        onClick = {
          if (qtyVal > 0 || selectedType in listOf(TransactionType.ADJUSTMENT, TransactionType.STOCK_COUNT)) {
            val notes = notesInput.ifBlank { "Manual ${selectedType.name.lowercase()} adjustment" }
            onConfirm(selectedStoreId, product.id, quantityChange, selectedType, notes)
          }
        },
        colors = ButtonDefaults.buttonColors(containerColor = RetailPrimary),
        modifier = Modifier.testTag("confirm_adjustment_button")
      ) {
        Text("Apply Adjustment")
      }
    },
    dismissButton = {
      TextButton(onClick = onDismiss) {
        Text("Cancel")
      }
    }
  )
}
