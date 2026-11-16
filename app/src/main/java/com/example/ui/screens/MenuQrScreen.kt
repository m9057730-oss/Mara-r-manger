package com.example.ui.screens

import android.content.Intent
import android.graphics.Bitmap
import android.graphics.ImageDecoder
import android.os.Build
import android.provider.MediaStore
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.CameraAlt
import androidx.compose.material.icons.rounded.DocumentScanner
import androidx.compose.material.icons.rounded.QrCode2
import androidx.compose.material.icons.rounded.RestaurantMenu
import androidx.compose.material.icons.rounded.Share
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.example.R
import com.example.data.ai.ScannedMenuItem
import com.example.data.local.MenuCategory
import com.example.data.local.MenuItemEntity
import com.example.ui.components.AiNoticeBanner
import com.example.ui.i18n.AppStrings
import com.example.ui.theme.LossRed
import com.example.ui.theme.ProfitGreen
import com.example.ui.viewmodel.RestaurantUiState
import com.example.util.QrCodeCard

@Composable
fun MenuQrScreen(
    state: RestaurantUiState,
    strings: AppStrings,
    onAddMenuItem: (String, String, Double, Double, Boolean, Int, String) -> Unit,
    onToggleAvailability: (MenuItemEntity) -> Unit,
    onScanMenuPhoto: (Bitmap?) -> Unit,
    onImportScannedItems: (List<ScannedMenuItem>) -> Unit
) {
    val context = LocalContext.current
    var selectedCategory by remember { mutableStateOf("ALL") }
    var selectedTable by remember { mutableIntStateOf(1) }
    var showQrDialog by remember { mutableStateOf(false) }
    var showAddDishDialog by remember { mutableStateOf(false) }
    var scannedBitmap by remember { mutableStateOf<Bitmap?>(null) }

    val photoPicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri ->
        if (uri != null) {
            val bmp = try {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                    val src = ImageDecoder.createSource(context.contentResolver, uri)
                    ImageDecoder.decodeBitmap(src) { decoder, _, _ -> decoder.allocator = ImageDecoder.ALLOCATOR_SOFTWARE }
                } else {
                    @Suppress("DEPRECATION")
                    MediaStore.Images.Media.getBitmap(context.contentResolver, uri)
                }
            } catch (_: Exception) {
                null
            }
            scannedBitmap = bmp
            onScanMenuPhoto(bmp)
        }
    }

    val categories = listOf(
        "ALL" to "All Fine-Dine",
        MenuCategory.STARTERS to "Starters",
        MenuCategory.TANDOOR to "Tandoor",
        MenuCategory.ROYAL_MAINS to "Royal Mains",
        MenuCategory.BREADS to "Breads",
        MenuCategory.COLD_DRINKS to "Cold Drinks",
        MenuCategory.DESSERTS to "Desserts"
    )

    val visibleMenu = remember(state.menuItems, selectedCategory) {
        if (selectedCategory == "ALL") state.menuItems
        else state.menuItems.filter { it.category == selectedCategory }
    }

    Scaffold(
        containerColor = Color.Transparent,
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showAddDishDialog = true },
                modifier = Modifier.testTag("add_menu_dish_fab"),
                containerColor = MaterialTheme.colorScheme.primary
            ) {
                Icon(Icons.Rounded.Add, contentDescription = "Add Menu Dish")
            }
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 12.dp, bottom = 92.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // 1. Fine-Dine Hero + QR Menu & Photo Scan Actions
            item {
                Card(
                    shape = RoundedCornerShape(22.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Column {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(130.dp)
                        ) {
                            Image(
                                painter = painterResource(id = R.drawable.img_fine_dine_dish),
                                contentDescription = strings.fineDineMenu,
                                contentScale = ContentScale.Crop,
                                modifier = Modifier.fillMaxSize()
                            )
                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .background(
                                        Brush.verticalGradient(
                                            listOf(Color.Transparent, Color.Black.copy(alpha = 0.7f))
                                        )
                                    )
                            )
                            Column(
                                modifier = Modifier
                                    .align(Alignment.BottomStart)
                                    .padding(14.dp)
                            ) {
                                Text(
                                    text = "${state.settings.restaurantName} • ${strings.fineDineMenu}",
                                    style = MaterialTheme.typography.titleLarge,
                                    color = Color.White
                                )
                                Text(
                                    text = "${state.menuItems.size} curated dishes & chilled beverages",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = Color.White.copy(alpha = 0.88f)
                                )
                            }
                        }

                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Button(
                                onClick = {
                                    photoPicker.launch(
                                        PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                                    )
                                },
                                modifier = Modifier
                                    .weight(1f)
                                    .testTag("photo_scan_menu_btn"),
                                shape = RoundedCornerShape(14.dp)
                            ) {
                                Icon(Icons.Rounded.DocumentScanner, contentDescription = strings.scanMenuPhoto, modifier = Modifier.size(18.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(strings.scanMenuPhoto, style = MaterialTheme.typography.labelLarge)
                            }
                            OutlinedButton(
                                onClick = { showQrDialog = true },
                                modifier = Modifier
                                    .weight(1f)
                                    .testTag("qr_order_menu_btn"),
                                shape = RoundedCornerShape(14.dp)
                            ) {
                                Icon(Icons.Rounded.QrCode2, contentDescription = strings.qrOrderMenu, modifier = Modifier.size(18.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(strings.qrOrderMenu, style = MaterialTheme.typography.labelLarge)
                            }
                        }
                    }
                }
            }

            // 2. AI Menu Photo Scan Result Preview
            if (state.aiScanLoading || state.aiScanResult != null) {
                item {
                    Card(
                        shape = RoundedCornerShape(20.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                    ) {
                        Column(
                            modifier = Modifier.padding(14.dp),
                            verticalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Rounded.CameraAlt, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Menu Photo Scanner", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                            }
                            scannedBitmap?.let { bmp ->
                                Image(
                                    bitmap = bmp.asImageBitmap(),
                                    contentDescription = "Selected menu photo",
                                    contentScale = ContentScale.Crop,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(110.dp)
                                        .clip(RoundedCornerShape(12.dp))
                                )
                            }
                            if (state.aiScanLoading) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    CircularProgressIndicator(modifier = Modifier.size(20.dp), strokeWidth = 2.dp)
                                    Spacer(modifier = Modifier.width(10.dp))
                                    Text("Reading menu dishes & prices...")
                                }
                            } else if (state.aiScanResult != null) {
                                AiNoticeBanner(notice = state.aiScanResult.notice)
                                state.aiScanResult.data.forEach { dish ->
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Text("• ${dish.name} (${dish.category})", style = MaterialTheme.typography.bodySmall, modifier = Modifier.weight(1f))
                                        Text("₹${dish.price.toInt()}", style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.Bold)
                                    }
                                }
                                Button(
                                    onClick = { onImportScannedItems(state.aiScanResult.data) },
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Text("Import All ${state.aiScanResult.data.size} Dishes to Fine-Dine Menu")
                                }
                            }
                        }
                    }
                }
            }

            // 3. Category Tabs
            item {
                LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(categories) { (key, label) ->
                        FilterChip(
                            selected = selectedCategory == key,
                            onClick = { selectedCategory = key },
                            label = { Text(label) }
                        )
                    }
                }
            }

            // 4. Fine-Dine Menu Items
            items(visibleMenu, key = { it.id }) { item ->
                Card(
                    shape = RoundedCornerShape(18.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .size(12.dp)
                                        .border(
                                            width = 1.5.dp,
                                            color = if (item.isVeg) ProfitGreen else LossRed,
                                            shape = RoundedCornerShape(3.dp)
                                        ),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(6.dp)
                                            .clip(CircleShape)
                                            .background(if (item.isVeg) ProfitGreen else LossRed)
                                    )
                                }
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = item.name,
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                            if (item.chefNote.isNotBlank()) {
                                Spacer(modifier = Modifier.height(3.dp))
                                Text(
                                    text = item.chefNote,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            Spacer(modifier = Modifier.height(6.dp))
                            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                                Text(
                                    text = "₹${item.price.toInt()}",
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.primary
                                )
                                Text(
                                    text = "Cost ₹${item.costPrice.toInt()} • ${item.prepMinutes}m",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                        Switch(
                            checked = item.isAvailable,
                            onCheckedChange = { onToggleAvailability(item) }
                        )
                    }
                }
            }
        }
    }

    // QR Order Menu Dialog
    if (showQrDialog) {
        val qrPayload = "https://dinejoy.menu/${state.settings.restaurantName.replace(" ", "-").lowercase()}?table=$selectedTable"
        AlertDialog(
            onDismissRequest = { showQrDialog = false },
            title = {
                Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
                    Text(state.settings.restaurantName, fontWeight = FontWeight.Bold)
                    Text("Scan for Fine-Dine Digital Menu", style = MaterialTheme.typography.bodySmall)
                }
            },
            text = {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        items((1..8).toList()) { tbl ->
                            FilterChip(
                                selected = selectedTable == tbl,
                                onClick = { selectedTable = tbl },
                                label = { Text("T$tbl") }
                            )
                        }
                    }
                    QrCodeCard(content = qrPayload, size = 190.dp)
                    Surface(
                        color = MaterialTheme.colorScheme.primaryContainer,
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Text(
                            text = "Table $selectedTable • Instant QR Ordering",
                            style = MaterialTheme.typography.labelLarge,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                        )
                    }
                }
            },
            confirmButton = {
                Button(onClick = {
                    val share = Intent(Intent.ACTION_SEND).apply {
                        type = "text/plain"
                        putExtra(Intent.EXTRA_TEXT, "Order from ${state.settings.restaurantName} (Table $selectedTable): $qrPayload")
                    }
                    context.startActivity(Intent.createChooser(share, "Share QR Menu Link"))
                }) {
                    Icon(Icons.Rounded.Share, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Share Link")
                }
            },
            dismissButton = {
                TextButton(onClick = { showQrDialog = false }) { Text("Close") }
            }
        )
    }

    // Add Dish Dialog
    if (showAddDishDialog) {
        AddDishDialog(
            onDismiss = { showAddDishDialog = false },
            onSave = { name, cat, price, cost, veg, prep, note ->
                onAddMenuItem(name, cat, price, cost, veg, prep, note)
                showAddDishDialog = false
            }
        )
    }
}

@Composable
private fun AddDishDialog(
    onDismiss: () -> Unit,
    onSave: (String, String, Double, Double, Boolean, Int, String) -> Unit
) {
    var name by remember { mutableStateOf("") }
    var category by remember { mutableStateOf(MenuCategory.ROYAL_MAINS) }
    var price by remember { mutableStateOf("420") }
    var cost by remember { mutableStateOf("140") }
    var prep by remember { mutableStateOf("15") }
    var note by remember { mutableStateOf("") }
    var isVeg by remember { mutableStateOf(true) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Rounded.RestaurantMenu, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Add Fine-Dine Dish")
            }
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(value = name, onValueChange = { name = it }, label = { Text("Dish Name") }, modifier = Modifier.fillMaxWidth())
                LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    items(
                        listOf(
                            MenuCategory.STARTERS,
                            MenuCategory.TANDOOR,
                            MenuCategory.ROYAL_MAINS,
                            MenuCategory.BREADS,
                            MenuCategory.COLD_DRINKS,
                            MenuCategory.DESSERTS
                        )
                    ) { cat ->
                        FilterChip(selected = category == cat, onClick = { category = cat }, label = { Text(cat.replace('_', ' ')) })
                    }
                }
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = price,
                        onValueChange = { price = it },
                        label = { Text("Sell ₹") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.weight(1f)
                    )
                    OutlinedTextField(
                        value = cost,
                        onValueChange = { cost = it },
                        label = { Text("Cost ₹") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.weight(1f)
                    )
                    OutlinedTextField(
                        value = prep,
                        onValueChange = { prep = it },
                        label = { Text("Mins") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.weight(0.8f)
                    )
                }
                OutlinedTextField(value = note, onValueChange = { note = it }, label = { Text("Chef Tasting Note") }, modifier = Modifier.fillMaxWidth())
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(if (isVeg) "Pure Veg Dish" else "Non-Veg Dish", fontWeight = FontWeight.SemiBold)
                    Switch(checked = isVeg, onCheckedChange = { isVeg = it })
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    onSave(
                        name,
                        category,
                        price.toDoubleOrNull() ?: 350.0,
                        cost.toDoubleOrNull() ?: 110.0,
                        isVeg,
                        prep.toIntOrNull() ?: 15,
                        note
                    )
                },
                enabled = name.isNotBlank()
            ) { Text("Add Dish") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel") } }
    )
}
