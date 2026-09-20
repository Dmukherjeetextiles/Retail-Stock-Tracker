package com.example.ui

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Assessment
import androidx.compose.material.icons.filled.Business
import androidx.compose.material.icons.filled.Dashboard
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Inventory
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.MoreHoriz
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.People
import androidx.compose.material.icons.filled.QrCodeScanner
import androidx.compose.material.icons.filled.Receipt
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.data.model.Product
import com.example.data.model.Store
import com.example.data.model.TransactionType
import com.example.ui.components.RetailTopAppBar
import com.example.ui.components.UserSwitchDialog
import com.example.ui.export.ExportHelper
import com.example.ui.screens.AddEditProductDialog
import com.example.ui.screens.AuditLogScreen
import com.example.ui.screens.BarcodeScannerScreen
import com.example.ui.screens.DashboardScreen
import com.example.ui.screens.EmployeeShiftScreen
import com.example.ui.screens.ExpirationManagementScreen
import com.example.ui.screens.InventoryScreen
import com.example.ui.screens.InvoiceOcrScreen
import com.example.ui.screens.MoreHubScreen
import com.example.ui.screens.NotificationsScreen
import com.example.ui.screens.ProductDetailDialog
import com.example.ui.screens.ReportsScreen
import com.example.ui.screens.StockAdjustmentDialog
import com.example.ui.screens.StockTransferScreen
import com.example.ui.screens.StoreManagementScreen
import com.example.ui.theme.RetailPrimary
import com.example.ui.theme.StatusAmber
import com.example.ui.theme.StatusGreen
import com.example.ui.viewmodel.RetailViewModel

@Composable
fun MainApp(viewModel: RetailViewModel = viewModel()) {
  val context = LocalContext.current

  // State collectors
  val stores by viewModel.stores.collectAsState()
  val users by viewModel.users.collectAsState()
  val products by viewModel.products.collectAsState()
  val storeStocks by viewModel.storeStocks.collectAsState()
  val transactions by viewModel.transactions.collectAsState()
  val transfers by viewModel.transfers.collectAsState()
  val shifts by viewModel.shifts.collectAsState()
  val notifications by viewModel.notifications.collectAsState()
  val auditLogs by viewModel.auditLogs.collectAsState()

  val selectedStoreId by viewModel.selectedStoreId.collectAsState()
  val currentUser by viewModel.currentUser.collectAsState()
  val productStockItems by viewModel.productStockItems.collectAsState()
  val dashboardMetrics by viewModel.dashboardMetrics.collectAsState()
  val salesAnalytics by viewModel.salesAnalytics.collectAsState()
  val dateTimeFilter by viewModel.dateTimeFilter.collectAsState()
  val filteredTransactions by viewModel.filteredTransactions.collectAsState()

  val scannedBarcode by viewModel.scannedBarcode.collectAsState()
  val scannedProduct by viewModel.scannedProduct.collectAsState()
  val barcodeNotFound by viewModel.barcodeNotFound.collectAsState()
  val invoiceReviewState by viewModel.invoiceReviewState.collectAsState()

  // Navigation route: dashboard, inventory, scan, reports, more, stores, staff, transfers, invoice_ocr, expiry, audit, notifications
  var currentRoute by remember { mutableStateOf("dashboard") }

  // Dialog states
  var showUserSwitchDialog by remember { mutableStateOf(false) }
  var productForDetail by remember { mutableStateOf<Product?>(null) }
  var productForAdjustment by remember { mutableStateOf<Product?>(null) }
  var productForEdit by remember { mutableStateOf<Product?>(null) }
  var initialBarcodeForAdd by remember { mutableStateOf("") }
  var showAddProductDialog by remember { mutableStateOf(false) }

  val unreadNotifications = notifications.count { !it.isRead }
  val selectedStoreName = if (selectedStoreId == "ALL") {
    "All Alimentation Stores"
  } else {
    stores.firstOrNull { it.id == selectedStoreId }?.name ?: "All Alimentation Stores"
  }

  Scaffold(
    topBar = {
      RetailTopAppBar(
        stores = stores,
        selectedStoreId = selectedStoreId,
        currentUser = currentUser,
        unreadNotificationsCount = unreadNotifications,
        onSelectStore = { viewModel.selectStore(it) },
        onOpenNotifications = { currentRoute = "notifications" },
        onOpenUserSwitchDialog = { showUserSwitchDialog = true }
      )
    },
    bottomBar = {
      NavigationBar(
        containerColor = MaterialTheme.colorScheme.surface,
        tonalElevation = 8.dp
      ) {
        // 1. Dashboard
        NavigationBarItem(
          selected = currentRoute == "dashboard",
          onClick = { currentRoute = "dashboard" },
          icon = { Icon(Icons.Default.Dashboard, contentDescription = "Dashboard") },
          label = { Text("Dashboard") },
          modifier = Modifier.testTag("nav_dashboard")
        )

        // 2. Inventory
        NavigationBarItem(
          selected = currentRoute == "inventory",
          onClick = { currentRoute = "inventory" },
          icon = { Icon(Icons.Default.Inventory, contentDescription = "Inventory") },
          label = { Text("Inventory") },
          modifier = Modifier.testTag("nav_inventory")
        )

        // 3. Scan (Prominent Center Action)
        NavigationBarItem(
          selected = currentRoute == "scan",
          onClick = { currentRoute = "scan" },
          icon = {
            Box(
              modifier = Modifier
                .size(38.dp)
                .clip(CircleShape)
                .background(RetailPrimary),
              contentAlignment = Alignment.Center
            ) {
              Icon(
                Icons.Default.QrCodeScanner,
                contentDescription = "Scan",
                tint = Color.White,
                modifier = Modifier.size(20.dp)
              )
            }
          },
          label = { Text("Scan", fontWeight = FontWeight.Bold) },
          modifier = Modifier.testTag("nav_scan")
        )

        // 4. Reports
        NavigationBarItem(
          selected = currentRoute == "reports",
          onClick = { currentRoute = "reports" },
          icon = { Icon(Icons.Default.TrendingUp, contentDescription = "Reports") },
          label = { Text("Reports") },
          modifier = Modifier.testTag("nav_reports")
        )

        // 5. More
        NavigationBarItem(
          selected = currentRoute in listOf("more", "stores", "staff", "transfers", "invoice_ocr", "expiry", "audit", "notifications"),
          onClick = { currentRoute = "more" },
          icon = { Icon(Icons.Default.MoreHoriz, contentDescription = "More") },
          label = { Text("More") },
          modifier = Modifier.testTag("nav_more")
        )
      }
    }
  ) { innerPadding ->
    Box(
      modifier = Modifier
        .fillMaxSize()
        .padding(innerPadding)
    ) {
      when (currentRoute) {
        "dashboard" -> {
          DashboardScreen(
            metrics = dashboardMetrics,
            currentUser = currentUser,
            selectedStoreName = selectedStoreName,
            dateTimeFilter = dateTimeFilter,
            onSelectPreset = { viewModel.setDateFilterPreset(it) },
            onApplyCustom = { start, end, label -> viewModel.setCustomDateTimeFilter(start, end, label) },
            onNavigateToScan = { currentRoute = "scan" },
            onNavigateToInventory = { currentRoute = "inventory" },
            onNavigateToReceive = {
              val firstProd = products.firstOrNull()
              if (firstProd != null) productForAdjustment = firstProd
            },
            onNavigateToInvoiceOcr = { currentRoute = "invoice_ocr" },
            onNavigateToTransfer = { currentRoute = "transfers" },
            onNavigateToAddProduct = {
              initialBarcodeForAdd = ""
              productForEdit = null
              showAddProductDialog = true
            },
            onNavigateToAlerts = { currentRoute = "expiry" },
            onNavigateToReports = { currentRoute = "reports" },
            onNavigateToStaff = { currentRoute = "staff" }
          )
        }

        "inventory" -> {
          InventoryScreen(
            items = productStockItems,
            onProductClick = { productForDetail = it },
            onAddProductClick = {
              initialBarcodeForAdd = ""
              productForEdit = null
              showAddProductDialog = true
            },
            onScanClick = { currentRoute = "scan" }
          )
        }

        "scan" -> {
          BarcodeScannerScreen(
            products = products,
            stocks = storeStocks,
            scannedProduct = scannedProduct,
            barcodeNotFound = barcodeNotFound,
            selectedStoreId = selectedStoreId,
            onScanBarcode = { viewModel.onScanBarcode(it) },
            onClearScan = { viewModel.clearScannedBarcode() },
            onQuickAdjust = { prod, delta, type ->
              val storeId = if (selectedStoreId != "ALL") selectedStoreId else "store-nini"
              viewModel.adjustStock(
                storeId = storeId,
                productId = prod.id,
                quantityChange = delta,
                type = type,
                notes = if (delta > 0) "Quick barcode receive intake" else "Quick point-of-sale checkout",
                onSuccess = {
                  Toast.makeText(context, "${prod.name}: ${if (delta > 0) "+$delta" else "$delta"}", Toast.LENGTH_SHORT).show()
                }
              )
            },
            onOpenAdjustStock = { productForAdjustment = it },
            onOpenTransfer = {
              currentRoute = "transfers"
            },
            onOpenProductDetail = { productForDetail = it },
            onAddNewProductWithBarcode = { barcode ->
              initialBarcodeForAdd = barcode
              productForEdit = null
              showAddProductDialog = true
            }
          )
        }

        "reports" -> {
          ReportsScreen(
            salesAnalytics = salesAnalytics,
            transactions = filteredTransactions,
            products = products,
            stocks = storeStocks,
            selectedStoreName = selectedStoreName,
            dateTimeFilter = dateTimeFilter,
            onSelectPreset = { viewModel.setDateFilterPreset(it) },
            onApplyCustom = { start, end, label -> viewModel.setCustomDateTimeFilter(start, end, label) },
            onExportInventoryCsv = {
              ExportHelper.exportInventoryCsv(context, products, storeStocks, selectedStoreName)
            },
            onExportMovementsCsv = {
              ExportHelper.exportStockMovementsCsv(context, filteredTransactions, selectedStoreName)
            }
          )
        }

        "more" -> {
          MoreHubScreen(
            stores = stores,
            currentUser = currentUser,
            notifications = notifications,
            auditLogs = auditLogs,
            onNavigateToStores = { currentRoute = "stores" },
            onNavigateToStaff = { currentRoute = "staff" },
            onNavigateToTransfers = { currentRoute = "transfers" },
            onNavigateToInvoiceOcr = { currentRoute = "invoice_ocr" },
            onNavigateToExpiry = { currentRoute = "expiry" },
            onNavigateToAudit = { currentRoute = "audit" },
            onNavigateToNotifications = { currentRoute = "notifications" },
            onExportInventory = {
              ExportHelper.exportInventoryCsv(context, products, storeStocks, selectedStoreName)
            },
            onExportMovements = {
              ExportHelper.exportStockMovementsCsv(context, transactions, selectedStoreName)
            },
            onExportRoster = {
              ExportHelper.exportShiftsCsv(context, shifts, selectedStoreName)
            },
            onOpenRoleSwitcher = { showUserSwitchDialog = true }
          )
        }

        "stores" -> {
          StoreManagementScreen(
            stores = stores,
            canManageStores = viewModel.canManageGlobalSettings(),
            onSaveStore = { store ->
              viewModel.saveStore(store) {
                Toast.makeText(context, "Store saved successfully", Toast.LENGTH_SHORT).show()
              }
            }
          )
        }

        "staff" -> {
          EmployeeShiftScreen(
            users = users,
            shifts = shifts,
            stores = stores,
            currentUser = currentUser,
            canManageEmployees = viewModel.canManageEmployees(),
            onSaveUser = { user ->
              viewModel.saveUser(user) {
                Toast.makeText(context, "Staff member updated", Toast.LENGTH_SHORT).show()
              }
            },
            onSaveShift = { shift ->
              viewModel.saveShift(shift) {
                Toast.makeText(context, "Shift scheduled", Toast.LENGTH_SHORT).show()
              }
            },
            onDeleteShift = { shiftId ->
              viewModel.deleteShift(shiftId) {
                Toast.makeText(context, "Shift removed", Toast.LENGTH_SHORT).show()
              }
            },
            onExportShiftsCsv = {
              ExportHelper.exportShiftsCsv(context, shifts, selectedStoreName)
            }
          )
        }

        "transfers" -> {
          StockTransferScreen(
            stores = stores,
            products = products,
            stocks = storeStocks,
            transfers = transfers,
            canTransferStock = viewModel.canTransferStock(),
            onInitiateTransfer = { sourceId, destId, prodId, qty, notes ->
              viewModel.initiateTransfer(sourceId, destId, prodId, qty, notes) {
                Toast.makeText(context, "Transfer dispatched (In Transit)", Toast.LENGTH_SHORT).show()
              }
            },
            onCompleteTransfer = { transferId ->
              viewModel.completeTransfer(transferId) {
                Toast.makeText(context, "Transfer marked received & stock credited", Toast.LENGTH_SHORT).show()
              }
            }
          )
        }

        "invoice_ocr" -> {
          InvoiceOcrScreen(
            reviewState = invoiceReviewState,
            onStartOcr = { templateIdx ->
              viewModel.startInvoiceOcrProcessing(templateIdx)
            },
            onUpdateItem = { item -> viewModel.updateInvoiceItem(item) },
            onRemoveItem = { id -> viewModel.removeInvoiceItem(id) },
            onAddItem = { name, qty, price, barcode ->
              viewModel.addInvoiceItem(name, qty, price, barcode)
            },
            onApproveInvoice = {
              viewModel.approveAndCommitInvoice {
                Toast.makeText(context, "Invoice approved & stock updated!", Toast.LENGTH_LONG).show()
              }
            },
            onClearInvoice = { viewModel.clearInvoiceReview() }
          )
        }

        "expiry" -> {
          ExpirationManagementScreen(
            items = productStockItems,
            selectedStoreId = selectedStoreId,
            canConfirmDisposal = viewModel.canConfirmDisposal(),
            onConfirmDisposal = { productId, storeId, qty, reason ->
              viewModel.disposeExpiredStock(storeId, productId, qty, reason) {
                Toast.makeText(context, "Disposal recorded and stock deducted", Toast.LENGTH_SHORT).show()
              }
            }
          )
        }

        "audit" -> {
          AuditLogScreen(auditLogs = auditLogs)
        }

        "notifications" -> {
          NotificationsScreen(
            notifications = notifications,
            onMarkAsRead = { viewModel.markNotificationAsRead(it) },
            onMarkAllAsRead = { viewModel.markAllNotificationsAsRead() },
            onClearAll = { viewModel.clearNotifications() }
          )
        }
      }
    }
  }

  // --- Modals and Dialogs ---

  // 1. Role / User Switcher Dialog
  if (showUserSwitchDialog) {
    UserSwitchDialog(
      users = users,
      currentUser = currentUser,
      onDismiss = { showUserSwitchDialog = false },
      onSwitchUser = { viewModel.switchUser(it) },
      onSwitchRole = { viewModel.switchRole(it) }
    )
  }

  // 2. Product Detail Dialog
  if (productForDetail != null) {
    ProductDetailDialog(
      product = productForDetail!!,
      stores = stores,
      stocks = storeStocks,
      canManageInventory = viewModel.canAdjustStock(),
      onDismiss = { productForDetail = null },
      onAdjustStockClick = {
        productForAdjustment = it
      },
      onTransferClick = {
        currentRoute = "transfers"
      },
      onEditProductClick = {
        productForEdit = it
        showAddProductDialog = true
      }
    )
  }

  // 3. Stock Adjustment Dialog
  if (productForAdjustment != null) {
    StockAdjustmentDialog(
      product = productForAdjustment!!,
      stores = stores,
      stocks = storeStocks,
      initialStoreId = selectedStoreId,
      onDismiss = { productForAdjustment = null },
      onConfirm = { storeId, productId, delta, type, notes ->
        viewModel.adjustStock(storeId, productId, delta, type, notes) {
          Toast.makeText(context, "Stock adjusted successfully", Toast.LENGTH_SHORT).show()
          productForAdjustment = null
        }
      }
    )
  }

  // 4. Add / Edit Product Dialog
  if (showAddProductDialog) {
    AddEditProductDialog(
      productToEdit = productForEdit,
      initialBarcode = initialBarcodeForAdd,
      onDismiss = {
        showAddProductDialog = false
        productForEdit = null
        initialBarcodeForAdd = ""
      },
      onSave = { product ->
        viewModel.saveProduct(product) {
          Toast.makeText(context, "Product saved successfully", Toast.LENGTH_SHORT).show()
          showAddProductDialog = false
          productForEdit = null
          initialBarcodeForAdd = ""
        }
      }
    )
  }
}
