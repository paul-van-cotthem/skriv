package com.skriv.app.ui.settings

import android.app.Application
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.Companion.APPLICATION_KEY
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.skriv.app.data.db.SkrivDatabase
import com.skriv.app.data.prefs.UserPrefsData
import com.skriv.app.data.prefs.UserPreferences
import com.skriv.app.data.repository.RecentsRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class SettingsViewModel(
    private val prefs: UserPreferences,
    private val recentsRepository: RecentsRepository
) : ViewModel() {

    val userPrefs: StateFlow<UserPrefsData> = prefs.data
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), UserPrefsData())

    fun setFontMonospace(v: Boolean)    { viewModelScope.launch { prefs.setFontMonospace(v) } }
    fun setFontSizeSp(v: Int)           { viewModelScope.launch { prefs.setFontSizeSp(v) } }
    fun setLineSpacing(v: String)       { viewModelScope.launch { prefs.setLineSpacing(v) } }
    fun setReadingMarginDp(v: Int)      { viewModelScope.launch { prefs.setReadingMarginDp(v) } }
    fun setWordWrap(v: Boolean)         { viewModelScope.launch { prefs.setWordWrap(v) } }
    fun setDarkMode(v: String)          { viewModelScope.launch { prefs.setDarkMode(v) } }
    fun setDefaultExtension(v: String)  { viewModelScope.launch { prefs.setDefaultExtension(v) } }
    fun setLineNumbers(v: Boolean)      { viewModelScope.launch { prefs.setLineNumbers(v) } }
    fun setWordCountVisible(v: Boolean) { viewModelScope.launch { prefs.setWordCountVisible(v) } }
    fun setAutoSaveOnBackground(v: Boolean) { viewModelScope.launch { prefs.setAutoSaveOnBackground(v) } }
    fun setOpenLastFileOnStartup(v: Boolean) { viewModelScope.launch { prefs.setOpenLastFileOnStartup(v) } }
    fun clearRecentFiles()                  { viewModelScope.launch { recentsRepository.clearAll() } }
}

val SettingsViewModelFactory = viewModelFactory {
    initializer {
        val context = (this[APPLICATION_KEY] as Application).applicationContext
        val db = SkrivDatabase.getInstance(context)
        SettingsViewModel(
            prefs = UserPreferences(context),
            recentsRepository = RecentsRepository(db.recentFileDao())
        )
    }
}
