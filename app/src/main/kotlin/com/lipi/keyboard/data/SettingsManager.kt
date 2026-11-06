package com.lipi.keyboard.data

import android.content.Context
import android.content.SharedPreferences
import android.graphics.Color

object SettingsManager {

    private const val PREF_NAME = "lipi_keyboard_prefs"

    private const val KEY_THEME = "key_theme"
    private const val KEY_LAYOUT = "key_layout"
    private const val KEY_HEIGHT = "key_height"
    private const val KEY_HAPTIC = "key_haptic"
    private const val KEY_SOUND = "key_sound"

    private fun getPrefs(context: Context): SharedPreferences {
        return context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
    }

    // --- Theme ---
    fun getTheme(context: Context): String {
        return getPrefs(context).getString(KEY_THEME, "Dark AMOLED") ?: "Dark AMOLED"
    }

    fun setTheme(context: Context, theme: String) {
        getPrefs(context).edit().putString(KEY_THEME, theme).apply()
    }

    data class ThemeColors(
        val bgColor: Int,
        val keyNormalColor: Int,
        val keySpecialColor: Int,
        val keyAccentColor: Int,
        val keyPressedColor: Int,
        val textColor: Int,
        val hintColor: Int
    )

    fun getThemeColors(themeName: String): ThemeColors {
        return when (themeName) {
            "Emerald Green" -> ThemeColors(
                bgColor = Color.parseColor("#004D40"),
                keyNormalColor = Color.parseColor("#00695C"),
                keySpecialColor = Color.parseColor("#00382E"),
                keyAccentColor = Color.parseColor("#FFB300"),
                keyPressedColor = Color.parseColor("#00897B"),
                textColor = Color.parseColor("#FFFFFF"),
                hintColor = Color.parseColor("#B2DFDB")
            )
            "Royal Blue" -> ThemeColors(
                bgColor = Color.parseColor("#0A192F"),
                keyNormalColor = Color.parseColor("#1E293B"),
                keySpecialColor = Color.parseColor("#0F172A"),
                keyAccentColor = Color.parseColor("#38BDF8"),
                keyPressedColor = Color.parseColor("#334155"),
                textColor = Color.parseColor("#FFFFFF"),
                hintColor = Color.parseColor("#94A3B8")
            )
            "Classic Light" -> ThemeColors(
                bgColor = Color.parseColor("#F1F3F4"),
                keyNormalColor = Color.parseColor("#FFFFFF"),
                keySpecialColor = Color.parseColor("#D2D5DA"),
                keyAccentColor = Color.parseColor("#1A73E8"),
                keyPressedColor = Color.parseColor("#D0D4DC"),
                textColor = Color.parseColor("#202124"),
                hintColor = Color.parseColor("#70757A")
            )
            else -> ThemeColors( // Dark AMOLED
                bgColor = Color.parseColor("#0B1918"),
                keyNormalColor = Color.parseColor("#253332"),
                keySpecialColor = Color.parseColor("#132321"),
                keyAccentColor = Color.parseColor("#4D827B"),
                keyPressedColor = Color.parseColor("#365E58"),
                textColor = Color.parseColor("#FFFFFF"),
                hintColor = Color.parseColor("#AAB5B3")
            )
        }
    }

    // --- Layout ---
    fun getDefaultLayoutId(context: Context): String {
        val name = getPrefs(context).getString(KEY_LAYOUT, "Odia Transliteration")
        return when (name) {
            "𑢹𑢺 Warang Chiti (Ho)" -> "warang_chiti"
            "Odia InScript" -> "odia_inscript"
            "English (US)" -> "qwerty_en"
            else -> "odia_translit"
        }
    }

    fun setDefaultLayoutName(context: Context, layoutName: String) {
        getPrefs(context).edit().putString(KEY_LAYOUT, layoutName).apply()
    }

    fun getDefaultLayoutName(context: Context): String {
        return getPrefs(context).getString(KEY_LAYOUT, "Odia Transliteration") ?: "Odia Transliteration"
    }

    // --- Height Scale ---
    fun getHeightScale(context: Context): Float {
        return getPrefs(context).getFloat(KEY_HEIGHT, 100f)
    }

    fun setHeightScale(context: Context, scale: Float) {
        getPrefs(context).edit().putFloat(KEY_HEIGHT, scale).apply()
    }

    // --- Haptics ---
    fun isHapticEnabled(context: Context): Boolean {
        return getPrefs(context).getBoolean(KEY_HAPTIC, true)
    }

    fun setHapticEnabled(context: Context, enabled: Boolean) {
        getPrefs(context).edit().putBoolean(KEY_HAPTIC, enabled).apply()
    }

    // --- Sound ---
    fun isSoundEnabled(context: Context): Boolean {
        return getPrefs(context).getBoolean(KEY_SOUND, false)
    }

    fun setSoundEnabled(context: Context, enabled: Boolean) {
        getPrefs(context).edit().putBoolean(KEY_SOUND, enabled).apply()
    }
}
