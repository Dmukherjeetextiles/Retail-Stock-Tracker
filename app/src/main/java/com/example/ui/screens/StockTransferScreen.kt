package com.example.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.LocalShipping
import androidx.compose.material.icons.filled.MoveDown
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.Product
import com.example.data.model.StockTransfer
import com.example.data.model.Store
import com.example.data.model.StoreStock
import com.example.ui.components.BadgeType
import com.example.ui.components.StatusBadge
import com.example.ui.theme.RetailPrimary
import com.example.ui.theme.RetailSecondary
import com.example.ui.theme.StatusAmber
import com.example.ui.theme.StatusGreen
import com.example.ui.theme.StatusRed
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StockTransferScreen(
  stores: List<Store>,
  products: List<Product>,
  stocks: List<StoreStock>,
  transfers: List<StockTransfer>,
  initialProduct: Product? = null,
  canTransferStock: Boolean,
  onInitiateTransfer: (sourceStoreId: String, destStoreId: String, productId: String, quantity: Int, notes: String) -> Unit,
  onCompleteTransfer: (String) -> Unit
) {
  var selectedTab by remember { mutableIntStateOf(0) } // 0: Transfer History & In-Transit, 1: New Transfer

  var sourceStoreId by remember { mutableStateOf(stores.firstOrNull()?.id ?: "store-1") }
  var destStoreId by remember { mutableStateOf(stores.getOrNull(1)?.id ?: "store-2") }
  var selectedProductId by remember { mutableStateOf(initialProduct?.id ?: products.firstOrNull()?.id ?: "") }
  var quantityInput by remember { mutableStateOf("10") }
  var transferNotes by remember { mutableStateOf("Inter-store stock replenishment") }

  var sourceDropdownExpanded by remember { mutableStateOf(false) }
  var destDropdownExpanded by remember { mutableStateOf(false) }
  var productDropdownExpanded by remember { mutableStateOf(false) }

  val sourceStock = stocks.firstOrNull { it.storeId == sourceStoreId && it.productId == selectedProductId }?.quantity ?: 0
  val selectedProductObj = products.firstOrNull { it.id == selectedProductId }

  Column(modifier = Modifier.fillMaxSize()) {
    TabRow(selectedTabIndex = selectedTab) {
      Tab(
        selected = selectedTab == 0,
        onClick = { selectedTab = 0 },
        text = { Text("Transfers (${transfers.size})") }
      )
      if (canTransferStock) {
        Tab(
          selected = selectedTab == 1,
          onClick = { selectedTab = 1 },
          text = { Text("New Transfer") }
        )
      }
    }

    if (selectedTab == 0) {
      // Transfer History List
      if (transfers.isEmpty()) {
        Box(
          modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
          contentAlignment = Alignment.Center
        ) {
          Text(
            text = "No stock transfers recorded yet.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
          )
        }
      } else {
        LazyColumn(
          modifier = Modifier.fillMaxSize(),
          contentPadding = PaddingValues(16.dp),
          verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
          items(transfers) { transfer ->
            TransferItemCard(
              transfer = transfer,
              canReceive = canTransferStock && transfer.status == "IN_TRANSIT",
              onReceive = { onCompleteTransfer(transfer.id) }
            )
          }
        }
      }
    } else {
      // New Transfer Form
      LazyColumn(
        modifier = Modifier
          .fillMaxSize()
          .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
      ) {
        item {
          Text(
            text = "Create Inter-Store Transfer",
            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
          )
          Text(
            text = "Stock will be deducted from the source store immediately and credited to the destination store upon receipt confirmation.",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
          )
        }

        // Source Store
        item {
          ExposedDropdownMenuBox(
            expanded = sourceDropdownExpanded,
            onExpandedChange = { sourceDropdownExpanded = !sourceDropdownExpanded }
          ) {
            val name = stores.firstOrNull { it.id == sourceStoreId }?.name ?: "Select Source Store"
            OutlinedTextField(
              value = name,
              onValueChange = {},
              readOnly = true,
              label = { Text("Source Store (From)") },
              trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = sourceDropdownExpanded) },
              modifier = Modifier
                .menuAnchor()
                .fillMaxWidth()
            )
            ExposedDropdownMenu(
              expanded = sourceDropdownExpanded,
              onDismissRequest = { sourceDropdownExpanded = false }
            ) {
              stores.forEach { store ->
                DropdownMenuItem(
                  text = { Text(store.name) },
                  onClick = {
                    sourceStoreId = store.id
                    sourceDropdownExpanded = false
                  }
                )
              }
            }
          }
        }

        // Destination Store
        item {
          ExposedDropdownMenuBox(
            expanded = destDropdownExpanded,
            onExpandedChange = { destDropdownExpanded = !destDropdownExpanded }
          ) {
            val name = stores.firstOrNull { it.id == destStoreId }?.name ?: "Select Destination Store"
            OutlinedTextField(
              value = name,
              onValueChange = {},
              readOnly = true,
              label = { Text("Destination Store (To)") },
              trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = destDropdownExpanded) },
              modifier = Modifier
                .menuAnchor()
                .fillMaxWidth()
            )
            ExposedDropdownMenu(
              expanded = destDropdownExpanded,
              onDismissRequest = { destDropdownExpanded = false }
            ) {
              stores.filter { it.id != sourceStoreId }.forEach { store ->
                DropdownMenuItem(
                  text = { Text(store.name) },
                  onClick = {
                    destStoreId = store.id
                    destDropdownExpanded = false
                  }
                )
              }
            }
          }
        }

        // Product Selector
        item {
          ExposedDropdownMenuBox(
            expanded = productDropdownExpanded,
            onExpandedChange = { productDropdownExpanded = !productDropdownExpanded }
          ) {
            val prodName = selectedProductObj?.name ?: "Select Product"
            OutlinedTextField(
              value = prodName,
              onValueChange = {},
              readOnly = true,
              label = { Text("Product to Transfer") },
              trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = productDropdownExpanded) },
              modifier = Modifier
                .menuAnchor()
                .fillMaxWidth()
            )
            ExposedDropdownMenu(
              expanded = productDropdownExpanded,
              onDismissRequest = { productDropdownExpanded = false }
            ) {
              products.forEach { prod ->
                DropdownMenuItem(
                  text = { Text("${prod.name} (${prod.sku})") },
                  onClick = {
                    selectedProductId = prod.id
                    productDropdownExpanded = false
                  }
                )
              }
            }
          }
        }

        // Available stock preview
        item {
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
              Text(text = "Available at Source Store:", style = MaterialTheme.typography.bodySmall)
              Text(
                text = "$sourceStock ${selectedProductObj?.unitType ?: "units"}",
                style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold),
                color = if (sourceStock > 0) StatusGreen else StatusRed
              )
            }
          }
        }

        // Quantity
        item {
          OutlinedTextField(
            value = quantityInput,
            onValueChange = { quantityInput = it },
            label = { Text("Transfer Quantity") },
            singleLine = true,
            modifier = Modifier
              .fillMaxWidth()
              .testTag("transfer_qty_input")
          )
        }

        // Notes
        item {
          OutlinedTextField(
            value = transferNotes,
            onValueChange = { transferNotes = it },
            label = { Text("Transfer Notes / Reason") },
            modifier = Modifier.fillMaxWidth()
          )
        }

        item {
          Spacer(modifier = Modifier.height(12.dp))
          val qty = quantityInput.toIntOrNull() ?: 0
          val isValid = sourceStoreId != destStoreId && selectedProductId.isNotBlank() && qty in 1..sourceStock

          Button(
            onClick = {
              if (isValid) {
                onInitiateTransfer(sourceStoreId, destStoreId, selectedProductId, qty, transferNotes)
                selectedTab = 0
              }
            },
            enabled = isValid,
            colors = ButtonDefaults.buttonColors(containerColor = RetailPrimary),
            modifier = Modifier
              .fillMaxWidth()
              .height(50.dp)
              .testTag("initiate_transfer_button")
          ) {
            Icon(imageVector = Icons.Default.MoveDown, contentDescription = null)
            Spacer(modifier = Modifier.width(8.dp))
            Text("Dispatch Transfer")
          }
        }
      }
    }
  }
}

@Composable
fun TransferItemCard(
  transfer: StockTransfer,
  canReceive: Boolean,
  onReceive: () -> Unit
) {
  val df = SimpleDateFormat("MMM dd, yyyy HH:mm", Locale.US)
  val formattedDate = df.format(Date(transfer.createdAt))

  Card(
    shape = RoundedCornerShape(12.dp),
    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
    elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
    modifier = Modifier
      .fillMaxWidth()
      .testTag("transfer_card_${transfer.id}")
  ) {
    Column(modifier = Modifier.padding(14.dp)) {
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        Text(
          text = transfer.transferNumber,
          style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
          color = RetailPrimary
        )
        StatusBadge(
          text = if (transfer.status == "IN_TRANSIT") "IN TRANSIT" else "COMPLETED",
          type = if (transfer.status == "IN_TRANSIT") BadgeType.WARNING else BadgeType.SUCCESS
        )
      }

      Spacer(modifier = Modifier.height(8.dp))

      Text(
        text = transfer.productName,
        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
      )

      Spacer(modifier = Modifier.height(6.dp))

      Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
      ) {
        Text(
          text = transfer.sourceStoreName,
          style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Medium),
          modifier = Modifier.weight(1f)
        )
        Icon(
          imageVector = Icons.Default.ArrowForward,
          contentDescription = "to",
          tint = MaterialTheme.colorScheme.onSurfaceVariant,
          modifier = Modifier.size(14.dp)
        )
        Spacer(modifier = Modifier.width(4.dp))
        Text(
          text = transfer.destStoreName,
          style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Medium),
          modifier = Modifier.weight(1f)
        )
      }

      Spacer(modifier = Modifier.height(6.dp))

      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        Text(
          text = "Quantity: ${transfer.quantity} units",
          style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold)
        )
        Text(
          text = formattedDate,
          style = MaterialTheme.typography.labelSmall,
          color = MaterialTheme.colorScheme.onSurfaceVariant
        )
      }

      if (transfer.notes.isNotBlank()) {
        Spacer(modifier = Modifier.height(4.dp))
        Text(
          text = "Note: ${transfer.notes}",
          style = MaterialTheme.typography.labelSmall,
          color = MaterialTheme.colorScheme.onSurfaceVariant
        )
      }

      if (canReceive) {
        Spacer(modifier = Modifier.height(10.dp))
        Button(
          onClick = onReceive,
          colors = ButtonDefaults.buttonColors(containerColor = StatusGreen),
          modifier = Modifier
            .fillMaxWidth()
            .testTag("confirm_receipt_${transfer.id}")
        ) {
          Icon(imageVector = Icons.Default.CheckCircle, contentDescription = null, modifier = Modifier.size(16.dp))
          Spacer(modifier = Modifier.width(6.dp))
          Text("Confirm Receipt & Add to Inventory")
        }
      }
    }
  }
}
