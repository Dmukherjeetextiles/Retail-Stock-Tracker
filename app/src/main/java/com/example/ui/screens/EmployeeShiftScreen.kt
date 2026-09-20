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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.People
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
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
import com.example.data.model.EmployeeShift
import com.example.data.model.Store
import com.example.data.model.UserRole
import com.example.ui.components.BadgeType
import com.example.ui.components.StatusBadge
import com.example.ui.theme.RetailPrimary
import com.example.ui.theme.StatusAmber
import com.example.ui.theme.StatusGreen
import com.example.ui.theme.StatusRed
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.UUID

@Composable
fun EmployeeShiftScreen(
  users: List<AppUser>,
  shifts: List<EmployeeShift>,
  stores: List<Store>,
  currentUser: AppUser,
  canManageEmployees: Boolean,
  onSaveUser: (AppUser) -> Unit,
  onSaveShift: (EmployeeShift) -> Unit,
  onDeleteShift: (String) -> Unit,
  onExportShiftsCsv: () -> Unit
) {
  var selectedTab by remember { mutableIntStateOf(0) } // 0: Work Roster, 1: Employee Directory
  var showAddShiftDialog by remember { mutableStateOf(false) }
  var showAddUserDialog by remember { mutableStateOf(false) }
  var shiftToEdit by remember { mutableStateOf<EmployeeShift?>(null) }
  var userToEdit by remember { mutableStateOf<AppUser?>(null) }

  Scaffold(
    floatingActionButton = {
      if (canManageEmployees) {
        FloatingActionButton(
          onClick = {
            if (selectedTab == 0) showAddShiftDialog = true else showAddUserDialog = true
          },
          containerColor = RetailPrimary,
          contentColor = Color.White,
          modifier = Modifier.testTag("employee_shift_fab")
        ) {
          Icon(
            imageVector = Icons.Default.Add,
            contentDescription = if (selectedTab == 0) "Add Shift" else "Add Employee"
          )
        }
      }
    }
  ) { padding ->
    Column(
      modifier = Modifier
        .fillMaxSize()
        .padding(padding)
    ) {
      TabRow(selectedTabIndex = selectedTab) {
        Tab(
          selected = selectedTab == 0,
          onClick = { selectedTab = 0 },
          text = { Text("Work Roster (${shifts.size})") }
        )
        Tab(
          selected = selectedTab == 1,
          onClick = { selectedTab = 1 },
          text = { Text("Staff Directory (${users.size})") }
        )
      }

      if (selectedTab == 0) {
        // Work Roster Tab
        LazyColumn(
          modifier = Modifier.fillMaxSize(),
          contentPadding = PaddingValues(16.dp),
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
                  text = "Weekly Staff Schedule",
                  style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                )
                Text(
                  text = "Track shifts, duty assignments & break times",
                  style = MaterialTheme.typography.bodySmall,
                  color = MaterialTheme.colorScheme.onSurfaceVariant
                )
              }
              OutlinedButton(
                onClick = onExportShiftsCsv,
                contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp)
              ) {
                Icon(imageVector = Icons.Default.Share, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text("Export", fontSize = 12.sp)
              }
            }
          }

          if (shifts.isEmpty()) {
            item {
              Box(modifier = Modifier.padding(32.dp), contentAlignment = Alignment.Center) {
                Text("No shifts scheduled yet.", color = MaterialTheme.colorScheme.onSurfaceVariant)
              }
            }
          } else {
            items(shifts) { shift ->
              ShiftCard(
                shift = shift,
                canManage = canManageEmployees,
                onEdit = {
                  shiftToEdit = shift
                  showAddShiftDialog = true
                },
                onDelete = { onDeleteShift(shift.id) },
                onDuplicate = {
                  val dup = shift.copy(
                    id = UUID.randomUUID().toString(),
                    notes = "${shift.notes} (Copy)"
                  )
                  onSaveShift(dup)
                }
              )
            }
          }
        }
      } else {
        // Employee Directory Tab
        LazyColumn(
          modifier = Modifier.fillMaxSize(),
          contentPadding = PaddingValues(16.dp),
          verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
          item {
            Text(
              text = "Staff & Access Roles",
              style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
            )
            Text(
              text = "Manage staff profiles, store allocations, and role permissions",
              style = MaterialTheme.typography.bodySmall,
              color = MaterialTheme.colorScheme.onSurfaceVariant
            )
          }

          items(users) { user ->
            UserCard(
              user = user,
              canManage = canManageEmployees,
              onEdit = {
                userToEdit = user
                showAddUserDialog = true
              }
            )
          }
        }
      }
    }
  }

  // Add/Edit Shift Dialog with Overlap Conflict Detection (Requirement 27)
  if (showAddShiftDialog) {
    AddEditShiftDialog(
      shiftToEdit = shiftToEdit,
      users = users,
      stores = stores,
      existingShifts = shifts,
      onDismiss = {
        showAddShiftDialog = false
        shiftToEdit = null
      },
      onSave = { shift ->
        onSaveShift(shift)
        showAddShiftDialog = false
        shiftToEdit = null
      }
    )
  }

  // Add/Edit User Dialog
  if (showAddUserDialog) {
    AddEditUserDialog(
      userToEdit = userToEdit,
      stores = stores,
      onDismiss = {
        showAddUserDialog = false
        userToEdit = null
      },
      onSave = { user ->
        onSaveUser(user)
        showAddUserDialog = false
        userToEdit = null
      }
    )
  }
}

@Composable
fun ShiftCard(
  shift: EmployeeShift,
  canManage: Boolean,
  onEdit: () -> Unit,
  onDelete: () -> Unit,
  onDuplicate: () -> Unit
) {
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
        Row(verticalAlignment = Alignment.CenterVertically) {
          Box(
            modifier = Modifier
              .size(34.dp)
              .clip(CircleShape)
              .background(RetailPrimary.copy(alpha = 0.15f)),
            contentAlignment = Alignment.Center
          ) {
            Icon(imageVector = Icons.Default.Schedule, contentDescription = null, tint = RetailPrimary, modifier = Modifier.size(18.dp))
          }
          Spacer(modifier = Modifier.width(10.dp))
          Column {
            Text(
              text = shift.employeeName,
              style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold)
            )
            Text(
              text = shift.storeName,
              style = MaterialTheme.typography.bodySmall,
              color = MaterialTheme.colorScheme.onSurfaceVariant
            )
          }
        }

        StatusBadge(
          text = shift.shiftType,
          type = when (shift.shiftType) {
            "Morning" -> BadgeType.INFO
            "Afternoon" -> BadgeType.WARNING
            "Evening" -> BadgeType.NEUTRAL
            else -> BadgeType.SUCCESS
          }
        )
      }

      Spacer(modifier = Modifier.height(10.dp))

      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
      ) {
        Column {
          Text(text = "Date", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
          Text(text = shift.date, style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Medium))
        }
        Column {
          Text(text = "Hours", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
          Text(text = "${shift.startTime} – ${shift.endTime}", style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold))
        }
        Column(horizontalAlignment = Alignment.End) {
          Text(text = "Break", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
          Text(text = "${shift.breakDurationMinutes} min", style = MaterialTheme.typography.bodyMedium)
        }
      }

      if (shift.notes.isNotBlank()) {
        Spacer(modifier = Modifier.height(6.dp))
        Text(
          text = "Assignment: ${shift.notes}",
          style = MaterialTheme.typography.bodySmall,
          color = MaterialTheme.colorScheme.onSurfaceVariant
        )
      }

      if (canManage) {
        Spacer(modifier = Modifier.height(8.dp))
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.End
        ) {
          IconButton(onClick = onDuplicate) {
            Icon(imageVector = Icons.Default.ContentCopy, contentDescription = "Duplicate", tint = RetailPrimary, modifier = Modifier.size(18.dp))
          }
          IconButton(onClick = onEdit) {
            Icon(imageVector = Icons.Default.Edit, contentDescription = "Edit", tint = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.size(18.dp))
          }
          IconButton(onClick = onDelete) {
            Icon(imageVector = Icons.Default.Delete, contentDescription = "Delete", tint = StatusRed, modifier = Modifier.size(18.dp))
          }
        }
      }
    }
  }
}

@Composable
fun UserCard(
  user: AppUser,
  canManage: Boolean,
  onEdit: () -> Unit
) {
  Card(
    shape = RoundedCornerShape(12.dp),
    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
    elevation = CardDefaults.cardElevation(defaultElevation = 0.5.dp),
    modifier = Modifier.fillMaxWidth()
  ) {
    Row(
      modifier = Modifier
        .fillMaxWidth()
        .padding(14.dp),
      verticalAlignment = Alignment.CenterVertically
    ) {
      Box(
        modifier = Modifier
          .size(42.dp)
          .clip(CircleShape)
          .background(RetailPrimary.copy(alpha = 0.2f)),
        contentAlignment = Alignment.Center
      ) {
        Text(
          text = user.name.take(2).uppercase(),
          fontWeight = FontWeight.Bold,
          color = RetailPrimary
        )
      }

      Spacer(modifier = Modifier.width(12.dp))

      Column(modifier = Modifier.weight(1f)) {
        Text(
          text = user.name,
          style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold)
        )
        Text(
          text = "${user.employeeCode} • ${user.role.displayName}",
          style = MaterialTheme.typography.bodySmall,
          color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
          text = "${user.email} • ${user.phone}",
          style = MaterialTheme.typography.labelSmall,
          color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f)
        )
      }

      if (canManage) {
        IconButton(onClick = onEdit) {
          Icon(imageVector = Icons.Default.Edit, contentDescription = "Edit", tint = RetailPrimary, modifier = Modifier.size(18.dp))
        }
      }
    }
  }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddEditShiftDialog(
  shiftToEdit: EmployeeShift?,
  users: List<AppUser>,
  stores: List<Store>,
  existingShifts: List<EmployeeShift>,
  onDismiss: () -> Unit,
  onSave: (EmployeeShift) -> Unit
) {
  val df = SimpleDateFormat("yyyy-MM-dd", Locale.US)
  val todayStr = df.format(Date())

  var selectedEmployeeId by remember { mutableStateOf(shiftToEdit?.employeeId ?: users.firstOrNull()?.id ?: "") }
  var selectedStoreId by remember { mutableStateOf(shiftToEdit?.storeId ?: stores.firstOrNull()?.id ?: "") }
  var dateStr by remember { mutableStateOf(shiftToEdit?.date ?: todayStr) }
  var startTime by remember { mutableStateOf(shiftToEdit?.startTime ?: "09:00") }
  var endTime by remember { mutableStateOf(shiftToEdit?.endTime ?: "17:30") }
  var breakMinutesStr by remember { mutableStateOf(shiftToEdit?.breakDurationMinutes?.toString() ?: "45") }
  var shiftType by remember { mutableStateOf(shiftToEdit?.shiftType ?: "Morning") }
  var notes by remember { mutableStateOf(shiftToEdit?.notes ?: "Floor & Inventory") }

  var empDropdownExpanded by remember { mutableStateOf(false) }
  var storeDropdownExpanded by remember { mutableStateOf(false) }

  // Overlap Conflict Detection (Requirement 27)
  val hasConflict = existingShifts.any { existing ->
    existing.id != shiftToEdit?.id &&
      existing.employeeId == selectedEmployeeId &&
      existing.date == dateStr &&
      !(endTime <= existing.startTime || startTime >= existing.endTime)
  }

  AlertDialog(
    onDismissRequest = onDismiss,
    title = { Text(if (shiftToEdit == null) "Schedule Shift" else "Edit Shift") },
    text = {
      Column(
        modifier = Modifier
          .fillMaxWidth()
          .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(8.dp)
      ) {
        if (hasConflict) {
          Card(
            shape = RoundedCornerShape(8.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer),
            modifier = Modifier.fillMaxWidth()
          ) {
            Row(modifier = Modifier.padding(10.dp), verticalAlignment = Alignment.CenterVertically) {
              Icon(imageVector = Icons.Default.Warning, contentDescription = null, tint = MaterialTheme.colorScheme.onErrorContainer)
              Spacer(modifier = Modifier.width(8.dp))
              Text(
                text = "Scheduling Conflict Detected! This employee already has an overlapping shift on $dateStr.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onErrorContainer
              )
            }
          }
        }

        // Employee dropdown
        ExposedDropdownMenuBox(
          expanded = empDropdownExpanded,
          onExpandedChange = { empDropdownExpanded = !empDropdownExpanded }
        ) {
          val empName = users.firstOrNull { it.id == selectedEmployeeId }?.name ?: "Select Employee"
          OutlinedTextField(
            value = empName,
            onValueChange = {},
            readOnly = true,
            label = { Text("Employee") },
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = empDropdownExpanded) },
            modifier = Modifier
              .menuAnchor()
              .fillMaxWidth()
          )
          ExposedDropdownMenu(
            expanded = empDropdownExpanded,
            onDismissRequest = { empDropdownExpanded = false }
          ) {
            users.forEach { u ->
              DropdownMenuItem(
                text = { Text("${u.name} (${u.role.displayName.substringBefore(" /")})") },
                onClick = {
                  selectedEmployeeId = u.id
                  empDropdownExpanded = false
                }
              )
            }
          }
        }

        // Store dropdown
        ExposedDropdownMenuBox(
          expanded = storeDropdownExpanded,
          onExpandedChange = { storeDropdownExpanded = !storeDropdownExpanded }
        ) {
          val storeName = stores.firstOrNull { it.id == selectedStoreId }?.name ?: "Select Store"
          OutlinedTextField(
            value = storeName,
            onValueChange = {},
            readOnly = true,
            label = { Text("Assigned Store") },
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = storeDropdownExpanded) },
            modifier = Modifier
              .menuAnchor()
              .fillMaxWidth()
          )
          ExposedDropdownMenu(
            expanded = storeDropdownExpanded,
            onDismissRequest = { storeDropdownExpanded = false }
          ) {
            stores.forEach { s ->
              DropdownMenuItem(
                text = { Text(s.name) },
                onClick = {
                  selectedStoreId = s.id
                  storeDropdownExpanded = false
                }
              )
            }
          }
        }

        OutlinedTextField(
          value = dateStr,
          onValueChange = { dateStr = it },
          label = { Text("Date (YYYY-MM-DD)") },
          modifier = Modifier.fillMaxWidth()
        )

        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
          OutlinedTextField(
            value = startTime,
            onValueChange = { startTime = it },
            label = { Text("Start Time") },
            modifier = Modifier.weight(1f)
          )
          OutlinedTextField(
            value = endTime,
            onValueChange = { endTime = it },
            label = { Text("End Time") },
            modifier = Modifier.weight(1f)
          )
        }

        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
          OutlinedTextField(
            value = shiftType,
            onValueChange = { shiftType = it },
            label = { Text("Shift (e.g. Morning, Afternoon)") },
            modifier = Modifier.weight(1f)
          )
          OutlinedTextField(
            value = breakMinutesStr,
            onValueChange = { breakMinutesStr = it },
            label = { Text("Break (min)") },
            modifier = Modifier.weight(1f)
          )
        }

        OutlinedTextField(
          value = notes,
          onValueChange = { notes = it },
          label = { Text("Notes / Duties") },
          modifier = Modifier.fillMaxWidth()
        )
      }
    },
    confirmButton = {
      Button(
        onClick = {
          val emp = users.firstOrNull { it.id == selectedEmployeeId }
          val str = stores.firstOrNull { it.id == selectedStoreId }
          if (emp != null && str != null) {
            val s = (shiftToEdit ?: EmployeeShift(
              id = UUID.randomUUID().toString(),
              employeeId = emp.id,
              employeeName = emp.name,
              storeId = str.id,
              storeName = str.name
            )).copy(
              employeeId = emp.id,
              employeeName = emp.name,
              storeId = str.id,
              storeName = str.name,
              date = dateStr,
              startTime = startTime,
              endTime = endTime,
              breakDurationMinutes = breakMinutesStr.toIntOrNull() ?: 45,
              shiftType = shiftType,
              notes = notes
            )
            onSave(s)
          }
        }
      ) {
        Text("Save Shift")
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
fun AddEditUserDialog(
  userToEdit: AppUser?,
  stores: List<Store>,
  onDismiss: () -> Unit,
  onSave: (AppUser) -> Unit
) {
  var name by remember { mutableStateOf(userToEdit?.name ?: "") }
  var email by remember { mutableStateOf(userToEdit?.email ?: "") }
  var phone by remember { mutableStateOf(userToEdit?.phone ?: "+1 555-") }
  var empCode by remember { mutableStateOf(userToEdit?.employeeCode ?: "EMP-00${(10..99).random()}") }
  var selectedRole by remember { mutableStateOf(userToEdit?.role ?: UserRole.EMPLOYEE) }

  AlertDialog(
    onDismissRequest = onDismiss,
    title = { Text(if (userToEdit == null) "Add Staff Member" else "Edit Staff") },
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
          label = { Text("Full Name *") },
          modifier = Modifier.fillMaxWidth()
        )
        OutlinedTextField(
          value = empCode,
          onValueChange = { empCode = it },
          label = { Text("Employee ID / Code") },
          modifier = Modifier.fillMaxWidth()
        )
        OutlinedTextField(
          value = email,
          onValueChange = { email = it },
          label = { Text("Email Address") },
          modifier = Modifier.fillMaxWidth()
        )
        OutlinedTextField(
          value = phone,
          onValueChange = { phone = it },
          label = { Text("Phone Number") },
          modifier = Modifier.fillMaxWidth()
        )

        Text("Access Role:", style = MaterialTheme.typography.labelMedium)
        UserRole.values().forEach { role ->
          Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
              .fillMaxWidth()
              .clickable { selectedRole = role }
          ) {
            androidx.compose.material3.RadioButton(
              selected = selectedRole == role,
              onClick = { selectedRole = role }
            )
            Spacer(modifier = Modifier.width(4.dp))
            Text(role.displayName, style = MaterialTheme.typography.bodySmall)
          }
        }
      }
    },
    confirmButton = {
      Button(
        onClick = {
          if (name.isNotBlank()) {
            val u = (userToEdit ?: AppUser(
              id = UUID.randomUUID().toString(),
              name = name,
              employeeCode = empCode
            )).copy(
              name = name,
              employeeCode = empCode,
              email = email,
              phone = phone,
              role = selectedRole
            )
            onSave(u)
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
