package com.example.ui.screens

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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.Receipt
import androidx.compose.material.icons.filled.RestartAlt
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
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
import com.example.data.model.InvoiceItem
import com.example.ui.components.BadgeType
import com.example.ui.components.StatusBadge
import com.example.ui.theme.RetailPrimary
import com.example.ui.theme.RetailSecondary
import com.example.ui.theme.StatusAmber
import com.example.ui.theme.StatusGreen
import com.example.ui.theme.StatusRed
import com.example.ui.viewmodel.RetailViewModel
import java.util.Locale

@Composable
fun InvoiceOcrScreen(
  reviewState: RetailViewModel.InvoiceReviewState?,
  onStartOcr: (Int) -> Unit,
  onUpdateItem: (InvoiceItem) -> Unit,
  onRemoveItem: (String) -> Unit,
  onAddItem: (String, Int, Double, String) -> Unit,
  onApproveInvoice: () -> Unit,
  onClearInvoice: () -> Unit
) {
  var itemToEdit by remember { mutableStateOf<InvoiceItem?>(null) }
  var showAddItemDialog by remember { mutableStateOf(false) }

  Column(
    modifier = Modifier
      .fillMaxSize()
      .padding(16.dp)
  ) {
    if (reviewState == null) {
      // Step 1: Upload / Scan Invoice Initial View
      Column(
        modifier = Modifier
          .fillMaxSize()
          .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally
      ) {
        Spacer(modifier = Modifier.height(16.dp))

        Box(
          modifier = Modifier
            .size(72.dp)
            .clip(CircleShape)
            .background(RetailPrimary.copy(alpha = 0.15f)),
          contentAlignment = Alignment.Center
        ) {
          Icon(
            imageVector = Icons.Default.Receipt,
            contentDescription = null,
            tint = RetailPrimary,
            modifier = Modifier.size(36.dp)
          )
        }

        Spacer(modifier = Modifier.height(16.dp))

        Text(
          text = "Purchase Invoice OCR Intake",
          style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)
        )
        Spacer(modifier = Modifier.height(6.dp))
        Text(
          text = "Scan or upload supplier invoices to automatically extract line items, prices, quantities, and update stock.",
          style = MaterialTheme.typography.bodyMedium,
          color = MaterialTheme.colorScheme.onSurfaceVariant,
          modifier = Modifier.padding(horizontal = 16.dp),
          textAlign = androidx.compose.ui.text.style.TextAlign.Center
        )

        Spacer(modifier = Modifier.height(24.dp))

        // Preset Templates for Fast Demo
        Text(
          text = "Select Supplier Invoice to Process:",
          style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
          modifier = Modifier.align(Alignment.Start)
        )
        Spacer(modifier = Modifier.height(10.dp))

        SupplierInvoiceCard(
          title = "Farm Fresh Logistics",
          invoiceNumber = "INV-89240",
          itemCount = 3,
          description = "Dairy shipment: Organic Milk, Barista Oat Milk, Greek Yogurt",
          onClick = { onStartOcr(0) },
          tag = "ocr_preset_farm_fresh"
        )
        Spacer(modifier = Modifier.height(10.dp))

        SupplierInvoiceCard(
          title = "Equator Imports & Roasters",
          invoiceNumber = "INV-77301",
          itemCount = 2,
          description = "Beverages: Espresso Roast Beans 1kg, Sparkling Mineral Water",
          onClick = { onStartOcr(1) },
          tag = "ocr_preset_equator"
        )
        Spacer(modifier = Modifier.height(10.dp))

        SupplierInvoiceCard(
          title = "Fresh Horizon & Local Bakeries",
          invoiceNumber = "INV-55109",
          itemCount = 2,
          description = "Produce & Bakery: Hass Avocados 4pk, Artisan Sourdough",
          onClick = { onStartOcr(2) },
          tag = "ocr_preset_bakeries"
        )
      }
    } else if (reviewState.isProcessing) {
      // Step 2: Processing OCR Animation
      Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
      ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
          CircularProgressIndicator(color = RetailPrimary, modifier = Modifier.size(54.dp))
          Spacer(modifier = Modifier.height(20.dp))
          Text(
            text = "Analyzing Invoice Document...",
            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
          )
          Spacer(modifier = Modifier.height(6.dp))
          Text(
            text = "Extracting line items, quantities, prices & expiry dates via OCR",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
          )
        }
      }
    } else if (reviewState.isApproved) {
      // Step 3: Success Banner
      Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
      ) {
        Column(
          horizontalAlignment = Alignment.CenterHorizontally,
          modifier = Modifier.padding(24.dp)
        ) {
          Icon(
            imageVector = Icons.Default.CheckCircle,
            contentDescription = null,
            tint = StatusGreen,
            modifier = Modifier.size(64.dp)
          )
          Spacer(modifier = Modifier.height(16.dp))
          Text(
            text = "Invoice Approved & Stock Updated!",
            style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)
          )
          Spacer(modifier = Modifier.height(8.dp))
          Text(
            text = "All ${reviewState.items.size} line items have been added to inventory at ${reviewState.storeName}, and stock transactions have been recorded.",
            textAlign = androidx.compose.ui.text.style.TextAlign.Center,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
          )
          Spacer(modifier = Modifier.height(24.dp))
          Button(
            onClick = onClearInvoice,
            colors = ButtonDefaults.buttonColors(containerColor = RetailPrimary),
            modifier = Modifier.testTag("ocr_scan_another_button")
          ) {
            Text("Process Another Invoice")
          }
        }
      }
    } else {
      // Step 4: Invoice Review & Verification Screen (Requirements 2, 8, 9)
      val totalCost = reviewState.items.sumOf { it.totalPrice }

      LazyColumn(
        verticalArrangement = Arrangement.spacedBy(12.dp),
        modifier = Modifier.fillMaxSize()
      ) {
        item {
          // Invoice Header Info Card
          Card(
            shape = RoundedCornerShape(14.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
            modifier = Modifier.fillMaxWidth()
          ) {
            Column(modifier = Modifier.padding(16.dp)) {
              Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
              ) {
                StatusBadge(text = "OCR EXTRACTED • VERIFY ITEMS", type = BadgeType.INFO)
                Text(
                  text = "Reset",
                  style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                  color = MaterialTheme.colorScheme.onPrimaryContainer,
                  modifier = Modifier.clickable { onClearInvoice() }
                )
              }
              Spacer(modifier = Modifier.height(10.dp))
              Text(
                text = reviewState.supplier,
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.onPrimaryContainer
              )
              Text(
                text = "Invoice #${reviewState.invoiceNumber} • Date: ${reviewState.invoiceDate}",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
              )
              Spacer(modifier = Modifier.height(4.dp))
              Text(
                text = "Receiving Store: ${reviewState.storeName}",
                style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Medium),
                color = MaterialTheme.colorScheme.onPrimaryContainer
              )
            }
          }
        }

        item {
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
          ) {
            Text(
              text = "Extracted Line Items (${reviewState.items.size})",
              style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold)
            )
            TextButton(
              onClick = { showAddItemDialog = true },
              modifier = Modifier.testTag("ocr_add_item_button")
            ) {
              Icon(imageVector = Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
              Spacer(modifier = Modifier.width(4.dp))
              Text("Add Item")
            }
          }
        }

        // List of items
        items(reviewState.items) { item ->
          InvoiceItemRow(
            item = item,
            onEdit = { itemToEdit = item },
            onDelete = { onRemoveItem(item.id) }
          )
        }

        // Summary and Commit button
        item {
          Spacer(modifier = Modifier.height(8.dp))
          Card(
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
            modifier = Modifier.fillMaxWidth()
          ) {
            Row(
              modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
              horizontalArrangement = Arrangement.SpaceBetween,
              verticalAlignment = Alignment.CenterVertically
            ) {
              Text(
                text = "Total Invoice Value:",
                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold)
              )
              Text(
                text = "€${String.format(Locale.US, "%.2f", totalCost)}",
                style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                color = RetailPrimary
              )
            }
          }

          Spacer(modifier = Modifier.height(14.dp))

          Button(
            onClick = onApproveInvoice,
            colors = ButtonDefaults.buttonColors(containerColor = StatusGreen),
            modifier = Modifier
              .fillMaxWidth()
              .height(52.dp)
              .testTag("approve_invoice_button")
          ) {
            Icon(imageVector = Icons.Default.Check, contentDescription = null)
            Spacer(modifier = Modifier.width(8.dp))
            Text("Approve & Update Inventory", fontWeight = FontWeight.Bold, fontSize = 16.sp)
          }

          Spacer(modifier = Modifier.height(40.dp))
        }
      }
    }
  }

  // Edit Item Dialog
  if (itemToEdit != null) {
    val current = itemToEdit!!
    var qtyStr by remember { mutableStateOf(current.quantity.toString()) }
    var priceStr by remember { mutableStateOf(current.unitPrice.toString()) }
    var expiryStr by remember { mutableStateOf(current.expirationDate) }

    AlertDialog(
      onDismissRequest = { itemToEdit = null },
      title = { Text("Edit Extracted Item") },
      text = {
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
          Text(
            text = current.productName,
            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold)
          )
          OutlinedTextField(
            value = qtyStr,
            onValueChange = { qtyStr = it },
            label = { Text("Quantity") },
            modifier = Modifier.fillMaxWidth()
          )
          OutlinedTextField(
            value = priceStr,
            onValueChange = { priceStr = it },
            label = { Text("Unit Price (€)") },
            modifier = Modifier.fillMaxWidth()
          )
          OutlinedTextField(
            value = expiryStr,
            onValueChange = { expiryStr = it },
            label = { Text("Expiry Date (YYYY-MM-DD)") },
            modifier = Modifier.fillMaxWidth()
          )
        }
      },
      confirmButton = {
        Button(
          onClick = {
            val q = qtyStr.toIntOrNull() ?: current.quantity
            val p = priceStr.toDoubleOrNull() ?: current.unitPrice
            onUpdateItem(
              current.copy(
                quantity = q,
                unitPrice = p,
                totalPrice = q * p,
                expirationDate = expiryStr,
                status = "Confirmed"
              )
            )
            itemToEdit = null
          }
        ) {
          Text("Done")
        }
      },
      dismissButton = {
        TextButton(onClick = { itemToEdit = null }) {
          Text("Cancel")
        }
      }
    )
  }

  // Add Item Dialog
  if (showAddItemDialog) {
    var name by remember { mutableStateOf("") }
    var qtyStr by remember { mutableStateOf("10") }
    var priceStr by remember { mutableStateOf("2.50") }
    var barcode by remember { mutableStateOf("890${(1000000000..9999999999L).random()}") }

    AlertDialog(
      onDismissRequest = { showAddItemDialog = false },
      title = { Text("Add Item to Invoice") },
      text = {
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
          OutlinedTextField(
            value = name,
            onValueChange = { name = it },
            label = { Text("Product Name") },
            modifier = Modifier.fillMaxWidth()
          )
          OutlinedTextField(
            value = qtyStr,
            onValueChange = { qtyStr = it },
            label = { Text("Quantity") },
            modifier = Modifier.fillMaxWidth()
          )
          OutlinedTextField(
            value = priceStr,
            onValueChange = { priceStr = it },
            label = { Text("Unit Price (€)") },
            modifier = Modifier.fillMaxWidth()
          )
          OutlinedTextField(
            value = barcode,
            onValueChange = { barcode = it },
            label = { Text("Barcode") },
            modifier = Modifier.fillMaxWidth()
          )
        }
      },
      confirmButton = {
        Button(
          onClick = {
            if (name.isNotBlank()) {
              val q = qtyStr.toIntOrNull() ?: 1
              val p = priceStr.toDoubleOrNull() ?: 1.0
              onAddItem(name, q, p, barcode)
              showAddItemDialog = false
            }
          },
          enabled = name.isNotBlank()
        ) {
          Text("Add")
        }
      },
      dismissButton = {
        TextButton(onClick = { showAddItemDialog = false }) {
          Text("Cancel")
        }
      }
    )
  }
}

@Composable
fun SupplierInvoiceCard(
  title: String,
  invoiceNumber: String,
  itemCount: Int,
  description: String,
  onClick: () -> Unit,
  tag: String
) {
  Card(
    shape = RoundedCornerShape(12.dp),
    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
    elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
    modifier = Modifier
      .fillMaxWidth()
      .clickable { onClick() }
      .testTag(tag)
  ) {
    Row(
      modifier = Modifier
        .fillMaxWidth()
        .padding(14.dp),
      verticalAlignment = Alignment.CenterVertically
    ) {
      Box(
        modifier = Modifier
          .size(44.dp)
          .clip(CircleShape)
          .background(RetailPrimary.copy(alpha = 0.1f)),
        contentAlignment = Alignment.Center
      ) {
        Icon(imageVector = Icons.Default.Description, contentDescription = null, tint = RetailPrimary)
      }
      Spacer(modifier = Modifier.width(12.dp))
      Column(modifier = Modifier.weight(1f)) {
        Text(
          text = title,
          style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold)
        )
        Text(
          text = "$invoiceNumber • $itemCount items",
          style = MaterialTheme.typography.bodySmall,
          color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(modifier = Modifier.height(2.dp))
        Text(
          text = description,
          style = MaterialTheme.typography.labelSmall,
          color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f)
        )
      }
      Button(
        onClick = onClick,
        colors = ButtonDefaults.buttonColors(containerColor = RetailPrimary),
        contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 12.dp, vertical = 6.dp)
      ) {
        Text("Scan OCR", fontSize = 12.sp)
      }
    }
  }
}

@Composable
fun InvoiceItemRow(
  item: InvoiceItem,
  onEdit: () -> Unit,
  onDelete: () -> Unit
) {
  Card(
    shape = RoundedCornerShape(10.dp),
    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
    elevation = CardDefaults.cardElevation(defaultElevation = 0.5.dp),
    modifier = Modifier
      .fillMaxWidth()
      .testTag("ocr_item_${item.id}")
  ) {
    Row(
      modifier = Modifier
        .fillMaxWidth()
        .padding(12.dp),
      verticalAlignment = Alignment.CenterVertically
    ) {
      Column(modifier = Modifier.weight(1f)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
          Text(
            text = item.productName,
            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold)
          )
          Spacer(modifier = Modifier.width(6.dp))
          StatusBadge(
            text = item.status,
            type = if (item.status == "Confirmed") BadgeType.SUCCESS else BadgeType.WARNING
          )
        }
        Spacer(modifier = Modifier.height(2.dp))
        Text(
          text = "Qty: ${item.quantity} × €${String.format(Locale.US, "%.2f", item.unitPrice)} = €${String.format(Locale.US, "%.2f", item.totalPrice)}",
          style = MaterialTheme.typography.bodySmall,
          color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        if (item.expirationDate.isNotBlank()) {
          Text(
            text = "📅 Expiry: ${item.expirationDate}",
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
          )
        }
      }

      IconButton(onClick = onEdit) {
        Icon(imageVector = Icons.Default.Edit, contentDescription = "Edit", tint = RetailPrimary, modifier = Modifier.size(18.dp))
      }
      IconButton(onClick = onDelete) {
        Icon(imageVector = Icons.Default.Delete, contentDescription = "Delete", tint = StatusRed, modifier = Modifier.size(18.dp))
      }
    }
  }
}
