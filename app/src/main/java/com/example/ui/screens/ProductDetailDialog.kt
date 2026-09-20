package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.LocalGroceryStore
import androidx.compose.material.icons.filled.MoveDown
import androidx.compose.material.icons.filled.QrCode
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.Product
import com.example.data.model.Store
import com.example.data.model.StoreStock
import com.example.ui.components.BadgeType
import com.example.ui.components.StatusBadge
import com.example.ui.theme.RetailPrimary
import com.example.ui.theme.RetailSecondary
import com.example.ui.theme.StatusAmber
import com.example.ui.theme.StatusGreen
import com.example.ui.theme.StatusRed
import java.util.Locale
import java.util.UUID

@Composable
fun ProductDetailDialog(
  product: Product,
  stores: List<Store>,
  stocks: List<StoreStock>,
  canManageInventory: Boolean,
  onDismiss: () -> Unit,
  onAdjustStockClick: (Product) -> Unit,
  onTransferClick: (Product) -> Unit,
  onEditProductClick: (Product) -> Unit
) {
  val productStocks = stocks.filter { it.productId == product.id }
  val totalStock = productStocks.sumOf { it.quantity }
  val totalExpired = productStocks.sumOf { it.expiredQuantity }
  val margin = if (product.sellingPrice > 0) {
    ((product.sellingPrice - product.purchasePrice) / product.sellingPrice) * 100
  } else 0.0

  AlertDialog(
    onDismissRequest = onDismiss,
    title = {
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        Column(modifier = Modifier.weight(1f)) {
          Text(
            text = product.name,
            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
            maxLines = 2
          )
          Text(
            text = "SKU: ${product.sku} • ${product.category}",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
          )
        }
        IconButton(onClick = onDismiss) {
          Icon(imageVector = Icons.Default.Close, contentDescription = "Close")
        }
      }
    },
    text = {
      Column(
        modifier = Modifier
          .fillMaxWidth()
          .verticalScroll(rememberScrollState())
      ) {
        // Price & Margin Card
        Card(
          shape = RoundedCornerShape(10.dp),
          colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
          modifier = Modifier.fillMaxWidth()
        ) {
          Row(
            modifier = Modifier
              .fillMaxWidth()
              .padding(12.dp),
            horizontalArrangement = Arrangement.SpaceBetween
          ) {
            Column {
              Text(
                text = "Retail Price",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
              )
              Text(
                text = "€${String.format(Locale.US, "%.2f", product.sellingPrice)}",
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                color = RetailPrimary
              )
            }
            Column {
              Text(
                text = "Cost Price",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
              )
              Text(
                text = "€${String.format(Locale.US, "%.2f", product.purchasePrice)}",
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
              )
            }
            Column {
              Text(
                text = "Margin",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
              )
              Text(
                text = "${String.format(Locale.US, "%.1f", margin)}%",
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                color = StatusGreen
              )
            }
          }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Product Details
        DetailRow("Barcode", product.barcode)
        DetailRow("Supplier", product.supplier)
        DetailRow("Brand", product.brand.ifBlank { "N/A" })
        DetailRow("Unit", product.unitType)
        DetailRow("Min Stock Level", "${product.minStockLevel} units")
        DetailRow("Max Stock Level", "${product.maxStockLevel} units")

        if (product.isPerishable) {
          Spacer(modifier = Modifier.height(6.dp))
          DetailRow("Perishable", "Yes")
          DetailRow("Batch Number", product.batchNumber.ifBlank { "N/A" })
          DetailRow("Expiration Date", product.expirationDate.ifBlank { "Not set" })
          if (totalExpired > 0) {
            DetailRow("Segregated Expired", "$totalExpired units (Awaiting Disposal)")
          }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Stock Across Stores
        Text(
          text = "Stock by Store",
          style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold)
        )
        Spacer(modifier = Modifier.height(6.dp))

        stores.forEach { store ->
          val st = productStocks.firstOrNull { it.storeId == store.id }
          val qty = st?.quantity ?: 0
          Row(
            modifier = Modifier
              .fillMaxWidth()
              .padding(vertical = 4.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
          ) {
            Column {
              Text(
                text = store.name,
                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Medium)
              )
              if (st?.locationInStore?.isNotBlank() == true) {
                Text(
                  text = "📍 ${st.locationInStore}",
                  style = MaterialTheme.typography.bodySmall,
                  color = MaterialTheme.colorScheme.onSurfaceVariant
                )
              }
            }
            Text(
              text = "$qty ${product.unitType}s",
              style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
              color = if (qty <= product.minStockLevel) StatusAmber else MaterialTheme.colorScheme.onSurface
            )
          }
          HorizontalDivider(modifier = Modifier.padding(vertical = 2.dp), color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Action Buttons
        if (canManageInventory) {
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
          ) {
            Button(
              onClick = {
                onDismiss()
                onAdjustStockClick(product)
              },
              modifier = Modifier
                .weight(1f)
                .testTag("adjust_stock_button"),
              colors = ButtonDefaults.buttonColors(containerColor = RetailPrimary)
            ) {
              Icon(imageVector = Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
              Spacer(modifier = Modifier.width(4.dp))
              Text("Adjust Stock")
            }

            OutlinedButton(
              onClick = {
                onDismiss()
                onTransferClick(product)
              },
              modifier = Modifier
                .weight(1f)
                .testTag("transfer_stock_button")
            ) {
              Icon(imageVector = Icons.Default.MoveDown, contentDescription = null, modifier = Modifier.size(16.dp))
              Spacer(modifier = Modifier.width(4.dp))
              Text("Transfer")
            }
          }

          Spacer(modifier = Modifier.height(6.dp))

          OutlinedButton(
            onClick = {
              onDismiss()
              onEditProductClick(product)
            },
            modifier = Modifier
              .fillMaxWidth()
              .testTag("edit_product_button")
          ) {
            Icon(imageVector = Icons.Default.Edit, contentDescription = null, modifier = Modifier.size(16.dp))
            Spacer(modifier = Modifier.width(4.dp))
            Text("Edit Product Details")
          }
        }
      }
    },
    confirmButton = {}
  )
}

@Composable
fun AddEditProductDialog(
  productToEdit: Product? = null,
  initialBarcode: String = "",
  onDismiss: () -> Unit,
  onSave: (Product) -> Unit
) {
  var name by remember { mutableStateOf(productToEdit?.name ?: "") }
  var sku by remember { mutableStateOf(productToEdit?.sku ?: "SKU-${(1000..9999).random()}") }
  var barcode by remember { mutableStateOf(productToEdit?.barcode ?: initialBarcode.ifBlank { "890${(1000000000..9999999999L).random()}" }) }
  var category by remember { mutableStateOf(productToEdit?.category ?: "Dairy & Eggs") }
  var supplier by remember { mutableStateOf(productToEdit?.supplier ?: "Local Supplier") }
  var purchasePriceStr by remember { mutableStateOf(productToEdit?.purchasePrice?.toString() ?: "1.50") }
  var sellingPriceStr by remember { mutableStateOf(productToEdit?.sellingPrice?.toString() ?: "2.99") }
  var minStockStr by remember { mutableStateOf(productToEdit?.minStockLevel?.toString() ?: "10") }
  var maxStockStr by remember { mutableStateOf(productToEdit?.maxStockLevel?.toString() ?: "100") }
  var unitType by remember { mutableStateOf(productToEdit?.unitType ?: "Piece") }
  var isPerishable by remember { mutableStateOf(productToEdit?.isPerishable ?: false) }
  var batchNumber by remember { mutableStateOf(productToEdit?.batchNumber ?: "B-2026-01") }
  var expirationDate by remember { mutableStateOf(productToEdit?.expirationDate ?: "2026-10-15") }

  AlertDialog(
    onDismissRequest = onDismiss,
    title = {
      Text(if (productToEdit == null) "Add New Product" else "Edit Product")
    },
    text = {
      Column(
        modifier = Modifier
          .fillMaxWidth()
          .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(8.dp)
      ) {
        OutlinedTextField(
          value = name,
          onValueChange = { name = it },
          label = { Text("Product Name *") },
          modifier = Modifier.fillMaxWidth()
        )
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
          OutlinedTextField(
            value = sku,
            onValueChange = { sku = it },
            label = { Text("SKU *") },
            modifier = Modifier.weight(1f)
          )
          OutlinedTextField(
            value = barcode,
            onValueChange = { barcode = it },
            label = { Text("Barcode *") },
            modifier = Modifier.weight(1f)
          )
        }
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
          OutlinedTextField(
            value = category,
            onValueChange = { category = it },
            label = { Text("Category") },
            modifier = Modifier.weight(1f)
          )
          OutlinedTextField(
            value = unitType,
            onValueChange = { unitType = it },
            label = { Text("Unit (e.g. Piece, Liter)") },
            modifier = Modifier.weight(1f)
          )
        }
        OutlinedTextField(
          value = supplier,
          onValueChange = { supplier = it },
          label = { Text("Supplier") },
          modifier = Modifier.fillMaxWidth()
        )
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
          OutlinedTextField(
            value = purchasePriceStr,
            onValueChange = { purchasePriceStr = it },
            label = { Text("Cost Price (€)") },
            modifier = Modifier.weight(1f)
          )
          OutlinedTextField(
            value = sellingPriceStr,
            onValueChange = { sellingPriceStr = it },
            label = { Text("Selling Price (€)") },
            modifier = Modifier.weight(1f)
          )
        }
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
          OutlinedTextField(
            value = minStockStr,
            onValueChange = { minStockStr = it },
            label = { Text("Min Stock") },
            modifier = Modifier.weight(1f)
          )
          OutlinedTextField(
            value = maxStockStr,
            onValueChange = { maxStockStr = it },
            label = { Text("Max Stock") },
            modifier = Modifier.weight(1f)
          )
        }

        Row(
          modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
          verticalAlignment = Alignment.CenterVertically,
          horizontalArrangement = Arrangement.SpaceBetween
        ) {
          Text("Perishable Product (Tracks Expiry)")
          Switch(checked = isPerishable, onCheckedChange = { isPerishable = it })
        }

        if (isPerishable) {
          OutlinedTextField(
            value = batchNumber,
            onValueChange = { batchNumber = it },
            label = { Text("Batch Number") },
            modifier = Modifier.fillMaxWidth()
          )
          OutlinedTextField(
            value = expirationDate,
            onValueChange = { expirationDate = it },
            label = { Text("Expiration Date (YYYY-MM-DD)") },
            modifier = Modifier.fillMaxWidth()
          )
        }
      }
    },
    confirmButton = {
      Button(
        onClick = {
          if (name.isNotBlank()) {
            val cost = purchasePriceStr.toDoubleOrNull() ?: 1.0
            val price = sellingPriceStr.toDoubleOrNull() ?: 2.0
            val minStock = minStockStr.toIntOrNull() ?: 10
            val maxStock = maxStockStr.toIntOrNull() ?: 100

            val product = (productToEdit ?: Product(
              id = UUID.randomUUID().toString(),
              name = name,
              sku = sku,
              barcode = barcode
            )).copy(
              name = name,
              sku = sku,
              barcode = barcode,
              category = category,
              supplier = supplier,
              purchasePrice = cost,
              sellingPrice = price,
              minStockLevel = minStock,
              maxStockLevel = maxStock,
              unitType = unitType,
              isPerishable = isPerishable,
              batchNumber = if (isPerishable) batchNumber else "",
              expirationDate = if (isPerishable) expirationDate else ""
            )
            onSave(product)
          }
        },
        enabled = name.isNotBlank()
      ) {
        Text("Save")
      }
    },
    dismissButton = {
      TextButton(onClick = onDismiss) {
        Text("Cancel")
      }
    }
  )
}

@Composable
fun DetailRow(label: String, value: String) {
  Row(
    modifier = Modifier
      .fillMaxWidth()
      .padding(vertical = 3.dp),
    horizontalArrangement = Arrangement.SpaceBetween
  ) {
    Text(
      text = label,
      style = MaterialTheme.typography.bodySmall,
      color = MaterialTheme.colorScheme.onSurfaceVariant
    )
    Text(
      text = value,
      style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Medium),
      color = MaterialTheme.colorScheme.onSurface
    )
  }
}
