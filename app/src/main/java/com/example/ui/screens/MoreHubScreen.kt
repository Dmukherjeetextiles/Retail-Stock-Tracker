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
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.Business
import androidx.compose.material.icons.filled.FileDownload
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.MoveDown
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.People
import androidx.compose.material.icons.filled.Receipt
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Store
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Divider
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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.AppNotification
import com.example.data.model.AppUser
import com.example.data.model.AuditLog
import com.example.data.model.Store
import com.example.ui.components.BadgeType
import com.example.ui.components.StatusBadge
import com.example.ui.theme.RetailPrimary
import com.example.ui.theme.RetailSecondary
import com.example.ui.theme.RetailTertiary
import com.example.ui.theme.StatusAmber
import com.example.ui.theme.StatusGreen
import com.example.ui.theme.StatusRed
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.UUID

@Composable
fun MoreHubScreen(
  stores: List<Store>,
  currentUser: AppUser,
  notifications: List<AppNotification>,
  auditLogs: List<AuditLog>,
  onNavigateToStores: () -> Unit,
  onNavigateToStaff: () -> Unit,
  onNavigateToTransfers: () -> Unit,
  onNavigateToInvoiceOcr: () -> Unit,
  onNavigateToExpiry: () -> Unit,
  onNavigateToAudit: () -> Unit,
  onNavigateToNotifications: () -> Unit,
  onExportInventory: () -> Unit,
  onExportMovements: () -> Unit,
  onExportRoster: () -> Unit,
  onOpenRoleSwitcher: () -> Unit
) {
  Column(
    modifier = Modifier
      .fillMaxSize()
      .padding(16.dp)
      .verticalScroll(rememberScrollState()),
    verticalArrangement = Arrangement.spacedBy(16.dp)
  ) {
    // User Profile Card
    Card(
      shape = RoundedCornerShape(14.dp),
      colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
      elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
      modifier = Modifier
        .fillMaxWidth()
        .clickable { onOpenRoleSwitcher() }
        .testTag("user_profile_summary_card")
    ) {
      Row(
        modifier = Modifier
          .fillMaxWidth()
          .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
      ) {
        Box(
          modifier = Modifier
            .size(48.dp)
            .clip(CircleShape)
            .background(RetailPrimary.copy(alpha = 0.15f)),
          contentAlignment = Alignment.Center
        ) {
          Text(
            text = currentUser.name.take(2).uppercase(),
            fontWeight = FontWeight.Bold,
            color = RetailPrimary,
            fontSize = 16.sp
          )
        }
        Spacer(modifier = Modifier.width(12.dp))
        Column(modifier = Modifier.weight(1f)) {
          Text(
            text = currentUser.name,
            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
          )
          Text(
            text = "${currentUser.role.displayName} • ${currentUser.employeeCode}",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
          )
        }
        Icon(imageVector = Icons.Default.ArrowForward, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant)
      }
    }

    // Management Modules
    Text(
      text = "Enterprise & Store Modules",
      style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold)
    )

    Card(
      shape = RoundedCornerShape(14.dp),
      colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
      elevation = CardDefaults.cardElevation(defaultElevation = 0.5.dp),
      modifier = Modifier.fillMaxWidth()
    ) {
      Column {
        MoreMenuItem(
          title = "Multi-Store Management",
          subtitle = "${stores.size} branches active across Metropolis",
          icon = Icons.Default.Store,
          iconColor = RetailPrimary,
          onClick = onNavigateToStores
        )
        HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp))
        MoreMenuItem(
          title = "Staff & Shift Scheduling",
          subtitle = "Employee directory, rosters & conflict checks",
          icon = Icons.Default.People,
          iconColor = RetailSecondary,
          onClick = onNavigateToStaff
        )
        HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp))
        MoreMenuItem(
          title = "Inter-Store Stock Transfers",
          subtitle = "Dispatch and receive inventory between branches",
          icon = Icons.Default.MoveDown,
          iconColor = StatusGreen,
          onClick = onNavigateToTransfers
        )
        HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp))
        MoreMenuItem(
          title = "Purchase Invoice OCR Intake",
          subtitle = "Extract items and commit invoices to inventory",
          icon = Icons.Default.Receipt,
          iconColor = RetailTertiary,
          onClick = onNavigateToInvoiceOcr
        )
        HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp))
        MoreMenuItem(
          title = "Perishable & Expiration Tracker",
          subtitle = "Track expiry alerts and manager disposals",
          icon = Icons.Default.Warning,
          iconColor = StatusAmber,
          onClick = onNavigateToExpiry
        )
        HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp))
        MoreMenuItem(
          title = "System Security & Audit Log",
          subtitle = "${auditLogs.size} recorded audit events",
          icon = Icons.Default.History,
          iconColor = RetailPrimary,
          onClick = onNavigateToAudit
        )
      }
    }

    // Export Center
    Text(
      text = "Export & Reports Center (Excel / CSV)",
      style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold)
    )

    Card(
      shape = RoundedCornerShape(14.dp),
      colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
      elevation = CardDefaults.cardElevation(defaultElevation = 0.5.dp),
      modifier = Modifier.fillMaxWidth()
    ) {
      Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
        ExportActionRow(
          title = "Export Inventory Stock Levels",
          subtitle = "SKU, barcode, category, prices, stock, valuation",
          onClick = onExportInventory
        )
        ExportActionRow(
          title = "Export Stock Movement Audit Log",
          subtitle = "All transaction timestamps, references, users & deltas",
          onClick = onExportMovements
        )
        ExportActionRow(
          title = "Export Work Roster & Shifts",
          subtitle = "Employee schedules, duty assignments and breaks",
          onClick = onExportRoster
        )
      }
    }

    Spacer(modifier = Modifier.height(20.dp))
  }
}

@Composable
fun MoreMenuItem(
  title: String,
  subtitle: String,
  icon: ImageVector,
  iconColor: Color,
  onClick: () -> Unit
) {
  Row(
    modifier = Modifier
      .fillMaxWidth()
      .clickable { onClick() }
      .padding(16.dp),
    verticalAlignment = Alignment.CenterVertically
  ) {
    Box(
      modifier = Modifier
        .size(38.dp)
        .clip(CircleShape)
        .background(iconColor.copy(alpha = 0.12f)),
      contentAlignment = Alignment.Center
    ) {
      Icon(imageVector = icon, contentDescription = null, tint = iconColor, modifier = Modifier.size(20.dp))
    }
    Spacer(modifier = Modifier.width(14.dp))
    Column(modifier = Modifier.weight(1f)) {
      Text(text = title, style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold))
      Text(text = subtitle, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
    Icon(imageVector = Icons.Default.ArrowForward, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.size(16.dp))
  }
}

@Composable
fun ExportActionRow(
  title: String,
  subtitle: String,
  onClick: () -> Unit
) {
  Row(
    modifier = Modifier
      .fillMaxWidth()
      .padding(vertical = 4.dp),
    horizontalArrangement = Arrangement.SpaceBetween,
    verticalAlignment = Alignment.CenterVertically
  ) {
    Column(modifier = Modifier.weight(1f)) {
      Text(text = title, style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Medium))
      Text(text = subtitle, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
    OutlinedButton(
      onClick = onClick,
      contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 10.dp, vertical = 4.dp)
    ) {
      Icon(imageVector = Icons.Default.FileDownload, contentDescription = null, modifier = Modifier.size(14.dp))
      Spacer(modifier = Modifier.width(4.dp))
      Text("CSV", fontSize = 11.sp)
    }
  }
}

@Composable
fun StoreManagementScreen(
  stores: List<Store>,
  canManageStores: Boolean,
  onSaveStore: (Store) -> Unit
) {
  var showAddStoreDialog by remember { mutableStateOf(false) }
  var storeToEdit by remember { mutableStateOf<Store?>(null) }

  Column(
    modifier = Modifier
      .fillMaxSize()
      .padding(16.dp)
  ) {
    Row(
      modifier = Modifier.fillMaxWidth(),
      horizontalArrangement = Arrangement.SpaceBetween,
      verticalAlignment = Alignment.CenterVertically
    ) {
      Column {
        Text(
          text = "Multi-Store Branches",
          style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
        )
        Text(
          text = "Store locations, managers and store codes",
          style = MaterialTheme.typography.bodySmall,
          color = MaterialTheme.colorScheme.onSurfaceVariant
        )
      }
      if (canManageStores) {
        Button(
          onClick = { showAddStoreDialog = true },
          colors = ButtonDefaults.buttonColors(containerColor = RetailPrimary)
        ) {
          Text("Add Store")
        }
      }
    }

    Spacer(modifier = Modifier.height(14.dp))

    LazyColumn(verticalArrangement = Arrangement.spacedBy(10.dp)) {
      items(stores) { store ->
        Card(
          shape = RoundedCornerShape(12.dp),
          colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
          elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
          modifier = Modifier.fillMaxWidth()
        ) {
          Column(modifier = Modifier.padding(14.dp)) {
            Row(
              modifier = Modifier.fillMaxWidth(),
              horizontalArrangement = Arrangement.SpaceBetween,
              verticalAlignment = Alignment.CenterVertically
            ) {
              Text(
                text = store.name,
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
              )
              StatusBadge(text = store.code, type = BadgeType.INFO)
            }
            Spacer(modifier = Modifier.height(6.dp))
            Text(
              text = "📍 ${store.address}, ${store.city}",
              style = MaterialTheme.typography.bodySmall,
              color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
              text = "Manager: ${store.managerName} • Phone: ${store.phone}",
              style = MaterialTheme.typography.bodySmall,
              color = MaterialTheme.colorScheme.onSurfaceVariant
            )
          }
        }
      }
    }
  }

  if (showAddStoreDialog) {
    var name by remember { mutableStateOf(storeToEdit?.name ?: "") }
    var code by remember { mutableStateOf(storeToEdit?.code ?: "BR-0${stores.size + 1}") }
    var address by remember { mutableStateOf(storeToEdit?.address ?: "") }
    var city by remember { mutableStateOf(storeToEdit?.city ?: "Metropolis") }
    var phone by remember { mutableStateOf(storeToEdit?.phone ?: "+1 555-010${stores.size + 1}") }
    var manager by remember { mutableStateOf(storeToEdit?.managerName ?: "") }

    AlertDialog(
      onDismissRequest = {
        showAddStoreDialog = false
        storeToEdit = null
      },
      title = { Text(if (storeToEdit == null) "Add Store Location" else "Edit Store") },
      text = {
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
          OutlinedTextField(value = name, onValueChange = { name = it }, label = { Text("Store Name *") }, modifier = Modifier.fillMaxWidth())
          OutlinedTextField(value = code, onValueChange = { code = it }, label = { Text("Store Code") }, modifier = Modifier.fillMaxWidth())
          OutlinedTextField(value = address, onValueChange = { address = it }, label = { Text("Street Address") }, modifier = Modifier.fillMaxWidth())
          OutlinedTextField(value = city, onValueChange = { city = it }, label = { Text("City") }, modifier = Modifier.fillMaxWidth())
          OutlinedTextField(value = phone, onValueChange = { phone = it }, label = { Text("Phone") }, modifier = Modifier.fillMaxWidth())
          OutlinedTextField(value = manager, onValueChange = { manager = it }, label = { Text("Store Manager Name") }, modifier = Modifier.fillMaxWidth())
        }
      },
      confirmButton = {
        Button(
          onClick = {
            if (name.isNotBlank()) {
              val s = (storeToEdit ?: Store(id = UUID.randomUUID().toString(), name = name, code = code)).copy(
                name = name,
                code = code,
                address = address,
                city = city,
                phone = phone,
                managerName = manager
              )
              onSaveStore(s)
              showAddStoreDialog = false
              storeToEdit = null
            }
          },
          enabled = name.isNotBlank()
        ) {
          Text("Save Store")
        }
      },
      dismissButton = {
        TextButton(onClick = {
          showAddStoreDialog = false
          storeToEdit = null
        }) {
          Text("Cancel")
        }
      }
    )
  }
}

@Composable
fun AuditLogScreen(
  auditLogs: List<AuditLog>
) {
  val df = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.US)

  Column(
    modifier = Modifier
      .fillMaxSize()
      .padding(16.dp)
  ) {
    Text(
      text = "Security & Operational Audit Log",
      style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
    )
    Text(
      text = "Immutable record of all stock adjustments, transfers, invoice approvals and user actions",
      style = MaterialTheme.typography.bodySmall,
      color = MaterialTheme.colorScheme.onSurfaceVariant
    )

    Spacer(modifier = Modifier.height(14.dp))

    LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
      items(auditLogs) { log ->
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
              StatusBadge(text = log.action, type = BadgeType.INFO)
              Text(
                text = df.format(Date(log.timestamp)),
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
              )
            }
            Spacer(modifier = Modifier.height(6.dp))
            Text(
              text = log.details,
              style = MaterialTheme.typography.bodyMedium
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
              text = "User: ${log.userName} • Store: ${log.storeId}",
              style = MaterialTheme.typography.labelSmall,
              color = MaterialTheme.colorScheme.onSurfaceVariant
            )
          }
        }
      }
    }
  }
}

@Composable
fun NotificationsScreen(
  notifications: List<AppNotification>,
  onMarkAsRead: (String) -> Unit,
  onMarkAllAsRead: () -> Unit,
  onClearAll: () -> Unit
) {
  val df = SimpleDateFormat("MMM dd, HH:mm", Locale.US)

  Column(
    modifier = Modifier
      .fillMaxSize()
      .padding(16.dp)
  ) {
    Row(
      modifier = Modifier.fillMaxWidth(),
      horizontalArrangement = Arrangement.SpaceBetween,
      verticalAlignment = Alignment.CenterVertically
    ) {
      Text(
        text = "Notifications & Alerts (${notifications.size})",
        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
      )
      Row {
        TextButton(onClick = onMarkAllAsRead) {
          Text("Read All", fontSize = 12.sp)
        }
        TextButton(onClick = onClearAll) {
          Text("Clear", fontSize = 12.sp, color = StatusRed)
        }
      }
    }

    Spacer(modifier = Modifier.height(10.dp))

    if (notifications.isEmpty()) {
      Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Text("No alerts or notifications.", color = MaterialTheme.colorScheme.onSurfaceVariant)
      }
    } else {
      LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        items(notifications) { notif ->
          Card(
            shape = RoundedCornerShape(10.dp),
            colors = CardDefaults.cardColors(
              containerColor = if (notif.isRead) MaterialTheme.colorScheme.surface else MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f)
            ),
            elevation = CardDefaults.cardElevation(defaultElevation = 0.5.dp),
            modifier = Modifier
              .fillMaxWidth()
              .clickable { onMarkAsRead(notif.id) }
          ) {
            Row(
              modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
              verticalAlignment = Alignment.CenterVertically
            ) {
              Box(
                modifier = Modifier
                  .size(36.dp)
                  .clip(CircleShape)
                  .background(
                    when (notif.type) {
                      "OUT_OF_STOCK" -> StatusRed.copy(alpha = 0.15f)
                      "LOW_STOCK" -> StatusAmber.copy(alpha = 0.15f)
                      "TRANSFER" -> StatusGreen.copy(alpha = 0.15f)
                      else -> RetailPrimary.copy(alpha = 0.15f)
                    }
                  ),
                contentAlignment = Alignment.Center
              ) {
                Icon(
                  imageVector = when (notif.type) {
                    "OUT_OF_STOCK" -> Icons.Default.Warning
                    "LOW_STOCK" -> Icons.Default.Warning
                    "TRANSFER" -> Icons.Default.MoveDown
                    else -> Icons.Default.Notifications
                  },
                  contentDescription = null,
                  tint = when (notif.type) {
                    "OUT_OF_STOCK" -> StatusRed
                    "LOW_STOCK" -> StatusAmber
                    "TRANSFER" -> StatusGreen
                    else -> RetailPrimary
                  },
                  modifier = Modifier.size(18.dp)
                )
              }

              Spacer(modifier = Modifier.width(12.dp))

              Column(modifier = Modifier.weight(1f)) {
                Text(
                  text = notif.title,
                  style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold)
                )
                Text(
                  text = notif.message,
                  style = MaterialTheme.typography.bodySmall,
                  color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                  text = df.format(Date(notif.timestamp)),
                  style = MaterialTheme.typography.labelSmall,
                  color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                )
              }
            }
          }
        }
      }
    }
  }
}
