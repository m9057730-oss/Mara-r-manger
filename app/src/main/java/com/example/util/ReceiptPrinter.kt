package com.example.util

import android.content.Context
import android.print.PrintAttributes
import android.print.PrintManager
import android.webkit.WebView
import android.webkit.WebViewClient
import com.example.data.local.OrderEntity
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object ReceiptPrinter {

    fun printKot(context: Context, restaurantName: String, order: OrderEntity) {
        val timeStr = SimpleDateFormat("dd MMM yyyy • hh:mm a", Locale.getDefault())
            .format(Date(order.createdAtMillis))
        val itemsHtml = order.itemsSummary.split(", ").joinToString("") { line ->
            "<tr><td style='padding:4px 0;font-size:15px;'><b>$line</b></td></tr>"
        }
        val html = """
            <html><body style="font-family:monospace;padding:16px;max-width:320px;">
            <h2 style="margin:0;text-align:center;">*** KITCHEN KOT ***</h2>
            <p style="margin:4px 0;text-align:center;"><b>$restaurantName</b></p>
            <hr/>
            <p><b>Order:</b> #${order.orderNumber}<br/>
            <b>Channel:</b> ${order.channel.replace('_', ' ')}<br/>
            <b>Table/Guest:</b> ${order.tableOrCustomer}<br/>
            <b>Time:</b> $timeStr</p>
            <hr/>
            <table style="width:100%;">$itemsHtml</table>
            <hr/>
            <p style="text-align:center;">Status: ${order.status}</p>
            </body></html>
        """.trimIndent()
        launchPrintJob(context, "KOT_${order.orderNumber}", html)
    }

    fun printBill(context: Context, restaurantName: String, order: OrderEntity) {
        val timeStr = SimpleDateFormat("dd MMM yyyy • hh:mm a", Locale.getDefault())
            .format(Date(order.createdAtMillis))
        val rows = order.itemsJson.lines().filter { it.isNotBlank() }.joinToString("") { row ->
            val parts = row.split("|")
            val name = parts.getOrNull(0) ?: row
            val qty = parts.getOrNull(1) ?: "1"
            val price = parts.getOrNull(2) ?: "0"
            "<tr><td>$name x$qty</td><td style='text-align:right;'>₹$price</td></tr>"
        }.ifBlank {
            "<tr><td>${order.itemsSummary}</td><td style='text-align:right;'>₹${order.subtotal.toInt()}</td></tr>"
        }
        val gstRow = if (order.gstEnabled) {
            "<tr><td>GST</td><td style='text-align:right;'>₹${"%.2f".format(order.gstAmount)}</td></tr>"
        } else {
            "<tr><td>GST</td><td style='text-align:right;'>Cancelled (₹0)</td></tr>"
        }
        val tipRow = if (order.tipAmount > 0) {
            "<tr><td>Staff Tip</td><td style='text-align:right;'>₹${"%.2f".format(order.tipAmount)}</td></tr>"
        } else ""

        val html = """
            <html><body style="font-family:sans-serif;padding:20px;max-width:360px;">
            <h2 style="margin:0;text-align:center;">$restaurantName</h2>
            <p style="margin:2px 0;text-align:center;color:#555;">Fine Dining Tax Invoice</p>
            <hr/>
            <p style="font-size:13px;"><b>Bill #:</b> ${order.orderNumber} &nbsp;|&nbsp; <b>${order.channel.replace('_', ' ')}</b><br/>
            <b>Guest/Table:</b> ${order.tableOrCustomer}<br/>
            <b>Date:</b> $timeStr<br/>
            <b>Payment:</b> ${order.paymentMethod.replace('_', ' ')}</p>
            <hr/>
            <table style="width:100%;font-size:14px;">$rows</table>
            <hr/>
            <table style="width:100%;font-size:14px;">
            <tr><td>Subtotal</td><td style='text-align:right;'>₹${"%.2f".format(order.subtotal)}</td></tr>
            $gstRow
            $tipRow
            <tr><td><b>TOTAL</b></td><td style='text-align:right;'><b>₹${"%.2f".format(order.totalAmount)}</b></td></tr>
            </table>
            <hr/>
            <p style="text-align:center;font-size:12px;">Thank you for dining with us!</p>
            </body></html>
        """.trimIndent()
        launchPrintJob(context, "Bill_${order.orderNumber}", html)
    }

    private fun launchPrintJob(context: Context, jobName: String, html: String) {
        val webView = WebView(context)
        webView.webViewClient = object : WebViewClient() {
            override fun onPageFinished(view: WebView?, url: String?) {
                val printManager = context.getSystemService(Context.PRINT_SERVICE) as? PrintManager
                val adapter = webView.createPrintDocumentAdapter(jobName)
                printManager?.print(jobName, adapter, PrintAttributes.Builder().build())
            }
        }
        webView.loadDataWithBaseURL(null, html, "text/html", "UTF-8", null)
    }
}
