package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
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
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.CreditCard
import androidx.compose.material.icons.rounded.Money
import androidx.compose.material.icons.rounded.Print
import androidx.compose.material.icons.rounded.QrCode2
import androidx.compose.material.icons.rounded.ReceiptLong
import androidx.compose.material.icons.rounded.Remove
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.example.data.local.MenuItemEntity
import com.example.data.local.OrderChannel
import com.example.data.local.OrderEntity
import com.example.data.local.OrderStatus
import com.example.data.local.PaymentMethod
import com.example.ui.components.LiveOrderTimerChip
import com.example.ui.components.channelColor
import com.example.ui.components.channelLabel
import com.example.ui.i18n.AppStrings
import com.example.ui.theme.LossRed
import com.example.ui.theme.ProfitGreen
import com.example.ui.viewmodel.CartLine
import com.example.ui.viewmodel.RestaurantUiState
import com.example.util.QrCodeCard
import com.example.util.ReceiptPrinter

@Composable
fun OrdersScreen(
    state: RestaurantUiState,
    strings: AppStrings,
    openCreateSheetInitially: Boolean,
    onCreateSheetConsumed: () -> Unit,
    onPlaceOrder: (
        channel: String,
        tableOrCustomer: String,
        cart: List<CartLine>,
        gstEnabled: Boolean,
        tipAmount: Double,
        paymentMethod: String,
        onCreated: (OrderEntity) -> Unit
    ) -> Unit,
    onAdvanceOrder: (OrderEntity) -> Unit,
    onToggleGst: (OrderEntity) -> Unit,
    onMarkKotPrinted: (OrderEntity) -> Unit,
    onCancelOrder: (OrderEntity) -> Unit
) {
    val context = LocalContext.current
    var selectedFilter by remember { mutableStateOf("ACTIVE") }
    var showNewOrderSheet by remember(openCreateSheetInitially) {
        mutableStateOf(openCreateSheetInitially).also {
            if (openCreateSheetInitially) onCreateSheetConsumed()
        }
    }

    val filters = listOf(
        "ACTIVE" to "Live (${state.activeOrders.size})",
        "RUNNING" to strings.runningOrders,
        "PENDING" to strings.pendingOrders,
        OrderChannel.DINE_IN to strings.dineInTables,
        OrderChannel.PICKUP to strings.pickupOrders,
        OrderChannel.OWN_DELIVERY to strings.ownDelivery,
        OrderChannel.ONLINE_DIRECT to strings.onlineDirect,
        OrderChannel.ZOMATO to strings.zomatoOrders,
        OrderChannel.SWIGGY to strings.swiggyOrders,
        "COMPLETED" to "Completed"
    )

    val filteredOrders = remember(state.allOrders, selectedFilter) {
        when (selectedFilter) {
            "ACTIVE" -> state.allOrders.filter {
                it.status == OrderStatus.PENDING || it.status == OrderStatus.RUNNING || it.status == OrderStatus.READY
            }
            "RUNNING" -> state.allOrders.filter { it.status == OrderStatus.RUNNING || it.status == OrderStatus.READY }
            "PENDING" -> state.allOrders.filter { it.status == OrderStatus.PENDING }
            "COMPLETED" -> state.allOrders.filter { it.status == OrderStatus.COMPLETED }
            else -> state.allOrders.filter { it.channel == selectedFilter && it.status != OrderStatus.CANCELLED }
        }
    }

    Scaffold(
        containerColor = Color.Transparent,
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showNewOrderSheet = true },
                modifier = Modifier.testTag("orders_fab_new"),
                containerColor = MaterialTheme.colorScheme.primary
            ) {
                Icon(Icons.Rounded.Add, contentDescription = strings.newOrderBtn)
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            LazyRow(
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 10.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(filters) { (key, label) ->
                    FilterChip(
                        selected = selectedFilter == key,
                        onClick = { selectedFilter = key },
                        label = { Text(label) }
                    )
                }
            }

            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(start = 16.dp, end = 16.dp, bottom = 92.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(filteredOrders, key = { it.id }) { order ->
                    OrderTicketCard(
                        order = order,
                        nowMillis = state.nowMillis,
                        strings = strings,
                        onPrintKot = {
                            onMarkKotPrinted(order)
                            ReceiptPrinter.printKot(context, state.settings.restaurantName, order)
                        },
                        onPrintBill = {
                            ReceiptPrinter.printBill(context, state.settings.restaurantName, order)
                        },
                        onToggleGst = { onToggleGst(order) },
                        onAdvance = { onAdvanceOrder(order) },
                        onCancel = { onCancelOrder(order) }
                    )
                }
            }
        }
    }

    if (showNewOrderSheet) {
        NewOrderBottomSheet(
            state = state,
            strings = strings,
            onDismiss = { showNewOrderSheet = false },
            onSubmit = { ch, cust, cart, gst, tip, pay ->
                onPlaceOrder(ch, cust, cart, gst, tip, pay) { createdOrder ->
                    if (state.settings.autoKotPrint) {
                        ReceiptPrinter.printKot(context, state.settings.restaurantName, createdOrder)
                    }
                }
                showNewOrderSheet = false
            }
        )
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun OrderTicketCard(
    order: OrderEntity,
    nowMillis: Long,
    strings: AppStrings,
    onPrintKot: () -> Unit,
    onPrintBill: () -> Unit,
    onToggleGst: () -> Unit,
    onAdvance: () -> Unit,
    onCancel: () -> Unit
) {
    val chColor = channelColor(order.channel)
    Card(
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Surface(
                        color = chColor.copy(alpha = 0.14f),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text(
                            text = "${channelLabel(order.channel)} • #${order.orderNumber}",
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold,
                            color = chColor,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(6.dp))
                    Surface(
                        color = MaterialTheme.colorScheme.primary.copy(alpha = 0.12f),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text(
                            text = order.status,
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }
                }
                if (order.status != OrderStatus.COMPLETED && order.status != OrderStatus.CANCELLED) {
                    LiveOrderTimerChip(createdAtMillis = order.createdAtMillis, nowMillis = nowMillis)
                }
            }

            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = order.tableOrCustomer,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = order.itemsSummary,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(10.dp))
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Toggleable GST Pill (Add or Cancel GST)
                Surface(
                    modifier = Modifier.clickable(onClick = onToggleGst),
                    color = if (order.gstEnabled) ProfitGreen.copy(alpha = 0.14f) else LossRed.copy(alpha = 0.12f),
                    shape = RoundedCornerShape(50)
                ) {
                    Text(
                        text = if (order.gstEnabled) "${strings.gstAdded} ₹${order.gstAmount.toInt()} (Tap to Cancel)"
                        else "${strings.gstCancelled} (Tap to Add)",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = if (order.gstEnabled) ProfitGreen else LossRed,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp)
                    )
                }

                Surface(
                    color = MaterialTheme.colorScheme.surfaceVariant,
                    shape = RoundedCornerShape(50)
                ) {
                    Text(
                        text = "Pay: ${order.paymentMethod.replace('_', ' ')}",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.SemiBold,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp)
                    )
                }

                if (order.kotPrinted) {
                    Surface(
                        color = Color(0xFF0EA5E9).copy(alpha = 0.14f),
                        shape = RoundedCornerShape(50)
                    ) {
                        Text(
                            text = "KOT Printed",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF0284C7),
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "₹${"%.0f".format(order.totalAmount)}",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )

                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    OutlinedButton(
                        onClick = onPrintKot,
                        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp)
                    ) {
                        Icon(Icons.Rounded.Print, contentDescription = strings.printKot, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("KOT", style = MaterialTheme.typography.labelMedium)
                    }
                    OutlinedButton(
                        onClick = onPrintBill,
                        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp)
                    ) {
                        Icon(Icons.Rounded.ReceiptLong, contentDescription = strings.printBill, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Bill", style = MaterialTheme.typography.labelMedium)
                    }
                    if (order.status != OrderStatus.COMPLETED && order.status != OrderStatus.CANCELLED) {
                        Button(
                            onClick = onAdvance,
                            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp)
                        ) {
                            Icon(Icons.Rounded.CheckCircle, contentDescription = "Next step", modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = when (order.status) {
                                    OrderStatus.PENDING -> "Start"
                                    OrderStatus.RUNNING -> "Ready"
                                    else -> "Settle"
                                },
                                style = MaterialTheme.typography.labelMedium
                            )
                        }
                        IconButton(onClick = onCancel, modifier = Modifier.size(36.dp)) {
                            Icon(Icons.Rounded.Close, contentDescription = "Cancel order", tint = LossRed)
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
private fun NewOrderBottomSheet(
    state: RestaurantUiState,
    strings: AppStrings,
    onDismiss: () -> Unit,
    onSubmit: (String, String, List<CartLine>, Boolean, Double, String) -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    var selectedChannel by remember { mutableStateOf(OrderChannel.DINE_IN) }
    var tableOrCustomer by remember { mutableStateOf("Table 05") }
    var gstEnabled by remember { mutableStateOf(state.settings.defaultGstEnabled) }
    var tipText by remember { mutableStateOf("0") }
    var paymentMethod by remember { mutableStateOf(PaymentMethod.UPI_QR) }
    val quantities = remember { mutableStateMapOf<Int, Int>() }

    val cart = state.menuItems.mapNotNull { item ->
        val qty = quantities[item.id] ?: 0
        if (qty > 0) CartLine(item, qty) else null
    }
    val subtotal = cart.sumOf { it.menuItem.price * it.quantity }
    val gstAmount = if (gstEnabled) subtotal * (state.settings.gstPercent / 100.0) else 0.0
    val tipAmount = tipText.toDoubleOrNull() ?: 0.0
    val grandTotal = subtotal + gstAmount + tipAmount

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(strings.newOrderBtn, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)

            // Channel selection
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                listOf(
                    OrderChannel.DINE_IN,
                    OrderChannel.PICKUP,
                    OrderChannel.OWN_DELIVERY,
                    OrderChannel.ONLINE_DIRECT,
                    OrderChannel.ZOMATO,
                    OrderChannel.SWIGGY,
                    OrderChannel.OTHER_APP
                ).forEach { ch ->
                    FilterChip(
                        selected = selectedChannel == ch,
                        onClick = {
                            selectedChannel = ch
                            tableOrCustomer = when (ch) {
                                OrderChannel.DINE_IN -> "Table 05"
                                OrderChannel.ZOMATO -> "Zomato Guest"
                                OrderChannel.SWIGGY -> "Swiggy Guest"
                                OrderChannel.OWN_DELIVERY -> "Own Delivery Guest"
                                else -> "Walk-in / Online"
                            }
                        },
                        label = { Text(channelLabel(ch), style = MaterialTheme.typography.labelSmall) }
                    )
                }
            }

            OutlinedTextField(
                value = tableOrCustomer,
                onValueChange = { tableOrCustomer = it },
                label = { Text("Table # / Customer Name") },
                modifier = Modifier.fillMaxWidth()
            )

            // Menu items picker
            Text("Select Fine-Dine Items", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
            state.menuItems.filter { it.isAvailable }.forEach { item ->
                val qty = quantities[item.id] ?: 0
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(item.name, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold)
                        Text("₹${item.price.toInt()}", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.primary)
                    }
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        IconButton(
                            onClick = { if (qty > 0) quantities[item.id] = qty - 1 },
                            modifier = Modifier.size(36.dp)
                        ) {
                            Icon(Icons.Rounded.Remove, contentDescription = "Decrease")
                        }
                        Text("$qty", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                        IconButton(
                            onClick = { quantities[item.id] = qty + 1 },
                            modifier = Modifier.size(36.dp)
                        ) {
                            Icon(Icons.Rounded.Add, contentDescription = "Increase")
                        }
                    }
                }
            }

            // GST Add/Cancel Toggle + Tip
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = if (gstEnabled) "${strings.addGst} (${state.settings.gstPercent}%)" else strings.gstCancelled,
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold
                    )
                    Text("Toggle to add or cancel GST on this bill", style = MaterialTheme.typography.labelSmall)
                }
                Switch(checked = gstEnabled, onCheckedChange = { gstEnabled = it })
            }

            OutlinedTextField(
                value = tipText,
                onValueChange = { tipText = it },
                label = { Text("Staff Tip (₹)") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier.fillMaxWidth()
            )

            // Payment Method (QR Code, Card, Cash)
            Text(strings.paymentMethod, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                listOf(
                    Triple(PaymentMethod.UPI_QR, strings.payQrCode, Icons.Rounded.QrCode2),
                    Triple(PaymentMethod.CARD, strings.payCard, Icons.Rounded.CreditCard),
                    Triple(PaymentMethod.CASH, strings.payCash, Icons.Rounded.Money)
                ).forEach { (method, label, icon) ->
                    FilterChip(
                        selected = paymentMethod == method,
                        onClick = { paymentMethod = method },
                        leadingIcon = { Icon(icon, contentDescription = label, modifier = Modifier.size(16.dp)) },
                        label = { Text(label) },
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            // Live Scannable UPI Payment QR when QR Code selected
            if (paymentMethod == PaymentMethod.UPI_QR && grandTotal > 0) {
                val upiUri = "upi://pay?pa=${state.settings.upiId}&pn=${state.settings.restaurantName.replace(" ", "%20")}&am=${"%.2f".format(grandTotal)}&cu=INR"
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(MaterialTheme.colorScheme.surfaceVariant, RoundedCornerShape(16.dp))
                        .padding(12.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        QrCodeCard(content = upiUri, size = 140.dp)
                        Spacer(modifier = Modifier.height(6.dp))
                        Text("Scan to Pay ₹${"%.2f".format(grandTotal)} • ${state.settings.upiId}", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold)
                    }
                }
            }

            Button(
                onClick = { onSubmit(selectedChannel, tableOrCustomer, cart, gstEnabled, tipAmount, paymentMethod) },
                enabled = cart.isNotEmpty(),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp)
                    .testTag("submit_new_order_btn")
            ) {
                Text(
                    text = if (state.settings.autoKotPrint)
                        "Place Order & Auto Print KOT • ₹${"%.0f".format(grandTotal)}"
                    else
                        "Place Order • ₹${"%.0f".format(grandTotal)}",
                    fontWeight = FontWeight.Bold
                )
            }
            Spacer(modifier = Modifier.height(20.dp))
        }
    }
}
