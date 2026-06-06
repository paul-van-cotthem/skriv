package com.skriv.app.data.prefs

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.*
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "user_prefs")

data class UserPrefsData(
    val fontMonospace: Boolean = false,
    val fontSizeSp: Int = 15,
    val lineSpacing: String = "normal",
    val readingMarginDp: Int = 16,
    val wordWrap: Boolean = true,
    val darkMode: String = "system",
    val defaultExtension: String = "txt",
    val lineNumbers: Boolean = false,
    val wordCountVisible: Boolean = true,
    val autoSaveOnBackground: Boolean = false,
    val openLastFileOnStartup: Boolean = false
)

class UserPreferences(private val context: Context) {
    private object Keys {
        val FONT_MONOSPACE = booleanPreferencesKey("font_monospace")
        val FONT_SIZE_SP = intPreferencesKey("font_size_sp")
        val LINE_SPACING = stringPreferencesKey("line_spacing")
        val READING_MARGIN_DP = intPreferencesKey("reading_margin_dp")
        val WORD_WRAP = booleanPreferencesKey("word_wrap")
        val DARK_MODE = stringPreferencesKey("dark_mode")
        val DEFAULT_EXTENSION = stringPreferencesKey("default_extension")
        val LINE_NUMBERS = booleanPreferencesKey("line_numbers")
        val WORD_COUNT_VISIBLE = booleanPreferencesKey("word_count_visible")
        val AUTO_SAVE_ON_BACKGROUND = booleanPreferencesKey("auto_save_on_background")
        val OPEN_LAST_FILE_ON_STARTUP = booleanPreferencesKey("open_last_file_on_startup")
    }

    val data: Flow<UserPrefsData> = context.dataStore.data.map { prefs ->
        UserPrefsData(
            fontMonospace = prefs[Keys.FONT_MONOSPACE] ?: false,
            fontSizeSp = prefs[Keys.FONT_SIZE_SP] ?: 15,
            lineSpacing = prefs[Keys.LINE_SPACING] ?: "normal",
            readingMarginDp = (prefs[Keys.READING_MARGIN_DP] ?: 16).coerceAtLeast(16),
            wordWrap = prefs[Keys.WORD_WRAP] ?: true,
            darkMode = prefs[Keys.DARK_MODE] ?: "system",
            defaultExtension = prefs[Keys.DEFAULT_EXTENSION] ?: "txt",
            lineNumbers = prefs[Keys.LINE_NUMBERS] ?: false,
            wordCountVisible = prefs[Keys.WORD_COUNT_VISIBLE] ?: true,
            autoSaveOnBackground = prefs[Keys.AUTO_SAVE_ON_BACKGROUND] ?: false,
            openLastFileOnStartup = prefs[Keys.OPEN_LAST_FILE_ON_STARTUP] ?: false
        )
    }

    suspend fun setFontMonospace(v: Boolean) { context.dataStore.edit { it[Keys.FONT_MONOSPACE] = v } }
    suspend fun setFontSizeSp(v: Int) { context.dataStore.edit { it[Keys.FONT_SIZE_SP] = v } }
    suspend fun setLineSpacing(v: String) { context.dataStore.edit { it[Keys.LINE_SPACING] = v } }
    suspend fun setReadingMarginDp(v: Int) { context.dataStore.edit { it[Keys.READING_MARGIN_DP] = v } }
    suspend fun setWordWrap(v: Boolean) { context.dataStore.edit { it[Keys.WORD_WRAP] = v } }
    suspend fun setDarkMode(v: String) { context.dataStore.edit { it[Keys.DARK_MODE] = v } }
    suspend fun setDefaultExtension(v: String) { context.dataStore.edit { it[Keys.DEFAULT_EXTENSION] = v } }
    suspend fun setLineNumbers(v: Boolean) { context.dataStore.edit { it[Keys.LINE_NUMBERS] = v } }
    suspend fun setWordCountVisible(v: Boolean) { context.dataStore.edit { it[Keys.WORD_COUNT_VISIBLE] = v } }
    suspend fun setAutoSaveOnBackground(v: Boolean) { context.dataStore.edit { it[Keys.AUTO_SAVE_ON_BACKGROUND] = v } }
    suspend fun setOpenLastFileOnStartup(v: Boolean) { context.dataStore.edit { it[Keys.OPEN_LAST_FILE_ON_STARTUP] = v } }
}
