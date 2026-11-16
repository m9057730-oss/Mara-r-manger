package com.example.data.ai

import android.graphics.Bitmap
import com.example.data.local.MenuCategory
import com.example.data.local.StaffMemberEntity
import com.example.ui.i18n.AppLanguage
import kotlinx.coroutines.delay

data class ScannedMenuItem(
    val name: String,
    val category: String,
    val price: Double,
    val costPrice: Double,
    val isVeg: Boolean,
    val chefNote: String
)

data class AiResult<T>(
    val data: T,
    val isSampleMode: Boolean,
    val notice: String
)

interface AiRepository {
    suspend fun scanMenuFromBitmap(
        bitmap: Bitmap?,
        restaurantName: String
    ): AiResult<List<ScannedMenuItem>>

    suspend fun calculateStaffSalaryAndTips(
        staff: List<StaffMemberEntity>,
        totalDayTips: Double,
        totalMonthTips: Double
    ): AiResult<String>

    suspend fun askRestaurantCopilot(
        prompt: String,
        summaryContext: String,
        language: AppLanguage
    ): AiResult<String>
}

/**
 * Sample / offline-safe AiRepository used when Firebase AI Logic is not enabled.
 * Provides structured sample previews computed from local restaurant metrics and
 * clearly labels every output with a notice to enable Firebase AI Logic for live Gemini inference.
 */
class PlaceholderAiRepository : AiRepository {

    private val noticeText =
        "Sample AI Preview — Firebase setup was declined. Enable Firebase AI Logic in AI Studio for live Gemini multimodal scanning & chat."

    override suspend fun scanMenuFromBitmap(
        bitmap: Bitmap?,
        restaurantName: String
    ): AiResult<List<ScannedMenuItem>> {
        delay(650)
        val dimNote = if (bitmap != null) " (${bitmap.width}x${bitmap.height}px)" else ""
        val sampleDishes = listOf(
            ScannedMenuItem(
                name = "Smoked Tandoori Avocado Chaat",
                category = MenuCategory.STARTERS,
                price = 385.0,
                costPrice = 115.0,
                isVeg = true,
                chefNote = "Sample scanned dish$dimNote for $restaurantName"
            ),
            ScannedMenuItem(
                name = "Nawabi Zafrani Galouti Kebab",
                category = MenuCategory.TANDOOR,
                price = 540.0,
                costPrice = 195.0,
                isVeg = false,
                chefNote = "Sample scanned dish$dimNote with saffron & rosewater"
            ),
            ScannedMenuItem(
                name = "Royal Shahi Paneer Lababdar",
                category = MenuCategory.ROYAL_MAINS,
                price = 465.0,
                costPrice = 155.0,
                isVeg = true,
                chefNote = "Sample scanned fine-dine main course"
            ),
            ScannedMenuItem(
                name = "Pistachio Kulfi Falooda",
                category = MenuCategory.DESSERTS,
                price = 260.0,
                costPrice = 75.0,
                isVeg = true,
                chefNote = "Sample scanned dessert"
            )
        )
        return AiResult(
            data = sampleDishes,
            isSampleMode = true,
            notice = noticeText
        )
    }

    override suspend fun calculateStaffSalaryAndTips(
        staff: List<StaffMemberEntity>,
        totalDayTips: Double,
        totalMonthTips: Double
    ): AiResult<String> {
        delay(500)
        val presentStaff = staff.filter { it.isPresentToday }
        val activeCount = presentStaff.size.coerceAtLeast(1)
        val dailyLaborTotal = presentStaff.sumOf { it.dailySalary }
        val staffFoodTotal = presentStaff.sumOf { it.staffMealCost }
        val suggestedDayTipPerHead = totalDayTips / activeCount

        val breakdown = buildString {
            appendLine("[Sample AI Payroll & Tip Breakdown]")
            appendLine("Present Staff Today: ${presentStaff.size} / ${staff.size}")
            appendLine("Today's Labor Cost: ₹${dailyLaborTotal.toInt()}")
            appendLine("Today's Staff Meal Cost: ₹${staffFoodTotal.toInt()}")
            appendLine("Today's Tips Pool: ₹${totalDayTips.toInt()} (Suggested equal split: ₹${suggestedDayTipPerHead.toInt()}/staff)")
            appendLine("Month-to-Date Tips Pool: ₹${totalMonthTips.toInt()}")
            appendLine()
            appendLine("Suggested Payout Per Staff (Salary + Today Tip):")
            staff.forEach { member ->
                val dayPay = if (member.isPresentToday) member.dailySalary else 0.0
                val estMonth = member.dailySalary * 26 + member.monthlyTipBonus
                appendLine("• ${member.name} (${member.role}): Today ₹${(dayPay + member.todayTip).toInt()} | Est. Month ₹${estMonth.toInt()}")
            }
        }
        return AiResult(
            data = breakdown,
            isSampleMode = true,
            notice = noticeText
        )
    }

    override suspend fun askRestaurantCopilot(
        prompt: String,
        summaryContext: String,
        language: AppLanguage
    ): AiResult<String> {
        delay(550)
        val reply = when (language) {
            AppLanguage.HINDI ->
                "[सैंपल AI सुझाव] आपके प्रश्न \"$prompt\" के लिए:\n$summaryContext\n\nसुझाव: पीक आवर्स के दौरान Zomato/Swiggy कमिशन बचाने के लिए QR ऑर्डर मेनू और अपनी डिलीवरी को प्रमोट करें, और कम स्टॉक वाले कोल्ड ड्रिंक्स व तंदूर कोयले की WhatsApp रिपोर्ट अभी भेजें।"
            AppLanguage.PUNJABI ->
                "[ਸੈਂਪਲ AI ਸੁਝਾਅ] ਤੁਹਾਡੇ ਸਵਾਲ \"$prompt\" ਲਈ:\n$summaryContext\n\nਸੁਝਾਅ: Zomato/Swiggy ਕਮਿਸ਼ਨ ਬਚਾਉਣ ਲਈ QR ਮੇਨੂ ਅਤੇ ਆਪਣੀ ਡਿਲੀਵਰੀ ਵਧਾਓ, ਅਤੇ ਘੱਟ ਸਟਾਕ ਵਾਲੇ ਕੋਲਡ ਡ੍ਰਿੰਕ ਤੇ ਤੰਦੂਰ ਸਮਾਨ ਦੀ WhatsApp ਰਿਪੋਰਟ ਭੇਜੋ।"
            AppLanguage.ENGLISH ->
                "[Sample AI Assistant] Regarding \"$prompt\":\n$summaryContext\n\nActionable Insight: Shift repeat Zomato/Swiggy diners to your QR Order Menu & Own Delivery to save aggregator fees, and dispatch the Chef WhatsApp report for low-stock Tandoor & Cold Drink items."
        }
        return AiResult(
            data = reply,
            isSampleMode = true,
            notice = noticeText
        )
    }
}
