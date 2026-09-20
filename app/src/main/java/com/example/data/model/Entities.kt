package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

enum class UserRole(val displayName: String, val level: Int) {
  OWNER("Owner / Super Admin", 100),
  STORE_MANAGER("Store Manager", 80),
  INVENTORY_STAFF("Inventory Staff", 50),
  SALES_STAFF("Sales Staff", 30),
  EMPLOYEE("Employee", 10)
}

enum class TransactionType(val title: String) {
  RECEIVED("Stock Received"),
  SOLD("Stock Sold"),
  ADJUSTMENT("Stock Adjustment"),
  DAMAGED("Damaged Goods"),
  EXPIRED("Expired Goods"),
  RETURNED("Customer Returned"),
  TRANSFERRED("Transferred"),
  DISPOSED("Disposed"),
  STOCK_COUNT("Stock Count Correction")
}

@Entity(tableName = "stores")
data class Store(
  @PrimaryKey val id: String,
  val name: String,
  val code: String = "",
  val address: String = "",
  val city: String = "",
  val phone: String = "",
  val managerName: String = "",
  val businessType: String = "Alimentation", // "Alimentation", "Restaurant", "Café", "Other"
  val isActive: Boolean = true
)

@Entity(tableName = "users")
data class AppUser(
  @PrimaryKey val id: String,
  val name: String,
  val employeeCode: String = "",
  val email: String = "",
  val phone: String = "",
  val role: UserRole = UserRole.EMPLOYEE,
  val assignedStoreId: String = "ALL", // "ALL" or specific store ID
  val status: String = "Active", // "Active", "Inactive"
  val joiningDate: String = "2024-01-15",
  val emergencyContact: String = "+1 555-0199"
)

@Entity(tableName = "products")
data class Product(
  @PrimaryKey val id: String = java.util.UUID.randomUUID().toString(),
  val name: String,
  val sku: String = "",
  val barcode: String = "",
  val category: String = "General",
  val subcategory: String = "General",
  val brand: String = "",
  val supplier: String = "",
  val purchasePrice: Double = 0.0,
  val sellingPrice: Double = 0.0,
  val taxRate: Double = 0.08,
  val minStockLevel: Int = 10,
  val maxStockLevel: Int = 200,
  val unitType: String = "Piece", // Piece, Box, Pack, Kilogram, Gram, Liter, Milliliter, Meter
  val isPerishable: Boolean = false,
  val batchNumber: String = "",
  val manufacturingDate: String = "",
  val expirationDate: String = "", // YYYY-MM-DD
  val imageUrl: String = "",
  val status: String = "Active", // "Active", "Archived"
  val createdAt: Long = System.currentTimeMillis(),
  val updatedAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "store_stocks")
data class StoreStock(
  @PrimaryKey val id: String, // "$storeId-$productId"
  val storeId: String,
  val productId: String,
  val quantity: Int = 0,
  val expiredQuantity: Int = 0, // Segregated from sellable stock
  val locationInStore: String = "Aisle 1",
  val lastUpdated: Long = System.currentTimeMillis()
)

@Entity(tableName = "stock_transactions")
data class StockTransaction(
  @PrimaryKey val id: String,
  val storeId: String,
  val productId: String,
  val productName: String,
  val quantity: Int, // Positive for in, negative for out
  val transactionType: String,
  val previousStock: Int,
  val newStock: Int,
  val userId: String,
  val userName: String,
  val referenceNumber: String,
  val timestamp: Long = System.currentTimeMillis(),
  val notes: String = ""
)

@Entity(tableName = "stock_transfers")
data class StockTransfer(
  @PrimaryKey val id: String,
  val transferNumber: String,
  val sourceStoreId: String,
  val sourceStoreName: String,
  val destStoreId: String,
  val destStoreName: String,
  val productId: String,
  val productName: String,
  val quantity: Int,
  val status: String = "IN_TRANSIT", // "IN_TRANSIT", "COMPLETED", "CANCELLED"
  val initiatedByUserId: String,
  val initiatedByUserName: String,
  val receivedByUserId: String? = null,
  val receivedByUserName: String? = null,
  val createdAt: Long = System.currentTimeMillis(),
  val completedAt: Long? = null,
  val notes: String = ""
)

@Entity(tableName = "purchase_invoices")
data class PurchaseInvoice(
  @PrimaryKey val id: String,
  val invoiceNumber: String,
  val supplier: String,
  val invoiceDate: String,
  val storeId: String,
  val storeName: String,
  val totalAmount: Double,
  val status: String = "DRAFT_REVIEW", // "DRAFT_REVIEW", "APPROVED", "CANCELLED"
  val rawOcrText: String = "",
  val createdAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "invoice_items")
data class InvoiceItem(
  @PrimaryKey val id: String,
  val invoiceId: String,
  val productName: String,
  val sku: String,
  val barcode: String,
  val quantity: Int,
  val unitPrice: Double,
  val totalPrice: Double,
  val tax: Double = 0.0,
  val batchNumber: String = "",
  val expirationDate: String = "",
  val status: String = "Review" // "Confirmed", "Review"
)

@Entity(tableName = "employee_shifts")
data class EmployeeShift(
  @PrimaryKey val id: String,
  val employeeId: String,
  val employeeName: String = "",
  val storeId: String = "",
  val storeName: String = "",
  val date: String = "", // YYYY-MM-DD
  val startTime: String = "09:00", // HH:mm
  val endTime: String = "17:00", // HH:mm
  val breakDurationMinutes: Int = 30,
  val shiftType: String = "Morning", // Morning, Afternoon, Evening, Night
  val notes: String = "",
  val isPublished: Boolean = true
)

@Entity(tableName = "notifications")
data class AppNotification(
  @PrimaryKey val id: String,
  val title: String,
  val message: String,
  val type: String, // "LOW_STOCK", "OUT_OF_STOCK", "OVERSTOCK", "EXPIRATION", "DISPOSAL", "TRANSFER", "SHIFT"
  val relatedId: String? = null,
  val storeId: String? = null,
  val timestamp: Long = System.currentTimeMillis(),
  val isRead: Boolean = false
)

@Entity(tableName = "audit_logs")
data class AuditLog(
  @PrimaryKey val id: String,
  val userId: String,
  val userName: String,
  val action: String,
  val entityType: String,
  val entityId: String,
  val storeId: String,
  val details: String,
  val timestamp: Long = System.currentTimeMillis()
)
