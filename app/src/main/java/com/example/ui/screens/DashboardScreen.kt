package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
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
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.DocumentScanner
import androidx.compose.material.icons.filled.Inventory
import androidx.compose.material.icons.filled.MoveDown
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.People
import androidx.compose.material.icons.filled.QrCodeScanner
import androidx.compose.material.icons.filled.Receipt
import androidx.compose.material.icons.filled.Report
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.AppUser
import com.example.data.model.DateFilterPreset
import com.example.data.model.DateTimeFilter
import com.example.data.model.StockTransaction
import com.example.data.model.UserRole
import com.example.ui.components.BadgeType
import com.example.ui.components.DateTimeFilterBar
import com.example.ui.components.MetricStatCard
import com.example.ui.components.StatusBadge
import com.example.ui.theme.RetailPrimary
import com.example.ui.theme.RetailSecondary
import com.example.ui.theme.RetailTertiary
import com.example.ui.theme.StatusAmber
import com.example.ui.theme.StatusGreen
import com.example.ui.theme.StatusRed
import com.example.ui.viewmodel.DashboardMetrics
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun DashboardScreen(
  metrics: DashboardMetrics,
  currentUser: AppUser,
  selectedStoreName: String,
  dateTimeFilter: DateTimeFilter,
  onSelectPreset: (DateFilterPreset) -> Unit,
  onApplyCustom: (Long, Long, String) -> Unit,
  onNavigateToScan: () -> Unit,
  onNavigateToInventory: () -> Unit,
  onNavigateToReceive: () -> Unit,
  onNavigateToInvoiceOcr: () -> Unit,
  onNavigateToTransfer: () -> Unit,
  onNavigateToAddProduct: () -> Unit,
  onNavigateToAlerts: () -> Unit,
  onNavigateToReports: () -> Unit,
  onNavigateToStaff: () -> Unit
) {
  LazyColumn(
    modifier = Modifier
      .fillMaxSize()
      .padding(horizontal = 16.dp),
    contentPadding = PaddingValues(top = 12.dp, bottom = 80.dp),
    verticalArrangement = Arrangement.spacedBy(16.dp)
  ) {
    // 1. Welcome Header
    item {
      Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
        modifier = Modifier.fillMaxWidth()
      ) {
        Column(modifier = Modifier.padding(16.dp)) {
          Text(
            text = "Welcome back, ${currentUser.name.substringBefore(" ")} 👋",
            style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
            color = MaterialTheme.colorScheme.onPrimaryContainer
          )
          Spacer(modifier = Modifier.height(4.dp))
          Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
              text = "Active Store: ",
              style = MaterialTheme.typography.bodyMedium,
              color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
            )
            Text(
              text = selectedStoreName,
              style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
              color = MaterialTheme.colorScheme.onPrimaryContainer
            )
          }
        }
      }
    }

    // 1.5 Date & Time Filter Bar
    item {
      DateTimeFilterBar(
        currentFilter = dateTimeFilter,
        onSelectPreset = onSelectPreset,
        onApplyCustom = onApplyCustom
      )
    }

    // 2. Overview Grid (Filtered by Date/Time)
    item {
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        Column {
          Text(
            text = "Operating Overview",
            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
          )
          Text(
            text = metrics.dateFilterRangeText,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
          )
        }
        StatusBadge(text = metrics.dateFilterLabel, type = BadgeType.INFO)
      }
      Spacer(modifier = Modifier.height(8.dp))
      Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
        MetricStatCard(
          title = "Period Sales",
          value = "€${String.format(Locale.US, "%,.2f", metrics.periodSalesAmount)}",
          subtitle = "${metrics.periodSoldCount} units sold",
          icon = Icons.Default.TrendingUp,
          iconTint = StatusGreen,
          modifier = Modifier.weight(1f),
          onClick = onNavigateToReports
        )
        MetricStatCard(
          title = "Stock Intake",
          value = "${metrics.periodReceivedCount} units",
          subtitle = "${metrics.periodTransactionCount} period moves",
          icon = Icons.Default.Inventory,
          iconTint = RetailPrimary,
          modifier = Modifier.weight(1f),
          onClick = onNavigateToReports
        )
      }
      Spacer(modifier = Modifier.height(10.dp))
      Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
        MetricStatCard(
          title = "Total Catalog",
          value = metrics.totalProducts.toString(),
          subtitle = "${metrics.totalUnits} in stock",
          icon = Icons.Default.Inventory,
          iconTint = RetailSecondary,
          modifier = Modifier.weight(1f),
          onClick = onNavigateToInventory
        )
        MetricStatCard(
          title = "Stock Alerts",
          value = (metrics.lowStockCount + metrics.outOfStockCount).toString(),
          subtitle = "${metrics.expiringSoonCount + metrics.expiredCount} expiring",
          icon = Icons.Default.Warning,
          iconTint = if (metrics.outOfStockCount > 0) StatusRed else StatusAmber,
          modifier = Modifier.weight(1f),
          onClick = onNavigateToAlerts
        )
      }
    }

    // 3. Quick Actions
    item {
      Text(
        text = "Quick Actions",
        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
      )
      Spacer(modifier = Modifier.height(8.dp))

      FlowRow(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
        maxItemsInEachRow = 3
      ) {
        QuickActionButton(
          label = "Scan Barcode",
          icon = Icons.Default.QrCodeScanner,
          color = RetailPrimary,
          onClick = onNavigateToScan,
          tag = "quick_action_scan"
        )
        QuickActionButton(
          label = "Receive Stock",
          icon = Icons.Default.Add,
          color = StatusGreen,
          onClick = onNavigateToReceive,
          tag = "quick_action_receive"
        )
        QuickActionButton(
          label = "Scan Invoice",
          icon = Icons.Default.Receipt,
          color = RetailTertiary,
          onClick = onNavigateToInvoiceOcr,
          tag = "quick_action_invoice"
        )
        QuickActionButton(
          label = "Transfer Stock",
          icon = Icons.Default.MoveDown,
          color = RetailSecondary,
          onClick = onNavigateToTransfer,
          tag = "quick_action_transfer"
        )
        QuickActionButton(
          label = "Add Product",
          icon = Icons.Default.Inventory,
          color = RetailPrimary,
          onClick = onNavigateToAddProduct,
          tag = "quick_action_add_product"
        )
        QuickActionButton(
          label = "View Reports",
          icon = Icons.Default.TrendingUp,
          color = StatusAmber,
          onClick = onNavigateToReports,
          tag = "quick_action_reports"
        )
      }
    }

    // 4. Inventory Alerts Banner
    item {
      Card(
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(
          containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        modifier = Modifier
          .fillMaxWidth()
          .clickable { onNavigateToAlerts() }
          .testTag("inventory_alerts_card")
      ) {
        Column(modifier = Modifier.padding(14.dp)) {
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
          ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
              Icon(
                imageVector = Icons.Default.NotificationsActive,
                contentDescription = null,
                tint = StatusAmber,
                modifier = Modifier.size(20.dp)
              )
              Spacer(modifier = Modifier.width(8.dp))
              Text(
                text = "Inventory Alerts",
                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold)
              )
            }
            Icon(
              imageVector = Icons.Default.ArrowForward,
              contentDescription = "View",
              modifier = Modifier.size(16.dp),
              tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
          }

          Spacer(modifier = Modifier.height(10.dp))

          // Alert 1: Out of stock
          Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(vertical = 3.dp)
          ) {
            Box(
              modifier = Modifier
                .size(10.dp)
                .clip(CircleShape)
                .background(StatusRed)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
              text = "${metrics.outOfStockCount} products out of stock",
              style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Medium)
            )
          }

          // Alert 2: Low stock
          Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(vertical = 3.dp)
          ) {
            Box(
              modifier = Modifier
                .size(10.dp)
                .clip(CircleShape)
                .background(StatusAmber)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
              text = "${metrics.lowStockCount} products below minimum stock level",
              style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Medium)
            )
          }

          // Alert 3: Expiring soon
          Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(vertical = 3.dp)
          ) {
            Box(
              modifier = Modifier
                .size(10.dp)
                .clip(CircleShape)
                .background(StatusAmber)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
              text = "${metrics.expiringSoonCount} perishable products expiring within 7 days",
              style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Medium)
            )
          }
        }
      }
    }

    // 5. Recent Activity
    item {
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        Column {
          Text(
            text = "Operations (${metrics.dateFilterLabel})",
            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
          )
          Text(
            text = "${metrics.recentTransactions.size} transactions in selected window",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
          )
        }
        Text(
          text = "Full Audit Log",
          style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.SemiBold),
          color = RetailPrimary,
          modifier = Modifier.clickable { onNavigateToReports() }
        )
      }
    }

    if (metrics.recentTransactions.isEmpty()) {
      item {
        Card(
          shape = RoundedCornerShape(10.dp),
          colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
          modifier = Modifier.fillMaxWidth()
        ) {
          Box(
            modifier = Modifier
              .fillMaxWidth()
              .padding(24.dp),
            contentAlignment = Alignment.Center
          ) {
            Text(
              text = "No recent inventory transactions.",
              color = MaterialTheme.colorScheme.onSurfaceVariant
            )
          }
        }
      }
    } else {
      items(metrics.recentTransactions) { tx ->
        TransactionItemRow(tx)
      }
    }
  }
}

@Composable
fun QuickActionButton(
  label: String,
  icon: ImageVector,
  color: Color,
  onClick: () -> Unit,
  tag: String
) {
  Card(
    shape = RoundedCornerShape(12.dp),
    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
    elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
    modifier = Modifier
      .clickable { onClick() }
      .testTag(tag)
  ) {
    Column(
      modifier = Modifier
        .padding(horizontal = 14.dp, vertical = 12.dp),
      horizontalAlignment = Alignment.CenterHorizontally
    ) {
      Box(
        modifier = Modifier
          .size(36.dp)
          .clip(CircleShape)
          .background(color.copy(alpha = 0.15f)),
        contentAlignment = Alignment.Center
      ) {
        Icon(
          imageVector = icon,
          contentDescription = label,
          tint = color,
          modifier = Modifier.size(20.dp)
        )
      }
      Spacer(modifier = Modifier.height(6.dp))
      Text(
        text = label,
        style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Medium),
        color = MaterialTheme.colorScheme.onSurface
      )
    }
  }
}

@Composable
fun TransactionItemRow(tx: StockTransaction) {
  val df = SimpleDateFormat("MMM dd, HH:mm", Locale.US)
  val formattedDate = df.format(Date(tx.timestamp))

  Card(
    shape = RoundedCornerShape(10.dp),
    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
    elevation = CardDefaults.cardElevation(defaultElevation = 0.5.dp),
    modifier = Modifier
      .fillMaxWidth()
      .testTag("tx_item_${tx.id}")
  ) {
    Row(
      modifier = Modifier
        .fillMaxWidth()
        .padding(12.dp),
      verticalAlignment = Alignment.CenterVertically
    ) {
      val (badgeType, typeText) = when (tx.transactionType) {
        "RECEIVED" -> BadgeType.SUCCESS to "+${tx.quantity}"
        "SOLD" -> BadgeType.INFO to "${tx.quantity}"
        "TRANSFERRED" -> BadgeType.WARNING to "${tx.quantity}"
        "EXPIRED", "DISPOSED", "DAMAGED" -> BadgeType.DANGER to "${tx.quantity}"
        else -> BadgeType.NEUTRAL to (if (tx.quantity > 0) "+${tx.quantity}" else "${tx.quantity}")
      }

      Box(
        modifier = Modifier
          .size(38.dp)
          .clip(CircleShape)
          .background(
            when (badgeType) {
              BadgeType.SUCCESS -> StatusGreen.copy(alpha = 0.15f)
              BadgeType.DANGER -> StatusRed.copy(alpha = 0.15f)
              BadgeType.WARNING -> StatusAmber.copy(alpha = 0.15f)
              else -> RetailPrimary.copy(alpha = 0.15f)
            }
          ),
        contentAlignment = Alignment.Center
      ) {
        Text(
          text = typeText,
          fontWeight = FontWeight.Bold,
          fontSize = 11.sp,
          color = when (badgeType) {
            BadgeType.SUCCESS -> StatusGreen
            BadgeType.DANGER -> StatusRed
            BadgeType.WARNING -> StatusAmber
            else -> RetailPrimary
          }
        )
      }

      Spacer(modifier = Modifier.width(12.dp))

      Column(modifier = Modifier.weight(1f)) {
        Text(
          text = tx.productName,
          style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
          maxLines = 1
        )
        Text(
          text = "${tx.transactionType.lowercase().replaceFirstChar { it.uppercase() }} by ${tx.userName} • $formattedDate",
          style = MaterialTheme.typography.bodySmall,
          color = MaterialTheme.colorScheme.onSurfaceVariant
        )
      }

      StatusBadge(
        text = "${tx.previousStock} → ${tx.newStock}",
        type = BadgeType.NEUTRAL
      )
    }
  }
}
