package com.example.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.AutoAwesome
import androidx.compose.material.icons.rounded.Info
import androidx.compose.material.icons.rounded.Timer
import androidx.compose.material.icons.rounded.WarningAmber
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.data.local.OrderChannel
import com.example.ui.theme.DineInGold
import com.example.ui.theme.LossRed
import com.example.ui.theme.OtherAppIndigo
import com.example.ui.theme.OwnDeliveryTeal
import com.example.ui.theme.PickupViolet
import com.example.ui.theme.ProfitGreen
import com.example.ui.theme.SwiggyOrange
import com.example.ui.theme.ZomatoRed
import com.example.ui.viewmodel.DayPnlPoint
import kotlin.math.abs

fun channelColor(channel: String): Color = when (channel) {
    OrderChannel.ZOMATO -> ZomatoRed
    OrderChannel.SWIGGY -> SwiggyOrange
    OrderChannel.OTHER_APP -> OtherAppIndigo
    OrderChannel.OWN_DELIVERY -> OwnDeliveryTeal
    OrderChannel.DINE_IN -> DineInGold
    OrderChannel.PICKUP -> PickupViolet
    else -> Color(0xFF0284C7)
}

fun channelLabel(channel: String): String = when (channel) {
    OrderChannel.ZOMATO -> "Zomato"
    OrderChannel.SWIGGY -> "Swiggy"
    OrderChannel.OTHER_APP -> "Other App"
    OrderChannel.OWN_DELIVERY -> "Own Delivery"
    OrderChannel.DINE_IN -> "Table Dine-In"
    OrderChannel.PICKUP -> "Pickup / Pack"
    else -> "Online Direct"
}

@Composable
fun LiveOrderTimerChip(
    createdAtMillis: Long,
    nowMillis: Long,
    modifier: Modifier = Modifier
) {
    val elapsedSec = ((nowMillis - createdAtMillis).coerceAtLeast(0L) / 1000L).toInt()
    val mins = elapsedSec / 60
    val secs = elapsedSec % 60
    val formatted = "%02d:%02d".format(mins, secs)
    val chipColor = when {
        mins >= 20 -> LossRed
        mins >= 12 -> Color(0xFFD97706)
        else -> ProfitGreen
    }
    Surface(
        modifier = modifier,
        color = chipColor.copy(alpha = 0.14f),
        shape = RoundedCornerShape(50)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Rounded.Timer,
                contentDescription = "Live order timer",
                tint = chipColor,
                modifier = Modifier.size(14.dp)
            )
            Spacer(modifier = Modifier.width(4.dp))
            Text(
                text = formatted,
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.Bold,
                color = chipColor
            )
        }
    }
}

@Composable
fun TodayProfitVsCostChart(
    sales: Double,
    cost: Double,
    netProfit: Double,
    salesLabel: String,
    costLabel: String,
    profitLabel: String,
    modifier: Modifier = Modifier
) {
    val primary = MaterialTheme.colorScheme.primary
    val maxVal = maxOf(sales, cost, abs(netProfit), 1000.0)
    val profitColor = if (netProfit >= 0) ProfitGreen else LossRed

    Column(modifier = modifier.fillMaxWidth()) {
        val bars = listOf(
            Triple(salesLabel, sales, primary),
            Triple(costLabel, cost, Color(0xFFF59E0B)),
            Triple(profitLabel, abs(netProfit), profitColor)
        )
        bars.forEach { (label, value, color) ->
            val ratio = (value / maxVal).toFloat().coerceIn(0.04f, 1f)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 5.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = label,
                    style = MaterialTheme.typography.bodySmall,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.width(90.dp),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .height(18.dp)
                        .clip(RoundedCornerShape(9.dp))
                        .background(color.copy(alpha = 0.12f))
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth(ratio)
                            .height(18.dp)
                            .clip(RoundedCornerShape(9.dp))
                            .background(
                                Brush.horizontalGradient(
                                    listOf(color, color.copy(alpha = 0.78f))
                                )
                            )
                    )
                }
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "${if (label == profitLabel && netProfit < 0) "-" else ""}₹${value.toInt()}",
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.Bold,
                    color = color,
                    modifier = Modifier.width(74.dp)
                )
            }
        }
    }
}

@Composable
fun SevenDayPnlChart(
    points: List<DayPnlPoint>,
    modifier: Modifier = Modifier
) {
    if (points.isEmpty()) return
    val primary = MaterialTheme.colorScheme.primary
    val gridColor = MaterialTheme.colorScheme.outline

    Column(modifier = modifier.fillMaxWidth()) {
        Canvas(
            modifier = Modifier
                .fillMaxWidth()
                .height(155.dp)
                .padding(top = 8.dp, bottom = 4.dp)
        ) {
            val maxMetric = points.maxOfOrNull { maxOf(it.sales, it.cost, abs(it.profit)) }
                ?.coerceAtLeast(1000.0) ?: 1000.0
            val chartHeight = size.height - 16f
            val groupWidth = size.width / points.size
            val barWidth = (groupWidth * 0.28f).coerceAtLeast(8f)

            // Baseline & mid grid lines
            drawLine(
                color = gridColor,
                start = Offset(0f, chartHeight),
                end = Offset(size.width, chartHeight),
                strokeWidth = 2f
            )
            drawLine(
                color = gridColor.copy(alpha = 0.5f),
                start = Offset(0f, chartHeight * 0.5f),
                end = Offset(size.width, chartHeight * 0.5f),
                strokeWidth = 1f
            )

            val profitLinePath = Path()
            points.forEachIndexed { index, pt ->
                val centerX = groupWidth * index + groupWidth / 2f
                val salesH = ((pt.sales / maxMetric).toFloat().coerceIn(0f, 1f)) * (chartHeight * 0.9f)
                val costH = ((pt.cost / maxMetric).toFloat().coerceIn(0f, 1f)) * (chartHeight * 0.9f)

                // Sales bar
                drawRoundRect(
                    color = primary,
                    topLeft = Offset(centerX - barWidth - 3f, chartHeight - salesH),
                    size = Size(barWidth, salesH),
                    cornerRadius = CornerRadius(6f, 6f)
                )
                // Cost bar
                drawRoundRect(
                    color = Color(0xFFF59E0B),
                    topLeft = Offset(centerX + 3f, chartHeight - costH),
                    size = Size(barWidth, costH),
                    cornerRadius = CornerRadius(6f, 6f)
                )

                // Net Profit dot position
                val profitRatio = (pt.profit.coerceAtLeast(0.0) / maxMetric).toFloat().coerceIn(0f, 1f)
                val dotY = chartHeight - profitRatio * (chartHeight * 0.9f)
                if (index == 0) profitLinePath.moveTo(centerX, dotY) else profitLinePath.lineTo(centerX, dotY)
            }

            drawPath(
                path = profitLinePath,
                color = ProfitGreen,
                style = Stroke(width = 5f, cap = StrokeCap.Round)
            )
            points.forEachIndexed { index, pt ->
                val centerX = groupWidth * index + groupWidth / 2f
                val profitRatio = (pt.profit.coerceAtLeast(0.0) / maxMetric).toFloat().coerceIn(0f, 1f)
                val dotY = chartHeight - profitRatio * (chartHeight * 0.9f)
                val dotColor = if (pt.profit >= 0) ProfitGreen else LossRed
                drawCircle(color = Color.White, radius = 8f, center = Offset(centerX, dotY))
                drawCircle(color = dotColor, radius = 5.5f, center = Offset(centerX, dotY))
            }
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            points.forEach { pt ->
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.weight(1f)
                ) {
                    Text(
                        text = pt.label,
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.SemiBold,
                        maxLines = 1
                    )
                    Text(
                        text = "${if (pt.profit < 0) "-" else "+"}₹${(abs(pt.profit) / 1000.0).let { "%.1fk".format(it) }}",
                        style = MaterialTheme.typography.labelSmall,
                        color = if (pt.profit >= 0) ProfitGreen else LossRed,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(6.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(14.dp)) {
            LegendDot(color = primary, label = "Sales")
            LegendDot(color = Color(0xFFF59E0B), label = "Cost")
            LegendDot(color = ProfitGreen, label = "Profit Line")
        }
    }
}

@Composable
private fun LegendDot(color: Color, label: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(
            modifier = Modifier
                .size(9.dp)
                .clip(CircleShape)
                .background(color)
        )
        Spacer(modifier = Modifier.width(5.dp))
        Text(text = label, style = MaterialTheme.typography.labelSmall)
    }
}

@Composable
fun AiNoticeBanner(
    notice: String,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier
            .fillMaxWidth()
            .border(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.25f), RoundedCornerShape(12.dp)),
        color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.45f),
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(
            modifier = Modifier.padding(10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Rounded.AutoAwesome,
                contentDescription = "AI status notice",
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(18.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = notice,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurface
            )
        }
    }
}

@Composable
fun LowStockAlertBanner(
    outCount: Int,
    lowCount: Int,
    title: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    if (outCount == 0 && lowCount == 0) return
    val alertColor = if (outCount > 0) LossRed else Color(0xFFD97706)
    Card(
        onClick = onClick,
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = alertColor.copy(alpha = 0.12f)),
        shape = RoundedCornerShape(16.dp)
    ) {
        Row(
            modifier = Modifier.padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(38.dp)
                    .clip(CircleShape)
                    .background(alertColor.copy(alpha = 0.18f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Rounded.WarningAmber,
                    contentDescription = "Inventory alert",
                    tint = alertColor
                )
            }
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = alertColor
                )
                Text(
                    text = "$outCount Out-of-Stock • $lowCount Low Stock (Tap to send Chef WhatsApp report)",
                    style = MaterialTheme.typography.bodySmall
                )
            }
            Icon(
                imageVector = Icons.Rounded.Info,
                contentDescription = "View stock alerts",
                tint = alertColor
            )
        }
    }
}
