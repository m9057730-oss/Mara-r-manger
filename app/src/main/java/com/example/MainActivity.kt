package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.rounded.Dashboard
import androidx.compose.material.icons.rounded.Groups
import androidx.compose.material.icons.rounded.Inventory2
import androidx.compose.material.icons.rounded.ReceiptLong
import androidx.compose.material.icons.rounded.RestaurantMenu
import androidx.compose.material.icons.rounded.Settings
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationRail
import androidx.compose.material3.NavigationRailItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import coil.compose.AsyncImage
import com.example.data.ai.PlaceholderAiRepository
import com.example.data.local.AppDatabase
import com.example.data.local.SettingsDataStore
import com.example.ui.i18n.stringsFor
import com.example.ui.screens.DashboardScreen
import com.example.ui.screens.InventoryPartyScreen
import com.example.ui.screens.MenuQrScreen
import com.example.ui.screens.OrdersScreen
import com.example.ui.screens.SettingsScreen
import com.example.ui.screens.StaffPnlScreen
import com.example.ui.theme.DineJoyTheme
import com.example.ui.viewmodel.RestaurantViewModel
import java.io.File

private const val ROUTE_DASHBOARD = "dashboard"
private const val ROUTE_ORDERS = "orders"
private const val ROUTE_MENU_QR = "menu_qr"
private const val ROUTE_INVENTORY = "inventory"
private const val ROUTE_STAFF_PNL = "staff_pnl"
private const val ROUTE_SETTINGS = "settings"

private data class NavTabItem(
    val route: String,
    val label: String,
    val icon: ImageVector,
    val badgeCount: Int = 0
)

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val context = LocalContext.current
            val viewModel: RestaurantViewModel = viewModel(
                factory = remember(context) {
                    val db = AppDatabase.getInstance(context)
                    RestaurantViewModel.Factory(
                        dao = db.appDao(),
                        settingsStore = SettingsDataStore(context),
                        aiRepository = PlaceholderAiRepository()
                    )
                }
            )
            DineJoyAppRoot(viewModel = viewModel)
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DineJoyAppRoot(viewModel: RestaurantViewModel) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val settings = state.settings
    val strings = remember(settings.language) { stringsFor(settings.language) }
    val hasWallpaper = settings.wallpaperPath.isNotBlank() && File(settings.wallpaperPath).exists()

    DineJoyTheme(
        primaryColor = Color(settings.primaryColorHex),
        accentColor = Color(settings.accentColorHex),
        backgroundColor = Color(settings.backgroundColorHex),
        cardColor = Color(settings.cardColorHex),
        textColor = Color(settings.textColorHex),
        darkTheme = settings.darkMode,
        hasWallpaper = hasWallpaper
    ) {
        val navController = rememberNavController()
        val navBackStackEntry by navController.currentBackStackEntryAsState()
        val currentRoute = navBackStackEntry?.destination?.route ?: ROUTE_DASHBOARD
        val snackbarHostState = remember { SnackbarHostState() }
        var openOrderCreateSheet by remember { mutableStateOf(false) }

        LaunchedEffect(state.statusMessage) {
            state.statusMessage?.let { msg ->
                snackbarHostState.showSnackbar(msg)
                viewModel.clearStatusMessage()
            }
        }

        val lowAlertCount = state.outOfStockItems.size + state.lowStockItems.size
        val navTabs = listOf(
            NavTabItem(ROUTE_DASHBOARD, strings.navDashboard, Icons.Rounded.Dashboard),
            NavTabItem(ROUTE_ORDERS, strings.navOrders, Icons.Rounded.ReceiptLong, state.activeOrders.size),
            NavTabItem(ROUTE_MENU_QR, strings.navMenuQr, Icons.Rounded.RestaurantMenu),
            NavTabItem(ROUTE_INVENTORY, strings.navInventory, Icons.Rounded.Inventory2, lowAlertCount),
            NavTabItem(ROUTE_STAFF_PNL, strings.navStaffPnl, Icons.Rounded.Groups)
        )

        fun navigateToTab(route: String) {
            navController.navigate(route) {
                popUpTo(navController.graph.findStartDestination().id) { saveState = true }
                launchSingleTop = true
                restoreState = true
            }
        }

        BoxWithConstraints(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    if (settings.darkMode) Color(0xFF17120F) else Color(settings.backgroundColorHex)
                )
        ) {
            val isExpandedScreen = maxWidth >= 600.dp

            // Optional custom Gallery Wallpaper layer
            if (hasWallpaper) {
                AsyncImage(
                    model = File(settings.wallpaperPath),
                    contentDescription = "Custom restaurant wallpaper",
                    contentScale = ContentScale.Crop,
                    alpha = settings.wallpaperOpacity,
                    modifier = Modifier.fillMaxSize()
                )
            }

            Row(modifier = Modifier.fillMaxSize()) {
                if (isExpandedScreen) {
                    NavigationRail(
                        containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.94f)
                    ) {
                        navTabs.forEach { tab ->
                            NavigationRailItem(
                                selected = currentRoute == tab.route,
                                onClick = { navigateToTab(tab.route) },
                                icon = {
                                    BadgedBox(
                                        badge = {
                                            if (tab.badgeCount > 0) {
                                                Badge { Text("${tab.badgeCount}") }
                                            }
                                        }
                                    ) {
                                        Icon(tab.icon, contentDescription = tab.label)
                                    }
                                },
                                label = { Text(tab.label, maxLines = 1, overflow = TextOverflow.Ellipsis) }
                            )
                        }
                        NavigationRailItem(
                            selected = currentRoute == ROUTE_SETTINGS,
                            onClick = { navigateToTab(ROUTE_SETTINGS) },
                            icon = { Icon(Icons.Rounded.Settings, contentDescription = strings.navSettings) },
                            label = { Text(strings.navSettings, maxLines = 1) }
                        )
                    }
                }

                Scaffold(
                    containerColor = Color.Transparent,
                    contentWindowInsets = WindowInsets.safeDrawing,
                    snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
                    topBar = {
                        TopAppBar(
                            colors = TopAppBarDefaults.topAppBarColors(
                                containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.92f)
                            ),
                            navigationIcon = {
                                if (currentRoute == ROUTE_SETTINGS) {
                                    IconButton(onClick = { navController.popBackStack() }) {
                                        Icon(Icons.AutoMirrored.Rounded.ArrowBack, contentDescription = "Back")
                                    }
                                }
                            },
                            title = {
                                Text(
                                    text = if (currentRoute == ROUTE_SETTINGS) strings.navSettings else settings.restaurantName,
                                    style = MaterialTheme.typography.titleLarge,
                                    fontWeight = FontWeight.Bold,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            },
                            actions = {
                                if (currentRoute != ROUTE_SETTINGS) {
                                    IconButton(
                                        onClick = { navigateToTab(ROUTE_SETTINGS) },
                                        modifier = Modifier.testTag("top_settings_button")
                                    ) {
                                        Icon(Icons.Rounded.Settings, contentDescription = strings.navSettings)
                                    }
                                }
                            }
                        )
                    },
                    bottomBar = {
                        if (!isExpandedScreen && currentRoute != ROUTE_SETTINGS) {
                            NavigationBar(
                                containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.96f)
                            ) {
                                navTabs.forEach { tab ->
                                    NavigationBarItem(
                                        selected = currentRoute == tab.route,
                                        onClick = { navigateToTab(tab.route) },
                                        icon = {
                                            BadgedBox(
                                                badge = {
                                                    if (tab.badgeCount > 0) {
                                                        Badge { Text("${tab.badgeCount}") }
                                                    }
                                                }
                                            ) {
                                                Icon(tab.icon, contentDescription = tab.label)
                                            }
                                        },
                                        label = {
                                            Text(
                                                text = tab.label,
                                                maxLines = 1,
                                                overflow = TextOverflow.Ellipsis,
                                                style = MaterialTheme.typography.labelSmall
                                            )
                                        }
                                    )
                                }
                            }
                        }
                    }
                ) { innerPadding ->
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(innerPadding)
                    ) {
                        NavHost(
                            navController = navController,
                            startDestination = ROUTE_DASHBOARD
                        ) {
                            composable(ROUTE_DASHBOARD) {
                                DashboardScreen(
                                    state = state,
                                    strings = strings,
                                    onOpenNewOrder = {
                                        openOrderCreateSheet = true
                                        navigateToTab(ROUTE_ORDERS)
                                    },
                                    onNavigateOrders = { navigateToTab(ROUTE_ORDERS) },
                                    onNavigateInventory = { navigateToTab(ROUTE_INVENTORY) },
                                    onNavigateStaff = { navigateToTab(ROUTE_STAFF_PNL) },
                                    onAdvanceOrder = viewModel::advanceOrderStatus,
                                    onMarkKotPrinted = viewModel::markKotPrinted,
                                    onAskCopilot = viewModel::askAiCopilot
                                )
                            }
                            composable(ROUTE_ORDERS) {
                                OrdersScreen(
                                    state = state,
                                    strings = strings,
                                    openCreateSheetInitially = openOrderCreateSheet,
                                    onCreateSheetConsumed = { openOrderCreateSheet = false },
                                    onPlaceOrder = viewModel::placeOrder,
                                    onAdvanceOrder = viewModel::advanceOrderStatus,
                                    onToggleGst = viewModel::toggleOrderGst,
                                    onMarkKotPrinted = viewModel::markKotPrinted,
                                    onCancelOrder = viewModel::cancelOrder
                                )
                            }
                            composable(ROUTE_MENU_QR) {
                                MenuQrScreen(
                                    state = state,
                                    strings = strings,
                                    onAddMenuItem = viewModel::addMenuItem,
                                    onToggleAvailability = viewModel::toggleMenuAvailability,
                                    onScanMenuPhoto = viewModel::scanMenuPhoto,
                                    onImportScannedItems = viewModel::importScannedMenuItems
                                )
                            }
                            composable(ROUTE_INVENTORY) {
                                InventoryPartyScreen(
                                    state = state,
                                    strings = strings,
                                    onAdjustStock = viewModel::adjustInventoryStock,
                                    onUpdateChefNeeded = viewModel::updateChefRequirement,
                                    onAddInventoryItem = viewModel::addInventoryItem,
                                    onAddPartyBooking = viewModel::addPartyBooking
                                )
                            }
                            composable(ROUTE_STAFF_PNL) {
                                StaffPnlScreen(
                                    state = state,
                                    strings = strings,
                                    onMarkAttendance = viewModel::markAttendance,
                                    onAddStaffMember = viewModel::addStaffMember,
                                    onUpdateStaffMeal = viewModel::updateStaffMeal,
                                    onAddStaffTip = viewModel::addStaffTip,
                                    onRunAiSalaryCalc = viewModel::runAiSalaryCalculator,
                                    onAddExpense = viewModel::addDailyExpense,
                                    onDeleteExpense = viewModel::deleteExpense
                                )
                            }
                            composable(ROUTE_SETTINGS) {
                                SettingsScreen(
                                    state = state,
                                    strings = strings,
                                    onUpdateRestaurantName = viewModel::updateRestaurantName,
                                    onUpdateLanguage = viewModel::updateLanguage,
                                    onApplyPreset = viewModel::applyThemePreset,
                                    onUpdateCustomColors = viewModel::updateCustomColors,
                                    onUpdateDarkMode = viewModel::updateDarkMode,
                                    onPickWallpaperUri = viewModel::saveWallpaperFromUri,
                                    onClearWallpaper = viewModel::clearWallpaper,
                                    onUpdateWallpaperOpacity = viewModel::updateWallpaperOpacity,
                                    onUpdateBilling = viewModel::updateBillingSettings,
                                    onUpdateTargetAndUpi = viewModel::updateTargetAndUpi,
                                    onUpdateConnectors = viewModel::updateConnectors
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
