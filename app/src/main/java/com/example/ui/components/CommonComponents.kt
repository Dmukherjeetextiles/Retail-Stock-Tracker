package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Business
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
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
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.AppUser
import com.example.data.model.DateFilterPreset
import com.example.data.model.DateTimeFilter
import com.example.data.model.Store
import com.example.data.model.UserRole
import com.example.ui.theme.RetailPrimary
import com.example.ui.theme.StatusAmber
import com.example.ui.theme.StatusGreen
import com.example.ui.theme.StatusRed
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RetailTopAppBar(
  stores: List<Store>,
  selectedStoreId: String,
  currentUser: AppUser,
  unreadNotificationsCount: Int,
  onSelectStore: (String) -> Unit,
  onOpenNotifications: () -> Unit,
  onOpenUserSwitchDialog: () -> Unit
) {
  var storeMenuExpanded by remember { mutableStateOf(false) }

  val selectedStoreName = if (selectedStoreId == "ALL") {
    "All Alimentation Stores"
  } else {
    stores.firstOrNull { it.id == selectedStoreId }?.name ?: "All Alimentation Stores"
  }

  TopAppBar(
    title = {
      Column {
        Text(
          text = "Retail Flow",
          style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
          color = MaterialTheme.colorScheme.onSurface
        )
        // Store Dropdown Selector
        Row(
          verticalAlignment = Alignment.CenterVertically,
          modifier = Modifier
            .clip(RoundedCornerShape(6.dp))
            .clickable { storeMenuExpanded = true }
            .padding(vertical = 2.dp)
            .testTag("store_selector_button")
        ) {
          Icon(
            imageVector = Icons.Default.Business,
            contentDescription = null,
            modifier = Modifier.size(14.dp),
            tint = RetailPrimary
          )
          Spacer(modifier = Modifier.width(4.dp))
          Text(
            text = selectedStoreName,
            style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Medium),
            color = RetailPrimary,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.widthIn(max = 200.dp)
          )
          Icon(
            imageVector = Icons.Default.ArrowDropDown,
            contentDescription = "Select Store",
            modifier = Modifier.size(16.dp),
            tint = RetailPrimary
          )
        }

        DropdownMenu(
          expanded = storeMenuExpanded,
          onDismissRequest = { storeMenuExpanded = false }
        ) {
          DropdownMenuItem(
            text = {
              Text(
                "🛒 All Alimentation Stores (Consolidated)",
                fontWeight = if (selectedStoreId == "ALL") FontWeight.Bold else FontWeight.Normal
              )
            },
            onClick = {
              onSelectStore("ALL")
              storeMenuExpanded = false
            }
          )

          val activeStores = stores.filter { it.isActive }
          val futureBusinesses = stores.filter { !it.isActive }

          if (activeStores.isNotEmpty()) {
            HorizontalDivider()
            DropdownMenuItem(
              text = {
                Text(
                  "ACTIVE ALIMENTATION PILOT",
                  style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                  color = MaterialTheme.colorScheme.primary
                )
              },
              onClick = {},
              enabled = false
            )
            activeStores.forEach { store ->
              DropdownMenuItem(
                text = {
                  Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                  ) {
                    Text(
                      "🏪 ${store.name}",
                      fontWeight = if (selectedStoreId == store.id) FontWeight.Bold else FontWeight.Normal
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                      store.code,
                      style = MaterialTheme.typography.labelSmall,
                      color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                  }
                },
                onClick = {
                  onSelectStore(store.id)
                  storeMenuExpanded = false
                }
              )
            }
          }

          if (futureBusinesses.isNotEmpty()) {
            HorizontalDivider()
            DropdownMenuItem(
              text = {
                Text(
                  "PLANNED GROUP BUSINESSES (PHASE 2)",
                  style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                  color = MaterialTheme.colorScheme.outline
                )
              },
              onClick = {},
              enabled = false
            )
            futureBusinesses.forEach { store ->
              val iconPrefix = if (store.businessType == "Restaurant") "🍽️" else if (store.businessType == "Café") "☕" else "🏢"
              DropdownMenuItem(
                text = {
                  Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                  ) {
                    Column {
                      Text(
                        "$iconPrefix ${store.name}",
                        fontWeight = if (selectedStoreId == store.id) FontWeight.Bold else FontWeight.Normal
                      )
                      Text(
                        "Planned rollout • ${store.businessType}",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                      )
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    StatusBadge(text = "Phase 2", type = BadgeType.NEUTRAL)
                  }
                },
                onClick = {
                  onSelectStore(store.id)
                  storeMenuExpanded = false
                }
              )
            }
          }
        }
      }
    },
    actions = {
      // Role & User Switcher Chip
      Surface(
        shape = RoundedCornerShape(16.dp),
        color = MaterialTheme.colorScheme.primaryContainer,
        modifier = Modifier
          .clickable { onOpenUserSwitchDialog() }
          .padding(end = 4.dp)
          .testTag("role_switcher_chip")
      ) {
        Row(
          verticalAlignment = Alignment.CenterVertically,
          modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
        ) {
          Box(
            modifier = Modifier
              .size(18.dp)
              .clip(CircleShape)
              .background(RetailPrimary),
            contentAlignment = Alignment.Center
          ) {
            Icon(
              imageVector = Icons.Default.Person,
              contentDescription = null,
              modifier = Modifier.size(12.dp),
              tint = Color.White
            )
          }
          Spacer(modifier = Modifier.width(4.dp))
          Text(
            text = currentUser.role.displayName.substringBefore(" /"),
            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.SemiBold),
            color = MaterialTheme.colorScheme.onPrimaryContainer
          )
        }
      }

      // Notifications Icon with badge
      IconButton(
        onClick = onOpenNotifications,
        modifier = Modifier.testTag("notifications_icon_button")
      ) {
        BadgedBox(
          badge = {
            if (unreadNotificationsCount > 0) {
              Badge { Text(unreadNotificationsCount.toString()) }
            }
          }
        ) {
          Icon(
            imageVector = Icons.Default.Notifications,
            contentDescription = "Notifications"
          )
        }
      }
    },
    colors = TopAppBarDefaults.topAppBarColors(
      containerColor = MaterialTheme.colorScheme.surface
    )
  )
}

@Composable
fun MetricStatCard(
  title: String,
  value: String,
  subtitle: String? = null,
  icon: ImageVector,
  iconTint: Color,
  backgroundColor: Color = MaterialTheme.colorScheme.surface,
  modifier: Modifier = Modifier,
  onClick: (() -> Unit)? = null
) {
  Card(
    shape = RoundedCornerShape(12.dp),
    colors = CardDefaults.cardColors(containerColor = backgroundColor),
    elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
    modifier = modifier
      .then(if (onClick != null) Modifier.clickable { onClick() } else Modifier)
  ) {
    Column(modifier = Modifier.padding(12.dp)) {
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        Text(
          text = title,
          style = MaterialTheme.typography.labelMedium,
          color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Box(
          modifier = Modifier
            .size(28.dp)
            .clip(CircleShape)
            .background(iconTint.copy(alpha = 0.15f)),
          contentAlignment = Alignment.Center
        ) {
          Icon(
            imageVector = icon,
            contentDescription = null,
            modifier = Modifier.size(16.dp),
            tint = iconTint
          )
        }
      }
      Spacer(modifier = Modifier.height(4.dp))
      Text(
        text = value,
        style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
        color = MaterialTheme.colorScheme.onSurface
      )
      if (subtitle != null) {
        Spacer(modifier = Modifier.height(2.dp))
        Text(
          text = subtitle,
          style = MaterialTheme.typography.bodySmall,
          color = MaterialTheme.colorScheme.onSurfaceVariant
        )
      }
    }
  }
}

@Composable
fun StatusBadge(
  text: String,
  type: BadgeType,
  modifier: Modifier = Modifier
) {
  val (bgColor, textColor) = when (type) {
    BadgeType.SUCCESS -> StatusGreen.copy(alpha = 0.15f) to StatusGreen
    BadgeType.WARNING -> StatusAmber.copy(alpha = 0.15f) to StatusAmber
    BadgeType.DANGER -> StatusRed.copy(alpha = 0.15f) to StatusRed
    BadgeType.INFO -> RetailPrimary.copy(alpha = 0.15f) to RetailPrimary
    BadgeType.NEUTRAL -> MaterialTheme.colorScheme.surfaceVariant to MaterialTheme.colorScheme.onSurfaceVariant
  }

  Surface(
    shape = RoundedCornerShape(6.dp),
    color = bgColor,
    modifier = modifier
  ) {
    Text(
      text = text,
      color = textColor,
      style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.SemiBold),
      modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
    )
  }
}

enum class BadgeType {
  SUCCESS, WARNING, DANGER, INFO, NEUTRAL
}

@Composable
fun DateTimeFilterBar(
  currentFilter: DateTimeFilter,
  onSelectPreset: (DateFilterPreset) -> Unit,
  onApplyCustom: (Long, Long, String) -> Unit,
  modifier: Modifier = Modifier
) {
  var showCustomDialog by remember { mutableStateOf(false) }

  Card(
    modifier = modifier.fillMaxWidth(),
    shape = RoundedCornerShape(12.dp),
    colors = CardDefaults.cardColors(
      containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f)
    )
  ) {
    Column(modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp)) {
      // Header with Filter Name and Range Summary
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        Row(
          verticalAlignment = Alignment.CenterVertically,
          horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
          Icon(
            imageVector = Icons.Default.DateRange,
            contentDescription = null,
            tint = RetailPrimary,
            modifier = Modifier.size(16.dp)
          )
          Text(
            text = "PERIOD FILTER",
            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
            color = MaterialTheme.colorScheme.onSurfaceVariant
          )
          StatusBadge(
            text = currentFilter.displayLabel,
            type = if (currentFilter.isCustom) BadgeType.WARNING else BadgeType.INFO
          )
        }

        Row(
          verticalAlignment = Alignment.CenterVertically,
          modifier = Modifier
            .clip(RoundedCornerShape(6.dp))
            .clickable { showCustomDialog = true }
            .padding(horizontal = 6.dp, vertical = 2.dp)
            .testTag("custom_date_time_button")
        ) {
          Icon(
            imageVector = Icons.Default.Edit,
            contentDescription = "Set Custom Date & Time",
            tint = RetailPrimary,
            modifier = Modifier.size(13.dp)
          )
          Spacer(modifier = Modifier.width(4.dp))
          Text(
            text = "Custom...",
            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.SemiBold),
            color = RetailPrimary
          )
        }
      }

      Spacer(modifier = Modifier.height(6.dp))

      // Horizontal Scroll Presets Row
      Row(
        modifier = Modifier
          .fillMaxWidth()
          .horizontalScroll(rememberScrollState()),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically
      ) {
        val presets = listOf(
          DateFilterPreset.TODAY to "Today",
          DateFilterPreset.YESTERDAY to "Yesterday",
          DateFilterPreset.LAST_7_DAYS to "Last 7 Days",
          DateFilterPreset.THIS_MONTH to "This Month",
          DateFilterPreset.ALL_TIME to "All Time"
        )

        presets.forEach { (preset, label) ->
          val isSelected = !currentFilter.isCustom && currentFilter.preset == preset
          FilterChip(
            selected = isSelected,
            onClick = { onSelectPreset(preset) },
            label = {
              Text(
                label,
                style = MaterialTheme.typography.labelSmall.copy(
                  fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                )
              )
            },
            colors = FilterChipDefaults.filterChipColors(
              selectedContainerColor = RetailPrimary,
              selectedLabelColor = Color.White
            ),
            modifier = Modifier.testTag("filter_chip_${preset.name.lowercase()}")
          )
        }

        // Custom chip indicator
        FilterChip(
          selected = currentFilter.isCustom,
          onClick = { showCustomDialog = true },
          leadingIcon = {
            Icon(
              imageVector = Icons.Default.Schedule,
              contentDescription = null,
              modifier = Modifier.size(14.dp)
            )
          },
          label = {
            Text(
              if (currentFilter.isCustom) currentFilter.displayLabel else "Custom Window",
              style = MaterialTheme.typography.labelSmall.copy(
                fontWeight = if (currentFilter.isCustom) FontWeight.Bold else FontWeight.Normal
              )
            )
          },
          colors = FilterChipDefaults.filterChipColors(
            selectedContainerColor = StatusAmber,
            selectedLabelColor = Color.Black
          ),
          modifier = Modifier.testTag("filter_chip_custom")
        )
      }

      Spacer(modifier = Modifier.height(4.dp))

      // Range text description
      Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(4.dp)
      ) {
        Icon(
          imageVector = Icons.Default.Schedule,
          contentDescription = null,
          modifier = Modifier.size(12.dp),
          tint = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
          text = currentFilter.rangeSummary,
          style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp),
          color = MaterialTheme.colorScheme.onSurfaceVariant,
          maxLines = 1,
          overflow = TextOverflow.Ellipsis
        )
      }
    }
  }

  if (showCustomDialog) {
    CustomDateTimeRangeDialog(
      currentFilter = currentFilter,
      onDismiss = { showCustomDialog = false },
      onApply = { startMs, endMs, label ->
        onApplyCustom(startMs, endMs, label)
        showCustomDialog = false
      }
    )
  }
}

@Composable
fun CustomDateTimeRangeDialog(
  currentFilter: DateTimeFilter,
  onDismiss: () -> Unit,
  onApply: (Long, Long, String) -> Unit
) {
  val dateSdf = remember { SimpleDateFormat("yyyy-MM-dd", Locale.US) }
  val timeSdf = remember { SimpleDateFormat("HH:mm", Locale.US) }
  val fullSdf = remember { SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.US) }

  val initialStartDate = remember { dateSdf.format(Date(currentFilter.startTimestamp)) }
  val initialStartTime = remember { timeSdf.format(Date(currentFilter.startTimestamp)) }
  val initialEndDate = remember { dateSdf.format(Date(currentFilter.endTimestamp)) }
  val initialEndTime = remember { timeSdf.format(Date(currentFilter.endTimestamp)) }

  var startDateStr by remember { mutableStateOf(initialStartDate) }
  var startTimeStr by remember { mutableStateOf(initialStartTime) }
  var endDateStr by remember { mutableStateOf(initialEndDate) }
  var endTimeStr by remember { mutableStateOf(initialEndTime) }
  var customLabel by remember { mutableStateOf(if (currentFilter.isCustom) currentFilter.displayLabel else "Custom Period") }
  var errorMessage by remember { mutableStateOf<String?>(null) }

  // Quick shift helper
  fun setShift(startHour: Int, startMinute: Int, endHour: Int, endMinute: Int, label: String) {
    val cal = Calendar.getInstance()
    startDateStr = dateSdf.format(cal.time)
    endDateStr = dateSdf.format(cal.time)
    startTimeStr = String.format(Locale.US, "%02d:%02d", startHour, startMinute)
    endTimeStr = String.format(Locale.US, "%02d:%02d", endHour, endMinute)
    customLabel = label
    errorMessage = null
  }

  AlertDialog(
    onDismissRequest = onDismiss,
    title = {
      Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(
          imageVector = Icons.Default.DateRange,
          contentDescription = null,
          tint = RetailPrimary,
          modifier = Modifier.size(22.dp)
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(
          "Specific Date & Time Window",
          style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
        )
      }
    },
    text = {
      Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(10.dp)
      ) {
        Text(
          "Filter dashboard metrics, transactions, and sales by an exact operating date and hour interval.",
          style = MaterialTheme.typography.bodySmall,
          color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        // Quick shift buttons
        Text(
          "Quick Operating Shifts:",
          style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
          color = MaterialTheme.colorScheme.primary
        )
        Row(
          modifier = Modifier
            .fillMaxWidth()
            .horizontalScroll(rememberScrollState()),
          horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
          FilterChip(
            selected = false,
            onClick = { setShift(6, 0, 14, 0, "Morning Shift") },
            label = { Text("Morning (06:00-14:00)", fontSize = 11.sp) }
          )
          FilterChip(
            selected = false,
            onClick = { setShift(8, 0, 18, 0, "Day Shift") },
            label = { Text("Day (08:00-18:00)", fontSize = 11.sp) }
          )
          FilterChip(
            selected = false,
            onClick = { setShift(14, 0, 22, 0, "Evening Shift") },
            label = { Text("Evening (14:00-22:00)", fontSize = 11.sp) }
          )
          FilterChip(
            selected = false,
            onClick = { setShift(0, 0, 23, 59, "Full Day") },
            label = { Text("Full Day (00:00-23:59)", fontSize = 11.sp) }
          )
        }

        HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp))

        // Start Date and Time
        Text(
          "From Date & Time:",
          style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold)
        )
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
          OutlinedTextField(
            value = startDateStr,
            onValueChange = { startDateStr = it },
            label = { Text("Start Date") },
            placeholder = { Text("YYYY-MM-DD") },
            modifier = Modifier.weight(1.3f),
            singleLine = true
          )
          OutlinedTextField(
            value = startTimeStr,
            onValueChange = { startTimeStr = it },
            label = { Text("Start Time") },
            placeholder = { Text("HH:mm") },
            modifier = Modifier.weight(1f),
            singleLine = true
          )
        }

        // End Date and Time
        Text(
          "To Date & Time:",
          style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold)
        )
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
          OutlinedTextField(
            value = endDateStr,
            onValueChange = { endDateStr = it },
            label = { Text("End Date") },
            placeholder = { Text("YYYY-MM-DD") },
            modifier = Modifier.weight(1.3f),
            singleLine = true
          )
          OutlinedTextField(
            value = endTimeStr,
            onValueChange = { endTimeStr = it },
            label = { Text("End Time") },
            placeholder = { Text("HH:mm") },
            modifier = Modifier.weight(1f),
            singleLine = true
          )
        }

        // Custom Label
        OutlinedTextField(
          value = customLabel,
          onValueChange = { customLabel = it },
          label = { Text("Filter Label (Optional)") },
          placeholder = { Text("e.g. Saturday Peak Rush") },
          modifier = Modifier.fillMaxWidth(),
          singleLine = true
        )

        errorMessage?.let { error ->
          Text(
            text = error,
            color = StatusRed,
            style = MaterialTheme.typography.bodySmall
          )
        }
      }
    },
    confirmButton = {
      Button(
        onClick = {
          try {
            val startFullStr = "${startDateStr.trim()} ${startTimeStr.trim()}"
            val endFullStr = "${endDateStr.trim()} ${endTimeStr.trim()}"
            val startDate = fullSdf.parse(startFullStr)
            val endDate = fullSdf.parse(endFullStr)

            if (startDate == null || endDate == null) {
              errorMessage = "Invalid date/time format. Use YYYY-MM-DD and HH:mm"
              return@Button
            }

            if (startDate.time > endDate.time) {
              errorMessage = "Start date/time cannot be after End date/time"
              return@Button
            }

            val finalLabel = if (customLabel.isNotBlank()) customLabel.trim() else "Custom Range"
            onApply(startDate.time, endDate.time, finalLabel)
          } catch (e: Exception) {
            errorMessage = "Error parsing timestamps: ${e.message}"
          }
        },
        modifier = Modifier.testTag("apply_custom_date_filter_button")
      ) {
        Text("Apply Filter")
      }
    },
    dismissButton = {
      TextButton(onClick = onDismiss) {
        Text("Cancel")
      }
    }
  )
}
