package com.example.data.repository

import com.example.data.database.AppDatabase
import com.example.data.model.AppNotification
import com.example.data.model.AppUser
import com.example.data.model.AuditLog
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
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import java.util.UUID

class RetailRepository(private val db: AppDatabase) {

  val stores: Flow<List<Store>> = db.storeDao().getAllStores()
  val users: Flow<List<AppUser>> = db.userDao().getAllUsers()
  val products: Flow<List<Product>> = db.productDao().getAllProducts()
  val storeStocks: Flow<List<StoreStock>> = db.storeStockDao().getAllStocks()
  val transactions: Flow<List<StockTransaction>> = db.transactionDao().getAllTransactions()
  val transfers: Flow<List<StockTransfer>> = db.transferDao().getAllTransfers()
  val invoices: Flow<List<PurchaseInvoice>> = db.invoiceDao().getAllInvoices()
  val shifts: Flow<List<EmployeeShift>> = db.shiftDao().getAllShifts()
  val notifications: Flow<List<AppNotification>> = db.notificationDao().getAllNotifications()
  val auditLogs: Flow<List<AuditLog>> = db.auditDao().getAllAuditLogs()

  suspend fun initializeSeedDataIfNeeded() = withContext(Dispatchers.IO) {
    val existingStores = db.storeDao().getAllStores().first()
    val hasOldSchema = existingStores.any { it.id == "store-1" || it.name == "Downtown Flagship" }
    if (existingStores.isNotEmpty() && !hasOldSchema) return@withContext

    if (hasOldSchema) {
      db.clearAllTables()
    }

    val df = SimpleDateFormat("yyyy-MM-dd", Locale.US)
    val now = Calendar.getInstance()
    val todayStr = df.format(now.time)

    val expSoonCal = Calendar.getInstance().apply { add(Calendar.DAY_OF_YEAR, 4) }
    val expSoonStr = df.format(expSoonCal.time)

    val expNextWeekCal = Calendar.getInstance().apply { add(Calendar.DAY_OF_YEAR, 12) }
    val expNextWeekStr = df.format(expNextWeekCal.time)

    val expiredCal = Calendar.getInstance().apply { add(Calendar.DAY_OF_YEAR, -2) }
    val expiredStr = df.format(expiredCal.time)

    val expLaterCal = Calendar.getInstance().apply { add(Calendar.MONTH, 6) }
    val expLaterStr = df.format(expLaterCal.time)

    // 1. Stores: 2 active Alimentation stores + prospective restaurant, cafe & other businesses
    val defaultStores = listOf(
      Store(
        id = "store-nini",
        name = "Nini Alimentation",
        code = "NINI-01",
        address = "14 Rue du Commerce",
        city = "Paris",
        phone = "+33 1 42 68 01 01",
        managerName = "Nini",
        businessType = "Alimentation",
        isActive = true
      ),
      Store(
        id = "store-petit-marche",
        name = "Au Petit Marché",
        code = "APM-02",
        address = "28 Avenue des Marchés",
        city = "Paris",
        phone = "+33 1 45 89 02 02",
        managerName = "Marc Lefèvre",
        businessType = "Alimentation",
        isActive = true
      ),
      Store(
        id = "store-naina",
        name = "Naina Restaurant",
        code = "NAINA-REST",
        address = "8 Boulevard Gastronomique",
        city = "Paris",
        phone = "+33 1 48 00 03 03",
        managerName = "Chef Naina",
        businessType = "Restaurant",
        isActive = false // Planned Phase 2 expansion
      ),
      Store(
        id = "store-richard",
        name = "Richard Café",
        code = "RICH-CAFE",
        address = "5 Place du Café",
        city = "Paris",
        phone = "+33 1 43 12 04 04",
        managerName = "Richard B.",
        businessType = "Café",
        isActive = false // Planned Phase 2 expansion
      )
    )
    db.storeDao().insertStores(defaultStores)

    // 2. Users
    val defaultUsers = listOf(
      AppUser("user-1", "Alexander Wright", "EMP-001", "alex@retailflow.com", "+33 6 55 10 01", UserRole.OWNER, "ALL", "Active", "2023-01-01"),
      AppUser("user-2", "Nini", "EMP-002", "nini@alimentation-nini.com", "+33 6 55 10 02", UserRole.STORE_MANAGER, "store-nini", "Active", "2023-03-15"),
      AppUser("user-3", "Marc Lefèvre", "EMP-003", "marc@aupetitmarche.fr", "+33 6 55 10 03", UserRole.STORE_MANAGER, "store-petit-marche", "Active", "2023-06-01"),
      AppUser("user-4", "David Kim", "EMP-004", "david.k@retailflow.com", "+33 6 55 10 04", UserRole.INVENTORY_STAFF, "store-nini", "Active", "2023-09-10"),
      AppUser("user-5", "Chloe Bennett", "EMP-005", "chloe.b@retailflow.com", "+33 6 55 10 05", UserRole.SALES_STAFF, "store-petit-marche", "Active", "2024-02-01"),
      AppUser("user-6", "Liam O'Connor", "EMP-006", "liam.o@retailflow.com", "+33 6 55 10 06", UserRole.EMPLOYEE, "store-nini", "Active", "2024-04-12")
    )
    db.userDao().insertUsers(defaultUsers)

    // 3. Products
    val defaultProducts = listOf(
      Product(
        id = "prod-1",
        name = "Organic Whole Milk 1L",
        sku = "MLK-ORG-1L",
        barcode = "8901030012345",
        category = "Dairy & Eggs",
        subcategory = "Milk",
        brand = "Ferme Bio",
        supplier = "Marché Frais Co.",
        purchasePrice = 1.35,
        sellingPrice = 2.49,
        minStockLevel = 15,
        maxStockLevel = 100,
        unitType = "Liter",
        isPerishable = true,
        batchNumber = "B-2026-901",
        manufacturingDate = "2026-09-15",
        expirationDate = expSoonStr
      ),
      Product(
        id = "prod-2",
        name = "Greek Yogurt 500g",
        sku = "YOG-GRK-500",
        barcode = "8901030067890",
        category = "Dairy & Eggs",
        subcategory = "Yogurt",
        brand = "Olympus",
        supplier = "Aegean Foods",
        purchasePrice = 1.60,
        sellingPrice = 3.20,
        minStockLevel = 12,
        maxStockLevel = 80,
        unitType = "Gram",
        isPerishable = true,
        batchNumber = "B-2026-882",
        manufacturingDate = "2026-09-10",
        expirationDate = expiredStr
      ),
      Product(
        id = "prod-3",
        name = "Artisan Sourdough Loaf",
        sku = "BAK-SRD-750",
        barcode = "8901030011223",
        category = "Bakery",
        subcategory = "Bread",
        brand = "Fournil Doré",
        supplier = "Boulangerie Artisanale",
        purchasePrice = 2.10,
        sellingPrice = 4.50,
        minStockLevel = 8,
        maxStockLevel = 40,
        unitType = "Piece",
        isPerishable = true,
        batchNumber = "B-2026-774",
        manufacturingDate = "2026-09-18",
        expirationDate = expSoonStr
      ),
      Product(
        id = "prod-4",
        name = "Espresso Roast Coffee Beans 1kg",
        sku = "BEV-COF-1KG",
        barcode = "8901030055443",
        category = "Beverages",
        subcategory = "Coffee",
        brand = "Volcano Roasters",
        supplier = "Equator Imports",
        purchasePrice = 9.80,
        sellingPrice = 18.90,
        minStockLevel = 10,
        maxStockLevel = 50,
        unitType = "Kilogram",
        isPerishable = false,
        batchNumber = "VR-9921",
        expirationDate = expLaterStr
      ),
      Product(
        id = "prod-5",
        name = "Sparkling Mineral Water 750ml",
        sku = "BEV-WAT-750",
        barcode = "8901030077889",
        category = "Beverages",
        subcategory = "Water",
        brand = "San Cristobal",
        supplier = "Spring Beverage Corp",
        purchasePrice = 0.85,
        sellingPrice = 1.95,
        minStockLevel = 25,
        maxStockLevel = 150,
        unitType = "Piece",
        isPerishable = false,
        expirationDate = expLaterStr
      ),
      Product(
        id = "prod-6",
        name = "Extra Virgin Olive Oil 500ml",
        sku = "PAN-EVOO-500",
        barcode = "8901030033221",
        category = "Pantry",
        subcategory = "Oils",
        brand = "Terra Nostra",
        supplier = "Mediterranean Goods",
        purchasePrice = 4.90,
        sellingPrice = 9.99,
        minStockLevel = 10,
        maxStockLevel = 60,
        unitType = "Milliliter",
        isPerishable = false,
        expirationDate = expLaterStr
      ),
      Product(
        id = "prod-7",
        name = "Artisanal Brie Cheese 250g",
        sku = "DRY-BRIE-250",
        barcode = "8901030099887",
        category = "Dairy & Eggs",
        subcategory = "Cheese",
        brand = "Maison Fromagère",
        supplier = "Marché Frais Co.",
        purchasePrice = 2.40,
        sellingPrice = 4.90,
        minStockLevel = 10,
        maxStockLevel = 50,
        unitType = "Piece",
        isPerishable = true,
        batchNumber = "B-2026-640",
        expirationDate = expNextWeekStr
      ),
      Product(
        id = "prod-8",
        name = "Hass Avocados 4-Pack",
        sku = "PRD-AVO-4PK",
        barcode = "8901030044556",
        category = "Produce",
        subcategory = "Fresh Fruit",
        brand = "Verde Organics",
        supplier = "Fresh Horizon Logistics",
        purchasePrice = 2.20,
        sellingPrice = 4.99,
        minStockLevel = 10,
        maxStockLevel = 50,
        unitType = "Pack",
        isPerishable = true,
        expirationDate = expNextWeekStr
      ),
      Product(
        id = "prod-9",
        name = "Organic Farm Eggs 10pk",
        sku = "EGG-BIO-10PK",
        barcode = "8901030022334",
        category = "Dairy & Eggs",
        subcategory = "Eggs",
        brand = "Ferme Bio",
        supplier = "Marché Frais Co.",
        purchasePrice = 1.80,
        sellingPrice = 3.60,
        minStockLevel = 15,
        maxStockLevel = 80,
        unitType = "Pack",
        isPerishable = true,
        expirationDate = expNextWeekStr
      ),
      Product(
        id = "prod-10",
        name = "Dark Chocolate 85% 100g",
        sku = "CNF-CHO-100",
        barcode = "8901030088112",
        category = "Confectionery",
        subcategory = "Chocolate",
        brand = "Cacao Oro",
        supplier = "Swiss Chocolatiers",
        purchasePrice = 1.40,
        sellingPrice = 3.10,
        minStockLevel = 15,
        maxStockLevel = 100,
        unitType = "Piece",
        isPerishable = false,
        expirationDate = expLaterStr
      )
    )
    db.productDao().insertProducts(defaultProducts)

    // 4. Initial Stocks for the two Alimentation stores (Nini & Au Petit Marché)
    val defaultStocks = listOf(
      // Nini Alimentation (store-nini)
      StoreStock("store-nini-prod-1", "store-nini", "prod-1", quantity = 18, expiredQuantity = 0, locationInStore = "Chiller 1 - Dairy"),
      StoreStock("store-nini-prod-2", "store-nini", "prod-2", quantity = 0, expiredQuantity = 12, locationInStore = "Chiller 2 - Yogurts"),
      StoreStock("store-nini-prod-3", "store-nini", "prod-3", quantity = 6, expiredQuantity = 0, locationInStore = "Bakery Display"), // Low stock (< 8)
      StoreStock("store-nini-prod-4", "store-nini", "prod-4", quantity = 34, expiredQuantity = 0, locationInStore = "Aisle 3 - Coffee"),
      StoreStock("store-nini-prod-5", "store-nini", "prod-5", quantity = 85, expiredQuantity = 0, locationInStore = "Beverage Section"),
      StoreStock("store-nini-prod-6", "store-nini", "prod-6", quantity = 22, expiredQuantity = 0, locationInStore = "Aisle 2 - Oils"),
      StoreStock("store-nini-prod-7", "store-nini", "prod-7", quantity = 28, expiredQuantity = 0, locationInStore = "Fromagerie"),
      StoreStock("store-nini-prod-8", "store-nini", "prod-8", quantity = 14, expiredQuantity = 0, locationInStore = "Produce Section A"),
      StoreStock("store-nini-prod-9", "store-nini", "prod-9", quantity = 40, expiredQuantity = 0, locationInStore = "Chiller 1 - Eggs"),
      StoreStock("store-nini-prod-10", "store-nini", "prod-10", quantity = 45, expiredQuantity = 0, locationInStore = "Checkout Aisle"),

      // Au Petit Marché (store-petit-marche)
      StoreStock("store-petit-marche-prod-1", "store-petit-marche", "prod-1", quantity = 32, expiredQuantity = 0, locationInStore = "Fresh Milk Fridge"),
      StoreStock("store-petit-marche-prod-2", "store-petit-marche", "prod-2", quantity = 24, expiredQuantity = 0, locationInStore = "Yogurt Fridge"),
      StoreStock("store-petit-marche-prod-3", "store-petit-marche", "prod-3", quantity = 18, expiredQuantity = 0, locationInStore = "Bread Basket"),
      StoreStock("store-petit-marche-prod-4", "store-petit-marche", "prod-4", quantity = 16, expiredQuantity = 0, locationInStore = "Coffee Shelf"),
      StoreStock("store-petit-marche-prod-5", "store-petit-marche", "prod-5", quantity = 60, expiredQuantity = 0, locationInStore = "Drinks Cooler"),
      StoreStock("store-petit-marche-prod-6", "store-petit-marche", "prod-6", quantity = 19, expiredQuantity = 0, locationInStore = "Grocery Shelf 1"),
      StoreStock("store-petit-marche-prod-7", "store-petit-marche", "prod-7", quantity = 15, expiredQuantity = 0, locationInStore = "Cheese Fridge"),
      StoreStock("store-petit-marche-prod-8", "store-petit-marche", "prod-8", quantity = 22, expiredQuantity = 0, locationInStore = "Fruit Stand"),
      StoreStock("store-petit-marche-prod-9", "store-petit-marche", "prod-9", quantity = 30, expiredQuantity = 0, locationInStore = "Chiller Front"),
      StoreStock("store-petit-marche-prod-10", "store-petit-marche", "prod-10", quantity = 52, expiredQuantity = 0, locationInStore = "Snack Stand")
    )
    db.storeStockDao().insertStocks(defaultStocks)

    // 5. Initial Transactions spanning Today, Yesterday, and past days for realistic date/time filtering
    val nowTs = System.currentTimeMillis()
    val oneHour = 3600000L
    val oneDay = 86400000L

    val initialTx = listOf(
      // TODAY - Morning (08:30)
      StockTransaction(UUID.randomUUID().toString(), "store-nini", "prod-1", "Organic Whole Milk 1L", 40, TransactionType.RECEIVED.name, 0, 40, "user-4", "David Kim", "PO-9001", nowTs - oneHour * 3, "Morning dairy delivery"),
      // TODAY - Midday (11:15)
      StockTransaction(UUID.randomUUID().toString(), "store-nini", "prod-1", "Organic Whole Milk 1L", -22, TransactionType.SOLD.name, 40, 18, "user-2", "Nini", "SALE-4001", nowTs - oneHour * 2, "Customer sales"),
      // TODAY - Afternoon (14:20)
      StockTransaction(UUID.randomUUID().toString(), "store-petit-marche", "prod-3", "Artisan Sourdough Loaf", -6, TransactionType.SOLD.name, 24, 18, "user-5", "Chloe Bennett", "SALE-4002", nowTs - oneHour, "Afternoon bakery sales"),
      // TODAY - Recent
      StockTransaction(UUID.randomUUID().toString(), "store-nini", "prod-2", "Greek Yogurt 500g", -12, TransactionType.EXPIRED.name, 12, 0, "user-2", "Nini", "EXP-201", nowTs - (oneHour / 2), "Segregated expired batch for disposal"),

      // YESTERDAY - Morning (09:45)
      StockTransaction(UUID.randomUUID().toString(), "store-nini", "prod-4", "Espresso Roast Coffee Beans 1kg", 30, TransactionType.RECEIVED.name, 10, 40, "user-4", "David Kim", "PO-8994", nowTs - oneDay - oneHour * 2, "Bulk coffee batch received"),
      // YESTERDAY - Afternoon (15:30)
      StockTransaction(UUID.randomUUID().toString(), "store-nini", "prod-4", "Espresso Roast Coffee Beans 1kg", -6, TransactionType.SOLD.name, 40, 34, "user-2", "Nini", "SALE-3990", nowTs - oneDay + oneHour, "Customer sale"),
      // YESTERDAY - Evening (18:15)
      StockTransaction(UUID.randomUUID().toString(), "store-petit-marche", "prod-5", "Sparkling Mineral Water 750ml", -14, TransactionType.SOLD.name, 74, 60, "user-5", "Chloe Bennett", "SALE-3995", nowTs - oneDay + oneHour * 3, "Evening beverage sales"),

      // 3 DAYS AGO
      StockTransaction(UUID.randomUUID().toString(), "store-petit-marche", "prod-7", "Artisanal Brie Cheese 250g", 25, TransactionType.RECEIVED.name, 0, 25, "user-3", "Marc Lefèvre", "PO-8980", nowTs - oneDay * 3, "Artisanal cheese delivery"),
      StockTransaction(UUID.randomUUID().toString(), "store-petit-marche", "prod-7", "Artisanal Brie Cheese 250g", -10, TransactionType.SOLD.name, 25, 15, "user-5", "Chloe Bennett", "SALE-3950", nowTs - oneDay * 3 + oneHour * 4, "Weekend cheese counter sales"),

      // 5 DAYS AGO
      StockTransaction(UUID.randomUUID().toString(), "store-nini", "prod-8", "Hass Avocados 4-Pack", 25, TransactionType.RECEIVED.name, 0, 25, "user-4", "David Kim", "PO-8960", nowTs - oneDay * 5, "Produce shipment arrival"),
      StockTransaction(UUID.randomUUID().toString(), "store-nini", "prod-8", "Hass Avocados 4-Pack", -11, TransactionType.SOLD.name, 25, 14, "user-2", "Nini", "SALE-3910", nowTs - oneDay * 5 + oneHour * 2, "Fresh produce sales")
    )
    db.transactionDao().insertTransactions(initialTx)

    // 6. Initial Transfers between Nini Alimentation and Au Petit Marché
    val initialTransfer = StockTransfer(
      id = "tr-101",
      transferNumber = "TR-2026-0010",
      sourceStoreId = "store-nini",
      sourceStoreName = "Nini Alimentation",
      destStoreId = "store-petit-marche",
      destStoreName = "Au Petit Marché",
      productId = "prod-4",
      productName = "Espresso Roast Coffee Beans 1kg",
      quantity = 8,
      status = "IN_TRANSIT",
      initiatedByUserId = "user-2",
      initiatedByUserName = "Nini",
      createdAt = nowTs - oneHour * 4,
      notes = "Transferring coffee stock to Au Petit Marché for weekly rush"
    )
    db.transferDao().insertTransfer(initialTransfer)

    // 7. Initial Invoice for OCR demo
    val sampleInvoice = PurchaseInvoice(
      id = "inv-101",
      invoiceNumber = "INV-77491",
      supplier = "Marché Frais Co.",
      invoiceDate = todayStr,
      storeId = "store-nini",
      storeName = "Nini Alimentation",
      totalAmount = 184.60,
      status = "DRAFT_REVIEW",
      rawOcrText = "MARCHÉ FRAIS CO.\nInvoice: INV-77491 Date: $todayStr\nStore: Nini Alimentation\nItems:\nOrganic Whole Milk 1L x 40 @ $1.35 = $54.00\nOrganic Farm Eggs 10pk x 30 @ $1.80 = $54.00\nArtisanal Brie Cheese 250g x 15 @ $2.40 = $36.00"
    )
    db.invoiceDao().insertInvoice(sampleInvoice)
    val invoiceItems = listOf(
      InvoiceItem(UUID.randomUUID().toString(), "inv-101", "Organic Whole Milk 1L", "MLK-ORG-1L", "8901030012345", 40, 1.35, 54.00, 4.32, "B-2026-990", expNextWeekStr, "Confirmed"),
      InvoiceItem(UUID.randomUUID().toString(), "inv-101", "Organic Farm Eggs 10pk", "EGG-BIO-10PK", "8901030022334", 30, 1.80, 54.00, 4.32, "B-2026-991", expNextWeekStr, "Confirmed"),
      InvoiceItem(UUID.randomUUID().toString(), "inv-101", "Artisanal Brie Cheese 250g", "DRY-BRIE-250", "8901030099887", 15, 2.40, 36.00, 2.88, "B-2026-992", expSoonStr, "Review")
    )
    db.invoiceDao().insertInvoiceItems(invoiceItems)

    // 8. Initial Shifts for Nini Alimentation and Au Petit Marché
    val defaultShifts = listOf(
      EmployeeShift("shift-1", "user-2", "Nini", "store-nini", "Nini Alimentation", todayStr, "08:00", "16:30", 45, "Morning", "Store Opening & Daily Stock Check"),
      EmployeeShift("shift-2", "user-4", "David Kim", "store-nini", "Nini Alimentation", todayStr, "09:00", "17:30", 60, "Morning", "Delivery Intake & Inventory Sorting"),
      EmployeeShift("shift-3", "user-3", "Marc Lefèvre", "store-petit-marche", "Au Petit Marché", todayStr, "08:30", "17:00", 45, "Morning", "Au Petit Marché Opening & Bakery Replenishment"),
      EmployeeShift("shift-4", "user-5", "Chloe Bennett", "store-petit-marche", "Au Petit Marché", todayStr, "12:00", "20:30", 45, "Afternoon", "Floor & Customer Checkout")
    )
    db.shiftDao().insertShifts(defaultShifts)

    // 9. Initial Notifications
    val defaultNotifications = listOf(
      AppNotification(UUID.randomUUID().toString(), "Low Stock Alert", "Artisan Sourdough Loaf has reached 6 units remaining at Nini Alimentation (Min: 8).", "LOW_STOCK", "prod-3", "store-nini", nowTs - oneHour),
      AppNotification(UUID.randomUUID().toString(), "Out of Stock & Expired", "Greek Yogurt 500g is out of stock. 12 expired units require disposal.", "OUT_OF_STOCK", "prod-2", "store-nini", nowTs - (oneHour * 2)),
      AppNotification(UUID.randomUUID().toString(), "Stock Transfer In Transit", "Transfer TR-2026-0010 (8x Coffee Beans) dispatched to Au Petit Marché.", "TRANSFER", "tr-101", "store-nini", nowTs - (oneHour * 4)),
      AppNotification(UUID.randomUUID().toString(), "Shift Assigned", "Scheduled for Morning Shift today at Nini Alimentation.", "SHIFT", "shift-1", "store-nini", nowTs - (oneHour * 6))
    )
    db.notificationDao().insertNotifications(defaultNotifications)

    // 10. Audit Logs
    val defaultAuditLogs = listOf(
      AuditLog(UUID.randomUUID().toString(), "user-1", "Alexander Wright", "SYSTEM_INIT", "SYSTEM", "SYS-001", "ALL", "System initialized for Nini Alimentation and Au Petit Marché pilot.", nowTs - oneDay * 7),
      AuditLog(UUID.randomUUID().toString(), "user-4", "David Kim", "STOCK_RECEIVE", "PRODUCT", "prod-1", "store-nini", "Received 40 units of Organic Whole Milk 1L (PO-9001).", nowTs - oneHour * 3),
      AuditLog(UUID.randomUUID().toString(), "user-2", "Nini", "TRANSFER_INITIATE", "TRANSFER", "tr-101", "store-nini", "Initiated transfer of 8 units Coffee Beans to Au Petit Marché.", nowTs - oneHour * 4)
    )
    db.auditDao().insertAuditLogs(defaultAuditLogs)
  }

  // --- Transactions & Stock Modification ---

  suspend fun adjustStock(
    storeId: String,
    productId: String,
    quantityChange: Int,
    type: TransactionType,
    user: AppUser,
    notes: String
  ) = withContext(Dispatchers.IO) {
    val currentStock = db.storeStockDao().getStock(storeId, productId)
    val prevQty = currentStock?.quantity ?: 0
    val newQty = (prevQty + quantityChange).coerceAtLeast(0)

    val updatedStock = currentStock?.copy(
      quantity = newQty,
      lastUpdated = System.currentTimeMillis()
    ) ?: StoreStock(
      id = "$storeId-$productId",
      storeId = storeId,
      productId = productId,
      quantity = newQty,
      lastUpdated = System.currentTimeMillis()
    )

    db.storeStockDao().insertStock(updatedStock)

    val product = db.productDao().getProductById(productId)
    val productName = product?.name ?: "Unknown Product"

    val tx = StockTransaction(
      id = UUID.randomUUID().toString(),
      storeId = storeId,
      productId = productId,
      productName = productName,
      quantity = quantityChange,
      transactionType = type.name,
      previousStock = prevQty,
      newStock = newQty,
      userId = user.id,
      userName = user.name,
      referenceNumber = "TX-${System.currentTimeMillis() % 100000}",
      notes = notes
    )
    db.transactionDao().insertTransaction(tx)

    // Audit log
    db.auditDao().insertAuditLog(
      AuditLog(
        id = UUID.randomUUID().toString(),
        userId = user.id,
        userName = user.name,
        action = "STOCK_ADJUST_${type.name}",
        entityType = "PRODUCT",
        entityId = productId,
        storeId = storeId,
        details = "$productName adjusted by $quantityChange ($prevQty → $newQty). Reason: $notes"
      )
    )

    // Alert check
    if (product != null) {
      if (newQty == 0) {
        db.notificationDao().insertNotification(
          AppNotification(
            id = UUID.randomUUID().toString(),
            title = "Out of Stock Alert",
            message = "$productName has 0 units remaining.",
            type = "OUT_OF_STOCK",
            relatedId = productId,
            storeId = storeId
          )
        )
      } else if (newQty <= product.minStockLevel) {
        db.notificationDao().insertNotification(
          AppNotification(
            id = UUID.randomUUID().toString(),
            title = "Low Stock Alert",
            message = "$productName is low on stock: $newQty units remaining (Min: ${product.minStockLevel}).",
            type = "LOW_STOCK",
            relatedId = productId,
            storeId = storeId
          )
        )
      }
    }
  }

  suspend fun initiateTransfer(
    sourceStoreId: String,
    sourceStoreName: String,
    destStoreId: String,
    destStoreName: String,
    productId: String,
    productName: String,
    quantity: Int,
    user: AppUser,
    notes: String
  ): StockTransfer = withContext(Dispatchers.IO) {
    // 1. Deduct from source store
    val currentSource = db.storeStockDao().getStock(sourceStoreId, productId)
    val prevQty = currentSource?.quantity ?: 0
    val newSourceQty = (prevQty - quantity).coerceAtLeast(0)

    db.storeStockDao().insertStock(
      currentSource?.copy(quantity = newSourceQty, lastUpdated = System.currentTimeMillis())
        ?: StoreStock(
          id = "$sourceStoreId-$productId",
          storeId = sourceStoreId,
          productId = productId,
          quantity = newSourceQty
        )
    )

    // 2. Log source deduction transaction
    db.transactionDao().insertTransaction(
      StockTransaction(
        id = UUID.randomUUID().toString(),
        storeId = sourceStoreId,
        productId = productId,
        productName = productName,
        quantity = -quantity,
        transactionType = TransactionType.TRANSFERRED.name,
        previousStock = prevQty,
        newStock = newSourceQty,
        userId = user.id,
        userName = user.name,
        referenceNumber = "TR-${System.currentTimeMillis() % 10000}",
        notes = "Transfer OUT to $destStoreName. $notes"
      )
    )

    // 3. Create Transfer record
    val transfer = StockTransfer(
      id = UUID.randomUUID().toString(),
      transferNumber = "TR-${System.currentTimeMillis() % 100000}",
      sourceStoreId = sourceStoreId,
      sourceStoreName = sourceStoreName,
      destStoreId = destStoreId,
      destStoreName = destStoreName,
      productId = productId,
      productName = productName,
      quantity = quantity,
      status = "IN_TRANSIT",
      initiatedByUserId = user.id,
      initiatedByUserName = user.name,
      notes = notes
    )
    db.transferDao().insertTransfer(transfer)

    // 4. Audit
    db.auditDao().insertAuditLog(
      AuditLog(
        id = UUID.randomUUID().toString(),
        userId = user.id,
        userName = user.name,
        action = "TRANSFER_INITIATED",
        entityType = "TRANSFER",
        entityId = transfer.id,
        storeId = sourceStoreId,
        details = "Transferred $quantity units of $productName to $destStoreName (In Transit)."
      )
    )

    // 5. Notification
    db.notificationDao().insertNotification(
      AppNotification(
        id = UUID.randomUUID().toString(),
        title = "Stock Transfer In Transit",
        message = "$quantity units of $productName dispatched from $sourceStoreName to $destStoreName.",
        type = "TRANSFER",
        relatedId = transfer.id,
        storeId = destStoreId
      )
    )

    transfer
  }

  suspend fun completeTransfer(transferId: String, user: AppUser) = withContext(Dispatchers.IO) {
    val transfer = db.transferDao().getTransferById(transferId) ?: return@withContext
    if (transfer.status != "IN_TRANSIT") return@withContext

    // Add to dest store
    val destStock = db.storeStockDao().getStock(transfer.destStoreId, transfer.productId)
    val prevDestQty = destStock?.quantity ?: 0
    val newDestQty = prevDestQty + transfer.quantity

    db.storeStockDao().insertStock(
      destStock?.copy(quantity = newDestQty, lastUpdated = System.currentTimeMillis())
        ?: StoreStock(
          id = "${transfer.destStoreId}-${transfer.productId}",
          storeId = transfer.destStoreId,
          productId = transfer.productId,
          quantity = newDestQty
        )
    )

    // Log destination addition
    db.transactionDao().insertTransaction(
      StockTransaction(
        id = UUID.randomUUID().toString(),
        storeId = transfer.destStoreId,
        productId = transfer.productId,
        productName = transfer.productName,
        quantity = transfer.quantity,
        transactionType = TransactionType.TRANSFERRED.name,
        previousStock = prevDestQty,
        newStock = newDestQty,
        userId = user.id,
        userName = user.name,
        referenceNumber = transfer.transferNumber,
        notes = "Transfer IN from ${transfer.sourceStoreName} received."
      )
    )

    // Update transfer status
    db.transferDao().updateTransfer(
      transfer.copy(
        status = "COMPLETED",
        receivedByUserId = user.id,
        receivedByUserName = user.name,
        completedAt = System.currentTimeMillis()
      )
    )

    // Audit log
    db.auditDao().insertAuditLog(
      AuditLog(
        id = UUID.randomUUID().toString(),
        userId = user.id,
        userName = user.name,
        action = "TRANSFER_RECEIVED",
        entityType = "TRANSFER",
        entityId = transfer.id,
        storeId = transfer.destStoreId,
        details = "Received ${transfer.quantity} units of ${transfer.productName} from ${transfer.sourceStoreName}."
      )
    )

    // Notification
    db.notificationDao().insertNotification(
      AppNotification(
        id = UUID.randomUUID().toString(),
        title = "Stock Transfer Completed",
        message = "Transfer ${transfer.transferNumber} received at ${transfer.destStoreName}.",
        type = "TRANSFER",
        relatedId = transfer.id,
        storeId = transfer.destStoreId
      )
    )
  }

  suspend fun disposeExpiredStock(
    storeId: String,
    productId: String,
    quantityToDispose: Int,
    user: AppUser,
    reason: String
  ) = withContext(Dispatchers.IO) {
    val stock = db.storeStockDao().getStock(storeId, productId)
    val prevExpired = stock?.expiredQuantity ?: 0
    val newExpired = (prevExpired - quantityToDispose).coerceAtLeast(0)

    db.storeStockDao().insertStock(
      stock?.copy(
        expiredQuantity = newExpired,
        lastUpdated = System.currentTimeMillis()
      ) ?: StoreStock(
        id = "$storeId-$productId",
        storeId = storeId,
        productId = productId,
        quantity = 0,
        expiredQuantity = newExpired
      )
    )

    val product = db.productDao().getProductById(productId)
    val productName = product?.name ?: "Product"

    // Transaction
    db.transactionDao().insertTransaction(
      StockTransaction(
        id = UUID.randomUUID().toString(),
        storeId = storeId,
        productId = productId,
        productName = productName,
        quantity = -quantityToDispose,
        transactionType = TransactionType.DISPOSED.name,
        previousStock = prevExpired,
        newStock = newExpired,
        userId = user.id,
        userName = user.name,
        referenceNumber = "DISP-${System.currentTimeMillis() % 10000}",
        notes = "Disposal confirmed by manager. Reason: $reason"
      )
    )

    // Audit Log
    db.auditDao().insertAuditLog(
      AuditLog(
        id = UUID.randomUUID().toString(),
        userId = user.id,
        userName = user.name,
        action = "STOCK_DISPOSED",
        entityType = "PRODUCT",
        entityId = productId,
        storeId = storeId,
        details = "Manager ${user.name} confirmed disposal of $quantityToDispose expired units of $productName. Reason: $reason"
      )
    )
  }

  suspend fun confirmAndApplyInvoice(
    invoice: PurchaseInvoice,
    items: List<InvoiceItem>,
    user: AppUser
  ) = withContext(Dispatchers.IO) {
    for (item in items) {
      // Find or create product
      var product = if (item.barcode.isNotBlank()) {
        db.productDao().getProductByBarcode(item.barcode)
      } else null

      if (product == null) {
        val newProdId = UUID.randomUUID().toString()
        product = Product(
          id = newProdId,
          name = item.productName,
          sku = item.sku.ifBlank { "SKU-${System.currentTimeMillis() % 10000}" },
          barcode = item.barcode.ifBlank { "890${System.currentTimeMillis() % 10000000000L}" },
          category = "General",
          purchasePrice = item.unitPrice,
          sellingPrice = item.unitPrice * 1.5,
          batchNumber = item.batchNumber,
          expirationDate = item.expirationDate,
          isPerishable = item.expirationDate.isNotBlank()
        )
        db.productDao().insertProduct(product)
      }

      // Add to store stock
      val stock = db.storeStockDao().getStock(invoice.storeId, product.id)
      val prevQty = stock?.quantity ?: 0
      val newQty = prevQty + item.quantity

      db.storeStockDao().insertStock(
        stock?.copy(quantity = newQty, lastUpdated = System.currentTimeMillis())
          ?: StoreStock(
            id = "${invoice.storeId}-${product.id}",
            storeId = invoice.storeId,
            productId = product.id,
            quantity = newQty
          )
      )

      // Transaction
      db.transactionDao().insertTransaction(
        StockTransaction(
          id = UUID.randomUUID().toString(),
          storeId = invoice.storeId,
          productId = product.id,
          productName = item.productName,
          quantity = item.quantity,
          transactionType = TransactionType.RECEIVED.name,
          previousStock = prevQty,
          newStock = newQty,
          userId = user.id,
          userName = user.name,
          referenceNumber = invoice.invoiceNumber,
          notes = "Invoice OCR intake approved. Supplier: ${invoice.supplier}"
        )
      )
    }

    // Mark invoice approved
    db.invoiceDao().updateInvoice(invoice.copy(status = "APPROVED"))

    // Audit log
    db.auditDao().insertAuditLog(
      AuditLog(
        id = UUID.randomUUID().toString(),
        userId = user.id,
        userName = user.name,
        action = "INVOICE_APPROVED",
        entityType = "INVOICE",
        entityId = invoice.id,
        storeId = invoice.storeId,
        details = "Invoice ${invoice.invoiceNumber} (${items.size} line items, total €${invoice.totalAmount}) approved and committed to inventory."
      )
    )
  }

  suspend fun insertProduct(product: Product) = withContext(Dispatchers.IO) {
    db.productDao().insertProduct(product)
  }

  suspend fun updateProduct(product: Product) = withContext(Dispatchers.IO) {
    db.productDao().updateProduct(product)
  }

  suspend fun insertStore(store: Store) = withContext(Dispatchers.IO) {
    db.storeDao().insertStore(store)
  }

  suspend fun updateStore(store: Store) = withContext(Dispatchers.IO) {
    db.storeDao().updateStore(store)
  }

  suspend fun insertUser(user: AppUser) = withContext(Dispatchers.IO) {
    db.userDao().insertUser(user)
  }

  suspend fun updateUser(user: AppUser) = withContext(Dispatchers.IO) {
    db.userDao().updateUser(user)
  }

  suspend fun insertShift(shift: EmployeeShift) = withContext(Dispatchers.IO) {
    db.shiftDao().insertShift(shift)
  }

  suspend fun updateShift(shift: EmployeeShift) = withContext(Dispatchers.IO) {
    db.shiftDao().updateShift(shift)
  }

  suspend fun deleteShift(id: String) = withContext(Dispatchers.IO) {
    db.shiftDao().deleteShift(id)
  }

  suspend fun markNotificationAsRead(id: String) = withContext(Dispatchers.IO) {
    db.notificationDao().markAsRead(id)
  }

  suspend fun markAllNotificationsAsRead() = withContext(Dispatchers.IO) {
    db.notificationDao().markAllAsRead()
  }

  suspend fun clearNotifications() = withContext(Dispatchers.IO) {
    db.notificationDao().clearAllNotifications()
  }
}
