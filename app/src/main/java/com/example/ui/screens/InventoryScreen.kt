package com.example.ui.screens

import androidx.compose.foundation.background
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.Inventory
import androidx.compose.material.icons.filled.LocalCafe
import androidx.compose.material.icons.filled.LocalGroceryStore
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.QrCode
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
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
import com.example.ui.components.BadgeType
import com.example.ui.components.StatusBadge
import com.example.ui.theme.RetailPrimary
import com.example.ui.theme.StatusAmber
import com.example.ui.theme.StatusGreen
import com.example.ui.theme.StatusRed
import com.example.ui.viewmodel.ProductStockItem
import java.util.Locale

@Composable
fun InventoryScreen(
  items: List<ProductStockItem>,
  onProductClick: (Product) -> Unit,
  onAddProductClick: () -> Unit,
  onScanClick: () -> Unit
) {
  var searchQuery by remember { mutableStateOf("") }
  var selectedFilter by remember { mutableStateOf("ALL") } // ALL, LOW_STOCK, OUT_OF_STOCK, EXPIRING, PERISHABLE

  val filteredItems = items.filter { item ->
    val matchesSearch = if (searchQuery.isBlank()) true else {
      val q = searchQuery.trim().lowercase(Locale.ROOT)
      item.product.name.lowercase(Locale.ROOT).contains(q) ||
        item.product.sku.lowercase(Locale.ROOT).contains(q) ||
        item.product.barcode.lowercase(Locale.ROOT).contains(q) ||
        item.product.category.lowercase(Locale.ROOT).contains(q) ||
        item.product.supplier.lowercase(Locale.ROOT).contains(q)
    }

    val matchesFilter = when (selectedFilter) {
      "LOW_STOCK" -> item.isLowStock
      "OUT_OF_STOCK" -> item.isOutOfStock
      "EXPIRING" -> item.isExpiringSoon || item.isExpired
      "PERISHABLE" -> item.product.isPerishable
      else -> true
    }

    matchesSearch && matchesFilter
  }

  Scaffold(
    floatingActionButton = {
      FloatingActionButton(
        onClick = onAddProductClick,
        containerColor = RetailPrimary,
        contentColor = Color.White,
        modifier = Modifier.testTag("add_product_fab")
      ) {
        Icon(imageVector = Icons.Default.Add, contentDescription = "Add Product")
      }
    }
  ) { paddingValues ->
    Column(
      modifier = Modifier
        .fillMaxSize()
        .padding(paddingValues)
        .padding(horizontal = 16.dp)
    ) {
      Spacer(modifier = Modifier.height(12.dp))

      // Search Bar
      OutlinedTextField(
        value = searchQuery,
        onValueChange = { searchQuery = it },
        placeholder = { Text("Search by name, SKU, barcode, supplier...") },
        leadingIcon = {
          Icon(imageVector = Icons.Default.Search, contentDescription = "Search")
        },
        trailingIcon = {
          if (searchQuery.isNotEmpty()) {
            IconButton(onClick = { searchQuery = "" }) {
              Icon(imageVector = Icons.Default.Clear, contentDescription = "Clear")
            }
          } else {
            IconButton(onClick = onScanClick) {
              Icon(imageVector = Icons.Default.QrCode, contentDescription = "Scan Barcode", tint = RetailPrimary)
            }
          }
        },
        singleLine = true,
        shape = RoundedCornerShape(12.dp),
        modifier = Modifier
          .fillMaxWidth()
          .testTag("inventory_search_input")
      )

      Spacer(modifier = Modifier.height(10.dp))

      // Quick Filters
      LazyRow(
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        modifier = Modifier.fillMaxWidth()
      ) {
        item {
          FilterChip(
            selected = selectedFilter == "ALL",
            onClick = { selectedFilter = "ALL" },
            label = { Text("All (${items.size})") }
          )
        }
        item {
          FilterChip(
            selected = selectedFilter == "LOW_STOCK",
            onClick = { selectedFilter = "LOW_STOCK" },
            label = { Text("Low Stock") }
          )
        }
        item {
          FilterChip(
            selected = selectedFilter == "OUT_OF_STOCK",
            onClick = { selectedFilter = "OUT_OF_STOCK" },
            label = { Text("Out of Stock") }
          )
        }
        item {
          FilterChip(
            selected = selectedFilter == "EXPIRING",
            onClick = { selectedFilter = "EXPIRING" },
            label = { Text("Expiring Soon") }
          )
        }
        item {
          FilterChip(
            selected = selectedFilter == "PERISHABLE",
            onClick = { selectedFilter = "PERISHABLE" },
            label = { Text("Perishable") }
          )
        }
      }

      Spacer(modifier = Modifier.height(10.dp))

      // Products List
      if (filteredItems.isEmpty()) {
        Box(
          modifier = Modifier
            .fillMaxSize()
            .padding(top = 40.dp),
          contentAlignment = Alignment.TopCenter
        ) {
          Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Icon(
              imageVector = Icons.Default.Inventory,
              contentDescription = null,
              modifier = Modifier.size(48.dp),
              tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
            )
            Spacer(modifier = Modifier.height(12.dp))
            Text(
              text = "No products found matching criteria.",
              style = MaterialTheme.typography.bodyMedium,
              color = MaterialTheme.colorScheme.onSurfaceVariant
            )
          }
        }
      } else {
        LazyColumn(
          verticalArrangement = Arrangement.spacedBy(10.dp),
          contentPadding = PaddingValues(bottom = 90.dp),
          modifier = Modifier.fillMaxSize()
        ) {
          items(filteredItems, key = { it.product.id }) { item ->
            ProductInventoryCard(
              item = item,
              onClick = { onProductClick(item.product) }
            )
          }
        }
      }
    }
  }
}

@Composable
fun ProductInventoryCard(
  item: ProductStockItem,
  onClick: () -> Unit
) {
  val prod = item.product
  val stock = item.totalStockAcrossStores

  Card(
    shape = RoundedCornerShape(12.dp),
    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
    elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
    modifier = Modifier
      .fillMaxWidth()
      .clickable { onClick() }
      .testTag("product_card_${prod.id}")
  ) {
    Row(
      modifier = Modifier
        .fillMaxWidth()
        .padding(12.dp),
      verticalAlignment = Alignment.CenterVertically
    ) {
      // Category / Product icon box
      Box(
        modifier = Modifier
          .size(44.dp)
          .clip(RoundedCornerShape(10.dp))
          .background(RetailPrimary.copy(alpha = 0.1f)),
        contentAlignment = Alignment.Center
      ) {
        Icon(
          imageVector = Icons.Default.LocalGroceryStore,
          contentDescription = null,
          tint = RetailPrimary,
          modifier = Modifier.size(24.dp)
        )
      }

      Spacer(modifier = Modifier.width(12.dp))

      Column(modifier = Modifier.weight(1f)) {
        Text(
          text = prod.name,
          style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
          maxLines = 1
        )
        Spacer(modifier = Modifier.height(2.dp))
        Text(
          text = "SKU: ${prod.sku} • ${prod.category}",
          style = MaterialTheme.typography.bodySmall,
          color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(modifier = Modifier.height(4.dp))
        Row(verticalAlignment = Alignment.CenterVertically) {
          Text(
            text = "€${String.format(Locale.US, "%.2f", prod.sellingPrice)}",
            style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold),
            color = RetailPrimary
          )
          Spacer(modifier = Modifier.width(8.dp))
          Text(
            text = "Cost: €${String.format(Locale.US, "%.2f", prod.purchasePrice)}",
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
          )
        }
      }

      // Stock status column
      Column(horizontalAlignment = Alignment.End) {
        Text(
          text = "$stock ${prod.unitType}s",
          style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
          color = when {
            item.isOutOfStock -> StatusRed
            item.isLowStock -> StatusAmber
            else -> StatusGreen
          }
        )
        Spacer(modifier = Modifier.height(4.dp))

        if (item.isOutOfStock) {
          StatusBadge(text = "Out of Stock", type = BadgeType.DANGER)
        } else if (item.isLowStock) {
          StatusBadge(text = "Low Stock", type = BadgeType.WARNING)
        } else {
          StatusBadge(text = "In Stock", type = BadgeType.SUCCESS)
        }

        if (item.isExpired) {
          Spacer(modifier = Modifier.height(2.dp))
          StatusBadge(text = "Expired", type = BadgeType.DANGER)
        } else if (item.isExpiringSoon && item.daysUntilExpiry != null) {
          Spacer(modifier = Modifier.height(2.dp))
          StatusBadge(text = "${item.daysUntilExpiry}d left", type = BadgeType.WARNING)
        }
      }
    }
  }
}
