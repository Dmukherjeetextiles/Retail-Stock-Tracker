package com.example.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
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
import kotlinx.coroutines.flow.Flow

@Dao
interface StoreDao {
  @Query("SELECT * FROM stores ORDER BY name ASC")
  fun getAllStores(): Flow<List<Store>>

  @Query("SELECT * FROM stores WHERE id = :id LIMIT 1")
  suspend fun getStoreById(id: String): Store?

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  suspend fun insertStore(store: Store)

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  suspend fun insertStores(stores: List<Store>)

  @Update
  suspend fun updateStore(store: Store)
}

@Dao
interface UserDao {
  @Query("SELECT * FROM users ORDER BY name ASC")
  fun getAllUsers(): Flow<List<AppUser>>

  @Query("SELECT * FROM users WHERE id = :id LIMIT 1")
  suspend fun getUserById(id: String): AppUser?

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  suspend fun insertUser(user: AppUser)

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  suspend fun insertUsers(users: List<AppUser>)

  @Update
  suspend fun updateUser(user: AppUser)
}

@Dao
interface ProductDao {
  @Query("SELECT * FROM products WHERE status != 'Archived' ORDER BY name ASC")
  fun getAllProducts(): Flow<List<Product>>

  @Query("SELECT * FROM products ORDER BY name ASC")
  fun getAllProductsIncludingArchived(): Flow<List<Product>>

  @Query("SELECT * FROM products WHERE id = :id LIMIT 1")
  suspend fun getProductById(id: String): Product?

  @Query("SELECT * FROM products WHERE barcode = :barcode LIMIT 1")
  suspend fun getProductByBarcode(barcode: String): Product?

  @Query("SELECT * FROM products WHERE barcode = :barcode LIMIT 1")
  fun observeProductByBarcode(barcode: String): Flow<Product?>

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  suspend fun insertProduct(product: Product)

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  suspend fun insertProducts(products: List<Product>)

  @Update
  suspend fun updateProduct(product: Product)
}

@Dao
interface StoreStockDao {
  @Query("SELECT * FROM store_stocks")
  fun getAllStocks(): Flow<List<StoreStock>>

  @Query("SELECT * FROM store_stocks WHERE storeId = :storeId")
  fun getStocksByStore(storeId: String): Flow<List<StoreStock>>

  @Query("SELECT * FROM store_stocks WHERE productId = :productId")
  fun getStocksByProduct(productId: String): Flow<List<StoreStock>>

  @Query("SELECT * FROM store_stocks WHERE storeId = :storeId AND productId = :productId LIMIT 1")
  suspend fun getStock(storeId: String, productId: String): StoreStock?

  @Query("SELECT * FROM store_stocks WHERE storeId = :storeId AND productId = :productId LIMIT 1")
  fun observeStock(storeId: String, productId: String): Flow<StoreStock?>

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  suspend fun insertStock(stock: StoreStock)

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  suspend fun insertStocks(stocks: List<StoreStock>)

  @Update
  suspend fun updateStock(stock: StoreStock)
}

@Dao
interface TransactionDao {
  @Query("SELECT * FROM stock_transactions ORDER BY timestamp DESC")
  fun getAllTransactions(): Flow<List<StockTransaction>>

  @Query("SELECT * FROM stock_transactions WHERE storeId = :storeId ORDER BY timestamp DESC")
  fun getTransactionsByStore(storeId: String): Flow<List<StockTransaction>>

  @Query("SELECT * FROM stock_transactions WHERE productId = :productId ORDER BY timestamp DESC")
  fun getTransactionsByProduct(productId: String): Flow<List<StockTransaction>>

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  suspend fun insertTransaction(transaction: StockTransaction)

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  suspend fun insertTransactions(transactions: List<StockTransaction>)
}

@Dao
interface TransferDao {
  @Query("SELECT * FROM stock_transfers ORDER BY createdAt DESC")
  fun getAllTransfers(): Flow<List<StockTransfer>>

  @Query("SELECT * FROM stock_transfers WHERE sourceStoreId = :storeId OR destStoreId = :storeId ORDER BY createdAt DESC")
  fun getTransfersByStore(storeId: String): Flow<List<StockTransfer>>

  @Query("SELECT * FROM stock_transfers WHERE id = :id LIMIT 1")
  suspend fun getTransferById(id: String): StockTransfer?

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  suspend fun insertTransfer(transfer: StockTransfer)

  @Update
  suspend fun updateTransfer(transfer: StockTransfer)
}

@Dao
interface InvoiceDao {
  @Query("SELECT * FROM purchase_invoices ORDER BY createdAt DESC")
  fun getAllInvoices(): Flow<List<PurchaseInvoice>>

  @Query("SELECT * FROM invoice_items WHERE invoiceId = :invoiceId")
  fun getItemsByInvoice(invoiceId: String): Flow<List<InvoiceItem>>

  @Query("SELECT * FROM invoice_items WHERE invoiceId = :invoiceId")
  suspend fun getItemsForInvoice(invoiceId: String): List<InvoiceItem>

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  suspend fun insertInvoice(invoice: PurchaseInvoice)

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  suspend fun insertInvoiceItems(items: List<InvoiceItem>)

  @Update
  suspend fun updateInvoice(invoice: PurchaseInvoice)

  @Update
  suspend fun updateInvoiceItem(item: InvoiceItem)

  @Query("DELETE FROM invoice_items WHERE id = :itemId")
  suspend fun deleteInvoiceItem(itemId: String)
}

@Dao
interface ShiftDao {
  @Query("SELECT * FROM employee_shifts ORDER BY date ASC, startTime ASC")
  fun getAllShifts(): Flow<List<EmployeeShift>>

  @Query("SELECT * FROM employee_shifts WHERE storeId = :storeId ORDER BY date ASC, startTime ASC")
  fun getShiftsByStore(storeId: String): Flow<List<EmployeeShift>>

  @Query("SELECT * FROM employee_shifts WHERE employeeId = :employeeId ORDER BY date ASC, startTime ASC")
  fun getShiftsByEmployee(employeeId: String): Flow<List<EmployeeShift>>

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  suspend fun insertShift(shift: EmployeeShift)

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  suspend fun insertShifts(shifts: List<EmployeeShift>)

  @Update
  suspend fun updateShift(shift: EmployeeShift)

  @Query("DELETE FROM employee_shifts WHERE id = :id")
  suspend fun deleteShift(id: String)
}

@Dao
interface NotificationDao {
  @Query("SELECT * FROM notifications ORDER BY timestamp DESC")
  fun getAllNotifications(): Flow<List<AppNotification>>

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  suspend fun insertNotification(notification: AppNotification)

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  suspend fun insertNotifications(notifications: List<AppNotification>)

  @Query("UPDATE notifications SET isRead = 1 WHERE id = :id")
  suspend fun markAsRead(id: String)

  @Query("UPDATE notifications SET isRead = 1")
  suspend fun markAllAsRead()

  @Query("DELETE FROM notifications")
  suspend fun clearAllNotifications()
}

@Dao
interface AuditDao {
  @Query("SELECT * FROM audit_logs ORDER BY timestamp DESC")
  fun getAllAuditLogs(): Flow<List<AuditLog>>

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  suspend fun insertAuditLog(log: AuditLog)

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  suspend fun insertAuditLogs(logs: List<AuditLog>)
}
