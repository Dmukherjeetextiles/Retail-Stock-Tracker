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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DeleteForever
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
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
import androidx.compose.ui.unit.sp
import com.example.data.model.Product
import com.example.data.model.StoreStock
import com.example.ui.components.BadgeType
import com.example.ui.components.StatusBadge
import com.example.ui.theme.RetailPrimary
import com.example.ui.theme.StatusAmber
import com.example.ui.theme.StatusRed
import com.example.ui.viewmodel.ProductStockItem

@Composable
fun ExpirationManagementScreen(
  items: List<ProductStockItem>,
  selectedStoreId: String,
  canConfirmDisposal: Boolean,
  onConfirmDisposal: (productId: String, storeId: String, quantity: Int, reason: String) -> Unit
) {
  var selectedTab by remember { mutableStateOf("ALL") } // ALL, EXPIRED, SOON_7_DAYS, SOON_30_DAYS
  var itemToDispose by remember { mutableStateOf<ProductStockItem?>(null) }
  var disposalReason by remember { mutableStateOf("Expired perishable goods - segregated disposal") }
  var disposalQtyStr by remember { mutableStateOf("1") }

  val perishableItems = items.filter { it.product.isPerishable }

  val filteredItems = perishableItems.filter { item ->
    when (selectedTab) {
      "EXPIRED" -> item.isExpired || (item.stock?.expiredQuantity ?: 0) > 0
      "SOON_7_DAYS" -> item.isExpiringSoon || (item.daysUntilExpiry != null && item.daysUntilExpiry in 0..7)
      "SOON_30_DAYS" -> item.daysUntilExpiry != null && item.daysUntilExpiry in 0..30
      else -> true
    }
  }

  Column(
    modifier = Modifier
      .fillMaxSize()
      .padding(16.dp)
  ) {
    Text(
      text = "Perishable & Expiration Tracker",
      style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
    )
    Text(
      text = "Monitor upcoming product expiries, segregated batches, and log manager-approved disposals.",
      style = MaterialTheme.typography.bodySmall,
      color = MaterialTheme.colorScheme.onSurfaceVariant
    )

    Spacer(modifier = Modifier.height(12.dp))

    // Filter Chips
    LazyRow(
      horizontalArrangement = Arrangement.spacedBy(8.dp),
      modifier = Modifier.fillMaxWidth()
    ) {
      item {
        FilterChip(
          selected = selectedTab == "ALL",
          onClick = { selectedTab = "ALL" },
          label = { Text("All Perishables (${perishableItems.size})") }
        )
      }
      item {
        FilterChip(
          selected = selectedTab == "EXPIRED",
          onClick = { selectedTab = "EXPIRED" },
          label = { Text("Expired / Segregated") }
        )
      }
      item {
        FilterChip(
          selected = selectedTab == "SOON_7_DAYS",
          onClick = { selectedTab = "SOON_7_DAYS" },
          label = { Text("Expiring in ≤ 7 Days") }
        )
      }
      item {
        FilterChip(
          selected = selectedTab == "SOON_30_DAYS",
          onClick = { selectedTab = "SOON_30_DAYS" },
          label = { Text("Expiring in ≤ 30 Days") }
        )
      }
    }

    Spacer(modifier = Modifier.height(12.dp))

    if (filteredItems.isEmpty()) {
      Box(
        modifier = Modifier
          .fillMaxSize()
          .padding(32.dp),
        contentAlignment = Alignment.Center
      ) {
        Text(
          text = "No perishable items found for selected filter.",
          style = MaterialTheme.typography.bodyMedium,
          color = MaterialTheme.colorScheme.onSurfaceVariant
        )
      }
    } else {
      LazyColumn(
        verticalArrangement = Arrangement.spacedBy(10.dp),
        contentPadding = PaddingValues(bottom = 60.dp),
        modifier = Modifier.fillMaxSize()
      ) {
        items(filteredItems) { item ->
          ExpirationItemCard(
            item = item,
            canDispose = canConfirmDisposal,
            onDisposeClick = {
              itemToDispose = item
              val expQty = item.stock?.expiredQuantity ?: 0
              disposalQtyStr = if (expQty > 0) expQty.toString() else "1"
            }
          )
        }
      }
    }
  }

  // Disposal Confirmation Dialog (Requirement 13: Manager confirms disposal with reason)
  if (itemToDispose != null) {
    val current = itemToDispose!!
    AlertDialog(
      onDismissRequest = { itemToDispose = null },
      title = {
        Row(verticalAlignment = Alignment.CenterVertically) {
          Icon(imageVector = Icons.Default.DeleteForever, contentDescription = null, tint = StatusRed)
          Spacer(modifier = Modifier.width(8.dp))
          Text("Confirm Stock Disposal")
        }
      },
      text = {
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
          Text(
            text = "Product: ${current.product.name}",
            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold)
          )
          Text(
            text = "Batch: ${current.product.batchNumber} • Expiry: ${current.product.expirationDate}",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
          )
          Spacer(modifier = Modifier.height(4.dp))
          OutlinedTextField(
            value = disposalQtyStr,
            onValueChange = { disposalQtyStr = it },
            label = { Text("Quantity to Dispose") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth()
          )
          OutlinedTextField(
            value = disposalReason,
            onValueChange = { disposalReason = it },
            label = { Text("Disposal Reason") },
            modifier = Modifier.fillMaxWidth()
          )
          Text(
            text = "Note: Disposals are logged as immutable audit events with your manager identity.",
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
          )
        }
      },
      confirmButton = {
        Button(
          onClick = {
            val q = disposalQtyStr.toIntOrNull() ?: 1
            val storeId = current.stock?.storeId ?: if (selectedStoreId != "ALL") selectedStoreId else "store-1"
            onConfirmDisposal(current.product.id, storeId, q, disposalReason)
            itemToDispose = null
          },
          colors = ButtonDefaults.buttonColors(containerColor = StatusRed),
          modifier = Modifier.testTag("confirm_disposal_dialog_button")
        ) {
          Text("Confirm Disposal")
        }
      },
      dismissButton = {
        TextButton(onClick = { itemToDispose = null }) {
          Text("Cancel")
        }
      }
    )
  }
}

@Composable
fun ExpirationItemCard(
  item: ProductStockItem,
  canDispose: Boolean,
  onDisposeClick: () -> Unit
) {
  val prod = item.product
  val expiredQty = item.stock?.expiredQuantity ?: 0

  Card(
    shape = RoundedCornerShape(12.dp),
    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
    elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
    modifier = Modifier
      .fillMaxWidth()
      .testTag("expiry_card_${prod.id}")
  ) {
    Column(modifier = Modifier.padding(14.dp)) {
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        Text(
          text = prod.name,
          style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
          modifier = Modifier.weight(1f)
        )
        if (item.isExpired || expiredQty > 0) {
          StatusBadge(text = "EXPIRED", type = BadgeType.DANGER)
        } else if (item.isExpiringSoon) {
          StatusBadge(text = "${item.daysUntilExpiry ?: 0} DAYS LEFT", type = BadgeType.WARNING)
        } else {
          StatusBadge(text = "VALID", type = BadgeType.SUCCESS)
        }
      }

      Spacer(modifier = Modifier.height(6.dp))

      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
      ) {
        Column {
          Text(text = "Batch Number", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
          Text(text = prod.batchNumber.ifBlank { "N/A" }, style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Medium))
        }
        Column {
          Text(text = "Expiration Date", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
          Text(text = prod.expirationDate, style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold))
        }
        Column(horizontalAlignment = Alignment.End) {
          Text(text = "Current Stock", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
          Text(text = "${item.totalStockAcrossStores} ${prod.unitType}s", style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold))
        }
      }

      if (expiredQty > 0) {
        Spacer(modifier = Modifier.height(8.dp))
        Card(
          shape = RoundedCornerShape(8.dp),
          colors = CardDefaults.cardColors(containerColor = StatusRed.copy(alpha = 0.1f)),
          modifier = Modifier.fillMaxWidth()
        ) {
          Row(
            modifier = Modifier
              .fillMaxWidth()
              .padding(8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
          ) {
            Text(
              text = "⚠️ $expiredQty units segregated for disposal",
              style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold),
              color = StatusRed
            )
            if (canDispose) {
              OutlinedButton(
                onClick = onDisposeClick,
                contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 8.dp, vertical = 2.dp)
              ) {
                Text("Dispose", fontSize = 11.sp, color = StatusRed)
              }
            }
          }
        }
      } else if (canDispose && (item.isExpired || item.isExpiringSoon)) {
        Spacer(modifier = Modifier.height(10.dp))
        OutlinedButton(
          onClick = onDisposeClick,
          modifier = Modifier.fillMaxWidth()
        ) {
          Icon(imageVector = Icons.Default.DeleteForever, contentDescription = null, modifier = Modifier.size(16.dp))
          Spacer(modifier = Modifier.width(6.dp))
          Text("Segregate & Dispose Stock")
        }
      }
    }
  }
}
