package com.example.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface AppDao {
    // Menu
    @Query("SELECT * FROM menu_items ORDER BY category, name")
    fun observeMenuItems(): Flow<List<MenuItemEntity>>

    @Query("SELECT COUNT(*) FROM menu_items")
    suspend fun getMenuCount(): Int

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMenuItem(item: MenuItemEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMenuItems(items: List<MenuItemEntity>)

    @Update
    suspend fun updateMenuItem(item: MenuItemEntity)

    @Query("DELETE FROM menu_items WHERE id = :id")
    suspend fun deleteMenuItem(id: Int)

    // Orders
    @Query("SELECT * FROM orders ORDER BY createdAtMillis DESC")
    fun observeAllOrders(): Flow<List<OrderEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrder(order: OrderEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrders(orders: List<OrderEntity>)

    @Update
    suspend fun updateOrder(order: OrderEntity)

    @Query("DELETE FROM orders WHERE id = :id")
    suspend fun deleteOrder(id: Int)

    // Inventory
    @Query("SELECT * FROM inventory_items ORDER BY category, name")
    fun observeInventory(): Flow<List<InventoryItemEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertInventoryItem(item: InventoryItemEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertInventoryItems(items: List<InventoryItemEntity>)

    @Update
    suspend fun updateInventoryItem(item: InventoryItemEntity)

    @Query("DELETE FROM inventory_items WHERE id = :id")
    suspend fun deleteInventoryItem(id: Int)

    // Staff & Attendance
    @Query("SELECT * FROM staff_members ORDER BY id ASC")
    fun observeStaff(): Flow<List<StaffMemberEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertStaff(member: StaffMemberEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertStaffList(members: List<StaffMemberEntity>)

    @Update
    suspend fun updateStaff(member: StaffMemberEntity)

    @Query("SELECT * FROM attendance_logs ORDER BY timestampMillis DESC LIMIT 60")
    fun observeAttendanceLogs(): Flow<List<AttendanceLogEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAttendanceLog(log: AttendanceLogEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAttendanceLogs(logs: List<AttendanceLogEntity>)

    // Daily Expenses & P&L
    @Query("SELECT * FROM daily_expenses ORDER BY createdAtMillis DESC")
    fun observeExpenses(): Flow<List<DailyExpenseEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertExpense(expense: DailyExpenseEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertExpenses(expenses: List<DailyExpenseEntity>)

    @Query("DELETE FROM daily_expenses WHERE id = :id")
    suspend fun deleteExpense(id: Int)

    // Party Book
    @Query("SELECT * FROM party_bookings ORDER BY createdAtMillis DESC")
    fun observePartyBookings(): Flow<List<PartyBookingEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPartyBooking(booking: PartyBookingEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPartyBookings(bookings: List<PartyBookingEntity>)

    @Update
    suspend fun updatePartyBooking(booking: PartyBookingEntity)
}
