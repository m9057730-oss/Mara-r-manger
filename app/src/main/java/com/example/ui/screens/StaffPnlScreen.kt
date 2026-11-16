package com.example.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.AutoAwesome
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.Delete
import androidx.compose.material.icons.rounded.Fastfood
import androidx.compose.material.icons.rounded.Person
import androidx.compose.material.icons.rounded.Schedule
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.example.data.local.ExpenseCategory
import com.example.data.local.SeedData
import com.example.data.local.StaffMemberEntity
import com.example.ui.components.AiNoticeBanner
import com.example.ui.components.TodayProfitVsCostChart
import com.example.ui.i18n.AppStrings
import com.example.ui.theme.LossRed
import com.example.ui.theme.ProfitGreen
import com.example.ui.viewmodel.RestaurantUiState

@Composable
fun StaffPnlScreen(
    state: RestaurantUiState,
    strings: AppStrings,
    onMarkAttendance: (StaffMemberEntity, Boolean, String) -> Unit,
    onAddStaffMember: (String, String, String, Double, String, String, Double) -> Unit,
    onUpdateStaffMeal: (StaffMemberEntity, String, Double) -> Unit,
    onAddStaffTip: (StaffMemberEntity, Double) -> Unit,
    onRunAiSalaryCalc: () -> Unit,
    onAddExpense: (String, String, Double, String) -> Unit,
    onDeleteExpense: (Int) -> Unit
) {
    var subTab by remember { mutableIntStateOf(0) } // 0=Daily P&L, 1=Staff & Attendance, 2=Labor Menu & AI
    var showAddExpenseDialog by remember { mutableStateOf(false) }
    var showAddStaffDialog by remember { mutableStateOf(false) }
    var selectedStaffDetail by remember { mutableStateOf<StaffMemberEntity?>(null) }

    val subTabs = listOf("Daily P&L", "Staff", "Labor & AI")

    Scaffold(
        containerColor = Color.Transparent,
        floatingActionButton = {
            if (subTab == 0 || subTab == 1) {
                FloatingActionButton(
                    onClick = {
                        if (subTab == 0) showAddExpenseDialog = true else showAddStaffDialog = true
                    },
                    modifier = Modifier.testTag("staff_pnl_fab"),
                    containerColor = MaterialTheme.colorScheme.primary
                ) {
                    Icon(Icons.Rounded.Add, contentDescription = if (subTab == 0) strings.addExpense else "Add Staff")
                }
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            SingleChoiceSegmentedButtonRow(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 10.dp)
            ) {
                subTabs.forEachIndexed { index, label ->
                    SegmentedButton(
                        selected = subTab == index,
                        onClick = { subTab = index },
                        shape = SegmentedButtonDefaults.itemShape(index = index, count = subTabs.size)
                    ) {
                        Text(label, maxLines = 1, style = MaterialTheme.typography.labelMedium)
                    }
                }
            }

            when (subTab) {
                0 -> DailyPnlTab(
                    state = state,
                    strings = strings,
                    onOpenAddExpense = { showAddExpenseDialog = true },
                    onDeleteExpense = onDeleteExpense
                )
                1 -> StaffAttendanceTab(
                    state = state,
                    strings = strings,
                    onMarkAttendance = onMarkAttendance,
                    onOpenStaffDetail = { selectedStaffDetail = it }
                )
                2 -> LaborMenuAndAiTab(
                    state = state,
                    strings = strings,
                    onUpdateStaffMeal = onUpdateStaffMeal,
                    onRunAiSalaryCalc = onRunAiSalaryCalc
                )
            }
        }
    }

    if (showAddExpenseDialog) {
        AddExpenseDialog(
            onDismiss = { showAddExpenseDialog = false },
            onSave = { cat, title, amount, notes ->
                onAddExpense(cat, title, amount, notes)
                showAddExpenseDialog = false
            }
        )
    }

    if (showAddStaffDialog) {
        AddStaffDialog(
            onDismiss = { showAddStaffDialog = false },
            onSave = { name, role, phone, salary, shift, meal, mealCost ->
                onAddStaffMember(name, role, phone, salary, shift, meal, mealCost)
                showAddStaffDialog = false
            }
        )
    }

    selectedStaffDetail?.let { member ->
        StaffDetailDialog(
            member = member,
            onDismiss = { selectedStaffDetail = null },
            onAddTip = { tip -> onAddStaffTip(member, tip) },
            onMarkPresent = { onMarkAttendance(member, true, "PRESENT") }
        )
    }
}

@Composable
private fun DailyPnlTab(
    state: RestaurantUiState,
    strings: AppStrings,
    onOpenAddExpense: () -> Unit,
    onDeleteExpense: (Int) -> Unit
) {
    val c = state.todayCostBreakdown
    val isProfit = state.todayNetProfit >= 0
    val todayExpenses = remember(state.expenses) {
        val key = SeedData.todayDateKey()
        state.expenses.filter { it.dateKey == key }
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(start = 16.dp, end = 16.dp, bottom = 92.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Card(
                shape = RoundedCornerShape(22.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(strings.dailyPnlStatement, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                        Button(onClick = onOpenAddExpense, contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)) {
                            Text(strings.addExpense, style = MaterialTheme.typography.labelMedium)
                        }
                    }

                    TodayProfitVsCostChart(
                        sales = state.todayNetSales,
                        cost = c.totalCost,
                        netProfit = state.todayNetProfit,
                        salesLabel = strings.todaySales,
                        costLabel = strings.todayCost,
                        profitLabel = if (isProfit) strings.netProfit else strings.netLoss
                    )

                    HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp))
                    PnlRow("Gross Sales (incl. GST)", "₹${state.todayGrossSales.toInt()}", MaterialTheme.colorScheme.primary)
                    PnlRow("GST Collected", "₹${state.todayGstCollected.toInt()}", MaterialTheme.colorScheme.onSurfaceVariant)
                    PnlRow("Net Sales (ex-GST)", "₹${state.todayNetSales.toInt()}", MaterialTheme.colorScheme.onSurface)
                    HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp))
                    PnlRow("Kitchen Food Cost (COGS)", "-₹${c.foodCogs.toInt()}", Color(0xFFD97706))
                    PnlRow("Staff Salary Today", "-₹${c.staffSalary.toInt()}", Color(0xFFD97706))
                    PnlRow("Shop Rent (Daily Share)", "-₹${c.shopRent.toInt()}", Color(0xFFD97706))
                    PnlRow("Raw Material Purchase", "-₹${c.rawMaterial.toInt()}", Color(0xFFD97706))
                    PnlRow("Gas & Tandoor Fuel", "-₹${c.gasFuel.toInt()}", Color(0xFFD97706))
                    PnlRow("Electricity Bill", "-₹${c.electricity.toInt()}", Color(0xFFD97706))
                    PnlRow("Expiry / Wastage Material", "-₹${c.expiryWastage.toInt()}", LossRed)
                    PnlRow("Staff Food Expense", "-₹${c.staffFood.toInt()}", Color(0xFFD97706))
                    HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp))
                    PnlRow(
                        label = if (isProfit) strings.netProfit else strings.netLoss,
                        value = "${if (isProfit) "+" else "-"}₹${kotlin.math.abs(state.todayNetProfit).toInt()}",
                        color = if (isProfit) ProfitGreen else LossRed,
                        bold = true
                    )
                }
            }
        }

        item {
            Text("Today's Logged Cost Entries", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
        }

        items(todayExpenses, key = { it.id }) { exp ->
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(exp.title, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                        Text("${exp.category.replace('_', ' ')} • ${exp.notes}", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text("₹${exp.amount.toInt()}", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold, color = LossRed)
                        IconButton(onClick = { onDeleteExpense(exp.id) }) {
                            Icon(Icons.Rounded.Delete, contentDescription = "Delete expense", tint = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun PnlRow(label: String, value: String, color: Color, bold: Boolean = false) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = label,
            style = if (bold) MaterialTheme.typography.titleMedium else MaterialTheme.typography.bodySmall,
            fontWeight = if (bold) FontWeight.Bold else FontWeight.Medium
        )
        Text(
            text = value,
            style = if (bold) MaterialTheme.typography.titleMedium else MaterialTheme.typography.bodySmall,
            fontWeight = FontWeight.Bold,
            color = color
        )
    }
}

@Composable
private fun StaffAttendanceTab(
    state: RestaurantUiState,
    strings: AppStrings,
    onMarkAttendance: (StaffMemberEntity, Boolean, String) -> Unit,
    onOpenStaffDetail: (StaffMemberEntity) -> Unit
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(start = 16.dp, end = 16.dp, bottom = 92.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Surface(
                    color = ProfitGreen.copy(alpha = 0.12f),
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Text(strings.dayTip, style = MaterialTheme.typography.labelMedium)
                        Text("₹${state.todayDayTips.toInt()}", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, color = ProfitGreen)
                    }
                }
                Surface(
                    color = MaterialTheme.colorScheme.primary.copy(alpha = 0.12f),
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Text(strings.monthTip, style = MaterialTheme.typography.labelMedium)
                        Text("₹${state.monthTips.toInt()}", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                    }
                }
            }
        }

        items(state.staff, key = { it.id }) { member ->
            Card(
                modifier = Modifier.clickable { onOpenStaffDetail(member) },
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Rounded.Person, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                            Spacer(modifier = Modifier.width(8.dp))
                            Column {
                                Text(member.name, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                                Text("${member.role} • ₹${member.dailySalary.toInt()}/day", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                        }
                        Surface(
                            color = if (member.isPresentToday) ProfitGreen.copy(alpha = 0.14f) else LossRed.copy(alpha = 0.12f),
                            shape = RoundedCornerShape(50)
                        ) {
                            Text(
                                text = if (member.isPresentToday) "PRESENT" else "ABSENT",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = if (member.isPresentToday) ProfitGreen else LossRed,
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                            )
                        }
                    }

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Rounded.Schedule, contentDescription = null, modifier = Modifier.size(14.dp), tint = MaterialTheme.colorScheme.onSurfaceVariant)
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = if (member.lastCheckInTime.isNotBlank())
                                "Attendance: ${member.lastAttendanceDate} at ${member.lastCheckInTime}"
                            else
                                "Not checked in today • Shift ${member.shiftTiming}",
                            style = MaterialTheme.typography.labelSmall
                        )
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Day Tip: ₹${member.todayTip.toInt()} • Month: ₹${member.monthlyTipBonus.toInt()}", style = MaterialTheme.typography.labelMedium, color = ProfitGreen, fontWeight = FontWeight.SemiBold)
                        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            OutlinedButton(
                                onClick = { onMarkAttendance(member, false, "ABSENT") },
                                contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp)
                            ) { Text("Absent", style = MaterialTheme.typography.labelSmall) }
                            Button(
                                onClick = { onMarkAttendance(member, true, "PRESENT") },
                                contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp)
                            ) {
                                Icon(Icons.Rounded.CheckCircle, contentDescription = null, modifier = Modifier.size(14.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(strings.markPresent, style = MaterialTheme.typography.labelSmall)
                            }
                        }
                    }
                }
            }
        }

        item {
            Text("Attendance Log (Date & Time)", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
        }
        items(state.attendanceLogs.take(12), key = { it.id }) { log ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text("${log.staffName} (${log.status})", style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.SemiBold)
                Text("${log.dateLabel} • ${log.timeLabel}", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
    }
}

@Composable
private fun LaborMenuAndAiTab(
    state: RestaurantUiState,
    strings: AppStrings,
    onUpdateStaffMeal: (StaffMemberEntity, String, Double) -> Unit,
    onRunAiSalaryCalc: () -> Unit
) {
    var editingStaff by remember { mutableStateOf<StaffMemberEntity?>(null) }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // AI Salary & Tip Calculator
        item {
            Card(
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Rounded.AutoAwesome, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(strings.aiSalaryCalc, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    }
                    AiNoticeBanner(notice = state.aiSalaryResult?.notice ?: strings.aiFirebaseNotice)
                    Button(
                        onClick = onRunAiSalaryCalc,
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("ai_salary_calc_btn")
                    ) {
                        Text("Calculate Staff Salary & Tips with AI")
                    }
                    if (state.aiSalaryLoading) {
                        CircularProgressIndicator(modifier = Modifier.size(22.dp), strokeWidth = 2.dp)
                    } else if (state.aiSalaryResult != null) {
                        Surface(
                            color = MaterialTheme.colorScheme.surfaceVariant,
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = state.aiSalaryResult.data,
                                style = MaterialTheme.typography.bodySmall,
                                modifier = Modifier.padding(12.dp)
                            )
                        }
                    }
                }
            }
        }

        // Labor Cost + Staff Meal Menu
        item {
            Card(
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Rounded.Fastfood, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(strings.staffMealMenu, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    }
                    Text(
                        text = "${strings.laborCost}: ₹${state.todayCostBreakdown.staffSalary.toInt()} • Staff Food Expense Today: ₹${state.todayCostBreakdown.staffFood.toInt()}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
        }

        items(state.staff, key = { it.id }) { member ->
            Card(
                modifier = Modifier.clickable { editingStaff = member },
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(14.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text("${member.name} (${member.role})", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                        Text("Staff Menu: ${member.staffMealAssigned}", style = MaterialTheme.typography.bodySmall)
                    }
                    Text("₹${member.staffMealCost.toInt()}/day", style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                }
            }
        }
    }

    editingStaff?.let { member ->
        var mealName by remember(member) { mutableStateOf(member.staffMealAssigned) }
        var mealCost by remember(member) { mutableStateOf(member.staffMealCost.toInt().toString()) }
        AlertDialog(
            onDismissRequest = { editingStaff = null },
            title = { Text("Staff Meal Menu • ${member.name}") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(value = mealName, onValueChange = { mealName = it }, label = { Text("Meal Items") }, modifier = Modifier.fillMaxWidth())
                    OutlinedTextField(
                        value = mealCost,
                        onValueChange = { mealCost = it },
                        label = { Text("Daily Meal Cost (₹)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(onClick = {
                    onUpdateStaffMeal(member, mealName, mealCost.toDoubleOrNull() ?: member.staffMealCost)
                    editingStaff = null
                }) { Text("Save Meal") }
            },
            dismissButton = { TextButton(onClick = { editingStaff = null }) { Text("Cancel") } }
        )
    }
}

@Composable
private fun StaffDetailDialog(
    member: StaffMemberEntity,
    onDismiss: () -> Unit,
    onAddTip: (Double) -> Unit,
    onMarkPresent: () -> Unit
) {
    var tipText by remember { mutableStateOf("50") }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(member.name, fontWeight = FontWeight.Bold) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Text("Role: ${member.role}")
                Text("Phone: ${member.phone}")
                Text("Shift: ${member.shiftTiming}")
                Text("Daily Salary: ₹${member.dailySalary.toInt()}")
                Text("Today Tip: ₹${member.todayTip.toInt()} | Month Tip: ₹${member.monthlyTipBonus.toInt()}")
                Text("Staff Meal: ${member.staffMealAssigned} (₹${member.staffMealCost.toInt()})")
                Spacer(modifier = Modifier.height(6.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                    OutlinedTextField(
                        value = tipText,
                        onValueChange = { tipText = it },
                        label = { Text("Add Tip ₹") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.weight(1f)
                    )
                    Button(onClick = { onAddTip(tipText.toDoubleOrNull() ?: 0.0) }) { Text("+Tip") }
                }
            }
        },
        confirmButton = {
            Button(onClick = {
                onMarkPresent()
                onDismiss()
            }) { Text("Mark Present") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Close") } }
    )
}

@Composable
private fun AddExpenseDialog(
    onDismiss: () -> Unit,
    onSave: (String, String, Double, String) -> Unit
) {
    var category by remember { mutableStateOf(ExpenseCategory.RAW_MATERIAL) }
    var title by remember { mutableStateOf("") }
    var amount by remember { mutableStateOf("") }
    var notes by remember { mutableStateOf("") }

    val cats = listOf(
        ExpenseCategory.SHOP_RENT to "Shop Rent",
        ExpenseCategory.RAW_MATERIAL to "Raw Material",
        ExpenseCategory.GAS_FUEL to "Gas Expense",
        ExpenseCategory.EXPIRY_WASTAGE to "Expiry Material",
        ExpenseCategory.ELECTRICITY to "Electricity Bill",
        ExpenseCategory.STAFF_FOOD to "Staff Food",
        ExpenseCategory.STAFF_SALARY to "Staff Salary"
    )

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Log Today's Cost") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    items(cats) { (key, label) ->
                        FilterChip(
                            selected = category == key,
                            onClick = {
                                category = key
                                if (title.isBlank()) title = label
                            },
                            label = { Text(label) }
                        )
                    }
                }
                OutlinedTextField(value = title, onValueChange = { title = it }, label = { Text("Cost Title") }, modifier = Modifier.fillMaxWidth())
                OutlinedTextField(
                    value = amount,
                    onValueChange = { amount = it },
                    label = { Text("Amount (₹)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(value = notes, onValueChange = { notes = it }, label = { Text("Notes") }, modifier = Modifier.fillMaxWidth())
            }
        },
        confirmButton = {
            Button(
                onClick = { onSave(category, title.ifBlank { category }, amount.toDoubleOrNull() ?: 0.0, notes) },
                enabled = (amount.toDoubleOrNull() ?: 0.0) > 0
            ) { Text("Add Cost") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel") } }
    )
}

@Composable
private fun AddStaffDialog(
    onDismiss: () -> Unit,
    onSave: (String, String, String, Double, String, String, Double) -> Unit
) {
    var name by remember { mutableStateOf("") }
    var role by remember { mutableStateOf("Steward") }
    var phone by remember { mutableStateOf("") }
    var salary by remember { mutableStateOf("800") }
    var shift by remember { mutableStateOf("11:00 AM - 10:30 PM") }
    var meal by remember { mutableStateOf("Dal Rice + 2 Roti") }
    var mealCost by remember { mutableStateOf("85") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Add Staff Member") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(value = name, onValueChange = { name = it }, label = { Text("Staff Name") }, modifier = Modifier.fillMaxWidth())
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(value = role, onValueChange = { role = it }, label = { Text("Role") }, modifier = Modifier.weight(1f))
                    OutlinedTextField(
                        value = salary,
                        onValueChange = { salary = it },
                        label = { Text("Daily ₹") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.weight(0.8f)
                    )
                }
                OutlinedTextField(value = phone, onValueChange = { phone = it }, label = { Text("Phone") }, modifier = Modifier.fillMaxWidth())
                OutlinedTextField(value = shift, onValueChange = { shift = it }, label = { Text("Shift Timing") }, modifier = Modifier.fillMaxWidth())
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(value = meal, onValueChange = { meal = it }, label = { Text("Staff Meal") }, modifier = Modifier.weight(1f))
                    OutlinedTextField(
                        value = mealCost,
                        onValueChange = { mealCost = it },
                        label = { Text("Meal ₹") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.weight(0.6f)
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    onSave(
                        name,
                        role,
                        phone,
                        salary.toDoubleOrNull() ?: 800.0,
                        shift,
                        meal,
                        mealCost.toDoubleOrNull() ?: 85.0
                    )
                },
                enabled = name.isNotBlank()
            ) { Text("Add Staff") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel") } }
    )
}
