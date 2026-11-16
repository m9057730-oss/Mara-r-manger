package com.example.ui.viewmodel

import android.graphics.Bitmap
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.data.ai.AiRepository
import com.example.data.ai.AiResult
import com.example.data.ai.ScannedMenuItem
import com.example.data.local.AppDao
import com.example.data.local.AppSettingsState
import com.example.data.local.AttendanceLogEntity
import com.example.data.local.DailyExpenseEntity
import com.example.data.local.ExpenseCategory
import com.example.data.local.InventoryItemEntity
import com.example.data.local.MenuItemEntity
import com.example.data.local.OrderChannel
import com.example.data.local.OrderEntity
import com.example.data.local.OrderStatus
import com.example.data.local.PartyBookingEntity
import com.example.data.local.SeedData
import com.example.data.local.SettingsDataStore
import com.example.data.local.StaffMemberEntity
import com.example.ui.i18n.AppLanguage
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

data class ChannelStat(
    val channel: String,
    val count: Int,
    val revenue: Double
)

data class DayPnlPoint(
    val label: String,
    val sales: Double,
    val cost: Double,
    val profit: Double
)

data class DailyCostBreakdown(
    val foodCogs: Double = 0.0,
    val staffSalary: Double = 0.0,
    val shopRent: Double = 0.0,
    val rawMaterial: Double = 0.0,
    val gasFuel: Double = 0.0,
    val expiryWastage: Double = 0.0,
    val electricity: Double = 0.0,
    val staffFood: Double = 0.0,
    val other: Double = 0.0
) {
    val totalCost: Double
        get() = foodCogs + staffSalary + shopRent + rawMaterial + gasFuel + expiryWastage + electricity + staffFood + other
}

data class CartLine(
    val menuItem: MenuItemEntity,
    val quantity: Int
)

data class RestaurantUiState(
    val settings: AppSettingsState = AppSettingsState(),
    val nowMillis: Long = System.currentTimeMillis(),
    val menuItems: List<MenuItemEntity> = emptyList(),
    val allOrders: List<OrderEntity> = emptyList(),
    val activeOrders: List<OrderEntity> = emptyList(),
    val todayOrders: List<OrderEntity> = emptyList(),
    val inventory: List<InventoryItemEntity> = emptyList(),
    val lowStockItems: List<InventoryItemEntity> = emptyList(),
    val outOfStockItems: List<InventoryItemEntity> = emptyList(),
    val staff: List<StaffMemberEntity> = emptyList(),
    val attendanceLogs: List<AttendanceLogEntity> = emptyList(),
    val expenses: List<DailyExpenseEntity> = emptyList(),
    val partyBookings: List<PartyBookingEntity> = emptyList(),
    val todayGrossSales: Double = 0.0,
    val todayNetSales: Double = 0.0,
    val todayGstCollected: Double = 0.0,
    val todayCostBreakdown: DailyCostBreakdown = DailyCostBreakdown(),
    val todayNetProfit: Double = 0.0,
    val todayDayTips: Double = 0.0,
    val monthTips: Double = 0.0,
    val channelStats: List<ChannelStat> = emptyList(),
    val thirdPartyCount: Int = 0,
    val thirdPartyRevenue: Double = 0.0,
    val ownRestaurantCount: Int = 0,
    val ownRestaurantRevenue: Double = 0.0,
    val sevenDayPnl: List<DayPnlPoint> = emptyList(),
    val aiScanResult: AiResult<List<ScannedMenuItem>>? = null,
    val aiScanLoading: Boolean = false,
    val aiSalaryResult: AiResult<String>? = null,
    val aiSalaryLoading: Boolean = false,
    val aiCopilotResult: AiResult<String>? = null,
    val aiCopilotLoading: Boolean = false,
    val statusMessage: String? = null
)

class RestaurantViewModel(
    private val dao: AppDao,
    private val settingsStore: SettingsDataStore,
    private val aiRepository: AiRepository
) : ViewModel() {

    private val tickerFlow = flow {
        while (true) {
            emit(System.currentTimeMillis())
            delay(1000L)
        }
    }

    private data class AiAndMessageState(
        val scanResult: AiResult<List<ScannedMenuItem>>? = null,
        val scanLoading: Boolean = false,
        val salaryResult: AiResult<String>? = null,
        val salaryLoading: Boolean = false,
        val copilotResult: AiResult<String>? = null,
        val copilotLoading: Boolean = false,
        val message: String? = null
    )

    private val aiState = MutableStateFlow(AiAndMessageState())

    private val baseDataFlow = combine(
        dao.observeMenuItems(),
        dao.observeAllOrders(),
        dao.observeInventory(),
        dao.observeStaff(),
        dao.observeAttendanceLogs()
    ) { menu, orders, inventory, staff, logs ->
        BaseData(menu, orders, inventory, staff, logs)
    }

    private data class BaseData(
        val menu: List<MenuItemEntity>,
        val orders: List<OrderEntity>,
        val inventory: List<InventoryItemEntity>,
        val staff: List<StaffMemberEntity>,
        val logs: List<AttendanceLogEntity>
    )

    private val secondaryDataFlow = combine(
        dao.observeExpenses(),
        dao.observePartyBookings(),
        settingsStore.settingsFlow,
        tickerFlow,
        aiState
    ) { expenses, parties, settings, now, ai ->
        SecondaryData(expenses, parties, settings, now, ai)
    }

    private data class SecondaryData(
        val expenses: List<DailyExpenseEntity>,
        val parties: List<PartyBookingEntity>,
        val settings: AppSettingsState,
        val now: Long,
        val ai: AiAndMessageState
    )

    val uiState: StateFlow<RestaurantUiState> = combine(baseDataFlow, secondaryDataFlow) { base, sec ->
        val todayKey = SeedData.todayDateKey()
        val todayOrders = base.orders.filter { it.dateKey == todayKey && it.status != OrderStatus.CANCELLED }
        val activeOrders = base.orders.filter {
            it.status == OrderStatus.PENDING || it.status == OrderStatus.RUNNING || it.status == OrderStatus.READY
        }

        val todayGrossSales = todayOrders.sumOf { it.totalAmount }
        val todayNetSales = todayOrders.sumOf { it.subtotal }
        val todayGstCollected = todayOrders.sumOf { if (it.gstEnabled) it.gstAmount else 0.0 }
        val todayFoodCogs = todayOrders.sumOf { it.foodCostTotal }

        val todayExpenses = sec.expenses.filter { it.dateKey == todayKey }
        val presentStaff = base.staff.filter { it.isPresentToday }
        val autoStaffSalary = presentStaff.sumOf { it.dailySalary }
        val autoStaffFood = presentStaff.sumOf { it.staffMealCost }

        val costBreakdown = DailyCostBreakdown(
            foodCogs = todayFoodCogs,
            staffSalary = autoStaffSalary + todayExpenses.filter { it.category == ExpenseCategory.STAFF_SALARY }.sumOf { it.amount },
            shopRent = todayExpenses.filter { it.category == ExpenseCategory.SHOP_RENT }.sumOf { it.amount },
            rawMaterial = todayExpenses.filter { it.category == ExpenseCategory.RAW_MATERIAL }.sumOf { it.amount },
            gasFuel = todayExpenses.filter { it.category == ExpenseCategory.GAS_FUEL }.sumOf { it.amount },
            expiryWastage = todayExpenses.filter { it.category == ExpenseCategory.EXPIRY_WASTAGE }.sumOf { it.amount },
            electricity = todayExpenses.filter { it.category == ExpenseCategory.ELECTRICITY }.sumOf { it.amount },
            staffFood = autoStaffFood + todayExpenses.filter { it.category == ExpenseCategory.STAFF_FOOD }.sumOf { it.amount },
            other = todayExpenses.filter { it.category == ExpenseCategory.OTHER }.sumOf { it.amount }
        )

        val todayNetProfit = todayNetSales - costBreakdown.totalCost

        val channels = listOf(
            OrderChannel.ZOMATO,
            OrderChannel.SWIGGY,
            OrderChannel.OTHER_APP,
            OrderChannel.OWN_DELIVERY,
            OrderChannel.DINE_IN,
            OrderChannel.PICKUP,
            OrderChannel.ONLINE_DIRECT
        )
        val channelStats = channels.map { ch ->
            val chOrders = todayOrders.filter { it.channel == ch }
            ChannelStat(ch, chOrders.size, chOrders.sumOf { it.totalAmount })
        }

        val thirdParty = todayOrders.filter { OrderChannel.isThirdParty(it.channel) }
        val ownOrders = todayOrders.filter { !OrderChannel.isThirdParty(it.channel) }

        val outOfStock = base.inventory.filter { it.currentStock <= 0.0 }
        val lowStock = base.inventory.filter { it.currentStock > 0.0 && it.currentStock <= it.minThreshold }

        val orderTipsToday = todayOrders.sumOf { it.tipAmount }
        val staffDayTips = base.staff.sumOf { it.todayTip }
        val todayDayTips = maxOf(orderTipsToday, staffDayTips)
        val monthTips = base.staff.sumOf { it.monthlyTipBonus } + orderTipsToday

        // 7-day trend
        val dayFmt = SimpleDateFormat("EEE", Locale.getDefault())
        val sevenDayPnl = (6 downTo 0).map { daysAgo ->
            val key = SeedData.dateKeyDaysAgo(daysAgo)
            val cal = Calendar.getInstance().apply { add(Calendar.DAY_OF_YEAR, -daysAgo) }
            val label = if (daysAgo == 0) "Today" else dayFmt.format(cal.time)
            if (daysAgo == 0) {
                DayPnlPoint(label, todayNetSales, costBreakdown.totalCost, todayNetProfit)
            } else {
                val dayOrders = base.orders.filter { it.dateKey == key && it.status != OrderStatus.CANCELLED }
                val daySales = dayOrders.sumOf { it.subtotal }
                val dayFood = dayOrders.sumOf { it.foodCostTotal }
                val dayExp = sec.expenses.filter { it.dateKey == key }.sumOf { it.amount }
                val totalDayCost = dayFood + dayExp
                DayPnlPoint(label, daySales, totalDayCost, daySales - totalDayCost)
            }
        }

        RestaurantUiState(
            settings = sec.settings,
            nowMillis = sec.now,
            menuItems = base.menu,
            allOrders = base.orders,
            activeOrders = activeOrders,
            todayOrders = todayOrders,
            inventory = base.inventory,
            lowStockItems = lowStock,
            outOfStockItems = outOfStock,
            staff = base.staff,
            attendanceLogs = base.logs,
            expenses = sec.expenses,
            partyBookings = sec.parties,
            todayGrossSales = todayGrossSales,
            todayNetSales = todayNetSales,
            todayGstCollected = todayGstCollected,
            todayCostBreakdown = costBreakdown,
            todayNetProfit = todayNetProfit,
            todayDayTips = todayDayTips,
            monthTips = monthTips,
            channelStats = channelStats,
            thirdPartyCount = thirdParty.size,
            thirdPartyRevenue = thirdParty.sumOf { it.totalAmount },
            ownRestaurantCount = ownOrders.size,
            ownRestaurantRevenue = ownOrders.sumOf { it.totalAmount },
            sevenDayPnl = sevenDayPnl,
            aiScanResult = sec.ai.scanResult,
            aiScanLoading = sec.ai.scanLoading,
            aiSalaryResult = sec.ai.salaryResult,
            aiSalaryLoading = sec.ai.salaryLoading,
            aiCopilotResult = sec.ai.copilotResult,
            aiCopilotLoading = sec.ai.copilotLoading,
            statusMessage = sec.ai.message
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000L),
        initialValue = RestaurantUiState()
    )

    fun clearStatusMessage() {
        aiState.value = aiState.value.copy(message = null)
    }

    // --- ORDERS ---
    fun placeOrder(
        channel: String,
        tableOrCustomer: String,
        cart: List<CartLine>,
        gstEnabled: Boolean,
        tipAmount: Double,
        paymentMethod: String,
        onOrderCreated: (OrderEntity) -> Unit = {}
    ) {
        if (cart.isEmpty()) return
        viewModelScope.launch {
            val settings = uiState.value.settings
            val subtotal = cart.sumOf { it.menuItem.price * it.quantity }
            val foodCost = cart.sumOf { it.menuItem.costPrice * it.quantity }
            val gstAmount = if (gstEnabled) subtotal * (settings.gstPercent / 100.0) else 0.0
            val total = subtotal + gstAmount + tipAmount
            val summary = cart.joinToString(", ") { "${it.menuItem.name} x${it.quantity}" }
            val itemsJson = cart.joinToString("\n") {
                "${it.menuItem.name}|${it.quantity}|${(it.menuItem.price * it.quantity).toInt()}|${(it.menuItem.costPrice * it.quantity).toInt()}"
            }
            val prefix = when (channel) {
                OrderChannel.ZOMATO -> "ZOM"
                OrderChannel.SWIGGY -> "SWG"
                OrderChannel.OTHER_APP -> "APP"
                OrderChannel.OWN_DELIVERY -> "OWN"
                OrderChannel.PICKUP -> "PKP"
                OrderChannel.ONLINE_DIRECT -> "ONL"
                else -> "DJ"
            }
            val num = "$prefix-${(100..999).random()}"
            val order = OrderEntity(
                orderNumber = num,
                channel = channel,
                status = OrderStatus.PENDING,
                tableOrCustomer = tableOrCustomer.ifBlank {
                    if (channel == OrderChannel.DINE_IN) "Table ${(1..12).random()}" else "Walk-in Guest"
                },
                itemsSummary = summary,
                itemsJson = itemsJson,
                subtotal = subtotal,
                gstEnabled = gstEnabled,
                gstAmount = gstAmount,
                tipAmount = tipAmount,
                totalAmount = total,
                foodCostTotal = foodCost,
                paymentMethod = paymentMethod,
                kotPrinted = settings.autoKotPrint,
                dateKey = SeedData.todayDateKey()
            )
            val id = dao.insertOrder(order).toInt()
            val savedOrder = order.copy(id = id)

            // Auto-deduct matching cold drinks if ordered
            val currentInv = uiState.value.inventory
            cart.forEach { line ->
                val match = currentInv.firstOrNull { inv ->
                    line.menuItem.name.contains("Coca-Cola", ignoreCase = true) && inv.name.contains("Coca-Cola", ignoreCase = true) ||
                        line.menuItem.name.contains("Pepsi", ignoreCase = true) && inv.name.contains("Pepsi", ignoreCase = true)
                }
                if (match != null) {
                    dao.updateInventoryItem(match.copy(currentStock = (match.currentStock - line.quantity).coerceAtLeast(0.0)))
                }
            }

            if (tipAmount > 0 && uiState.value.staff.isNotEmpty()) {
                val present = uiState.value.staff.filter { it.isPresentToday }
                val recipients = present.ifEmpty { uiState.value.staff }
                val share = tipAmount / recipients.size
                recipients.forEach { s ->
                    dao.updateStaff(s.copy(todayTip = s.todayTip + share, monthlyTipBonus = s.monthlyTipBonus + share))
                }
            }

            aiState.value = aiState.value.copy(
                message = if (settings.autoKotPrint) "Order #$num created & Auto-KOT queued!" else "Order #$num created!"
            )
            onOrderCreated(savedOrder)
        }
    }

    fun advanceOrderStatus(order: OrderEntity) {
        viewModelScope.launch {
            val next = when (order.status) {
                OrderStatus.PENDING -> OrderStatus.RUNNING
                OrderStatus.RUNNING -> OrderStatus.READY
                OrderStatus.READY -> OrderStatus.COMPLETED
                else -> OrderStatus.COMPLETED
            }
            dao.updateOrder(
                order.copy(
                    status = next,
                    completedAtMillis = if (next == OrderStatus.COMPLETED) System.currentTimeMillis() else order.completedAtMillis
                )
            )
        }
    }

    fun toggleOrderGst(order: OrderEntity) {
        viewModelScope.launch {
            val newGstEnabled = !order.gstEnabled
            val gstPct = uiState.value.settings.gstPercent / 100.0
            val newGstAmount = if (newGstEnabled) order.subtotal * gstPct else 0.0
            val newTotal = order.subtotal + newGstAmount + order.tipAmount
            dao.updateOrder(
                order.copy(
                    gstEnabled = newGstEnabled,
                    gstAmount = newGstAmount,
                    totalAmount = newTotal
                )
            )
        }
    }

    fun markKotPrinted(order: OrderEntity) {
        viewModelScope.launch {
            dao.updateOrder(order.copy(kotPrinted = true))
        }
    }

    fun cancelOrder(order: OrderEntity) {
        viewModelScope.launch {
            dao.updateOrder(order.copy(status = OrderStatus.CANCELLED))
        }
    }

    // --- MENU & PHOTO SCAN ---
    fun addMenuItem(
        name: String,
        category: String,
        price: Double,
        costPrice: Double,
        isVeg: Boolean,
        prepMinutes: Int,
        chefNote: String
    ) {
        if (name.isBlank() || price <= 0) return
        viewModelScope.launch {
            dao.insertMenuItem(
                MenuItemEntity(
                    name = name.trim(),
                    category = category,
                    price = price,
                    costPrice = costPrice.coerceAtLeast(0.0),
                    isVeg = isVeg,
                    prepMinutes = prepMinutes.coerceIn(1, 90),
                    chefNote = chefNote.trim()
                )
            )
            aiState.value = aiState.value.copy(message = "Added \"$name\" to Fine-Dine Menu")
        }
    }

    fun toggleMenuAvailability(item: MenuItemEntity) {
        viewModelScope.launch {
            dao.updateMenuItem(item.copy(isAvailable = !item.isAvailable))
        }
    }

    fun scanMenuPhoto(bitmap: Bitmap?) {
        viewModelScope.launch {
            aiState.value = aiState.value.copy(scanLoading = true)
            val res = aiRepository.scanMenuFromBitmap(bitmap, uiState.value.settings.restaurantName)
            aiState.value = aiState.value.copy(scanLoading = false, scanResult = res)
        }
    }

    fun importScannedMenuItems(items: List<ScannedMenuItem>) {
        viewModelScope.launch {
            val entities = items.map {
                MenuItemEntity(
                    name = it.name,
                    category = it.category,
                    price = it.price,
                    costPrice = it.costPrice,
                    isVeg = it.isVeg,
                    prepMinutes = 16,
                    chefNote = it.chefNote
                )
            }
            dao.insertMenuItems(entities)
            aiState.value = aiState.value.copy(
                scanResult = null,
                message = "Imported ${entities.size} dishes into Fine-Dine Menu!"
            )
        }
    }

    // --- INVENTORY ---
    fun adjustInventoryStock(item: InventoryItemEntity, delta: Double) {
        viewModelScope.launch {
            val newStock = (item.currentStock + delta).coerceAtLeast(0.0)
            dao.updateInventoryItem(item.copy(currentStock = newStock, lastUpdatedMillis = System.currentTimeMillis()))
        }
    }

    fun updateChefRequirement(item: InventoryItemEntity, neededQty: Double) {
        viewModelScope.launch {
            dao.updateInventoryItem(item.copy(chefNeededQty = neededQty.coerceAtLeast(0.0)))
        }
    }

    fun addInventoryItem(
        name: String,
        category: String,
        unit: String,
        currentStock: Double,
        minThreshold: Double,
        costPerUnit: Double
    ) {
        if (name.isBlank()) return
        viewModelScope.launch {
            dao.insertInventoryItem(
                InventoryItemEntity(
                    name = name.trim(),
                    category = category,
                    unit = unit.ifBlank { "units" },
                    currentStock = currentStock.coerceAtLeast(0.0),
                    minThreshold = minThreshold.coerceAtLeast(1.0),
                    costPerUnit = costPerUnit.coerceAtLeast(0.0)
                )
            )
            aiState.value = aiState.value.copy(message = "Added $name to inventory")
        }
    }

    // --- STAFF & ATTENDANCE ---
    fun markAttendance(member: StaffMemberEntity, present: Boolean, statusLabel: String = if (present) "PRESENT" else "ABSENT") {
        viewModelScope.launch {
            val dateStr = SimpleDateFormat("dd MMM yyyy", Locale.getDefault()).format(Date())
            val timeStr = SimpleDateFormat("hh:mm a", Locale.getDefault()).format(Date())
            dao.updateStaff(
                member.copy(
                    isPresentToday = present,
                    lastAttendanceDate = dateStr,
                    lastCheckInTime = timeStr
                )
            )
            dao.insertAttendanceLog(
                AttendanceLogEntity(
                    staffId = member.id,
                    staffName = member.name,
                    dateLabel = dateStr,
                    timeLabel = timeStr,
                    status = statusLabel
                )
            )
            aiState.value = aiState.value.copy(message = "${member.name} marked $statusLabel at $timeStr")
        }
    }

    fun addStaffMember(
        name: String,
        role: String,
        phone: String,
        dailySalary: Double,
        shiftTiming: String,
        staffMealAssigned: String,
        staffMealCost: Double
    ) {
        if (name.isBlank()) return
        viewModelScope.launch {
            dao.insertStaff(
                StaffMemberEntity(
                    name = name.trim(),
                    role = role.ifBlank { "Steward" },
                    phone = phone.ifBlank { "+91 98000 00000" },
                    dailySalary = dailySalary.coerceAtLeast(100.0),
                    shiftTiming = shiftTiming.ifBlank { "11:00 AM - 10:30 PM" },
                    staffMealAssigned = staffMealAssigned.ifBlank { "Dal Rice + Roti" },
                    staffMealCost = staffMealCost.coerceAtLeast(0.0)
                )
            )
        }
    }

    fun updateStaffMeal(member: StaffMemberEntity, mealName: String, mealCost: Double) {
        viewModelScope.launch {
            dao.updateStaff(
                member.copy(
                    staffMealAssigned = mealName.ifBlank { member.staffMealAssigned },
                    staffMealCost = mealCost.coerceAtLeast(0.0)
                )
            )
        }
    }

    fun addStaffTip(member: StaffMemberEntity, extraTip: Double) {
        if (extraTip <= 0) return
        viewModelScope.launch {
            dao.updateStaff(
                member.copy(
                    todayTip = member.todayTip + extraTip,
                    monthlyTipBonus = member.monthlyTipBonus + extraTip
                )
            )
        }
    }

    fun runAiSalaryCalculator() {
        viewModelScope.launch {
            aiState.value = aiState.value.copy(salaryLoading = true)
            val st = uiState.value
            val res = aiRepository.calculateStaffSalaryAndTips(st.staff, st.todayDayTips, st.monthTips)
            aiState.value = aiState.value.copy(salaryLoading = false, salaryResult = res)
        }
    }

    fun askAiCopilot(prompt: String) {
        if (prompt.isBlank()) return
        viewModelScope.launch {
            aiState.value = aiState.value.copy(copilotLoading = true)
            val st = uiState.value
            val ctx = "Restaurant: ${st.settings.restaurantName}, Today Sales: ₹${st.todayGrossSales.toInt()}, Today Cost: ₹${st.todayCostBreakdown.totalCost.toInt()}, Net Profit: ₹${st.todayNetProfit.toInt()}, Zomato+Swiggy Orders: ${st.thirdPartyCount}, Own Orders: ${st.ownRestaurantCount}, Low/Out Stock Items: ${st.lowStockItems.size + st.outOfStockItems.size}"
            val res = aiRepository.askRestaurantCopilot(prompt.trim(), ctx, st.settings.language)
            aiState.value = aiState.value.copy(copilotLoading = false, copilotResult = res)
        }
    }

    // --- DAILY EXPENSES & P&L ---
    fun addDailyExpense(category: String, title: String, amount: Double, notes: String) {
        if (title.isBlank() || amount <= 0) return
        viewModelScope.launch {
            dao.insertExpense(
                DailyExpenseEntity(
                    category = category,
                    title = title.trim(),
                    amount = amount,
                    notes = notes.trim(),
                    dateKey = SeedData.todayDateKey()
                )
            )
            aiState.value = aiState.value.copy(message = "Logged ₹${amount.toInt()} cost under $title")
        }
    }

    fun deleteExpense(id: Int) {
        viewModelScope.launch { dao.deleteExpense(id) }
    }

    // --- PARTY BOOK ---
    fun addPartyBooking(
        partyName: String,
        partyPhone: String,
        eventDate: String,
        guestCount: Int,
        totalBillAmount: Double,
        selectedInventory: Map<InventoryItemEntity, Double>
    ) {
        if (partyName.isBlank()) return
        viewModelScope.launch {
            var invCost = 0.0
            val summaryParts = mutableListOf<String>()
            selectedInventory.forEach { (item, qtyUsed) ->
                if (qtyUsed > 0.0) {
                    invCost += qtyUsed * item.costPerUnit
                    summaryParts.add("${item.name} x${if (qtyUsed % 1.0 == 0.0) qtyUsed.toInt() else qtyUsed} ${item.unit}")
                    dao.updateInventoryItem(
                        item.copy(
                            currentStock = (item.currentStock - qtyUsed).coerceAtLeast(0.0),
                            lastUpdatedMillis = System.currentTimeMillis()
                        )
                    )
                }
            }
            dao.insertPartyBooking(
                PartyBookingEntity(
                    partyName = partyName.trim(),
                    partyPhone = partyPhone.trim(),
                    eventDate = eventDate.ifBlank { SimpleDateFormat("dd MMM yyyy", Locale.getDefault()).format(Date()) },
                    guestCount = guestCount.coerceAtLeast(1),
                    totalBillAmount = totalBillAmount.coerceAtLeast(0.0),
                    inventoryUsedSummary = summaryParts.joinToString(", ").ifBlank { "Standard Catering Package" },
                    inventoryCost = invCost
                )
            )
            aiState.value = aiState.value.copy(message = "Party booked for $partyName & inventory updated!")
        }
    }

    // --- SETTINGS ---
    fun updateRestaurantName(name: String) = viewModelScope.launch { settingsStore.updateRestaurantName(name) }
    fun updateLanguage(language: AppLanguage) = viewModelScope.launch { settingsStore.updateLanguage(language) }
    fun applyThemePreset(id: Int) = viewModelScope.launch { settingsStore.applyPreset(id) }
    fun updateCustomColors(primaryHex: Long? = null, accentHex: Long? = null, cardHex: Long? = null, textHex: Long? = null) =
        viewModelScope.launch { settingsStore.updateCustomColors(primaryHex, accentHex, cardHex, textHex) }
    fun updateDarkMode(enabled: Boolean) = viewModelScope.launch { settingsStore.updateDarkMode(enabled) }
    fun saveWallpaperFromUri(uri: Uri) = viewModelScope.launch { settingsStore.saveWallpaperFromUri(uri) }
    fun clearWallpaper() = viewModelScope.launch { settingsStore.clearWallpaper() }
    fun updateWallpaperOpacity(opacity: Float) = viewModelScope.launch { settingsStore.updateWallpaperOpacity(opacity) }
    fun updateBillingSettings(gstEnabled: Boolean, gstPercent: Float, autoKot: Boolean) =
        viewModelScope.launch { settingsStore.updateBillingSettings(gstEnabled, gstPercent, autoKot) }
    fun updateTargetAndUpi(target: Double, upiId: String) =
        viewModelScope.launch { settingsStore.updateTargetAndUpi(target, upiId) }
    fun updateConnectors(zomatoOn: Boolean, swiggyOn: Boolean, ownOn: Boolean, zomatoId: String, swiggyId: String) =
        viewModelScope.launch { settingsStore.updateConnectors(zomatoOn, swiggyOn, ownOn, zomatoId, swiggyId) }

    class Factory(
        private val dao: AppDao,
        private val settingsStore: SettingsDataStore,
        private val aiRepository: AiRepository
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return RestaurantViewModel(dao, settingsStore, aiRepository) as T
        }
    }
}
