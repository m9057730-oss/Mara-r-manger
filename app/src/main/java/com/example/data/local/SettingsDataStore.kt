package com.example.data.local

import android.content.Context
import android.net.Uri
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.doublePreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.floatPreferencesKey
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.example.ui.i18n.AppLanguage
import com.example.ui.theme.HappyThemePresets
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import java.io.File

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "dinejoy_settings")

data class AppSettingsState(
    val restaurantName: String = "Saffron Crown Fine Dine",
    val language: AppLanguage = AppLanguage.ENGLISH,
    val themePresetId: Int = 0,
    val primaryColorHex: Long = HappyThemePresets[0].primaryHex,
    val accentColorHex: Long = HappyThemePresets[0].accentHex,
    val cardColorHex: Long = HappyThemePresets[0].cardHex,
    val textColorHex: Long = HappyThemePresets[0].textHex,
    val backgroundColorHex: Long = HappyThemePresets[0].bgHex,
    val darkMode: Boolean = false,
    val wallpaperPath: String = "",
    val wallpaperOpacity: Float = 0.28f,
    val defaultGstEnabled: Boolean = true,
    val gstPercent: Float = 5.0f,
    val autoKotPrint: Boolean = true,
    val dailyTarget: Double = 35000.0,
    val upiId: String = "saffroncrown@upi",
    val zomatoConnected: Boolean = true,
    val swiggyConnected: Boolean = true,
    val ownOnlineConnected: Boolean = true,
    val zomatoMerchantId: String = "ZOM-CHD-9021",
    val swiggyMerchantId: String = "SWG-CHD-4482"
)

class SettingsDataStore(private val context: Context) {
    private object Keys {
        val RESTAURANT_NAME = stringPreferencesKey("restaurant_name")
        val LANGUAGE = stringPreferencesKey("language_code")
        val PRESET_ID = intPreferencesKey("preset_id")
        val PRIMARY_HEX = longPreferencesKey("primary_hex")
        val ACCENT_HEX = longPreferencesKey("accent_hex")
        val CARD_HEX = longPreferencesKey("card_hex")
        val TEXT_HEX = longPreferencesKey("text_hex")
        val BG_HEX = longPreferencesKey("bg_hex")
        val DARK_MODE = booleanPreferencesKey("dark_mode")
        val WALLPAPER_PATH = stringPreferencesKey("wallpaper_path")
        val WALLPAPER_OPACITY = floatPreferencesKey("wallpaper_opacity")
        val GST_ENABLED = booleanPreferencesKey("gst_enabled")
        val GST_PERCENT = floatPreferencesKey("gst_percent")
        val AUTO_KOT = booleanPreferencesKey("auto_kot")
        val DAILY_TARGET = doublePreferencesKey("daily_target")
        val UPI_ID = stringPreferencesKey("upi_id")
        val ZOMATO_ON = booleanPreferencesKey("zomato_on")
        val SWIGGY_ON = booleanPreferencesKey("swiggy_on")
        val OWN_ONLINE_ON = booleanPreferencesKey("own_online_on")
        val ZOMATO_ID = stringPreferencesKey("zomato_id")
        val SWIGGY_ID = stringPreferencesKey("swiggy_id")
    }

    val settingsFlow: Flow<AppSettingsState> = context.dataStore.data.map { prefs ->
        AppSettingsState(
            restaurantName = prefs[Keys.RESTAURANT_NAME] ?: "Saffron Crown Fine Dine",
            language = AppLanguage.fromCode(prefs[Keys.LANGUAGE] ?: "en"),
            themePresetId = prefs[Keys.PRESET_ID] ?: 0,
            primaryColorHex = prefs[Keys.PRIMARY_HEX] ?: HappyThemePresets[0].primaryHex,
            accentColorHex = prefs[Keys.ACCENT_HEX] ?: HappyThemePresets[0].accentHex,
            cardColorHex = prefs[Keys.CARD_HEX] ?: HappyThemePresets[0].cardHex,
            textColorHex = prefs[Keys.TEXT_HEX] ?: HappyThemePresets[0].textHex,
            backgroundColorHex = prefs[Keys.BG_HEX] ?: HappyThemePresets[0].bgHex,
            darkMode = prefs[Keys.DARK_MODE] ?: false,
            wallpaperPath = prefs[Keys.WALLPAPER_PATH] ?: "",
            wallpaperOpacity = prefs[Keys.WALLPAPER_OPACITY] ?: 0.28f,
            defaultGstEnabled = prefs[Keys.GST_ENABLED] ?: true,
            gstPercent = prefs[Keys.GST_PERCENT] ?: 5.0f,
            autoKotPrint = prefs[Keys.AUTO_KOT] ?: true,
            dailyTarget = prefs[Keys.DAILY_TARGET] ?: 35000.0,
            upiId = prefs[Keys.UPI_ID] ?: "saffroncrown@upi",
            zomatoConnected = prefs[Keys.ZOMATO_ON] ?: true,
            swiggyConnected = prefs[Keys.SWIGGY_ON] ?: true,
            ownOnlineConnected = prefs[Keys.OWN_ONLINE_ON] ?: true,
            zomatoMerchantId = prefs[Keys.ZOMATO_ID] ?: "ZOM-CHD-9021",
            swiggyMerchantId = prefs[Keys.SWIGGY_ID] ?: "SWG-CHD-4482"
        )
    }

    suspend fun updateRestaurantName(name: String) {
        context.dataStore.edit { it[Keys.RESTAURANT_NAME] = name.ifBlank { "DineJoy Bistro" } }
    }

    suspend fun updateLanguage(language: AppLanguage) {
        context.dataStore.edit { it[Keys.LANGUAGE] = language.code }
    }

    suspend fun applyPreset(presetId: Int) {
        val preset = HappyThemePresets.getOrElse(presetId) { HappyThemePresets[0] }
        context.dataStore.edit {
            it[Keys.PRESET_ID] = preset.id
            it[Keys.PRIMARY_HEX] = preset.primaryHex
            it[Keys.ACCENT_HEX] = preset.accentHex
            it[Keys.CARD_HEX] = preset.cardHex
            it[Keys.TEXT_HEX] = preset.textHex
            it[Keys.BG_HEX] = preset.bgHex
        }
    }

    suspend fun updateCustomColors(
        primaryHex: Long? = null,
        accentHex: Long? = null,
        cardHex: Long? = null,
        textHex: Long? = null
    ) {
        context.dataStore.edit {
            primaryHex?.let { hex -> it[Keys.PRIMARY_HEX] = hex }
            accentHex?.let { hex -> it[Keys.ACCENT_HEX] = hex }
            cardHex?.let { hex -> it[Keys.CARD_HEX] = hex }
            textHex?.let { hex -> it[Keys.TEXT_HEX] = hex }
        }
    }

    suspend fun updateDarkMode(enabled: Boolean) {
        context.dataStore.edit { it[Keys.DARK_MODE] = enabled }
    }

    suspend fun saveWallpaperFromUri(uri: Uri) {
        val savedPath = withContext(Dispatchers.IO) {
            val dest = File(context.filesDir, "custom_wallpaper_${System.currentTimeMillis()}.jpg")
            // Clean previous wallpaper files
            context.filesDir.listFiles()?.filter { it.name.startsWith("custom_wallpaper_") }?.forEach { it.delete() }
            context.contentResolver.openInputStream(uri)?.use { input ->
                dest.outputStream().use { output -> input.copyTo(output) }
            }
            if (dest.exists() && dest.length() > 0) dest.absolutePath else ""
        }
        if (savedPath.isNotBlank()) {
            context.dataStore.edit { it[Keys.WALLPAPER_PATH] = savedPath }
        }
    }

    suspend fun clearWallpaper() {
        withContext(Dispatchers.IO) {
            context.filesDir.listFiles()?.filter { it.name.startsWith("custom_wallpaper_") }?.forEach { it.delete() }
        }
        context.dataStore.edit { it[Keys.WALLPAPER_PATH] = "" }
    }

    suspend fun updateWallpaperOpacity(opacity: Float) {
        context.dataStore.edit { it[Keys.WALLPAPER_OPACITY] = opacity.coerceIn(0.1f, 0.85f) }
    }

    suspend fun updateBillingSettings(gstEnabled: Boolean, gstPercent: Float, autoKot: Boolean) {
        context.dataStore.edit {
            it[Keys.GST_ENABLED] = gstEnabled
            it[Keys.GST_PERCENT] = gstPercent.coerceIn(0f, 28f)
            it[Keys.AUTO_KOT] = autoKot
        }
    }

    suspend fun updateTargetAndUpi(dailyTarget: Double, upiId: String) {
        context.dataStore.edit {
            it[Keys.DAILY_TARGET] = dailyTarget.coerceAtLeast(1000.0)
            it[Keys.UPI_ID] = upiId.ifBlank { "dinejoy@upi" }
        }
    }

    suspend fun updateConnectors(
        zomatoOn: Boolean,
        swiggyOn: Boolean,
        ownOnlineOn: Boolean,
        zomatoId: String,
        swiggyId: String
    ) {
        context.dataStore.edit {
            it[Keys.ZOMATO_ON] = zomatoOn
            it[Keys.SWIGGY_ON] = swiggyOn
            it[Keys.OWN_ONLINE_ON] = ownOnlineOn
            it[Keys.ZOMATO_ID] = zomatoId
            it[Keys.SWIGGY_ID] = swiggyId
        }
    }
}
