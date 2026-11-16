package com.example.data.local

import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

object SeedData {
    fun todayDateKey(): String =
        SimpleDateFormat("yyyy-MM-dd", Locale.US).format(Date())

    fun dateKeyDaysAgo(daysAgo: Int): String {
        val cal = Calendar.getInstance()
        cal.add(Calendar.DAY_OF_YEAR, -daysAgo)
        return SimpleDateFormat("yyyy-MM-dd", Locale.US).format(cal.time)
    }

    fun initialMenu(): List<MenuItemEntity> = listOf(
        MenuItemEntity(
            name = "Saffron Malai Paneer Tikka",
            category = MenuCategory.TANDOOR,
            price = 425.0,
            costPrice = 145.0,
            isVeg = true,
            prepMinutes = 18,
            chefNote = "Charcoal-roasted cottage cheese glazed with Kashmiri saffron & cream"
        ),
        MenuItemEntity(
            name = "Truffle Butter Chicken",
            category = MenuCategory.ROYAL_MAINS,
            price = 560.0,
            costPrice = 210.0,
            isVeg = false,
            prepMinutes = 22,
            chefNote = "Smoked tandoori tikka simmered in velvety heirloom tomato makhani & truffle oil"
        ),
        MenuItemEntity(
            name = "Dal Bukhara Royal (24-Hr)",
            category = MenuCategory.ROYAL_MAINS,
            price = 440.0,
            costPrice = 120.0,
            isVeg = true,
            prepMinutes = 14,
            chefNote = "Slow-simmered black urad lentils finished with white butter & fenugreek"
        ),
        MenuItemEntity(
            name = "Tandoori Bhatti Murgh",
            category = MenuCategory.TANDOOR,
            price = 520.0,
            costPrice = 195.0,
            isVeg = false,
            prepMinutes = 24,
            chefNote = "Clay-oven roasted half chicken marinated in hung curd & roasted cumin"
        ),
        MenuItemEntity(
            name = "Crispy Lotus Stem & Truffle Chaat",
            category = MenuCategory.STARTERS,
            price = 360.0,
            costPrice = 110.0,
            isVeg = true,
            prepMinutes = 12,
            chefNote = "Amuse-bouche lotus chips with tamarind caviar & mint foam"
        ),
        MenuItemEntity(
            name = "Charcoal Garlic Butter Naan",
            category = MenuCategory.BREADS,
            price = 110.0,
            costPrice = 28.0,
            isVeg = true,
            prepMinutes = 8,
            chefNote = "Flaky tandoor flatbread brushed with roasted garlic & cultured butter"
        ),
        MenuItemEntity(
            name = "Lachha Lacquered Paratha",
            category = MenuCategory.BREADS,
            price = 95.0,
            costPrice = 22.0,
            isVeg = true,
            prepMinutes = 8,
            chefNote = "Multi-layered whole wheat crisp tandoori bread"
        ),
        MenuItemEntity(
            name = "Coca-Cola (Chilled 300ml)",
            category = MenuCategory.COLD_DRINKS,
            price = 75.0,
            costPrice = 28.0,
            isVeg = true,
            prepMinutes = 2,
            chefNote = "Served chilled with fresh lime slice & ice"
        ),
        MenuItemEntity(
            name = "Pepsi (Chilled 300ml)",
            category = MenuCategory.COLD_DRINKS,
            price = 75.0,
            costPrice = 28.0,
            isVeg = true,
            prepMinutes = 2,
            chefNote = "Chilled can with crushed mint garnish"
        ),
        MenuItemEntity(
            name = "Mint Shikanji Fizz",
            category = MenuCategory.COLD_DRINKS,
            price = 185.0,
            costPrice = 42.0,
            isVeg = true,
            prepMinutes = 5,
            chefNote = "Sparkling roasted cumin & garden mint cooler"
        ),
        MenuItemEntity(
            name = "Masala Chai Crème Brûlée",
            category = MenuCategory.DESSERTS,
            price = 320.0,
            costPrice = 95.0,
            isVeg = true,
            prepMinutes = 10,
            chefNote = "Cardamom-infused custard with torched jaggery caramel crust"
        )
    )

    fun initialOrders(): List<OrderEntity> {
        val now = System.currentTimeMillis()
        val today = todayDateKey()
        return listOf(
            // Active Live & Pending Orders
            OrderEntity(
                orderNumber = "DJ-401",
                channel = OrderChannel.DINE_IN,
                status = OrderStatus.RUNNING,
                tableOrCustomer = "Table 04 (Royal Booth)",
                itemsSummary = "Truffle Butter Chicken x1, Charcoal Garlic Naan x3, Coca-Cola x2",
                itemsJson = "Truffle Butter Chicken|1|560|210\nCharcoal Garlic Butter Naan|3|330|84\nCoca-Cola (Chilled 300ml)|2|150|56",
                subtotal = 1040.0,
                gstEnabled = true,
                gstAmount = 52.0,
                tipAmount = 100.0,
                totalAmount = 1192.0,
                foodCostTotal = 350.0,
                paymentMethod = PaymentMethod.UPI_QR,
                kotPrinted = true,
                dateKey = today,
                createdAtMillis = now - 9 * 60_000L
            ),
            OrderEntity(
                orderNumber = "ZOM-882",
                channel = OrderChannel.ZOMATO,
                status = OrderStatus.PENDING,
                tableOrCustomer = "Zomato • Aarav K.",
                itemsSummary = "Saffron Malai Paneer Tikka x1, Dal Bukhara x1, Pepsi x2",
                itemsJson = "Saffron Malai Paneer Tikka|1|425|145\nDal Bukhara Royal (24-Hr)|1|440|120\nPepsi (Chilled 300ml)|2|150|56",
                subtotal = 1015.0,
                gstEnabled = true,
                gstAmount = 50.75,
                tipAmount = 0.0,
                totalAmount = 1065.75,
                foodCostTotal = 321.0,
                paymentMethod = PaymentMethod.CARD,
                kotPrinted = false,
                dateKey = today,
                createdAtMillis = now - 3 * 60_000L
            ),
            OrderEntity(
                orderNumber = "SWG-519",
                channel = OrderChannel.SWIGGY,
                status = OrderStatus.RUNNING,
                tableOrCustomer = "Swiggy • Simran D.",
                itemsSummary = "Tandoori Bhatti Murgh x1, Lachha Paratha x2, Mint Shikanji x2",
                itemsJson = "Tandoori Bhatti Murgh|1|520|195\nLachha Lacquered Paratha|2|190|44\nMint Shikanji Fizz|2|370|84",
                subtotal = 1080.0,
                gstEnabled = true,
                gstAmount = 54.0,
                tipAmount = 40.0,
                totalAmount = 1174.0,
                foodCostTotal = 323.0,
                paymentMethod = PaymentMethod.UPI_QR,
                kotPrinted = true,
                dateKey = today,
                createdAtMillis = now - 14 * 60_000L
            ),
            OrderEntity(
                orderNumber = "OWN-207",
                channel = OrderChannel.OWN_DELIVERY,
                status = OrderStatus.READY,
                tableOrCustomer = "Own Delivery • Sector 18",
                itemsSummary = "Dal Bukhara Royal x2, Charcoal Garlic Naan x4, Coca-Cola x3",
                itemsJson = "Dal Bukhara Royal (24-Hr)|2|880|240\nCharcoal Garlic Butter Naan|4|440|112\nCoca-Cola (Chilled 300ml)|3|225|84",
                subtotal = 1545.0,
                gstEnabled = true,
                gstAmount = 77.25,
                tipAmount = 80.0,
                totalAmount = 1702.25,
                foodCostTotal = 436.0,
                paymentMethod = PaymentMethod.CASH,
                kotPrinted = true,
                dateKey = today,
                createdAtMillis = now - 19 * 60_000L
            ),
            OrderEntity(
                orderNumber = "PKP-114",
                channel = OrderChannel.PICKUP,
                status = OrderStatus.PENDING,
                tableOrCustomer = "Pickup • Gurpreet S.",
                itemsSummary = "Crispy Lotus Stem x1, Masala Chai Crème Brûlée x2",
                itemsJson = "Crispy Lotus Stem & Truffle Chaat|1|360|110\nMasala Chai Crème Brûlée|2|640|190",
                subtotal = 1000.0,
                gstEnabled = false,
                gstAmount = 0.0,
                tipAmount = 0.0,
                totalAmount = 1000.0,
                foodCostTotal = 300.0,
                paymentMethod = PaymentMethod.CASH,
                kotPrinted = false,
                dateKey = today,
                createdAtMillis = now - 2 * 60_000L
            ),
            // Completed Orders Today (to give realistic sales + P&L on first launch)
            OrderEntity(
                orderNumber = "DJ-395",
                channel = OrderChannel.DINE_IN,
                status = OrderStatus.COMPLETED,
                tableOrCustomer = "Table 02",
                itemsSummary = "Truffle Butter Chicken x2, Saffron Paneer Tikka x2, Naan x6, Pepsi x4",
                itemsJson = "Truffle Butter Chicken|2|1120|420\nSaffron Malai Paneer Tikka|2|850|290\nCharcoal Garlic Butter Naan|6|660|168\nPepsi (Chilled 300ml)|4|300|112",
                subtotal = 2930.0,
                gstEnabled = true,
                gstAmount = 146.5,
                tipAmount = 250.0,
                totalAmount = 3326.5,
                foodCostTotal = 990.0,
                paymentMethod = PaymentMethod.CARD,
                kotPrinted = true,
                dateKey = today,
                createdAtMillis = now - 140 * 60_000L,
                completedAtMillis = now - 105 * 60_000L
            ),
            OrderEntity(
                orderNumber = "ONL-390",
                channel = OrderChannel.ONLINE_DIRECT,
                status = OrderStatus.COMPLETED,
                tableOrCustomer = "QR Web • Priya M.",
                itemsSummary = "Dal Bukhara x2, Tandoori Murgh x2, Coca-Cola x4",
                itemsJson = "Dal Bukhara Royal (24-Hr)|2|880|240\nTandoori Bhatti Murgh|2|1040|390\nCoca-Cola (Chilled 300ml)|4|300|112",
                subtotal = 2220.0,
                gstEnabled = true,
                gstAmount = 111.0,
                tipAmount = 120.0,
                totalAmount = 2451.0,
                foodCostTotal = 742.0,
                paymentMethod = PaymentMethod.UPI_QR,
                kotPrinted = true,
                dateKey = today,
                createdAtMillis = now - 190 * 60_000L,
                completedAtMillis = now - 160 * 60_000L
            ),
            OrderEntity(
                orderNumber = "ZOM-871",
                channel = OrderChannel.ZOMATO,
                status = OrderStatus.COMPLETED,
                tableOrCustomer = "Zomato • Kabir V.",
                itemsSummary = "Truffle Butter Chicken x2, Garlic Naan x4, Pepsi x2",
                itemsJson = "Truffle Butter Chicken|2|1120|420\nCharcoal Garlic Butter Naan|4|440|112\nPepsi (Chilled 300ml)|2|150|56",
                subtotal = 1710.0,
                gstEnabled = true,
                gstAmount = 85.5,
                tipAmount = 50.0,
                totalAmount = 1845.5,
                foodCostTotal = 588.0,
                paymentMethod = PaymentMethod.UPI_QR,
                kotPrinted = true,
                dateKey = today,
                createdAtMillis = now - 230 * 60_000L,
                completedAtMillis = now - 200 * 60_000L
            ),
            OrderEntity(
                orderNumber = "OTH-102",
                channel = OrderChannel.OTHER_APP,
                status = OrderStatus.COMPLETED,
                tableOrCustomer = "Magicpin • Neha R.",
                itemsSummary = "Saffron Paneer Tikka x2, Mint Shikanji x3",
                itemsJson = "Saffron Malai Paneer Tikka|2|850|290\nMint Shikanji Fizz|3|555|126",
                subtotal = 1405.0,
                gstEnabled = true,
                gstAmount = 70.25,
                tipAmount = 30.0,
                totalAmount = 1505.25,
                foodCostTotal = 416.0,
                paymentMethod = PaymentMethod.CARD,
                kotPrinted = true,
                dateKey = today,
                createdAtMillis = now - 260 * 60_000L,
                completedAtMillis = now - 230 * 60_000L
            ),
            // Past 5 days sample orders for 7-day Profit & Loss trend chart
            OrderEntity(
                orderNumber = "HIST-1",
                channel = OrderChannel.DINE_IN,
                status = OrderStatus.COMPLETED,
                tableOrCustomer = "Day Summary -1",
                itemsSummary = "Daily Dine-In & Delivery Batch",
                itemsJson = "",
                subtotal = 28400.0,
                gstEnabled = true,
                gstAmount = 1420.0,
                tipAmount = 950.0,
                totalAmount = 30770.0,
                foodCostTotal = 9800.0,
                paymentMethod = PaymentMethod.UPI_QR,
                kotPrinted = true,
                dateKey = dateKeyDaysAgo(1),
                createdAtMillis = now - 1 * 86_400_000L
            ),
            OrderEntity(
                orderNumber = "HIST-2",
                channel = OrderChannel.ZOMATO,
                status = OrderStatus.COMPLETED,
                tableOrCustomer = "Day Summary -2",
                itemsSummary = "Daily Dine-In & Delivery Batch",
                itemsJson = "",
                subtotal = 24200.0,
                gstEnabled = true,
                gstAmount = 1210.0,
                tipAmount = 720.0,
                totalAmount = 26130.0,
                foodCostTotal = 8600.0,
                paymentMethod = PaymentMethod.CARD,
                kotPrinted = true,
                dateKey = dateKeyDaysAgo(2),
                createdAtMillis = now - 2 * 86_400_000L
            ),
            OrderEntity(
                orderNumber = "HIST-3",
                channel = OrderChannel.DINE_IN,
                status = OrderStatus.COMPLETED,
                tableOrCustomer = "Day Summary -3",
                itemsSummary = "Slow Weekday Batch",
                itemsJson = "",
                subtotal = 14800.0,
                gstEnabled = true,
                gstAmount = 740.0,
                tipAmount = 380.0,
                totalAmount = 15920.0,
                foodCostTotal = 6100.0,
                paymentMethod = PaymentMethod.CASH,
                kotPrinted = true,
                dateKey = dateKeyDaysAgo(3),
                createdAtMillis = now - 3 * 86_400_000L
            ),
            OrderEntity(
                orderNumber = "HIST-4",
                channel = OrderChannel.SWIGGY,
                status = OrderStatus.COMPLETED,
                tableOrCustomer = "Day Summary -4",
                itemsSummary = "Daily Batch",
                itemsJson = "",
                subtotal = 31900.0,
                gstEnabled = true,
                gstAmount = 1595.0,
                tipAmount = 1120.0,
                totalAmount = 34615.0,
                foodCostTotal = 10900.0,
                paymentMethod = PaymentMethod.UPI_QR,
                kotPrinted = true,
                dateKey = dateKeyDaysAgo(4),
                createdAtMillis = now - 4 * 86_400_000L
            ),
            OrderEntity(
                orderNumber = "HIST-5",
                channel = OrderChannel.OWN_DELIVERY,
                status = OrderStatus.COMPLETED,
                tableOrCustomer = "Day Summary -5",
                itemsSummary = "Daily Batch",
                itemsJson = "",
                subtotal = 26700.0,
                gstEnabled = true,
                gstAmount = 1335.0,
                tipAmount = 810.0,
                totalAmount = 28845.0,
                foodCostTotal = 9100.0,
                paymentMethod = PaymentMethod.CARD,
                kotPrinted = true,
                dateKey = dateKeyDaysAgo(5),
                createdAtMillis = now - 5 * 86_400_000L
            )
        )
    }

    fun initialInventory(): List<InventoryItemEntity> = listOf(
        // Kitchen Items
        InventoryItemEntity(name = "Basmati Aged Rice", category = InventoryCategory.KITCHEN, unit = "kg", currentStock = 28.0, minThreshold = 15.0, costPerUnit = 115.0, chefNeededQty = 0.0),
        InventoryItemEntity(name = "Fresh Cream & White Butter", category = InventoryCategory.KITCHEN, unit = "kg", currentStock = 3.0, minThreshold = 6.0, costPerUnit = 420.0, chefNeededQty = 8.0),
        InventoryItemEntity(name = "Heirloom Tomatoes", category = InventoryCategory.KITCHEN, unit = "kg", currentStock = 18.0, minThreshold = 12.0, costPerUnit = 55.0, chefNeededQty = 0.0),
        InventoryItemEntity(name = "Fresh Chicken Breast", category = InventoryCategory.KITCHEN, unit = "kg", currentStock = 4.5, minThreshold = 8.0, costPerUnit = 260.0, chefNeededQty = 10.0),
        InventoryItemEntity(name = "Kashmiri Saffron & Spices", category = InventoryCategory.KITCHEN, unit = "packs", currentStock = 6.0, minThreshold = 4.0, costPerUnit = 350.0, chefNeededQty = 0.0),
        // Tandoor Items
        InventoryItemEntity(name = "Hardwood Charcoal (Koyla)", category = InventoryCategory.TANDOOR, unit = "kg", currentStock = 12.0, minThreshold = 25.0, costPerUnit = 45.0, chefNeededQty = 35.0),
        InventoryItemEntity(name = "Malai Paneer Blocks", category = InventoryCategory.TANDOOR, unit = "kg", currentStock = 5.0, minThreshold = 7.0, costPerUnit = 380.0, chefNeededQty = 8.0),
        InventoryItemEntity(name = "Fine Maida & Wheat Atta", category = InventoryCategory.TANDOOR, unit = "kg", currentStock = 32.0, minThreshold = 15.0, costPerUnit = 42.0, chefNeededQty = 0.0),
        InventoryItemEntity(name = "Hung Curd & Tikka Marinade", category = InventoryCategory.TANDOOR, unit = "kg", currentStock = 0.0, minThreshold = 5.0, costPerUnit = 160.0, chefNeededQty = 8.0),
        // Packing Material
        InventoryItemEntity(name = "500ml Leak-Proof Meal Boxes", category = InventoryCategory.PACKING, unit = "pcs", currentStock = 45.0, minThreshold = 100.0, costPerUnit = 9.0, chefNeededQty = 200.0),
        InventoryItemEntity(name = "Aluminium Foil & Butter Paper", category = InventoryCategory.PACKING, unit = "rolls", currentStock = 8.0, minThreshold = 5.0, costPerUnit = 140.0, chefNeededQty = 0.0),
        InventoryItemEntity(name = "Branded Kraft Carry Bags", category = InventoryCategory.PACKING, unit = "pcs", currentStock = 130.0, minThreshold = 80.0, costPerUnit = 7.0, chefNeededQty = 0.0),
        InventoryItemEntity(name = "Wooden Cutlery & Tissue Kits", category = InventoryCategory.PACKING, unit = "packs", currentStock = 22.0, minThreshold = 50.0, costPerUnit = 6.0, chefNeededQty = 100.0),
        // Cold Drinks (Separate Inventory!)
        InventoryItemEntity(name = "Coca-Cola 300ml Cans", category = InventoryCategory.COLD_DRINK, unit = "cans", currentStock = 14.0, minThreshold = 24.0, costPerUnit = 28.0, chefNeededQty = 48.0),
        InventoryItemEntity(name = "Pepsi 300ml Cans", category = InventoryCategory.COLD_DRINK, unit = "cans", currentStock = 32.0, minThreshold = 24.0, costPerUnit = 28.0, chefNeededQty = 0.0),
        InventoryItemEntity(name = "Sprite & Soda Bottles", category = InventoryCategory.COLD_DRINK, unit = "bottles", currentStock = 8.0, minThreshold = 20.0, costPerUnit = 22.0, chefNeededQty = 36.0),
        InventoryItemEntity(name = "Sparkling Mineral Water", category = InventoryCategory.COLD_DRINK, unit = "bottles", currentStock = 48.0, minThreshold = 24.0, costPerUnit = 14.0, chefNeededQty = 0.0)
    )

    fun initialStaff(): List<StaffMemberEntity> {
        val todayDisplay = SimpleDateFormat("dd MMM yyyy", Locale.getDefault()).format(Date())
        return listOf(
            StaffMemberEntity(
                name = "Chef Harpreet Gill",
                role = "Head Chef",
                phone = "+91 98140 11223",
                dailySalary = 1650.0,
                todayTip = 180.0,
                monthlyTipBonus = 4650.0,
                shiftTiming = "10:00 AM - 10:30 PM",
                isPresentToday = true,
                lastAttendanceDate = todayDisplay,
                lastCheckInTime = "09:48 AM",
                staffMealAssigned = "Dal Makhani + 3 Roti + Salad",
                staffMealCost = 110.0
            ),
            StaffMemberEntity(
                name = "Ustad Imran Khan",
                role = "Tandoor Master",
                phone = "+91 98140 33445",
                dailySalary = 1200.0,
                todayTip = 120.0,
                monthlyTipBonus = 3400.0,
                shiftTiming = "11:00 AM - 11:00 PM",
                isPresentToday = true,
                lastAttendanceDate = todayDisplay,
                lastCheckInTime = "10:52 AM",
                staffMealAssigned = "Chicken Curry + 3 Naan",
                staffMealCost = 125.0
            ),
            StaffMemberEntity(
                name = "Simranjeet Kaur",
                role = "Floor Captain & POS",
                phone = "+91 98140 55667",
                dailySalary = 950.0,
                todayTip = 210.0,
                monthlyTipBonus = 5120.0,
                shiftTiming = "11:00 AM - 10:30 PM",
                isPresentToday = true,
                lastAttendanceDate = todayDisplay,
                lastCheckInTime = "10:58 AM",
                staffMealAssigned = "Paneer Bhurji + 2 Roti + Curd",
                staffMealCost = 95.0
            ),
            StaffMemberEntity(
                name = "Rohan Verma",
                role = "Fine-Dine Steward",
                phone = "+91 98140 77889",
                dailySalary = 750.0,
                todayTip = 160.0,
                monthlyTipBonus = 3850.0,
                shiftTiming = "12:00 PM - 11:00 PM",
                isPresentToday = false,
                lastAttendanceDate = "",
                lastCheckInTime = "",
                staffMealAssigned = "Rajma Chawal + Pickle",
                staffMealCost = 80.0
            ),
            StaffMemberEntity(
                name = "Balwinder Singh",
                role = "Own Delivery Rider",
                phone = "+91 98140 99001",
                dailySalary = 700.0,
                todayTip = 80.0,
                monthlyTipBonus = 2200.0,
                shiftTiming = "11:30 AM - 11:00 PM",
                isPresentToday = true,
                lastAttendanceDate = todayDisplay,
                lastCheckInTime = "11:24 AM",
                staffMealAssigned = "Dal Rice + 2 Roti",
                staffMealCost = 75.0
            )
        )
    }

    fun initialAttendanceLogs(): List<AttendanceLogEntity> {
        val todayDisplay = SimpleDateFormat("dd MMM yyyy", Locale.getDefault()).format(Date())
        return listOf(
            AttendanceLogEntity(staffId = 1, staffName = "Chef Harpreet Gill", dateLabel = todayDisplay, timeLabel = "09:48 AM", status = "PRESENT"),
            AttendanceLogEntity(staffId = 2, staffName = "Ustad Imran Khan", dateLabel = todayDisplay, timeLabel = "10:52 AM", status = "PRESENT"),
            AttendanceLogEntity(staffId = 3, staffName = "Simranjeet Kaur", dateLabel = todayDisplay, timeLabel = "10:58 AM", status = "PRESENT"),
            AttendanceLogEntity(staffId = 5, staffName = "Balwinder Singh", dateLabel = todayDisplay, timeLabel = "11:24 AM", status = "PRESENT")
        )
    }

    fun initialExpenses(): List<DailyExpenseEntity> {
        val today = todayDateKey()
        return listOf(
            DailyExpenseEntity(category = ExpenseCategory.SHOP_RENT, title = "Shop Rent (Daily Share)", amount = 2200.0, notes = "Monthly ₹66,000 / 30 days", dateKey = today),
            DailyExpenseEntity(category = ExpenseCategory.RAW_MATERIAL, title = "Morning Sabzi & Dairy Purchase", amount = 1850.0, notes = "Paneer, cream, tomatoes, herbs", dateKey = today),
            DailyExpenseEntity(category = ExpenseCategory.GAS_FUEL, title = "Commercial LPG & Tandoor Coal", amount = 780.0, notes = "Kitchen gas & charcoal", dateKey = today),
            DailyExpenseEntity(category = ExpenseCategory.ELECTRICITY, title = "Electricity & AC Bill (Daily Share)", amount = 650.0, notes = "Smart meter daily estimate", dateKey = today),
            DailyExpenseEntity(category = ExpenseCategory.EXPIRY_WASTAGE, title = "Expired Herb & Milk Spoilage", amount = 210.0, notes = "Daily expiry audit", dateKey = today),
            // Past 5 days cost benchmarks for 7-day trend
            DailyExpenseEntity(category = ExpenseCategory.OTHER, title = "Day -1 Operating Costs", amount = 11200.0, notes = "Full daily overhead", dateKey = dateKeyDaysAgo(1)),
            DailyExpenseEntity(category = ExpenseCategory.OTHER, title = "Day -2 Operating Costs", amount = 10400.0, notes = "Full daily overhead", dateKey = dateKeyDaysAgo(2)),
            DailyExpenseEntity(category = ExpenseCategory.OTHER, title = "Day -3 Operating Costs", amount = 11900.0, notes = "Full daily overhead", dateKey = dateKeyDaysAgo(3)),
            DailyExpenseEntity(category = ExpenseCategory.OTHER, title = "Day -4 Operating Costs", amount = 12100.0, notes = "Full daily overhead", dateKey = dateKeyDaysAgo(4)),
            DailyExpenseEntity(category = ExpenseCategory.OTHER, title = "Day -5 Operating Costs", amount = 10800.0, notes = "Full daily overhead", dateKey = dateKeyDaysAgo(5))
        )
    }

    fun initialPartyBookings(): List<PartyBookingEntity> = listOf(
        PartyBookingEntity(
            partyName = "Mehta Anniversary Dinner",
            partyPhone = "+91 98765 43210",
            eventDate = SimpleDateFormat("dd MMM yyyy", Locale.getDefault()).format(Date()),
            guestCount = 35,
            totalBillAmount = 24500.0,
            inventoryUsedSummary = "Basmati Aged Rice x5 kg, Malai Paneer Blocks x4 kg, Coca-Cola 300ml Cans x24 cans",
            inventoryCost = 2767.0,
            status = "UPCOMING"
        )
    )
}
