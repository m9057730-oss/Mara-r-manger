package com.example.util

import android.content.Context
import android.content.Intent
import android.net.Uri
import com.example.data.local.InventoryItemEntity
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object WhatsAppHelper {

    fun buildChefInventoryReport(
        restaurantName: String,
        items: List<InventoryItemEntity>
    ): String {
        val dateStr = SimpleDateFormat("dd MMM yyyy, hh:mm a", Locale.getDefault()).format(Date())
        val needed = items.filter { it.chefNeededQty > 0 || it.currentStock <= it.minThreshold }
        val grouped = needed.groupBy { it.category }

        val sb = StringBuilder()
        sb.appendLine("*$restaurantName — Chef Inventory Requirement*")
        sb.appendLine("Date: $dateStr")
        sb.appendLine("--------------------------------")

        val labels = listOf(
            "KITCHEN" to "Kitchen Items",
            "TANDOOR" to "Tandoor Items",
            "PACKING" to "Packing Material",
            "COLD_DRINK" to "Cold Drinks (Cola / Pepsi)"
        )

        var anyFound = false
        for ((key, title) in labels) {
            val catItems = grouped[key].orEmpty()
            if (catItems.isNotEmpty()) {
                anyFound = true
                sb.appendLine("\n*$title:*")
                catItems.forEach { item ->
                    val reqQty = if (item.chefNeededQty > 0) item.chefNeededQty else (item.minThreshold * 2 - item.currentStock).coerceAtLeast(1.0)
                    val stockNote = if (item.currentStock <= 0.0) "OUT OF STOCK" else "Left: ${formatQty(item.currentStock)} ${item.unit}"
                    sb.appendLine("• ${item.name}: Need *${formatQty(reqQty)} ${item.unit}* ($stockNote)")
                }
            }
        }

        if (!anyFound) {
            sb.appendLine("\nAll Kitchen, Tandoor, Packing & Cold Drink stocks are healthy!")
        }
        sb.appendLine("\nSent via $restaurantName POS")
        return sb.toString()
    }

    fun sendToWhatsApp(context: Context, message: String) {
        val whatsappIntent = Intent(Intent.ACTION_SEND).apply {
            type = "text/plain"
            putExtra(Intent.EXTRA_TEXT, message)
            setPackage("com.whatsapp")
        }
        try {
            context.startActivity(whatsappIntent)
        } catch (_: Exception) {
            try {
                val waUrl = "https://wa.me/?text=${Uri.encode(message)}"
                context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(waUrl)))
            } catch (_: Exception) {
                val chooser = Intent.createChooser(
                    Intent(Intent.ACTION_SEND).apply {
                        type = "text/plain"
                        putExtra(Intent.EXTRA_TEXT, message)
                    },
                    "Send Inventory Report"
                )
                context.startActivity(chooser)
            }
        }
    }

    private fun formatQty(value: Double): String =
        if (value % 1.0 == 0.0) value.toInt().toString() else "%.1f".format(value)
}
