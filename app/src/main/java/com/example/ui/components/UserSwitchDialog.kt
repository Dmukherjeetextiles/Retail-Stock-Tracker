package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Security
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
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
import com.example.data.model.AppUser
import com.example.data.model.UserRole
import com.example.ui.theme.RetailPrimary

@Composable
fun UserSwitchDialog(
  users: List<AppUser>,
  currentUser: AppUser,
  onDismiss: () -> Unit,
  onSwitchUser: (AppUser) -> Unit,
  onSwitchRole: (UserRole) -> Unit
) {
  var selectedTab by remember { mutableIntStateOf(0) }

  AlertDialog(
    onDismissRequest = onDismiss,
    title = {
      Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(
          imageVector = Icons.Default.Security,
          contentDescription = null,
          tint = RetailPrimary,
          modifier = Modifier.size(24.dp)
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text("User Roles & Profiles")
      }
    },
    text = {
      Column(modifier = Modifier.fillMaxWidth()) {
        Text(
          "Test any access level or switch employee profile:",
          style = MaterialTheme.typography.bodySmall,
          color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(modifier = Modifier.height(8.dp))

        TabRow(selectedTabIndex = selectedTab) {
          Tab(
            selected = selectedTab == 0,
            onClick = { selectedTab = 0 },
            text = { Text("Quick Role") }
          )
          Tab(
            selected = selectedTab == 1,
            onClick = { selectedTab = 1 },
            text = { Text("Employees (${users.size})") }
          )
        }

        Spacer(modifier = Modifier.height(12.dp))

        if (selectedTab == 0) {
          // Switch Role
          LazyColumn(modifier = Modifier.height(300.dp)) {
            items(UserRole.values()) { role ->
              val isSelected = currentUser.role == role
              Card(
                shape = RoundedCornerShape(10.dp),
                colors = CardDefaults.cardColors(
                  containerColor = if (isSelected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surface
                ),
                elevation = CardDefaults.cardElevation(defaultElevation = if (isSelected) 2.dp else 0.5.dp),
                modifier = Modifier
                  .fillMaxWidth()
                  .padding(vertical = 4.dp)
                  .clickable {
                    onSwitchRole(role)
                    onDismiss()
                  }
                  .testTag("role_option_${role.name}")
              ) {
                Row(
                  verticalAlignment = Alignment.CenterVertically,
                  modifier = Modifier.padding(12.dp)
                ) {
                  RadioButton(
                    selected = isSelected,
                    onClick = {
                      onSwitchRole(role)
                      onDismiss()
                    }
                  )
                  Spacer(modifier = Modifier.width(8.dp))
                  Column {
                    Text(
                      text = role.displayName,
                      style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                      color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                      text = when (role) {
                        UserRole.OWNER -> "Full access across all stores, settings & financials"
                        UserRole.STORE_MANAGER -> "Manage store stock, transfers, rosters & reports"
                        UserRole.INVENTORY_STAFF -> "Scan barcodes, receive stock & adjust counts"
                        UserRole.SALES_STAFF -> "Product lookup, price check & daily sales"
                        UserRole.EMPLOYEE -> "Personal shift schedule, roster & alerts"
                      },
                      style = MaterialTheme.typography.bodySmall,
                      color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                  }
                }
              }
            }
          }
        } else {
          // Switch Employee Profile
          LazyColumn(modifier = Modifier.height(300.dp)) {
            items(users) { user ->
              val isSelected = currentUser.id == user.id
              Card(
                shape = RoundedCornerShape(10.dp),
                colors = CardDefaults.cardColors(
                  containerColor = if (isSelected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surface
                ),
                elevation = CardDefaults.cardElevation(defaultElevation = if (isSelected) 2.dp else 0.5.dp),
                modifier = Modifier
                  .fillMaxWidth()
                  .padding(vertical = 4.dp)
                  .clickable {
                    onSwitchUser(user)
                    onDismiss()
                  }
                  .testTag("user_option_${user.id}")
              ) {
                Row(
                  verticalAlignment = Alignment.CenterVertically,
                  modifier = Modifier.padding(12.dp)
                ) {
                  Box(
                    modifier = Modifier
                      .size(36.dp)
                      .clip(CircleShape)
                      .background(RetailPrimary.copy(alpha = 0.2f)),
                    contentAlignment = Alignment.Center
                  ) {
                    Text(
                      text = user.name.take(2).uppercase(),
                      fontWeight = FontWeight.Bold,
                      color = RetailPrimary,
                      fontSize = 12.sp
                    )
                  }
                  Spacer(modifier = Modifier.width(10.dp))
                  Column(modifier = Modifier.weight(1f)) {
                    Text(
                      text = user.name,
                      style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold)
                    )
                    Text(
                      text = "${user.employeeCode} • ${user.role.displayName.substringBefore(" /")}",
                      style = MaterialTheme.typography.bodySmall,
                      color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                  }
                  if (isSelected) {
                    Icon(
                      imageVector = Icons.Default.Check,
                      contentDescription = null,
                      tint = RetailPrimary,
                      modifier = Modifier.size(20.dp)
                    )
                  }
                }
              }
            }
          }
        }
      }
    },
    confirmButton = {
      TextButton(onClick = onDismiss) {
        Text("Done")
      }
    }
  )
}
