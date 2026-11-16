package com.example.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.Celebration
import androidx.compose.material.icons.rounded.LocalDrink
import androidx.compose.material.icons.rounded.Phone
import androidx.compose.material.icons.rounded.Remove
import androidx.compose.material.icons.rounded.Send
import androidx.compose.material.icons.rounded.WarningAmber
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.example.R
import com.example.data.local.InventoryCategory
import com.example.data.local.InventoryItemEntity
import com.example.ui.components.LowStockAlertBanner
import com.example.ui.i18n.AppStrings
import com.example.ui.theme.LossRed
import com.example.ui.theme.ProfitGreen
import com.example.ui.viewmodel.RestaurantUiState
import com.example.util.WhatsAppHelper

@Composable
fun InventoryPartyScreen(
    state: RestaurantUiState,
    strings: AppStrings,
    onAdjustStock: (InventoryItemEntity, Double) -> Unit,
    onUpdateChefNeeded: (InventoryItemEntity, Double) -> Unit,
    onAddInventoryItem: (String, String, String, Double, Double, Double) -> Unit,
    onAddPartyBooking: (String, String, String, Int, Double, Map<InventoryItemEntity, Double>) -> Unit
) {
    val context = LocalContext.current
    var subTab by remember { mutableIntStateOf(0) } // 0=Inventory, 1=Chef WhatsApp, 2=Party Book
    var selectedCategory by remember { mutableStateOf("ALL") }
    var showAddInventoryDialog by remember { mutableStateOf(false) }
    var showAddPartyDialog by remember { mutableStateOf(false) }

    val subTabs = listOf("Inventory", "Chef WhatsApp", strings.partyBook)

    Scaffold(
        containerColor = Color.Transparent,
        floatingActionButton = {
            if (subTab == 0 || subTab == 2) {
                FloatingActionButton(
                    onClick = {
                        if (subTab == 0) showAddInventoryDialog = true else showAddPartyDialog = true
                    },
                    modifier = Modifier.testTag("inventory_party_fab"),
                    containerColor = MaterialTheme.colorScheme.primary
                ) {
                    Icon(Icons.Rounded.Add, contentDescription = "Add entry")
                }
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            SingleChoiceSegmentedButtonRow(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 10.dp)
            ) {
                subTabs.forEachIndexed { index, label ->
                    SegmentedButton(
                        selected = subTab == index,
                        onClick = { subTab = index },
                        shape = SegmentedButtonDefaults.itemShape(index = index, count = subTabs.size)
                    ) {
                        Text(label, maxLines = 1, style = MaterialTheme.typography.labelMedium)
                    }
                }
            }

            when (subTab) {
                0 -> InventoryListTab(
                    state = state,
                    strings = strings,
                    selectedCategory = selectedCategory,
                    onSelectCategory = { selectedCategory = it },
                    onAdjustStock = onAdjustStock,
                    onOpenChefReport = { subTab = 1 }
                )
                1 -> ChefWhatsAppTab(
                    state = state,
                    strings = strings,
                    onUpdateChefNeeded = onUpdateChefNeeded,
                    onSendWhatsApp = { report ->
                        WhatsAppHelper.sendToWhatsApp(context, report)
                    }
                )
                2 -> PartyBookTab(
                    state = state,
                    strings = strings,
                    onOpenNewBooking = { showAddPartyDialog = true }
                )
            }
        }
    }

    if (showAddInventoryDialog) {
        AddInventoryDialog(
            onDismiss = { showAddInventoryDialog = false },
            onSave = { name, cat, unit, stock, min, cost ->
                onAddInventoryItem(name, cat, unit, stock, min, cost)
                showAddInventoryDialog = false
            }
        )
    }

    if (showAddPartyDialog) {
        AddPartyBookingDialog(
            inventory = state.inventory,
            onDismiss = { showAddPartyDialog = false },
            onSave = { name, phone, date, guests, bill, usedMap ->
                onAddPartyBooking(name, phone, date, guests, bill, usedMap)
                showAddPartyDialog = false
            }
        )
    }
}

@Composable
private fun InventoryListTab(
    state: RestaurantUiState,
    strings: AppStrings,
    selectedCategory: String,
    onSelectCategory: (String) -> Unit,
    onAdjustStock: (InventoryItemEntity, Double) -> Unit,
    onOpenChefReport: () -> Unit
) {
    val categories = listOf(
        "ALL" to "All Stock",
        InventoryCategory.KITCHEN to strings.kitchenInventory,
        InventoryCategory.TANDOOR to strings.tandoorInventory,
        InventoryCategory.PACKING to strings.packingMaterial,
        InventoryCategory.COLD_DRINK to strings.coldDrinksInventory
    )

    val filtered = remember(state.inventory, selectedCategory) {
        if (selectedCategory == "ALL") state.inventory
        else state.inventory.filter { it.category == selectedCategory }
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(start = 16.dp, end = 16.dp, bottom = 92.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        if (state.outOfStockItems.isNotEmpty() || state.lowStockItems.isNotEmpty()) {
            item {
                LowStockAlertBanner(
                    outCount = state.outOfStockItems.size,
                    lowCount = state.lowStockItems.size,
                    title = strings.lowStockAlert,
                    onClick = onOpenChefReport
                )
            }
        }

        // Separate Cold Drinks Highlight Card
        item {
            val coldDrinks = state.inventory.filter { it.category == InventoryCategory.COLD_DRINK }
            val totalBottles = coldDrinks.sumOf { it.currentStock }.toInt()
            Surface(
                color = Color(0xFF0284C7).copy(alpha = 0.10f),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Rounded.LocalDrink, contentDescription = null, tint = Color(0xFF0284C7))
                    Spacer(modifier = Modifier.width(10.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Cold Drinks Cellar (Coca-Cola, Pepsi, Sprite)",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "$totalBottles total cans/bottles across ${coldDrinks.size} beverages",
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                }
            }
        }

        item {
            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                items(categories) { (key, label) ->
                    FilterChip(
                        selected = selectedCategory == key,
                        onClick = { onSelectCategory(key) },
                        label = { Text(label) }
                    )
                }
            }
        }

        items(filtered, key = { it.id }) { item ->
            val isOut = item.currentStock <= 0.0
            val isLow = !isOut && item.currentStock <= item.minThreshold
            val statusColor = when {
                isOut -> LossRed
                isLow -> Color(0xFFD97706)
                else -> ProfitGreen
            }
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
                            Text(
                                text = item.name,
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold
                            )
                            if (isOut || isLow) {
                                Spacer(modifier = Modifier.width(6.dp))
                                Icon(Icons.Rounded.WarningAmber, contentDescription = "Alert", tint = statusColor, modifier = Modifier.size(16.dp))
                            }
                        }
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = "${item.category.replace('_', ' ')} • Min ${item.minThreshold.toInt()} ${item.unit} • ₹${item.costPerUnit.toInt()}/${item.unit}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Surface(
                            color = statusColor.copy(alpha = 0.14f),
                            shape = RoundedCornerShape(50)
                        ) {
                            Text(
                                text = if (isOut) "OUT OF STOCK (0 ${item.unit})"
                                else if (isLow) "LOW: ${item.currentStock.toInt()} ${item.unit}"
                                else "In Stock: ${item.currentStock.toInt()} ${item.unit}",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = statusColor,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                            )
                        }
                    }

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        IconButton(onClick = { onAdjustStock(item, -1.0) }) {
                            Icon(Icons.Rounded.Remove, contentDescription = "Decrease stock")
                        }
                        IconButton(onClick = { onAdjustStock(item, +1.0) }) {
                            Icon(Icons.Rounded.Add, contentDescription = "Increase stock", tint = MaterialTheme.colorScheme.primary)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun ChefWhatsAppTab(
    state: RestaurantUiState,
    strings: AppStrings,
    onUpdateChefNeeded: (InventoryItemEntity, Double) -> Unit,
    onSendWhatsApp: (String) -> Unit
) {
    val reportPreview = remember(state.settings.restaurantName, state.inventory) {
        WhatsAppHelper.buildChefInventoryReport(state.settings.restaurantName, state.inventory)
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Card(
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text(strings.chefRequirement, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    Text(
                        text = "Covers Kitchen, Tandoor, Packing Material, and Cold Drinks (Coca-Cola / Pepsi).",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Button(
                        onClick = { onSendWhatsApp(reportPreview) },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF16A34A)),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp)
                            .testTag("send_whatsapp_report_btn")
                    ) {
                        Icon(Icons.Rounded.Send, contentDescription = strings.sendWhatsApp, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(strings.sendWhatsApp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        item {
            Surface(
                color = MaterialTheme.colorScheme.surfaceVariant,
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = reportPreview,
                    style = MaterialTheme.typography.bodySmall,
                    modifier = Modifier.padding(14.dp)
                )
            }
        }

        items(state.inventory, key = { it.id }) { item ->
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(item.name, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold)
                        Text(
                            text = "${item.category.replace('_', ' ')} • Left: ${item.currentStock.toInt()} ${item.unit}",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        IconButton(onClick = { onUpdateChefNeeded(item, (item.chefNeededQty - 2.0).coerceAtLeast(0.0)) }) {
                            Icon(Icons.Rounded.Remove, contentDescription = "Less needed")
                        }
                        Text(
                            text = "Need ${item.chefNeededQty.toInt()}",
                            style = MaterialTheme.typography.labelLarge,
                            fontWeight = FontWeight.Bold
                        )
                        IconButton(onClick = { onUpdateChefNeeded(item, item.chefNeededQty + 2.0) }) {
                            Icon(Icons.Rounded.Add, contentDescription = "More needed", tint = MaterialTheme.colorScheme.primary)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun PartyBookTab(
    state: RestaurantUiState,
    strings: AppStrings,
    onOpenNewBooking: () -> Unit
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(start = 16.dp, end = 16.dp, bottom = 92.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Card(
                shape = RoundedCornerShape(22.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(125.dp)
                    ) {
                        Image(
                            painter = painterResource(id = R.drawable.img_party_catering),
                            contentDescription = strings.partyBook,
                            contentScale = ContentScale.Crop,
                            modifier = Modifier.fillMaxSize()
                        )
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(Brush.verticalGradient(listOf(Color.Transparent, Color.Black.copy(alpha = 0.7f))))
                        )
                        Column(
                            modifier = Modifier
                                .align(Alignment.BottomStart)
                                .padding(14.dp)
                        ) {
                            Text(strings.partyBook, style = MaterialTheme.typography.titleLarge, color = Color.White)
                            Text("Track Party Name, Number & Exact Inventory Used", style = MaterialTheme.typography.bodySmall, color = Color.White.copy(alpha = 0.88f))
                        }
                    }
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("${state.partyBookings.size} Party Bookings", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                        Button(onClick = onOpenNewBooking, modifier = Modifier.testTag("new_party_booking_btn")) {
                            Icon(Icons.Rounded.Celebration, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(strings.addPartyBooking)
                        }
                    }
                }
            }
        }

        items(state.partyBookings, key = { it.id }) { party ->
            Card(
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(party.partyName, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                        Text("₹${party.totalBillAmount.toInt()}", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = ProfitGreen)
                    }
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Rounded.Phone, contentDescription = null, modifier = Modifier.size(14.dp), tint = MaterialTheme.colorScheme.primary)
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("${party.partyPhone} • ${party.eventDate} • ${party.guestCount} Guests", style = MaterialTheme.typography.bodySmall)
                    }
                    Surface(
                        color = MaterialTheme.colorScheme.surfaceVariant,
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(10.dp)) {
                            Text("Inventory Consumed (Cost ₹${party.inventoryCost.toInt()}):", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold)
                            Text(party.inventoryUsedSummary, style = MaterialTheme.typography.bodySmall)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun AddInventoryDialog(
    onDismiss: () -> Unit,
    onSave: (String, String, String, Double, Double, Double) -> Unit
) {
    var name by remember { mutableStateOf("") }
    var category by remember { mutableStateOf(InventoryCategory.KITCHEN) }
    var unit by remember { mutableStateOf("kg") }
    var stock by remember { mutableStateOf("10") }
    var minStock by remember { mutableStateOf("5") }
    var cost by remember { mutableStateOf("120") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Add Inventory Item") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(value = name, onValueChange = { name = it }, label = { Text("Item Name") }, modifier = Modifier.fillMaxWidth())
                LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    items(
                        listOf(
                            InventoryCategory.KITCHEN to "Kitchen",
                            InventoryCategory.TANDOOR to "Tandoor",
                            InventoryCategory.PACKING to "Packing",
                            InventoryCategory.COLD_DRINK to "Cold Drink"
                        )
                    ) { (cat, label) ->
                        FilterChip(selected = category == cat, onClick = { category = cat }, label = { Text(label) })
                    }
                }
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(value = unit, onValueChange = { unit = it }, label = { Text("Unit") }, modifier = Modifier.weight(1f))
                    OutlinedTextField(
                        value = stock,
                        onValueChange = { stock = it },
                        label = { Text("Stock") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.weight(1f)
                    )
                }
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = minStock,
                        onValueChange = { minStock = it },
                        label = { Text("Alert Min") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.weight(1f)
                    )
                    OutlinedTextField(
                        value = cost,
                        onValueChange = { cost = it },
                        label = { Text("Cost/Unit ₹") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    onSave(
                        name,
                        category,
                        unit,
                        stock.toDoubleOrNull() ?: 0.0,
                        minStock.toDoubleOrNull() ?: 5.0,
                        cost.toDoubleOrNull() ?: 0.0
                    )
                },
                enabled = name.isNotBlank()
            ) { Text("Save") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel") } }
    )
}

@Composable
private fun AddPartyBookingDialog(
    inventory: List<InventoryItemEntity>,
    onDismiss: () -> Unit,
    onSave: (String, String, String, Int, Double, Map<InventoryItemEntity, Double>) -> Unit
) {
    var partyName by remember { mutableStateOf("") }
    var partyPhone by remember { mutableStateOf("") }
    var eventDate by remember { mutableStateOf("20 Nov 2026") }
    var guests by remember { mutableStateOf("40") }
    var billAmount by remember { mutableStateOf("28000") }
    val usedQtyMap = remember { mutableStateMapOf<Int, Double>() }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("New Party Book Entry") },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedTextField(value = partyName, onValueChange = { partyName = it }, label = { Text("Party Name") }, modifier = Modifier.fillMaxWidth())
                OutlinedTextField(value = partyPhone, onValueChange = { partyPhone = it }, label = { Text("Party Number / Phone") }, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone), modifier = Modifier.fillMaxWidth())
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(value = eventDate, onValueChange = { eventDate = it }, label = { Text("Date") }, modifier = Modifier.weight(1f))
                    OutlinedTextField(value = guests, onValueChange = { guests = it }, label = { Text("Guests") }, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number), modifier = Modifier.weight(0.7f))
                }
                OutlinedTextField(value = billAmount, onValueChange = { billAmount = it }, label = { Text("Total Party Bill (₹)") }, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number), modifier = Modifier.fillMaxWidth())

                Text("Inventory Used (Auto-deducts stock):", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                inventory.take(8).forEach { item ->
                    val qty = usedQtyMap[item.id] ?: 0.0
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("${item.name} (${item.unit})", style = MaterialTheme.typography.bodySmall, modifier = Modifier.weight(1f))
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            IconButton(
                                onClick = { if (qty > 0) usedQtyMap[item.id] = (qty - 1).coerceAtLeast(0.0) },
                                modifier = Modifier.size(32.dp)
                            ) { Icon(Icons.Rounded.Remove, contentDescription = "Less") }
                            Text("${qty.toInt()}", style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.Bold)
                            IconButton(
                                onClick = { usedQtyMap[item.id] = qty + 1 },
                                modifier = Modifier.size(32.dp)
                            ) { Icon(Icons.Rounded.Add, contentDescription = "More") }
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val selected = inventory.mapNotNull { item ->
                        val q = usedQtyMap[item.id] ?: 0.0
                        if (q > 0) item to q else null
                    }.toMap()
                    onSave(
                        partyName,
                        partyPhone,
                        eventDate,
                        guests.toIntOrNull() ?: 25,
                        billAmount.toDoubleOrNull() ?: 15000.0,
                        selected
                    )
                },
                enabled = partyName.isNotBlank()
            ) { Text("Book Party") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel") } }
    )
}
