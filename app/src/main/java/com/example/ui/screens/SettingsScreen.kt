package com.example.ui.screens

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.ColorLens
import androidx.compose.material.icons.rounded.DeleteOutline
import androidx.compose.material.icons.rounded.Image
import androidx.compose.material.icons.rounded.Language
import androidx.compose.material.icons.rounded.Link
import androidx.compose.material.icons.rounded.Print
import androidx.compose.material.icons.rounded.Store
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Slider
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.example.ui.i18n.AppLanguage
import com.example.ui.i18n.AppStrings
import com.example.ui.theme.HappyThemePresets
import com.example.ui.theme.SwatchColorOptions
import com.example.ui.viewmodel.RestaurantUiState

@Composable
fun SettingsScreen(
    state: RestaurantUiState,
    strings: AppStrings,
    onUpdateRestaurantName: (String) -> Unit,
    onUpdateLanguage: (AppLanguage) -> Unit,
    onApplyPreset: (Int) -> Unit,
    onUpdateCustomColors: (Long?, Long?, Long?, Long?) -> Unit,
    onUpdateDarkMode: (Boolean) -> Unit,
    onPickWallpaperUri: (android.net.Uri) -> Unit,
    onClearWallpaper: () -> Unit,
    onUpdateWallpaperOpacity: (Float) -> Unit,
    onUpdateBilling: (Boolean, Float, Boolean) -> Unit,
    onUpdateTargetAndUpi: (Double, String) -> Unit,
    onUpdateConnectors: (Boolean, Boolean, Boolean, String, String) -> Unit
) {
    val s = state.settings
    var nameInput by remember(s.restaurantName) { mutableStateOf(s.restaurantName) }
    var targetInput by remember(s.dailyTarget) { mutableStateOf(s.dailyTarget.toInt().toString()) }
    var upiInput by remember(s.upiId) { mutableStateOf(s.upiId) }
    var gstPctSlider by remember(s.gstPercent) { mutableFloatStateOf(s.gstPercent) }
    var zomatoId by remember(s.zomatoMerchantId) { mutableStateOf(s.zomatoMerchantId) }
    var swiggyId by remember(s.swiggyMerchantId) { mutableStateOf(s.swiggyMerchantId) }

    val wallpaperPicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri ->
        if (uri != null) onPickWallpaperUri(uri)
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // 1. Restaurant Profile & Target
        item {
            Card(
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Rounded.Store, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(strings.restaurantNameLabel, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    }
                    OutlinedTextField(
                        value = nameInput,
                        onValueChange = { nameInput = it },
                        label = { Text(strings.restaurantNameLabel) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("settings_restaurant_name_input")
                    )
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedTextField(
                            value = targetInput,
                            onValueChange = { targetInput = it },
                            label = { Text("${strings.todayTarget} (₹)") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            modifier = Modifier.weight(1f)
                        )
                        OutlinedTextField(
                            value = upiInput,
                            onValueChange = { upiInput = it },
                            label = { Text("UPI ID for QR") },
                            modifier = Modifier.weight(1f)
                        )
                    }
                    Button(
                        onClick = {
                            onUpdateRestaurantName(nameInput)
                            onUpdateTargetAndUpi(targetInput.toDoubleOrNull() ?: s.dailyTarget, upiInput)
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Save Restaurant Profile")
                    }
                }
            }
        }

        // 2. Multi-Language Setting (Hindi, Punjabi, English)
        item {
            Card(
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Rounded.Language, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(strings.languageSetting, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    }
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        AppLanguage.entries.forEach { lang ->
                            FilterChip(
                                selected = s.language == lang,
                                onClick = { onUpdateLanguage(lang) },
                                label = { Text("${lang.nativeLabel} (${lang.displayName})") },
                                modifier = Modifier
                                    .weight(1f)
                                    .testTag("lang_chip_${lang.code}")
                            )
                        }
                    }
                }
            }
        }

        // 3. Gallery Wallpaper Picker
        item {
            Card(
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Rounded.Image, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(strings.wallpaperSetting, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    }
                    Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        Button(
                            onClick = {
                                wallpaperPicker.launch(
                                    PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                                )
                            },
                            modifier = Modifier
                                .weight(1f)
                                .testTag("pick_wallpaper_btn")
                        ) {
                            Text(strings.pickFromGallery)
                        }
                        if (s.wallpaperPath.isNotBlank()) {
                            OutlinedButton(onClick = onClearWallpaper) {
                                Icon(Icons.Rounded.DeleteOutline, contentDescription = "Remove wallpaper")
                            }
                        }
                    }
                    if (s.wallpaperPath.isNotBlank()) {
                        Text("Wallpaper Visibility", style = MaterialTheme.typography.labelMedium)
                        Slider(
                            value = s.wallpaperOpacity,
                            onValueChange = onUpdateWallpaperOpacity,
                            valueRange = 0.1f..0.85f
                        )
                    }
                }
            }
        }

        // 4. Full App Color & Word Color Studio
        item {
            Card(
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Rounded.ColorLens, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(strings.themeColorsSetting, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                        }
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text("Dark", style = MaterialTheme.typography.labelMedium)
                            Spacer(modifier = Modifier.width(6.dp))
                            Switch(checked = s.darkMode, onCheckedChange = onUpdateDarkMode)
                        }
                    }

                    LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        items(HappyThemePresets) { preset ->
                            FilterChip(
                                selected = s.themePresetId == preset.id,
                                onClick = { onApplyPreset(preset.id) },
                                label = { Text(preset.name) }
                            )
                        }
                    }

                    ColorSwatchRow(
                        title = strings.primaryColorSetting,
                        selectedHex = s.primaryColorHex,
                        onSelect = { onUpdateCustomColors(it, null, null, null) }
                    )
                    ColorSwatchRow(
                        title = "Accent / Profit Color",
                        selectedHex = s.accentColorHex,
                        onSelect = { onUpdateCustomColors(null, it, null, null) }
                    )
                    ColorSwatchRow(
                        title = strings.cardColorSetting,
                        selectedHex = s.cardColorHex,
                        onSelect = { onUpdateCustomColors(null, null, it, null) }
                    )
                    ColorSwatchRow(
                        title = strings.wordColorSetting,
                        selectedHex = s.textColorHex,
                        onSelect = { onUpdateCustomColors(null, null, null, it) }
                    )
                }
            }
        }

        // 5. GST Add/Cancel Default + Auto KOT Print
        item {
            Card(
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Rounded.Print, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Billing, GST & Auto KOT", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    }
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(if (s.defaultGstEnabled) strings.gstAdded else strings.gstCancelled, fontWeight = FontWeight.SemiBold)
                            Text("Default GST (${gstPctSlider.toInt()}%) for new orders", style = MaterialTheme.typography.bodySmall)
                        }
                        Switch(
                            checked = s.defaultGstEnabled,
                            onCheckedChange = { onUpdateBilling(it, gstPctSlider, s.autoKotPrint) }
                        )
                    }
                    Slider(
                        value = gstPctSlider,
                        onValueChange = { gstPctSlider = it },
                        onValueChangeFinished = { onUpdateBilling(s.defaultGstEnabled, gstPctSlider, s.autoKotPrint) },
                        valueRange = 0f..18f
                    )
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(strings.autoKotEnabled, fontWeight = FontWeight.SemiBold)
                            Text("Automatically print KOT when new order arrives", style = MaterialTheme.typography.bodySmall)
                        }
                        Switch(
                            checked = s.autoKotPrint,
                            onCheckedChange = { onUpdateBilling(s.defaultGstEnabled, gstPctSlider, it) }
                        )
                    }
                }
            }
        }

        // 6. Zomato, Swiggy & Own Online Ordering Connectors
        item {
            Card(
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Rounded.Link, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(strings.thirdPartyConnectors, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    }
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Zomato Auto-Sync", fontWeight = FontWeight.SemiBold)
                        Switch(
                            checked = s.zomatoConnected,
                            onCheckedChange = { onUpdateConnectors(it, s.swiggyConnected, s.ownOnlineConnected, zomatoId, swiggyId) }
                        )
                    }
                    OutlinedTextField(value = zomatoId, onValueChange = { zomatoId = it }, label = { Text("Zomato Restaurant ID") }, modifier = Modifier.fillMaxWidth())

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Swiggy Auto-Sync", fontWeight = FontWeight.SemiBold)
                        Switch(
                            checked = s.swiggyConnected,
                            onCheckedChange = { onUpdateConnectors(s.zomatoConnected, it, s.ownOnlineConnected, zomatoId, swiggyId) }
                        )
                    }
                    OutlinedTextField(value = swiggyId, onValueChange = { swiggyId = it }, label = { Text("Swiggy Outlet ID") }, modifier = Modifier.fillMaxWidth())

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Own Website / QR Online Ordering", fontWeight = FontWeight.SemiBold)
                        Switch(
                            checked = s.ownOnlineConnected,
                            onCheckedChange = { onUpdateConnectors(s.zomatoConnected, s.swiggyConnected, it, zomatoId, swiggyId) }
                        )
                    }
                    Button(
                        onClick = { onUpdateConnectors(s.zomatoConnected, s.swiggyConnected, s.ownOnlineConnected, zomatoId, swiggyId) },
                        modifier = Modifier.fillMaxWidth()
                    ) { Text("Save Channel Connections") }
                }
            }
        }
    }
}

@Composable
private fun ColorSwatchRow(
    title: String,
    selectedHex: Long,
    onSelect: (Long) -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
        Text(title, style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.SemiBold)
        LazyRow(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            items(SwatchColorOptions) { hex ->
                val isSelected = selectedHex == hex
                Box(
                    modifier = Modifier
                        .size(38.dp)
                        .clip(CircleShape)
                        .background(Color(hex))
                        .border(
                            width = if (isSelected) 3.dp else 1.dp,
                            color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline,
                            shape = CircleShape
                        )
                        .clickable { onSelect(hex) },
                    contentAlignment = Alignment.Center
                ) {
                    if (isSelected) {
                        Icon(
                            imageVector = Icons.Rounded.Check,
                            contentDescription = "Selected color",
                            tint = if (hex == 0xFFFFFFFFL || hex == 0xFFFFF9F2L) Color.Black else Color.White,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }
        }
    }
}
