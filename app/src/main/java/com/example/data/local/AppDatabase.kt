package com.example.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Database(
    entities = [
        MenuItemEntity::class,
        OrderEntity::class,
        InventoryItemEntity::class,
        StaffMemberEntity::class,
        AttendanceLogEntity::class,
        DailyExpenseEntity::class,
        PartyBookingEntity::class
    ],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun appDao(): AppDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getInstance(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "dinejoy_restaurant.db"
                ).build()
                INSTANCE = instance
                CoroutineScope(Dispatchers.IO).launch {
                    seedIfEmpty(instance.appDao())
                }
                instance
            }
        }

        private suspend fun seedIfEmpty(dao: AppDao) {
            if (dao.getMenuCount() == 0) {
                dao.insertMenuItems(SeedData.initialMenu())
                dao.insertOrders(SeedData.initialOrders())
                dao.insertInventoryItems(SeedData.initialInventory())
                dao.insertStaffList(SeedData.initialStaff())
                dao.insertAttendanceLogs(SeedData.initialAttendanceLogs())
                dao.insertExpenses(SeedData.initialExpenses())
                dao.insertPartyBookings(SeedData.initialPartyBookings())
            }
        }
    }
}
