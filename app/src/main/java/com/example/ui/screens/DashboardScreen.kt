package com.example.ui.screens

import androidx.compose.foundation.Image
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.AutoAwesome
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.DeliveryDining
import androidx.compose.material.icons.rounded.Groups
import androidx.compose.material.icons.rounded.Payments
import androidx.compose.material.icons.rounded.Print
import androidx.compose.material.icons.rounded.Restaurant
import androidx.compose.material.icons.rounded.Send
import androidx.compose.material.icons.rounded.Storefront
import androidx.compose.material.icons.rounded.TrendingUp
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.R
import com.example.data.local.OrderChannel
import com.example.data.local.OrderEntity
import com.example.data.local.OrderStatus
import com.example.ui.components.AiNoticeBanner
import com.example.ui.components.LiveOrderTimerChip
import com.example.ui.components.LowStockAlertBanner
import com.example.ui.components.SevenDayPnlChart
import com.example.ui.components.TodayProfitVsCostChart
import com.example.ui.components.channelColor
import com.example.ui.components.channelLabel
import com.example.ui.i18n.AppStrings
import com.example.ui.theme.LossRed
import com.example.ui.theme.ProfitGreen
import com.example.ui.viewmodel.RestaurantUiState
import com.example.util.ReceiptPrinter
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun DashboardScreen(
    state: RestaurantUiState,
    strings: AppStrings,
    onOpenNewOrder: () -> Unit,
    onNavigateOrders: () -> Unit,
    onNavigateInventory: () -> Unit,
    onNavigateStaff: () -> Unit,
    onAdvanceOrder: (OrderEntity) -> Unit,
    onMarkKotPrinted: (OrderEntity) -> Unit,
    onAskCopilot: (String) -> Unit
) {
    val context = LocalContext.current
    var showCopilotDialog by remember { mutableStateOf(false) }

    val targetProgress = (state.todayGrossSales / state.settings.dailyTarget.coerceAtLeast(1.0))
        .toFloat()
        .coerceIn(0f, 1f)
    val dateNowLabel = remember(state.nowMillis / 60_000L) {
        SimpleDateFormat("EEE, dd MMM • hh:mm a", Locale.getDefault()).format(Date(state.nowMillis))
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // 1. Restaurant Hero Banner + Daily Target
        item {
            Card(
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 3.dp)
            ) {
                Column {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(145.dp)
                    ) {
                        Image(
                            painter = painterResource(id = R.drawable.img_hero_bistro),
                            contentDescription = state.settings.restaurantName,
                            contentScale = ContentScale.Crop,
                            modifier = Modifier.fillMaxSize()
                        )
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(
                                    Brush.verticalGradient(
                                        listOf(
                                            Color.Black.copy(alpha = 0.15f),
                                            Color.Black.copy(alpha = 0.72f)
                                        )
                                    )
                                )
                        )
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .align(Alignment.TopEnd)
                                .padding(12.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Surface(
                                color = Color.Black.copy(alpha = 0.45f),
                                shape = RoundedCornerShape(50)
                            ) {
                                Text(
                                    text = dateNowLabel,
                                    style = MaterialTheme.typography.labelMedium,
                                    color = Color.White,
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp)
                                )
                            }
                            FilledTonalButton(
                                onClick = { showCopilotDialog = true },
                                modifier = Modifier.testTag("ai_copilot_button"),
                                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Rounded.AutoAwesome,
                                    contentDescription = strings.aiCopilot,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("AI Auto", style = MaterialTheme.typography.labelLarge)
                            }
                        }
                        Column(
                            modifier = Modifier
                                .align(Alignment.BottomStart)
                                .padding(14.dp)
                        ) {
                            Text(
                                text = state.settings.restaurantName,
                                style = MaterialTheme.typography.headlineMedium,
                                color = Color.White,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            Text(
                                text = "${state.todayOrders.size} Orders Today • ${state.activeOrders.size} Live Now",
                                style = MaterialTheme.typography.bodySmall,
                                color = Color.White.copy(alpha = 0.9f)
                            )
                        }
                    }

                    // Daily Target Meter + Quick New Order CTA
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = strings.todayTarget,
                                    style = MaterialTheme.typography.labelLarge,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = "₹${state.todayGrossSales.toInt()} / ₹${state.settings.dailyTarget.toInt()}",
                                    style = MaterialTheme.typography.labelMedium,
                                    color = MaterialTheme.colorScheme.primary,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                            Spacer(modifier = Modifier.height(6.dp))
                            LinearProgressIndicator(
                                progress = { targetProgress },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(9.dp)
                                    .clip(RoundedCornerShape(5.dp)),
                                color = MaterialTheme.colorScheme.primary,
                                trackColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.16f)
                            )
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Button(
                            onClick = onOpenNewOrder,
                            modifier = Modifier.testTag("dashboard_new_order_btn"),
                            shape = RoundedCornerShape(14.dp)
                        ) {
                            Icon(Icons.Rounded.Add, contentDescription = strings.newOrderBtn, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(strings.newOrderBtn)
                        }
                    }
                }
            }
        }

        // 2. Low-Stock Inventory Alert Banner (if any low or out-of-stock)
        if (state.outOfStockItems.isNotEmpty() || state.lowStockItems.isNotEmpty()) {
            item {
                LowStockAlertBanner(
                    outCount = state.outOfStockItems.size,
                    lowCount = state.lowStockItems.size,
                    title = strings.lowStockAlert,
                    onClick = onNavigateInventory
                )
            }
        }

        // 3. Today Sales, Today Cost & Net Profit/Loss KPI Strip
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                KpiTile(
                    title = strings.todaySales,
                    value = "₹${state.todayGrossSales.toInt()}",
                    subtitle = "${state.todayOrders.size} bills • GST ₹${state.todayGstCollected.toInt()}",
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.weight(1f)
                )
                KpiTile(
                    title = strings.todayCost,
                    value = "₹${state.todayCostBreakdown.totalCost.toInt()}",
                    subtitle = "Food + Labor + Rent",
                    tint = Color(0xFFD97706),
                    modifier = Modifier.weight(1f)
                )
                val isProfit = state.todayNetProfit >= 0
                KpiTile(
                    title = if (isProfit) strings.netProfit else strings.netLoss,
                    value = "${if (isProfit) "+" else "-"}₹${kotlin.math.abs(state.todayNetProfit).toInt()}",
                    subtitle = if (isProfit) "Healthy margin" else "Check daily cost",
                    tint = if (isProfit) ProfitGreen else LossRed,
                    modifier = Modifier.weight(1f)
                )
            }
        }

        // 4. Today Profit vs Cost Chart + 7-Day Trend Chart
        item {
            Card(
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Rounded.TrendingUp,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = strings.profitLossChart,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    Spacer(modifier = Modifier.height(10.dp))
                    TodayProfitVsCostChart(
                        sales = state.todayNetSales,
                        cost = state.todayCostBreakdown.totalCost,
                        netProfit = state.todayNetProfit,
                        salesLabel = strings.todaySales,
                        costLabel = strings.todayCost,
                        profitLabel = if (state.todayNetProfit >= 0) strings.netProfit else strings.netLoss
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = strings.dailyTrendChart,
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold
                    )
                    SevenDayPnlChart(points = state.sevenDayPnl)
                }
            }
        }

        // 5. 3rd-Party Apps (Zomato / Swiggy / Other) vs Restaurant Own Delivery & Dine-In
        item {
            Card(
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = strings.thirdPartyVsOwn,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(10.dp))

                    // Two comparison hero pills
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Surface(
                            modifier = Modifier.weight(1f),
                            color = Color(0xFFE23744).copy(alpha = 0.11f),
                            shape = RoundedCornerShape(16.dp)
                        ) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Rounded.DeliveryDining, contentDescription = null, tint = Color(0xFFE23744), modifier = Modifier.size(18.dp))
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text("3rd-Party Apps", style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.Bold, color = Color(0xFFE23744))
                                }
                                Spacer(modifier = Modifier.height(4.dp))
                                Text("${state.thirdPartyCount} Orders", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                                Text("₹${state.thirdPartyRevenue.toInt()} total", style = MaterialTheme.typography.bodySmall)
                            }
                        }
                        Surface(
                            modifier = Modifier.weight(1f),
                            color = ProfitGreen.copy(alpha = 0.11f),
                            shape = RoundedCornerShape(16.dp)
                        ) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Rounded.Storefront, contentDescription = null, tint = ProfitGreen, modifier = Modifier.size(18.dp))
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text("Restaurant Own", style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.Bold, color = ProfitGreen)
                                }
                                Spacer(modifier = Modifier.height(4.dp))
                                Text("${state.ownRestaurantCount} Orders", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                                Text("₹${state.ownRestaurantRevenue.toInt()} total", style = MaterialTheme.typography.bodySmall)
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))
                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        state.channelStats.forEach { stat ->
                            val chColor = channelColor(stat.channel)
                            val chName = when (stat.channel) {
                                OrderChannel.ZOMATO -> strings.zomatoOrders
                                OrderChannel.SWIGGY -> strings.swiggyOrders
                                OrderChannel.OTHER_APP -> strings.otherAppOrders
                                OrderChannel.OWN_DELIVERY -> strings.ownDelivery
                                OrderChannel.DINE_IN -> strings.dineInTables
                                OrderChannel.PICKUP -> strings.pickupOrders
                                else -> strings.onlineDirect
                            }
                            Surface(
                                color = chColor.copy(alpha = 0.10f),
                                shape = RoundedCornerShape(14.dp)
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 7.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(8.dp)
                                            .clip(CircleShape)
                                            .background(chColor)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = "$chName: ${stat.count} (₹${stat.revenue.toInt()})",
                                        style = MaterialTheme.typography.labelMedium,
                                        fontWeight = FontWeight.SemiBold,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        // 6. Live & Pending Orders Strip (with live timer + Auto KOT print)
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "${strings.liveOrdersTitle} (${state.activeOrders.size})",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                TextButton(onClick = onNavigateOrders) {
                    Text("View All")
                }
            }
        }

        if (state.activeOrders.isEmpty()) {
            item {
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Rounded.Restaurant, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                        Spacer(modifier = Modifier.width(12.dp))
                        Text("No active orders right now. Tap + New Order to create one!", style = MaterialTheme.typography.bodyMedium)
                    }
                }
            }
        } else {
            item {
                LazyRow(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    items(state.activeOrders, key = { it.id }) { order ->
                        val chColor = channelColor(order.channel)
                        Card(
                            modifier = Modifier.width(270.dp),
                            shape = RoundedCornerShape(18.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                        ) {
                            Column(modifier = Modifier.padding(14.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Surface(
                                        color = chColor.copy(alpha = 0.14f),
                                        shape = RoundedCornerShape(8.dp)
                                    ) {
                                        Text(
                                            text = "${channelLabel(order.channel)} • #${order.orderNumber}",
                                            style = MaterialTheme.typography.labelSmall,
                                            fontWeight = FontWeight.Bold,
                                            color = chColor,
                                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                        )
                                    }
                                    LiveOrderTimerChip(
                                        createdAtMillis = order.createdAtMillis,
                                        nowMillis = state.nowMillis
                                    )
                                }
                                Spacer(modifier = Modifier.height(6.dp))
                                Text(
                                    text = order.tableOrCustomer,
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = order.itemsSummary,
                                    style = MaterialTheme.typography.bodySmall,
                                    maxLines = 2,
                                    overflow = TextOverflow.Ellipsis,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Spacer(modifier = Modifier.height(10.dp))
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = "₹${order.totalAmount.toInt()} • ${order.status}",
                                        style = MaterialTheme.typography.labelLarge,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Row {
                                        IconButton(
                                            onClick = {
                                                onMarkKotPrinted(order)
                                                ReceiptPrinter.printKot(context, state.settings.restaurantName, order)
                                            },
                                            modifier = Modifier.size(36.dp)
                                        ) {
                                            Icon(Icons.Rounded.Print, contentDescription = strings.printKot, tint = MaterialTheme.colorScheme.primary)
                                        }
                                        IconButton(
                                            onClick = { onAdvanceOrder(order) },
                                            modifier = Modifier.size(36.dp)
                                        ) {
                                            Icon(Icons.Rounded.CheckCircle, contentDescription = "Advance status", tint = ProfitGreen)
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        // 7. Quick Staff Attendance, Labor Cost & Tips Snapshot
        item {
            val presentCount = state.staff.count { it.isPresentToday }
            Card(
                modifier = Modifier.clickable(onClick = onNavigateStaff),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(44.dp)
                                .clip(CircleShape)
                                .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.14f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Rounded.Groups, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                text = "${strings.staffAttendance}: $presentCount/${state.staff.size} Present",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "${strings.laborCost}: ₹${state.todayCostBreakdown.staffSalary.toInt()} • Food ₹${state.todayCostBreakdown.staffFood.toInt()}",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                    Column(horizontalAlignment = Alignment.End) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Rounded.Payments, contentDescription = null, tint = ProfitGreen, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "₹${state.todayDayTips.toInt()}",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold,
                                color = ProfitGreen
                            )
                        }
                        Text(
                            text = "${strings.monthTip}: ₹${state.monthTips.toInt()}",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }
    }

    if (showCopilotDialog) {
        AiCopilotDialog(
            state = state,
            strings = strings,
            onAsk = onAskCopilot,
            onDismiss = { showCopilotDialog = false }
        )
    }
}

@Composable
private fun KpiTile(
    title: String,
    value: String,
    subtitle: String,
    tint: Color,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Text(
                text = title,
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = value,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = tint,
                maxLines = 1
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = subtitle,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun AiCopilotDialog(
    state: RestaurantUiState,
    strings: AppStrings,
    onAsk: (String) -> Unit,
    onDismiss: () -> Unit
) {
    var prompt by remember { mutableStateOf("") }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Rounded.AutoAwesome, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                Spacer(modifier = Modifier.width(8.dp))
                Text(strings.aiCopilot)
            }
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                AiNoticeBanner(notice = state.aiCopilotResult?.notice ?: strings.aiFirebaseNotice)
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    listOf(
                        "Today P&L & cost advice",
                        "Reduce Zomato/Swiggy commission",
                        "Staff shift & tip plan"
                    ).forEach { quick ->
                        AssistChip(
                            onClick = {
                                prompt = quick
                                onAsk(quick)
                            },
                            label = { Text(quick, style = MaterialTheme.typography.labelSmall) }
                        )
                    }
                }
                OutlinedTextField(
                    value = prompt,
                    onValueChange = { prompt = it },
                    label = { Text("Ask AI assistant...") },
                    trailingIcon = {
                        IconButton(onClick = { onAsk(prompt) }) {
                            Icon(Icons.Rounded.Send, contentDescription = "Send prompt")
                        }
                    },
                    modifier = Modifier.fillMaxWidth()
                )
                if (state.aiCopilotLoading) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        CircularProgressIndicator(modifier = Modifier.size(18.dp), strokeWidth = 2.dp)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Generating insights...", style = MaterialTheme.typography.bodySmall)
                    }
                } else if (state.aiCopilotResult != null) {
                    Surface(
                        color = MaterialTheme.colorScheme.surfaceVariant,
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text(
                            text = state.aiCopilotResult.data,
                            style = MaterialTheme.typography.bodySmall,
                            modifier = Modifier.padding(10.dp)
                        )
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) { Text("Close") }
        }
    )
}
