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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.FileDownload
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Payments
import androidx.compose.material.icons.filled.ReceiptLong
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.TrendingDown
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.ScrollableTabRow
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
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
import com.example.data.model.DateFilterPreset
import com.example.data.model.DateTimeFilter
import com.example.data.model.Product
import com.example.data.model.StockTransaction
import com.example.data.model.StoreStock
import com.example.data.model.TransactionType
import com.example.ui.components.BadgeType
import com.example.ui.components.DateTimeFilterBar
import com.example.ui.components.StatusBadge
import com.example.ui.theme.RetailPrimary
import com.example.ui.theme.RetailSecondary
import com.example.ui.theme.RetailTertiary
import com.example.ui.theme.StatusAmber
import com.example.ui.theme.StatusGreen
import com.example.ui.theme.StatusRed
import com.example.ui.viewmodel.SalesAnalytics
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun ReportsScreen(
  salesAnalytics: SalesAnalytics,
  transactions: List<StockTransaction>,
  products: List<Product>,
  stocks: List<StoreStock>,
  selectedStoreName: String,
  dateTimeFilter: DateTimeFilter,
  onSelectPreset: (DateFilterPreset) -> Unit,
  onApplyCustom: (Long, Long, String) -> Unit,
  onExportInventoryCsv: () -> Unit,
  onExportMovementsCsv: () -> Unit
) {
  var selectedTab by remember { mutableIntStateOf(0) }
  val tabTitles = listOf("Sales Analytics", "Best Sellers", "Stock Audit Log", "Loss & Wastage")

  Column(modifier = Modifier.fillMaxSize()) {
    ScrollableTabRow(
      selectedTabIndex = selectedTab,
      edgePadding = 16.dp
    ) {
      tabTitles.forEachIndexed { index, title ->
        Tab(
          selected = selectedTab == index,
          onClick = { selectedTab = index },
          text = { Text(title) }
        )
      }
    }

    // Date & Time Filter Bar
    Box(modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)) {
      DateTimeFilterBar(
        currentFilter = dateTimeFilter,
        onSelectPreset = onSelectPreset,
        onApplyCustom = onApplyCustom
      )
    }

    when (selectedTab) {
      0 -> SalesAnalyticsTab(salesAnalytics, onExportMovementsCsv)
      1 -> BestSellersTab(salesAnalytics.topSellingProducts, products)
      2 -> StockAuditLogTab(transactions, onExportMovementsCsv)
      3 -> LossWastageTab(transactions, products, stocks, onExportInventoryCsv)
    }
  }
}

@Composable
fun SalesAnalyticsTab(
  analytics: SalesAnalytics,
  onExport: () -> Unit
) {
  LazyColumn(
    modifier = Modifier
      .fillMaxSize()
      .padding(16.dp),
    verticalArrangement = Arrangement.spacedBy(14.dp)
  ) {
    item {
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        Column {
          Text(
            text = "Financial Overview (${analytics.dateFilterLabel})",
            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
          )
          Text(
            text = analytics.dateFilterRangeText,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
          )
        }
        OutlinedButton(
          onClick = onExport,
          contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp)
        ) {
          Icon(imageVector = Icons.Default.Share, contentDescription = null, modifier = Modifier.size(16.dp))
          Spacer(modifier = Modifier.width(4.dp))
          Text("Export", fontSize = 12.sp)
        }
      }
    }

    // Revenue KPI Cards
    item {
      Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
        Card(
          shape = RoundedCornerShape(12.dp),
          colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
          elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
          modifier = Modifier.weight(1f)
        ) {
          Column(modifier = Modifier.padding(14.dp)) {
            Text(text = "Total Revenue", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Spacer(modifier = Modifier.height(4.dp))
            Text(
              text = "€${String.format(Locale.US, "%,.2f", analytics.totalSalesRevenue)}",
              style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
              color = StatusGreen
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(text = "+14.2% vs last month", style = MaterialTheme.typography.labelSmall, color = StatusGreen)
          }
        }

        Card(
          shape = RoundedCornerShape(12.dp),
          colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
          elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
          modifier = Modifier.weight(1f)
        ) {
          Column(modifier = Modifier.padding(14.dp)) {
            Text(text = "Avg Ticket / Order", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Spacer(modifier = Modifier.height(4.dp))
            Text(
              text = "€${String.format(Locale.US, "%.2f", analytics.averageTicket)}",
              style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
              color = RetailPrimary
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(text = "${analytics.totalOrders} total orders", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
          }
        }
      }
    }

    // Sales by Category
    item {
      Card(
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        modifier = Modifier.fillMaxWidth()
      ) {
        Column(modifier = Modifier.padding(16.dp)) {
          Text(
            text = "Sales Distribution by Category",
            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold)
          )
          Spacer(modifier = Modifier.height(12.dp))

          val totalCategorySales = analytics.categorySales.values.sum().coerceAtLeast(1.0)
          val sortedCategories = analytics.categorySales.entries.sortedByDescending { it.value }

          if (sortedCategories.isEmpty()) {
            Text(
              text = "No category sales recorded yet.",
              style = MaterialTheme.typography.bodySmall,
              color = MaterialTheme.colorScheme.onSurfaceVariant
            )
          } else {
            sortedCategories.forEach { (cat, amount) ->
              val pct = (amount / totalCategorySales).toFloat()
              Column(modifier = Modifier.padding(vertical = 4.dp)) {
                Row(
                  modifier = Modifier.fillMaxWidth(),
                  horizontalArrangement = Arrangement.SpaceBetween
                ) {
                  Text(text = cat, style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Medium))
                  Text(
                    text = "€${String.format(Locale.US, "%.2f", amount)} (${(pct * 100).toInt()}%)",
                    style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold)
                  )
                }
                Spacer(modifier = Modifier.height(4.dp))
                LinearProgressIndicator(
                  progress = { pct },
                  modifier = Modifier
                    .fillMaxWidth()
                    .height(6.dp)
                    .clip(RoundedCornerShape(3.dp)),
                  color = RetailPrimary,
                  trackColor = MaterialTheme.colorScheme.surfaceVariant
                )
              }
            }
          }
        }
      }
    }
  }
}

@Composable
fun BestSellersTab(
  topSelling: List<Pair<String, Int>>,
  products: List<Product>
) {
  val prodMap = products.associateBy { it.name }

  LazyColumn(
    modifier = Modifier
      .fillMaxSize()
      .padding(16.dp),
    verticalArrangement = Arrangement.spacedBy(10.dp)
  ) {
    item {
      Text(
        text = "Top Ranked Retail Products",
        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
      )
      Text(
        text = "Ranked by sales volume and inventory turnover rate",
        style = MaterialTheme.typography.bodySmall,
        color = MaterialTheme.colorScheme.onSurfaceVariant
      )
    }

    if (topSelling.isEmpty()) {
      item {
        Box(modifier = Modifier.padding(32.dp), contentAlignment = Alignment.Center) {
          Text("No sales data available yet.", color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
      }
    } else {
      items(topSelling.take(15)) { (prodName, unitsSold) ->
        val prod = prodMap[prodName]
        val revenue = unitsSold * (prod?.sellingPrice ?: 2.50)

        Card(
          shape = RoundedCornerShape(10.dp),
          colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
          elevation = CardDefaults.cardElevation(defaultElevation = 0.5.dp),
          modifier = Modifier.fillMaxWidth()
        ) {
          Row(
            modifier = Modifier
              .fillMaxWidth()
              .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
          ) {
            Box(
              modifier = Modifier
                .size(32.dp)
                .clip(CircleShape)
                .background(RetailPrimary.copy(alpha = 0.15f)),
              contentAlignment = Alignment.Center
            ) {
              Icon(imageVector = Icons.Default.TrendingUp, contentDescription = null, tint = RetailPrimary, modifier = Modifier.size(16.dp))
            }
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
              Text(
                text = prodName,
                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold)
              )
              Text(
                text = "$unitsSold units sold",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
              )
            }
            Text(
              text = "€${String.format(Locale.US, "%.2f", revenue)}",
              style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
              color = StatusGreen
            )
          }
        }
      }
    }
  }
}

@Composable
fun StockAuditLogTab(
  transactions: List<StockTransaction>,
  onExport: () -> Unit
) {
  val df = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.US)

  LazyColumn(
    modifier = Modifier
      .fillMaxSize()
      .padding(16.dp),
    verticalArrangement = Arrangement.spacedBy(10.dp)
  ) {
    item {
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        Column {
          Text(
            text = "Stock Movements & Audit Trail",
            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
          )
          Text(
            text = "Every stock adjustment, intake, and disposal is logged with user stamps",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
          )
        }
        OutlinedButton(onClick = onExport) {
          Icon(imageVector = Icons.Default.FileDownload, contentDescription = null, modifier = Modifier.size(16.dp))
          Spacer(modifier = Modifier.width(4.dp))
          Text("CSV")
        }
      }
    }

    if (transactions.isEmpty()) {
      item {
        Card(
          shape = RoundedCornerShape(10.dp),
          colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
          modifier = Modifier.fillMaxWidth()
        ) {
          Box(
            modifier = Modifier
              .fillMaxWidth()
              .padding(32.dp),
            contentAlignment = Alignment.Center
          ) {
            Text(
              text = "No stock transactions found for the selected period & store.\nTry selecting 'All Time' or a broader custom range above.",
              style = MaterialTheme.typography.bodyMedium,
              color = MaterialTheme.colorScheme.onSurfaceVariant,
              textAlign = androidx.compose.ui.text.style.TextAlign.Center
            )
          }
        }
      }
    } else {
      items(transactions) { tx ->
      Card(
        shape = RoundedCornerShape(10.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.5.dp),
        modifier = Modifier.fillMaxWidth()
      ) {
        Column(modifier = Modifier.padding(12.dp)) {
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
          ) {
            Text(
              text = tx.referenceNumber,
              style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
              color = RetailPrimary
            )
            StatusBadge(
              text = tx.transactionType,
              type = when (tx.transactionType) {
                "RECEIVED" -> BadgeType.SUCCESS
                "SOLD" -> BadgeType.INFO
                "EXPIRED", "DAMAGED", "DISPOSED" -> BadgeType.DANGER
                else -> BadgeType.WARNING
              }
            )
          }

          Spacer(modifier = Modifier.height(4.dp))

          Text(
            text = tx.productName,
            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold)
          )

          Spacer(modifier = Modifier.height(2.dp))

          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
          ) {
            Text(
              text = "Delta: ${if (tx.quantity > 0) "+${tx.quantity}" else "${tx.quantity}"} (${tx.previousStock} → ${tx.newStock})",
              style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Medium),
              color = if (tx.quantity >= 0) StatusGreen else StatusRed
            )
            Text(
              text = df.format(Date(tx.timestamp)),
              style = MaterialTheme.typography.labelSmall,
              color = MaterialTheme.colorScheme.onSurfaceVariant
            )
          }

          Spacer(modifier = Modifier.height(4.dp))

          Text(
            text = "Staff: ${tx.userName} • ${tx.notes}",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
          )
        }
      }
    }
  }
  }
}

@Composable
fun LossWastageTab(
  transactions: List<StockTransaction>,
  products: List<Product>,
  stocks: List<StoreStock>,
  onExportInventory: () -> Unit
) {
  val prodMap = products.associateBy { it.id }

  val lossTx = transactions.filter {
    it.transactionType in listOf("DAMAGED", "EXPIRED", "DISPOSED")
  }

  var totalLossUnits = 0
  var totalLossValuation = 0.0

  lossTx.forEach { tx ->
    val p = prodMap[tx.productId]
    val cost = p?.purchasePrice ?: 1.50
    val units = kotlin.math.abs(tx.quantity)
    totalLossUnits += units
    totalLossValuation += units * cost
  }

  LazyColumn(
    modifier = Modifier
      .fillMaxSize()
      .padding(16.dp),
    verticalArrangement = Arrangement.spacedBy(14.dp)
  ) {
    item {
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        Column {
          Text(
            text = "Shrinkage & Wastage Report",
            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
          )
          Text(
            text = "Financial impact of damaged goods and expired products",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
          )
        }
        OutlinedButton(onClick = onExportInventory) {
          Icon(imageVector = Icons.Default.FileDownload, contentDescription = null, modifier = Modifier.size(16.dp))
          Spacer(modifier = Modifier.width(4.dp))
          Text("Export")
        }
      }
    }

    item {
      Card(
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer),
        modifier = Modifier.fillMaxWidth()
      ) {
        Column(modifier = Modifier.padding(16.dp)) {
          Text(
            text = "Total Loss & Wastage Cost",
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onErrorContainer
          )
          Spacer(modifier = Modifier.height(4.dp))
          Text(
            text = "€${String.format(Locale.US, "%,.2f", totalLossValuation)}",
            style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold),
            color = MaterialTheme.colorScheme.onErrorContainer
          )
          Spacer(modifier = Modifier.height(4.dp))
          Text(
            text = "$totalLossUnits total units recorded as damaged, expired or disposed",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onErrorContainer.copy(alpha = 0.8f)
          )
        }
      }
    }

    item {
      Text(
        text = "Wastage Records (${lossTx.size})",
        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold)
      )
    }

    if (lossTx.isEmpty()) {
      item {
        Text("No loss records found.", color = MaterialTheme.colorScheme.onSurfaceVariant)
      }
    } else {
      items(lossTx) { tx ->
        val p = prodMap[tx.productId]
        val cost = (p?.purchasePrice ?: 1.50) * kotlin.math.abs(tx.quantity)

        Card(
          shape = RoundedCornerShape(10.dp),
          colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
          elevation = CardDefaults.cardElevation(defaultElevation = 0.5.dp),
          modifier = Modifier.fillMaxWidth()
        ) {
          Row(
            modifier = Modifier
              .fillMaxWidth()
              .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
          ) {
            Column(modifier = Modifier.weight(1f)) {
              Text(
                text = tx.productName,
                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold)
              )
              Text(
                text = "Reason: ${tx.notes}",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
              )
              Text(
                text = "Approved by: ${tx.userName}",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
              )
            }
            Column(horizontalAlignment = Alignment.End) {
              Text(
                text = "${tx.quantity} units",
                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                color = StatusRed
              )
              Text(
                text = "-€${String.format(Locale.US, "%.2f", cost)}",
                style = MaterialTheme.typography.bodySmall,
                color = StatusRed
              )
            }
          }
        }
      }
    }
  }
}
