package com.example.data.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.example.data.dao.AuditDao
import com.example.data.dao.InvoiceDao
import com.example.data.dao.NotificationDao
import com.example.data.dao.ProductDao
import com.example.data.dao.ShiftDao
import com.example.data.dao.StoreDao
import com.example.data.dao.StoreStockDao
import com.example.data.dao.TransactionDao
import com.example.data.dao.TransferDao
import com.example.data.dao.UserDao
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

@Database(
  entities = [
    Store::class,
    AppUser::class,
    Product::class,
    StoreStock::class,
    StockTransaction::class,
    StockTransfer::class,
    PurchaseInvoice::class,
    InvoiceItem::class,
    EmployeeShift::class,
    AppNotification::class,
    AuditLog::class
  ],
  version = 2,
  exportSchema = false
)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {
  abstract fun storeDao(): StoreDao
  abstract fun userDao(): UserDao
  abstract fun productDao(): ProductDao
  abstract fun storeStockDao(): StoreStockDao
  abstract fun transactionDao(): TransactionDao
  abstract fun transferDao(): TransferDao
  abstract fun invoiceDao(): InvoiceDao
  abstract fun shiftDao(): ShiftDao
  abstract fun notificationDao(): NotificationDao
  abstract fun auditDao(): AuditDao

  companion object {
    @Volatile
    private var INSTANCE: AppDatabase? = null

    fun getDatabase(context: Context): AppDatabase {
      return INSTANCE ?: synchronized(this) {
        val instance = Room.databaseBuilder(
          context.applicationContext,
          AppDatabase::class.java,
          "retail_flow_inventory.db"
        )
          .fallbackToDestructiveMigration()
          .build()
        INSTANCE = instance
        instance
      }
    }
  }
}
