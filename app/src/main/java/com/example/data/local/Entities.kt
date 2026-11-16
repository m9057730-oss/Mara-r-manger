package com.example.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

object OrderChannel {
    const val DINE_IN = "DINE_IN"
    const val PICKUP = "PICKUP"
    const val OWN_DELIVERY = "OWN_DELIVERY"
    const val ONLINE_DIRECT = "ONLINE_DIRECT"
    const val ZOMATO = "ZOMATO"
    const val SWIGGY = "SWIGGY"
    const val OTHER_APP = "OTHER_APP"

    fun isThirdParty(channel: String): Boolean =
        channel == ZOMATO || channel == SWIGGY || channel == OTHER_APP
}

object OrderStatus {
    const val PENDING = "PENDING"
    const val RUNNING = "RUNNING"
    const val READY = "READY"
    const val COMPLETED = "COMPLETED"
    const val CANCELLED = "CANCELLED"
}

object PaymentMethod {
    const val UPI_QR = "UPI_QR"
    const val CARD = "CARD"
    const val CASH = "CASH"
}

object MenuCategory {
    const val STARTERS = "STARTERS"
    const val TANDOOR = "TANDOOR"
    const val ROYAL_MAINS = "ROYAL_MAINS"
    const val BREADS = "BREADS"
    const val COLD_DRINKS = "COLD_DRINKS"
    const val DESSERTS = "DESSERTS"
}

object InventoryCategory {
    const val KITCHEN = "KITCHEN"
    const val TANDOOR = "TANDOOR"
    const val PACKING = "PACKING"
    const val COLD_DRINK = "COLD_DRINK"
}

object ExpenseCategory {
    const val STAFF_SALARY = "STAFF_SALARY"
    const val SHOP_RENT = "SHOP_RENT"
    const val RAW_MATERIAL = "RAW_MATERIAL"
    const val GAS_FUEL = "GAS_FUEL"
    const val EXPIRY_WASTAGE = "EXPIRY_WASTAGE"
    const val ELECTRICITY = "ELECTRICITY"
    const val STAFF_FOOD = "STAFF_FOOD"
    const val OTHER = "OTHER"
}

@Entity(tableName = "menu_items")
data class MenuItemEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val name: String,
    val category: String,
    val price: Double,
    val costPrice: Double,
    val isVeg: Boolean = true,
    val prepMinutes: Int = 15,
    val chefNote: String = "",
    val isAvailable: Boolean = true
)

@Entity(tableName = "orders")
data class OrderEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val orderNumber: String,
    val channel: String,
    val status: String,
    val tableOrCustomer: String,
    val itemsSummary: String,
    val itemsJson: String,
    val subtotal: Double,
    val gstEnabled: Boolean,
    val gstAmount: Double,
    val tipAmount: Double = 0.0,
    val totalAmount: Double,
    val foodCostTotal: Double,
    val paymentMethod: String = PaymentMethod.UPI_QR,
    val kotPrinted: Boolean = false,
    val dateKey: String,
    val createdAtMillis: Long = System.currentTimeMillis(),
    val completedAtMillis: Long? = null
)

@Entity(tableName = "inventory_items")
data class InventoryItemEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val name: String,
    val category: String,
    val unit: String,
    val currentStock: Double,
    val minThreshold: Double,
    val costPerUnit: Double,
    val chefNeededQty: Double = 0.0,
    val lastUpdatedMillis: Long = System.currentTimeMillis()
)

@Entity(tableName = "staff_members")
data class StaffMemberEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val name: String,
    val role: String,
    val phone: String,
    val dailySalary: Double,
    val todayTip: Double = 0.0,
    val monthlyTipBonus: Double = 0.0,
    val shiftTiming: String,
    val isPresentToday: Boolean = false,
    val lastAttendanceDate: String = "",
    val lastCheckInTime: String = "",
    val staffMealAssigned: String = "Dal Rice + 2 Roti + Curd",
    val staffMealCost: Double = 85.0
)

@Entity(tableName = "attendance_logs")
data class AttendanceLogEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val staffId: Int,
    val staffName: String,
    val dateLabel: String,
    val timeLabel: String,
    val status: String,
    val timestampMillis: Long = System.currentTimeMillis()
)

@Entity(tableName = "daily_expenses")
data class DailyExpenseEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val category: String,
    val title: String,
    val amount: Double,
    val notes: String = "",
    val dateKey: String,
    val createdAtMillis: Long = System.currentTimeMillis()
)

@Entity(tableName = "party_bookings")
data class PartyBookingEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val partyName: String,
    val partyPhone: String,
    val eventDate: String,
    val guestCount: Int,
    val totalBillAmount: Double,
    val inventoryUsedSummary: String,
    val inventoryCost: Double,
    val status: String = "UPCOMING",
    val createdAtMillis: Long = System.currentTimeMillis()
)
