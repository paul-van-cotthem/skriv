package com.skriv.app.ui.recents

import android.app.Application
import android.net.Uri
import androidx.core.net.toUri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.Companion.APPLICATION_KEY
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.skriv.app.data.db.RecentFileEntity
import com.skriv.app.data.db.SkrivDatabase
import com.skriv.app.data.repository.FileRepository
import com.skriv.app.data.repository.RecentsRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import com.skriv.app.data.prefs.UserPreferences
import com.skriv.app.data.prefs.UserPrefsData


class RecentsViewModel(
    private val recentsRepository: RecentsRepository,
    private val fileRepository: FileRepository,
    private val prefs: UserPreferences
) : ViewModel() {

    val recentFiles: StateFlow<List<RecentFileEntity>> = recentsRepository.recentFiles
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val userPrefs: StateFlow<UserPrefsData> = prefs.data
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), UserPrefsData())

    fun remove(uri: String) {
        viewModelScope.launch { recentsRepository.remove(uri.toUri()) }
    }

    fun clearRecentFiles() {
        viewModelScope.launch { recentsRepository.clearAll() }
    }

    fun reconnectFile(oldUriString: String, newUri: Uri, onComplete: () -> Unit) {
        viewModelScope.launch {
            val oldUri = oldUriString.toUri()
            fileRepository.persistPermission(newUri)
            val displayName = fileRepository.getDisplayName(newUri)
            val lastModified = fileRepository.getLastModified(newUri)
            recentsRepository.reconnect(oldUri, newUri, displayName, lastModified)
            onComplete()
        }
    }

    fun checkFileAvailability(file: RecentFileEntity, onResult: (Boolean) -> Unit) {
        viewModelScope.launch {
            val uri = file.uri.toUri()
            val available = fileRepository.isUriAvailable(uri)
            if (!available) {
                recentsRepository.markUnavailable(uri)
            }
            onResult(available)
        }
    }
}

val RecentsViewModelFactory = viewModelFactory {
    initializer {
        val context = (this[APPLICATION_KEY] as Application).applicationContext
        val db = SkrivDatabase.getInstance(context)
        RecentsViewModel(
            recentsRepository = RecentsRepository(db.recentFileDao()),
            fileRepository = FileRepository(context),
            prefs = UserPreferences(context)
        )
    }
}
