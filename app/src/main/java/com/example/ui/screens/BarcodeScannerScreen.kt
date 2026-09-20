package com.example.ui.screens

import android.content.Context
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.FlashOff
import androidx.compose.material.icons.filled.FlashOn
import androidx.compose.material.icons.filled.Inventory
import androidx.compose.material.icons.filled.Keyboard
import androidx.compose.material.icons.filled.MoveDown
import androidx.compose.material.icons.filled.QrCodeScanner
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.filled.ShoppingBag
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.Product
import com.example.data.model.StoreStock
import com.example.data.model.TransactionType
import com.example.ui.components.BadgeType
import com.example.ui.components.StatusBadge
import com.example.ui.theme.RetailPrimary
import com.example.ui.theme.RetailSecondary
import com.example.ui.theme.StatusAmber
import com.example.ui.theme.StatusGreen
import com.example.ui.theme.StatusRed
import java.util.Locale

@Composable
fun BarcodeScannerScreen(
  products: List<Product>,
  stocks: List<StoreStock>,
  scannedProduct: Product?,
  barcodeNotFound: String?,
  selectedStoreId: String,
  onScanBarcode: (String) -> Unit,
  onClearScan: () -> Unit,
  onQuickAdjust: (Product, Int, TransactionType) -> Unit,
  onOpenAdjustStock: (Product) -> Unit,
  onOpenTransfer: (Product) -> Unit,
  onOpenProductDetail: (Product) -> Unit,
  onAddNewProductWithBarcode: (String) -> Unit
) {
  val context = LocalContext.current
  var isFlashOn by remember { mutableStateOf(false) }
  var manualBarcode by remember { mutableStateOf("") }
  var showManualInput by remember { mutableStateOf(false) }

  // Scanning laser animation
  val infiniteTransition = rememberInfiniteTransition(label = "laser")
  val laserPosition by infiniteTransition.animateFloat(
    initialValue = 0f,
    targetValue = 1f,
    animationSpec = infiniteRepeatable(
      animation = tween(1500, easing = FastOutSlowInEasing),
      repeatMode = RepeatMode.Reverse
    ),
    label = "laser_y"
  )

  fun triggerHaptic() {
    try {
      val vibrator = context.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
      if (vibrator != null && vibrator.hasVibrator()) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
          vibrator.vibrate(VibrationEffect.createOneShot(50, VibrationEffect.DEFAULT_AMPLITUDE))
        } else {
          @Suppress("DEPRECATION")
          vibrator.vibrate(50)
        }
      }
    } catch (_: Exception) {}
  }

  Column(
    modifier = Modifier
      .fillMaxSize()
      .padding(16.dp)
      .verticalScroll(rememberScrollState()),
    horizontalAlignment = Alignment.CenterHorizontally
  ) {
    // 1. Viewfinder Box
    Box(
      modifier = Modifier
        .fillMaxWidth()
        .height(240.dp)
        .clip(RoundedCornerShape(16.dp))
        .background(Color(0xFF1E293B)),
      contentAlignment = Alignment.Center
    ) {
      // Camera Reticle
      Box(
        modifier = Modifier
          .size(200.dp, 160.dp)
          .border(2.dp, RetailPrimary, RoundedCornerShape(12.dp))
      ) {
        // Laser line moving up and down
        Box(
          modifier = Modifier
            .fillMaxWidth()
            .height(2.dp)
            .padding(top = (160 * laserPosition).dp)
            .background(Color.Red)
        )
      }

      // Flash & Manual Input toggles
      Row(
        modifier = Modifier
          .align(Alignment.TopEnd)
          .padding(8.dp)
      ) {
        IconButton(
          onClick = { isFlashOn = !isFlashOn },
          modifier = Modifier.testTag("toggle_torch_button")
        ) {
          Icon(
            imageVector = if (isFlashOn) Icons.Default.FlashOn else Icons.Default.FlashOff,
            contentDescription = "Toggle Torch",
            tint = if (isFlashOn) StatusAmber else Color.White
          )
        }
        IconButton(
          onClick = { showManualInput = !showManualInput },
          modifier = Modifier.testTag("toggle_manual_barcode_input")
        ) {
          Icon(
            imageVector = Icons.Default.Keyboard,
            contentDescription = "Manual Code",
            tint = Color.White
          )
        }
      }

      Text(
        text = "Point camera at retail barcode",
        style = MaterialTheme.typography.bodySmall,
        color = Color.White.copy(alpha = 0.8f),
        modifier = Modifier
          .align(Alignment.BottomCenter)
          .padding(bottom = 12.dp)
      )
    }

    Spacer(modifier = Modifier.height(12.dp))

    // Manual Barcode Input field
    if (showManualInput) {
      Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
      ) {
        OutlinedTextField(
          value = manualBarcode,
          onValueChange = { manualBarcode = it },
          placeholder = { Text("Enter barcode manually...") },
          singleLine = true,
          modifier = Modifier
            .weight(1f)
            .testTag("manual_barcode_input")
        )
        Spacer(modifier = Modifier.width(8.dp))
        Button(
          onClick = {
            if (manualBarcode.isNotBlank()) {
              triggerHaptic()
              onScanBarcode(manualBarcode)
            }
          },
          enabled = manualBarcode.isNotBlank()
        ) {
          Text("Scan")
        }
      }
      Spacer(modifier = Modifier.height(12.dp))
    }

    // Quick Test Barcodes Row (For testing in emulator)
    Text(
      text = "Simulate Scan (Sample Barcodes):",
      style = MaterialTheme.typography.labelMedium,
      color = MaterialTheme.colorScheme.onSurfaceVariant,
      modifier = Modifier.align(Alignment.Start)
    )
    Spacer(modifier = Modifier.height(6.dp))

    LazyRow(
      horizontalArrangement = Arrangement.spacedBy(8.dp),
      modifier = Modifier.fillMaxWidth()
    ) {
      items(products.take(6)) { p ->
        Surface(
          shape = RoundedCornerShape(8.dp),
          color = MaterialTheme.colorScheme.surfaceVariant,
          modifier = Modifier
            .clickable {
              triggerHaptic()
              onScanBarcode(p.barcode)
            }
            .testTag("simulate_scan_${p.id}")
        ) {
          Column(modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)) {
            Text(
              text = p.name,
              style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold),
              maxLines = 1
            )
            Text(
              text = p.barcode,
              style = MaterialTheme.typography.labelSmall,
              color = MaterialTheme.colorScheme.onSurfaceVariant
            )
          }
        }
      }
      // Unrecognized barcode for testing "New Product" flow
      item {
        Surface(
          shape = RoundedCornerShape(8.dp),
          color = StatusAmber.copy(alpha = 0.2f),
          modifier = Modifier
            .clickable {
              triggerHaptic()
              onScanBarcode("8909999999999")
            }
            .testTag("simulate_unknown_scan")
        ) {
          Column(modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)) {
            Text(
              text = "Unknown Barcode",
              style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold),
              color = StatusAmber
            )
            Text(
              text = "8909999999999",
              style = MaterialTheme.typography.labelSmall,
              color = MaterialTheme.colorScheme.onSurfaceVariant
            )
          }
        }
      }
    }

    Spacer(modifier = Modifier.height(16.dp))

    // 2. Scan Results Section
    if (scannedProduct != null) {
      val prod = scannedProduct
      val storeStocksList = stocks.filter { it.productId == prod.id }
      val currentStoreStock = if (selectedStoreId == "ALL") {
        storeStocksList.sumOf { it.quantity }
      } else {
        storeStocksList.firstOrNull { it.storeId == selectedStoreId }?.quantity ?: 0
      }

      ElevatedCard(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.elevatedCardColors(containerColor = MaterialTheme.colorScheme.surface),
        modifier = Modifier
          .fillMaxWidth()
          .testTag("scanned_product_card")
      ) {
        Column(modifier = Modifier.padding(16.dp)) {
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
          ) {
            StatusBadge(text = "PRODUCT DETECTED", type = BadgeType.SUCCESS)
            Text(
              text = "Clear",
              style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
              color = RetailPrimary,
              modifier = Modifier.clickable { onClearScan() }
            )
          }

          Spacer(modifier = Modifier.height(10.dp))

          Text(
            text = prod.name,
            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
          )
          Text(
            text = "SKU: ${prod.sku} • Barcode: ${prod.barcode}",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
          )

          Spacer(modifier = Modifier.height(10.dp))

          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
          ) {
            Column {
              Text(
                text = "Retail Price",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
              )
              Text(
                text = "€${String.format(Locale.US, "%.2f", prod.sellingPrice)}",
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                color = RetailPrimary
              )
            }
            Column {
              Text(
                text = "Current Stock",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
              )
              Text(
                text = "$currentStoreStock ${prod.unitType}s",
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                color = if (currentStoreStock <= prod.minStockLevel) StatusAmber else StatusGreen
              )
            }
            if (prod.isPerishable) {
              Column {
                Text(
                  text = "Expiry Date",
                  style = MaterialTheme.typography.labelSmall,
                  color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                  text = prod.expirationDate,
                  style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Medium)
                )
              }
            }
          }

          Spacer(modifier = Modifier.height(16.dp))

          // Instant Actions
          Text(
            text = "Instant Inventory Actions:",
            style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold)
          )
          Spacer(modifier = Modifier.height(8.dp))

          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
          ) {
            Button(
              onClick = {
                triggerHaptic()
                onQuickAdjust(prod, 10, TransactionType.RECEIVED)
              },
              colors = ButtonDefaults.buttonColors(containerColor = StatusGreen),
              modifier = Modifier
                .weight(1f)
                .testTag("quick_receive_10")
            ) {
              Icon(imageVector = Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
              Spacer(modifier = Modifier.width(4.dp))
              Text("Receive +10")
            }

            Button(
              onClick = {
                triggerHaptic()
                onQuickAdjust(prod, -1, TransactionType.SOLD)
              },
              colors = ButtonDefaults.buttonColors(containerColor = RetailPrimary),
              modifier = Modifier
                .weight(1f)
                .testTag("quick_sell_1")
            ) {
              Icon(imageVector = Icons.Default.ShoppingBag, contentDescription = null, modifier = Modifier.size(16.dp))
              Spacer(modifier = Modifier.width(4.dp))
              Text("Sell -1")
            }
          }

          Spacer(modifier = Modifier.height(8.dp))

          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
          ) {
            OutlinedButton(
              onClick = { onOpenAdjustStock(prod) },
              modifier = Modifier.weight(1f)
            ) {
              Text("Adjust")
            }
            OutlinedButton(
              onClick = { onOpenTransfer(prod) },
              modifier = Modifier.weight(1f)
            ) {
              Text("Transfer")
            }
            OutlinedButton(
              onClick = { onOpenProductDetail(prod) },
              modifier = Modifier.weight(1f)
            ) {
              Text("Details")
            }
          }
        }
      }
    } else if (barcodeNotFound != null) {
      // Barcode Not Found Card
      Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        modifier = Modifier
          .fillMaxWidth()
          .testTag("barcode_not_found_card")
      ) {
        Column(
          modifier = Modifier.padding(20.dp),
          horizontalAlignment = Alignment.CenterHorizontally
        ) {
          StatusBadge(text = "UNREGISTERED BARCODE", type = BadgeType.WARNING)
          Spacer(modifier = Modifier.height(10.dp))
          Text(
            text = "No Product Found",
            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
          )
          Spacer(modifier = Modifier.height(4.dp))
          Text(
            text = "Barcode \"$barcodeNotFound\" is not registered in your inventory system.",
            textAlign = TextAlign.Center,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
          )
          Spacer(modifier = Modifier.height(16.dp))
          Button(
            onClick = { onAddNewProductWithBarcode(barcodeNotFound) },
            colors = ButtonDefaults.buttonColors(containerColor = RetailPrimary),
            modifier = Modifier.testTag("create_scanned_product_button")
          ) {
            Icon(imageVector = Icons.Default.Add, contentDescription = null)
            Spacer(modifier = Modifier.width(8.dp))
            Text("Create New Product with this Barcode")
          }
        }
      }
    }
  }
}
