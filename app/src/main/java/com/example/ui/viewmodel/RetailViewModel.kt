package com.example.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.database.AppDatabase
import com.example.data.model.AppNotification
import com.example.data.model.AppUser
import com.example.data.model.AuditLog
import com.example.data.model.DateFilterPreset
import com.example.data.model.DateTimeFilter
import com.example.data.model.DateTimeFilterUtils
import com.example.data.model.EmployeeShift
import com.example.data.model.InvoiceItem
import com.example.data.model.Product
import com.example.data.model.PurchaseInvoice
import com.example.data.model.StockTransaction
import com.example.data.model.StockTransfer
import com.example.data.model.Store
import com.example.data.model.StoreStock
import com.example.data.model.TransactionType
import com.example.data.model.UserRole
import com.example.data.repository.RetailRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import java.util.UUID

data class DashboardMetrics(
  val totalProducts: Int = 0,
  val totalUnits: Int = 0,
  val totalValue: Double = 0.0,
  val lowStockCount: Int = 0,
  val outOfStockCount: Int = 0,
  val expiringSoonCount: Int = 0,
  val expiredCount: Int = 0,
  val receivedTodayCount: Int = 0,
  val soldTodayCount: Int = 0,
  val salesTodayAmount: Double = 0.0,
  val recentTransactions: List<StockTransaction> = emptyList(),
  val dateFilterLabel: String = "Today",
  val dateFilterRangeText: String = "Today (Full Day)",
  val periodReceivedCount: Int = 0,
  val periodSoldCount: Int = 0,
  val periodSalesAmount: Double = 0.0,
  val periodTransactionCount: Int = 0
)

data class ProductStockItem(
  val product: Product,
  val stock: StoreStock?,
  val totalStockAcrossStores: Int,
  val isLowStock: Boolean,
  val isOutOfStock: Boolean,
  val isExpiringSoon: Boolean,
  val isExpired: Boolean,
  val daysUntilExpiry: Int?
)

data class SalesAnalytics(
  val totalSalesRevenue: Double = 0.0,
  val totalUnitsSold: Int = 0,
  val totalOrders: Int = 0,
  val averageTicket: Double = 0.0,
  val categorySales: Map<String, Double> = emptyMap(),
  val topSellingProducts: List<Pair<String, Int>> = emptyList(),
  val dateFilterLabel: String = "Today",
  val dateFilterRangeText: String = "Today (Full Day)"
)

class RetailViewModel(application: Application) : AndroidViewModel(application) {

  private val repository: RetailRepository

  init {
    val db = AppDatabase.getDatabase(application)
    repository = RetailRepository(db)
    viewModelScope.launch {
      repository.initializeSeedDataIfNeeded()
    }
  }

  // Selected Store filter: "ALL" or specific store ID
  private val _selectedStoreId = MutableStateFlow("ALL")
  val selectedStoreId: StateFlow<String> = _selectedStoreId.asStateFlow()

  // Date & Time Filter state
  private val _dateTimeFilter = MutableStateFlow(DateTimeFilterUtils.createPresetFilter(DateFilterPreset.TODAY))
  val dateTimeFilter: StateFlow<DateTimeFilter> = _dateTimeFilter.asStateFlow()

  fun setDateFilterPreset(preset: DateFilterPreset) {
    _dateTimeFilter.value = DateTimeFilterUtils.createPresetFilter(preset)
  }

  fun setCustomDateTimeFilter(startTimestamp: Long, endTimestamp: Long, customLabel: String = "") {
    _dateTimeFilter.value = DateTimeFilterUtils.createCustomFilter(startTimestamp, endTimestamp, customLabel)
  }

  // Current active user
  private val _currentUser = MutableStateFlow(
    AppUser(
      id = "user-1",
      name = "Alexander Wright",
      employeeCode = "EMP-001",
      email = "alex@retailflow.com",
      phone = "+1 555-1001",
      role = UserRole.OWNER,
      assignedStoreId = "ALL"
    )
  )
  val currentUser: StateFlow<AppUser> = _currentUser.asStateFlow()

  // Base Flows
  val stores: StateFlow<List<Store>> = repository.stores.stateIn(
    viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList()
  )

  val users: StateFlow<List<AppUser>> = repository.users.stateIn(
    viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList()
  )

  val products: StateFlow<List<Product>> = repository.products.stateIn(
    viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList()
  )

  val storeStocks: StateFlow<List<StoreStock>> = repository.storeStocks.stateIn(
    viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList()
  )

  val transactions: StateFlow<List<StockTransaction>> = repository.transactions.stateIn(
    viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList()
  )

  // Transactions filtered by both Store and Date/Time filter
  val filteredTransactions: StateFlow<List<StockTransaction>> = combine(
    transactions, selectedStoreId, dateTimeFilter
  ) { allTx, storeId, filter ->
    allTx.filter { tx ->
      (storeId == "ALL" || tx.storeId == storeId) &&
      (tx.timestamp in filter.startTimestamp..filter.endTimestamp)
    }
  }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

  val transfers: StateFlow<List<StockTransfer>> = repository.transfers.stateIn(
    viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList()
  )

  val invoices: StateFlow<List<PurchaseInvoice>> = repository.invoices.stateIn(
    viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList()
  )

  val shifts: StateFlow<List<EmployeeShift>> = repository.shifts.stateIn(
    viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList()
  )

  val notifications: StateFlow<List<AppNotification>> = repository.notifications.stateIn(
    viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList()
  )

  val auditLogs: StateFlow<List<AuditLog>> = repository.auditLogs.stateIn(
    viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList()
  )

  // Derived: Combined Product Stock Item list
  val productStockItems: StateFlow<List<ProductStockItem>> = combine(
    products, storeStocks, selectedStoreId
  ) { prods, stocks, storeId ->
    val df = SimpleDateFormat("yyyy-MM-dd", Locale.US)
    val todayCal = Calendar.getInstance()

    prods.map { prod ->
      val relevantStocks = if (storeId == "ALL") {
        stocks.filter { it.productId == prod.id }
      } else {
        stocks.filter { it.productId == prod.id && it.storeId == storeId }
      }

      val totalQty = relevantStocks.sumOf { it.quantity }
      val totalExpired = relevantStocks.sumOf { it.expiredQuantity }
      val primaryStock = relevantStocks.firstOrNull()

      var daysUntilExp: Int? = null
      var isExpSoon = false
      var isExp = false

      if (prod.isPerishable && prod.expirationDate.isNotBlank()) {
        try {
          val expDate = df.parse(prod.expirationDate)
          if (expDate != null) {
            val diffMs = expDate.time - todayCal.timeInMillis
            val days = (diffMs / (1000 * 60 * 60 * 24)).toInt()
            daysUntilExp = days
            if (days < 0 || totalExpired > 0) {
              isExp = true
            } else if (days <= 7) {
              isExpSoon = true
            }
          }
        } catch (_: Exception) {}
      }

      ProductStockItem(
        product = prod,
        stock = primaryStock,
        totalStockAcrossStores = totalQty,
        isLowStock = totalQty in 1..prod.minStockLevel,
        isOutOfStock = totalQty == 0,
        isExpiringSoon = isExpSoon,
        isExpired = isExp,
        daysUntilExpiry = daysUntilExp
      )
    }
  }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

  // Dashboard Metrics Combined Flow
  val dashboardMetrics: StateFlow<DashboardMetrics> = combine(
    productStockItems, filteredTransactions, dateTimeFilter, products
  ) { items, periodTx, filter, prods ->
    val prodMap = prods.associateBy { it.id }
    val receivedCount = periodTx.filter { it.transactionType == TransactionType.RECEIVED.name }.sumOf { it.quantity }
    val soldTx = periodTx.filter { it.transactionType == TransactionType.SOLD.name }
    val soldCount = soldTx.sumOf { -it.quantity }
    val salesAmount = soldTx.sumOf { tx ->
      val p = prodMap[tx.productId]
      val price = p?.sellingPrice ?: 2.50
      (-tx.quantity) * price
    }

    var totalUnits = 0
    var totalVal = 0.0
    var lowCount = 0
    var outCount = 0
    var expSoon = 0
    var expired = 0

    items.forEach { item ->
      totalUnits += item.totalStockAcrossStores
      totalVal += item.totalStockAcrossStores * item.product.sellingPrice
      if (item.isOutOfStock) outCount++
      else if (item.isLowStock) lowCount++
      if (item.isExpired) expired++
      else if (item.isExpiringSoon) expSoon++
    }

    DashboardMetrics(
      totalProducts = items.size,
      totalUnits = totalUnits,
      totalValue = totalVal,
      lowStockCount = lowCount,
      outOfStockCount = outCount,
      expiringSoonCount = expSoon,
      expiredCount = expired,
      receivedTodayCount = receivedCount,
      soldTodayCount = soldCount,
      salesTodayAmount = salesAmount,
      recentTransactions = periodTx.take(8),
      dateFilterLabel = filter.displayLabel,
      dateFilterRangeText = filter.rangeSummary,
      periodReceivedCount = receivedCount,
      periodSoldCount = soldCount,
      periodSalesAmount = salesAmount,
      periodTransactionCount = periodTx.size
    )
  }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), DashboardMetrics())

  // Sales Analytics Flow
  val salesAnalytics: StateFlow<SalesAnalytics> = combine(
    filteredTransactions, products, dateTimeFilter
  ) { periodTx, prods, filter ->
    val prodMap = prods.associateBy { it.id }
    val salesTx = periodTx.filter {
      it.transactionType == TransactionType.SOLD.name
    }

    val totalSoldUnits = salesTx.sumOf { -it.quantity }
    val totalOrders = salesTx.size
    var totalRev = 0.0
    val catMap = mutableMapOf<String, Double>()
    val prodSalesMap = mutableMapOf<String, Int>()

    salesTx.forEach { tx ->
      val p = prodMap[tx.productId]
      val price = p?.sellingPrice ?: 2.50
      val itemUnits = -tx.quantity
      val itemRev = itemUnits * price
      totalRev += itemRev

      val cat = p?.category ?: "Alimentation"
      catMap[cat] = (catMap[cat] ?: 0.0) + itemRev

      val pName = p?.name ?: tx.productName
      prodSalesMap[pName] = (prodSalesMap[pName] ?: 0) + itemUnits
    }

    SalesAnalytics(
      totalSalesRevenue = totalRev,
      totalUnitsSold = totalSoldUnits,
      totalOrders = totalOrders,
      averageTicket = if (totalOrders > 0) totalRev / totalOrders else 0.0,
      categorySales = catMap,
      topSellingProducts = prodSalesMap.entries.sortedByDescending { it.value }.map { it.key to it.value },
      dateFilterLabel = filter.displayLabel,
      dateFilterRangeText = filter.rangeSummary
    )
  }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), SalesAnalytics())

  // --- Barcode Scanner State ---
  private val _scannedBarcode = MutableStateFlow<String?>(null)
  val scannedBarcode: StateFlow<String?> = _scannedBarcode.asStateFlow()

  private val _scannedProduct = MutableStateFlow<Product?>(null)
  val scannedProduct: StateFlow<Product?> = _scannedProduct.asStateFlow()

  private val _barcodeNotFound = MutableStateFlow<String?>(null)
  val barcodeNotFound: StateFlow<String?> = _barcodeNotFound.asStateFlow()

  fun onScanBarcode(barcode: String) {
    _scannedBarcode.value = barcode
    val found = products.value.firstOrNull { it.barcode.trim() == barcode.trim() }
    if (found != null) {
      _scannedProduct.value = found
      _barcodeNotFound.value = null
    } else {
      _scannedProduct.value = null
      _barcodeNotFound.value = barcode
    }
  }

  fun clearScannedBarcode() {
    _scannedBarcode.value = null
    _scannedProduct.value = null
    _barcodeNotFound.value = null
  }

  // --- Invoice OCR Intake & Review State ---
  data class InvoiceReviewState(
    val supplier: String = "Farm Fresh Logistics",
    val invoiceNumber: String = "INV-89240",
    val invoiceDate: String = "2026-09-20",
    val storeId: String = "store-1",
    val storeName: String = "Downtown Flagship",
    val items: List<InvoiceItem> = emptyList(),
    val isProcessing: Boolean = false,
    val isApproved: Boolean = false
  )

  private val _invoiceReviewState = MutableStateFlow<InvoiceReviewState?>(null)
  val invoiceReviewState: StateFlow<InvoiceReviewState?> = _invoiceReviewState.asStateFlow()

  fun startInvoiceOcrProcessing(sampleTemplateIndex: Int = 0) {
    _invoiceReviewState.value = InvoiceReviewState(isProcessing = true)

    viewModelScope.launch {
      kotlinx.coroutines.delay(600) // realistic OCR processing simulation
      val df = SimpleDateFormat("yyyy-MM-dd", Locale.US)
      val todayStr = df.format(Date())

      val expCal = Calendar.getInstance().apply { add(Calendar.DAY_OF_YEAR, 21) }
      val expStr = df.format(expCal.time)

      val invoiceId = "inv-${System.currentTimeMillis() % 10000}"
      val items = when (sampleTemplateIndex) {
        0 -> listOf(
          InvoiceItem(UUID.randomUUID().toString(), invoiceId, "Organic Whole Milk 1L", "MLK-ORG-1L", "8901030012345", 30, 1.35, 40.50, 3.24, "B-2026-995", expStr, "Confirmed"),
          InvoiceItem(UUID.randomUUID().toString(), invoiceId, "Barista Oat Milk 1L", "MLK-OAT-1L", "8901030022334", 24, 1.50, 36.00, 2.88, "B-2026-996", expStr, "Confirmed"),
          InvoiceItem(UUID.randomUUID().toString(), invoiceId, "Greek Yogurt 500g", "YOG-GRK-500", "8901030067890", 20, 1.60, 32.00, 2.56, "B-2026-997", expStr, "Review")
        )
        1 -> listOf(
          InvoiceItem(UUID.randomUUID().toString(), invoiceId, "Espresso Roast Coffee Beans 1kg", "BEV-COF-1KG", "8901030055443", 15, 9.80, 147.00, 11.76, "VR-9930", "", "Confirmed"),
          InvoiceItem(UUID.randomUUID().toString(), invoiceId, "Sparkling Mineral Water 750ml", "BEV-WAT-750", "8901030077889", 48, 0.85, 40.80, 3.26, "SC-102", "", "Confirmed")
        )
        else -> listOf(
          InvoiceItem(UUID.randomUUID().toString(), invoiceId, "Hass Avocados 4-Pack", "PRD-AVO-4PK", "8901030044556", 20, 2.20, 44.00, 3.52, "VO-771", expStr, "Review"),
          InvoiceItem(UUID.randomUUID().toString(), invoiceId, "Artisan Sourdough Loaf", "BAK-SRD-750", "8901030011223", 15, 2.10, 31.50, 2.52, "GH-302", expStr, "Confirmed")
        )
      }

      val store = stores.value.firstOrNull { it.id == _selectedStoreId.value } ?: stores.value.firstOrNull()
      val sId = store?.id ?: "store-1"
      val sName = store?.name ?: "Downtown Flagship"

      _invoiceReviewState.value = InvoiceReviewState(
        supplier = if (sampleTemplateIndex == 1) "Equator Imports & Roasters" else "Farm Fresh Logistics",
        invoiceNumber = "INV-${(10000..99999).random()}",
        invoiceDate = todayStr,
        storeId = sId,
        storeName = sName,
        items = items,
        isProcessing = false
      )
    }
  }

  fun updateInvoiceItem(updatedItem: InvoiceItem) {
    val current = _invoiceReviewState.value ?: return
    val newItems = current.items.map { if (it.id == updatedItem.id) updatedItem else it }
    _invoiceReviewState.value = current.copy(items = newItems)
  }

  fun removeInvoiceItem(itemId: String) {
    val current = _invoiceReviewState.value ?: return
    _invoiceReviewState.value = current.copy(items = current.items.filter { it.id != itemId })
  }

  fun addInvoiceItem(name: String, qty: Int, price: Double, barcode: String) {
    val current = _invoiceReviewState.value ?: return
    val newItem = InvoiceItem(
      id = UUID.randomUUID().toString(),
      invoiceId = "inv-${System.currentTimeMillis() % 10000}",
      productName = name,
      sku = "SKU-${(1000..9999).random()}",
      barcode = barcode,
      quantity = qty,
      unitPrice = price,
      totalPrice = qty * price,
      tax = qty * price * 0.08,
      status = "Confirmed"
    )
    _invoiceReviewState.value = current.copy(items = current.items + newItem)
  }

  fun approveAndCommitInvoice(onComplete: () -> Unit) {
    val current = _invoiceReviewState.value ?: return
    viewModelScope.launch {
      val total = current.items.sumOf { it.totalPrice }
      val invoice = PurchaseInvoice(
        id = UUID.randomUUID().toString(),
        invoiceNumber = current.invoiceNumber,
        supplier = current.supplier,
        invoiceDate = current.invoiceDate,
        storeId = current.storeId,
        storeName = current.storeName,
        totalAmount = total,
        status = "APPROVED"
      )
      repository.confirmAndApplyInvoice(invoice, current.items, _currentUser.value)
      _invoiceReviewState.value = current.copy(isApproved = true)
      onComplete()
    }
  }

  fun clearInvoiceReview() {
    _invoiceReviewState.value = null
  }

  // --- Inventory Actions ---

  fun adjustStock(
    storeId: String,
    productId: String,
    quantityChange: Int,
    type: TransactionType,
    notes: String,
    onSuccess: () -> Unit
  ) {
    viewModelScope.launch {
      repository.adjustStock(
        storeId = storeId,
        productId = productId,
        quantityChange = quantityChange,
        type = type,
        user = _currentUser.value,
        notes = notes
      )
      onSuccess()
    }
  }

  fun initiateTransfer(
    sourceStoreId: String,
    destStoreId: String,
    productId: String,
    quantity: Int,
    notes: String,
    onSuccess: () -> Unit
  ) {
    val sourceStore = stores.value.firstOrNull { it.id == sourceStoreId }
    val destStore = stores.value.firstOrNull { it.id == destStoreId }
    val prod = products.value.firstOrNull { it.id == productId }

    if (sourceStore != null && destStore != null && prod != null) {
      viewModelScope.launch {
        repository.initiateTransfer(
          sourceStoreId = sourceStoreId,
          sourceStoreName = sourceStore.name,
          destStoreId = destStoreId,
          destStoreName = destStore.name,
          productId = productId,
          productName = prod.name,
          quantity = quantity,
          user = _currentUser.value,
          notes = notes
        )
        onSuccess()
      }
    }
  }

  fun completeTransfer(transferId: String, onSuccess: () -> Unit) {
    viewModelScope.launch {
      repository.completeTransfer(transferId, _currentUser.value)
      onSuccess()
    }
  }

  fun disposeExpiredStock(
    storeId: String,
    productId: String,
    quantity: Int,
    reason: String,
    onSuccess: () -> Unit
  ) {
    viewModelScope.launch {
      repository.disposeExpiredStock(storeId, productId, quantity, _currentUser.value, reason)
      onSuccess()
    }
  }

  fun saveProduct(product: Product, onSuccess: () -> Unit) {
    viewModelScope.launch {
      val existing = products.value.firstOrNull { it.id == product.id }
      if (existing != null) {
        repository.updateProduct(product.copy(updatedAt = System.currentTimeMillis()))
      } else {
        repository.insertProduct(product)
        // initialize stock record for active stores
        stores.value.forEach { s ->
          repository.adjustStock(
            storeId = s.id,
            productId = product.id,
            quantityChange = 0,
            type = TransactionType.STOCK_COUNT,
            user = _currentUser.value,
            notes = "Initial stock entry"
          )
        }
      }
      onSuccess()
    }
  }

  fun saveStore(store: Store, onSuccess: () -> Unit) {
    viewModelScope.launch {
      val existing = stores.value.firstOrNull { it.id == store.id }
      if (existing != null) {
        repository.updateStore(store)
      } else {
        repository.insertStore(store)
      }
      onSuccess()
    }
  }

  fun saveUser(user: AppUser, onSuccess: () -> Unit) {
    viewModelScope.launch {
      val existing = users.value.firstOrNull { it.id == user.id }
      if (existing != null) {
        repository.updateUser(user)
      } else {
        repository.insertUser(user)
      }
      onSuccess()
    }
  }

  fun saveShift(shift: EmployeeShift, onSuccess: () -> Unit) {
    viewModelScope.launch {
      val existing = shifts.value.firstOrNull { it.id == shift.id }
      if (existing != null) {
        repository.updateShift(shift)
      } else {
        repository.insertShift(shift)
      }
      onSuccess()
    }
  }

  fun deleteShift(shiftId: String, onSuccess: () -> Unit) {
    viewModelScope.launch {
      repository.deleteShift(shiftId)
      onSuccess()
    }
  }

  // --- Filtering & Role Switching ---

  fun selectStore(storeId: String) {
    _selectedStoreId.value = storeId
  }

  fun switchUser(user: AppUser) {
    _currentUser.value = user
    if (user.assignedStoreId != "ALL") {
      _selectedStoreId.value = user.assignedStoreId
    }
  }

  fun switchRole(role: UserRole) {
    _currentUser.value = _currentUser.value.copy(role = role)
  }

  fun markNotificationAsRead(id: String) {
    viewModelScope.launch {
      repository.markNotificationAsRead(id)
    }
  }

  fun markAllNotificationsAsRead() {
    viewModelScope.launch {
      repository.markAllNotificationsAsRead()
    }
  }

  fun clearNotifications() {
    viewModelScope.launch {
      repository.clearNotifications()
    }
  }

  // Permissions helpers
  fun canManageGlobalSettings(): Boolean = _currentUser.value.role == UserRole.OWNER
  fun canManageEmployees(): Boolean = _currentUser.value.role in listOf(UserRole.OWNER, UserRole.STORE_MANAGER)
  fun canViewFinancialReports(): Boolean = _currentUser.value.role in listOf(UserRole.OWNER, UserRole.STORE_MANAGER)
  fun canAdjustStock(): Boolean = _currentUser.value.role in listOf(UserRole.OWNER, UserRole.STORE_MANAGER, UserRole.INVENTORY_STAFF)
  fun canTransferStock(): Boolean = _currentUser.value.role in listOf(UserRole.OWNER, UserRole.STORE_MANAGER)
  fun canConfirmDisposal(): Boolean = _currentUser.value.role in listOf(UserRole.OWNER, UserRole.STORE_MANAGER)
}
