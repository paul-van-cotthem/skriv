package com.skriv.app.ui.editor

import android.app.Application
import android.net.Uri
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.text.input.TextFieldState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.Companion.APPLICATION_KEY
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.skriv.app.data.db.SkrivDatabase
import com.skriv.app.data.prefs.UserPrefsData
import com.skriv.app.data.prefs.UserPreferences
import com.skriv.app.data.repository.FileRepository
import com.skriv.app.data.repository.FileTooLargeException
import com.skriv.app.data.repository.RecentsRepository
import com.skriv.app.model.DocumentState
import com.skriv.app.model.PendingAction
import com.skriv.app.model.UiEvent
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@OptIn(ExperimentalFoundationApi::class, FlowPreview::class)
class EditorViewModel(
    private val fileRepository: FileRepository,
    private val recentsRepository: RecentsRepository,
    private val prefs: UserPreferences
) : ViewModel() {

    private val _documentState = MutableStateFlow(
        DocumentState(
            uri = null,
            displayName = "Untitled.txt",
            isSaved = true,
            isReadOnly = false,
            isLoading = false,
            hadBom = false
        )
    )
    val documentState: StateFlow<DocumentState> = _documentState.asStateFlow()

    var textFieldState by mutableStateOf(TextFieldState())
        private set

    private val _findBarState = MutableStateFlow(FindBarState())
    val findBarState: StateFlow<FindBarState> = _findBarState.asStateFlow()

    val userPreferences: StateFlow<UserPrefsData> = prefs.data
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), UserPrefsData())

    private val _events = MutableSharedFlow<UiEvent>()
    val events: SharedFlow<UiEvent> = _events.asSharedFlow()

    private val _pendingAction = MutableStateFlow<PendingAction?>(null)
    val pendingAction: StateFlow<PendingAction?> = _pendingAction.asStateFlow()

    private val _createDocumentRequest = MutableSharedFlow<String>()
    val createDocumentRequest: SharedFlow<String> = _createDocumentRequest.asSharedFlow()

    private val _restoredScrollOffset = MutableSharedFlow<Int>()
    val restoredScrollOffset: SharedFlow<Int> = _restoredScrollOffset.asSharedFlow()

    private val _isFocusMode = MutableStateFlow(false)
    val isFocusMode: StateFlow<Boolean> = _isFocusMode.asStateFlow()

    private val _isChromeHidden = MutableStateFlow(false)
    val isChromeHidden: StateFlow<Boolean> = _isChromeHidden.asStateFlow()

    private var inactivityJob: Job? = null

    val canUndo: StateFlow<Boolean> = snapshotFlow { textFieldState.undoState.canUndo }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), false)

    val canRedo: StateFlow<Boolean> = snapshotFlow { textFieldState.undoState.canRedo }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), false)

    val wordCount: StateFlow<Int> = snapshotFlow { textFieldState.text.toString() }
        .debounce(300)
        .map { text ->
            if (text.isBlank()) 0
            else text.trim().split(Regex("\\s+")).count()
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    private var lastSavedContent: String = ""
    private var _savedPendingAction: PendingAction? = null
    private var _pendingOpenUri: Uri? = null
    private var recomputeJob: Job? = null

    init {
        snapshotFlow { textFieldState.text.toString() }
            .onEach { content ->
                _documentState.update { it.copy(isSaved = (content == lastSavedContent)) }
            }
            .launchIn(viewModelScope)

        snapshotFlow { textFieldState.text.toString() }
            .drop(1)
            .onEach {
                if (_isFocusMode.value && !_documentState.value.isLoading) {
                    _isChromeHidden.value = true
                    resetInactivityTimer()
                }
            }
            .launchIn(viewModelScope)


        userPreferences
            .map { it.defaultExtension }
            .distinctUntilChanged()
            .onEach { ext ->
                _documentState.update { state ->
                    if (state.uri == null && (state.displayName == "Untitled.txt" || state.displayName == "Untitled.md")) {
                        state.copy(displayName = "Untitled.$ext")
                    } else {
                        state
                    }
                }
            }
            .launchIn(viewModelScope)
    }

    fun loadFile(uri: Uri, forceReload: Boolean = false) {
        val currentState = _documentState.value
        if (!forceReload && currentState.uri == uri && !currentState.isLoading) {
            return
        }
        _isFocusMode.value = false
        _isChromeHidden.value = false
        inactivityJob?.cancel()

        _documentState.update { it.copy(isLoading = true) }
        viewModelScope.launch {
            val displayName = withContext(Dispatchers.IO) {
                fileRepository.getDisplayName(uri)
            }
            _documentState.update { it.copy(uri = uri, displayName = displayName) }

            fileRepository.readFile(uri)
                .onSuccess { (content, hadBom) ->
                    fileRepository.persistPermission(uri)
                    val lastModified = fileRepository.getLastModified(uri)

                    recentsRepository.recordOpen(uri, displayName, lastModified)

                    val entry = recentsRepository.getEntry(uri)
                    val initialPos = entry?.cursorPosition?.coerceIn(0, content.length) ?: 0
                    val initialOffset = entry?.scrollOffset ?: 0

                    withContext(Dispatchers.Main) {
                        textFieldState = TextFieldState(
                            initialText = content,
                            initialSelection = androidx.compose.ui.text.TextRange(initialPos)
                        )
                        _restoredScrollOffset.emit(initialOffset)
                    }

                    lastSavedContent = content

                    _documentState.update {
                        DocumentState(
                            uri = uri,
                            displayName = displayName,
                            isSaved = true,
                            isReadOnly = !fileRepository.hasWritePermission(uri),
                            isLoading = false,
                            hadBom = hadBom
                        )
                    }
                }
                .onFailure { exception ->
                    _documentState.update { it.copy(isLoading = false) }
                    if (exception !is FileTooLargeException) {
                        recentsRepository.markUnavailable(uri)
                    }
                    val title = if (exception is FileTooLargeException) "File too large" else "Cannot open file"
                    val message = when {
                        exception is FileTooLargeException -> "This file is over 5MB. Skriv is designed for text files."
                        exception is java.nio.charset.CharacterCodingException -> "This file does not appear to be a UTF-8 text file."
                        uri.scheme == "file" -> "Skriv cannot open direct file paths due to Android security restrictions. Please use the folder icon inside Skriv."
                        else -> exception.localizedMessage ?: "Unknown error"
                    }
                    _events.emit(UiEvent.ErrorDialog(title, message))
                }
        }
    }

    fun getDisplayName(uri: Uri): String {
        return fileRepository.getDisplayName(uri)
    }

    fun deleteFile(uri: Uri) {
        viewModelScope.launch(Dispatchers.IO) {
            fileRepository.deleteFile(uri)
        }
    }

    fun onCreateDocumentResult(uri: Uri) {
        fileRepository.persistPermission(uri)
        val content = textFieldState.text.toString()
        val hadBom = _documentState.value.hadBom
        _documentState.update { it.copy(isLoading = true) }
        viewModelScope.launch {
            fileRepository.writeFile(uri, content, hadBom)
                .onSuccess {
                    val displayName = fileRepository.getDisplayName(uri)
                    val lastModified = fileRepository.getLastModified(uri)
                    recentsRepository.recordOpen(uri, displayName, lastModified)
                    lastSavedContent = content
                    _documentState.update {
                        DocumentState(
                            uri = uri,
                            displayName = displayName,
                            isSaved = true,
                            isReadOnly = !fileRepository.hasWritePermission(uri),
                            isLoading = false,
                            hadBom = hadBom
                        )
                    }
                    _events.emit(UiEvent.Snackbar("Saved"))

                    val pending = _savedPendingAction
                    _savedPendingAction = null
                    if (pending != null) {
                        executePendingAction(pending)
                    }
                }
                .onFailure { exception ->
                    _documentState.update { it.copy(isLoading = false) }
                    _events.emit(UiEvent.Snackbar("Could not save: ${exception.localizedMessage}"))
                    _savedPendingAction = null
                }
        }
    }

    fun undo() {
        if (canUndo.value) {
            textFieldState.undoState.undo()
        }
    }

    fun redo() {
        if (canRedo.value) {
            textFieldState.undoState.redo()
        }
    }

    fun save() {
        val state = _documentState.value
        val uri = state.uri
        val content = textFieldState.text.toString()
        if (uri == null) {
            viewModelScope.launch {
                val extension = userPreferences.value.defaultExtension
                _createDocumentRequest.emit("Untitled.$extension")
            }
        } else {
            if (!fileRepository.hasWritePermission(uri)) {
                viewModelScope.launch {
                    _events.emit(UiEvent.Snackbar("File opened read-only. Use Save As to save a copy."))
                }
                return
            }
            _documentState.update { it.copy(isLoading = true) }
            viewModelScope.launch {
                fileRepository.writeFile(uri, content, state.hadBom)
                    .onSuccess {
                        lastSavedContent = content
                        _documentState.update { it.copy(isSaved = true, isLoading = false) }
                        _events.emit(UiEvent.Snackbar("Saved"))
                    }
                    .onFailure { exception ->
                        _documentState.update { it.copy(isLoading = false) }
                        _events.emit(UiEvent.Snackbar("Could not save: ${exception.localizedMessage}", "Retry"))
                    }
            }
        }
    }

    fun requestSaveAs() {
        viewModelScope.launch {
            val displayName = _documentState.value.displayName
            val defaultExt = userPreferences.value.defaultExtension
            val isReadOnly = _documentState.value.isReadOnly
            val nameWithoutExt = when {
                displayName.endsWith(".txt", ignoreCase = true) -> displayName.substring(0, displayName.length - 4)
                displayName.endsWith(".md", ignoreCase = true) -> displayName.substring(0, displayName.length - 3)
                else -> displayName
            }
            val suggestedName = if (isReadOnly) {
                "${nameWithoutExt}_copy.$defaultExt"
            } else {
                "$nameWithoutExt.$defaultExt"
            }
            _createDocumentRequest.emit(suggestedName)
        }
    }

    fun onSaveAsResult(uri: Uri) {
        fileRepository.persistPermission(uri)
        val content = textFieldState.text.toString()
        val hadBom = _documentState.value.hadBom
        _documentState.update { it.copy(isLoading = true) }
        viewModelScope.launch {
            fileRepository.writeFile(uri, content, hadBom)
                .onSuccess {
                    val displayName = fileRepository.getDisplayName(uri)
                    val lastModified = fileRepository.getLastModified(uri)
                    recentsRepository.recordOpen(uri, displayName, lastModified)
                    lastSavedContent = content
                    _documentState.update {
                        DocumentState(
                            uri = uri,
                            displayName = displayName,
                            isSaved = true,
                            isReadOnly = !fileRepository.hasWritePermission(uri),
                            isLoading = false,
                            hadBom = hadBom
                        )
                    }
                    _events.emit(UiEvent.Snackbar("Saved as $displayName"))
                }
                .onFailure { exception ->
                    _documentState.update { it.copy(isLoading = false) }
                    _events.emit(UiEvent.Snackbar("Could not save: ${exception.localizedMessage}"))
                }
        }
    }

    fun requestNew() {
        val state = _documentState.value
        if (state.isSaved) {
            createNewDocument()
        } else {
            _pendingAction.value = PendingAction.NEW
        }
    }

    fun requestBack() {
        val state = _documentState.value
        if (state.isSaved) {
            viewModelScope.launch {
                _events.emit(UiEvent.NavigateBack)
            }
        } else {
            _pendingAction.value = PendingAction.BACK
        }
    }

    fun requestOpen(uri: Uri) {
        val state = _documentState.value
        if (state.isSaved) {
            loadFile(uri)
        } else {
            _pendingOpenUri = uri
            _pendingAction.value = PendingAction.OPEN
        }
    }

    fun confirmPendingAction(save: Boolean) {
        val action = _pendingAction.value ?: return
        _pendingAction.value = null
        if (save) {
            val state = _documentState.value
            if (state.uri != null) {
                val content = textFieldState.text.toString()
                if (!fileRepository.hasWritePermission(state.uri)) {
                    _savedPendingAction = action
                    requestSaveAs()
                } else {
                    _documentState.update { it.copy(isLoading = true) }
                    viewModelScope.launch {
                        fileRepository.writeFile(state.uri, content, state.hadBom)
                            .onSuccess {
                                lastSavedContent = content
                                _documentState.update { it.copy(isSaved = true, isLoading = false) }
                                executePendingAction(action)
                            }
                            .onFailure { exception ->
                                _documentState.update { it.copy(isLoading = false) }
                                _events.emit(UiEvent.Snackbar("Could not save: ${exception.localizedMessage}"))
                            }
                    }
                }
            } else {
                _savedPendingAction = action
                save()
            }
        } else {
            executePendingAction(action)
        }
    }

    private fun executePendingAction(action: PendingAction) {
        when (action) {
            PendingAction.BACK -> {
                viewModelScope.launch {
                    _events.emit(UiEvent.NavigateBack)
                }
            }
            PendingAction.NEW -> {
                createNewDocument()
            }
            PendingAction.OPEN -> {
                val uri = _pendingOpenUri
                _pendingOpenUri = null
                if (uri != null) {
                    loadFile(uri)
                }
            }
        }
    }

    fun cancelPendingAction() {
        _pendingAction.value = null
        _pendingOpenUri = null
        _savedPendingAction = null
    }

    fun onBackground() {
        val state = _documentState.value
        val uri = state.uri
        val content = textFieldState.text.toString()
        val autoSaveEnabled = userPreferences.value.autoSaveOnBackground
        if (autoSaveEnabled && uri != null && !state.isReadOnly && !state.isSaved) {
            viewModelScope.launch {
                fileRepository.writeFile(uri, content, state.hadBom)
                    .onSuccess {
                        lastSavedContent = content
                        _documentState.update { it.copy(isSaved = true) }
                    }
                    .onFailure {
                        // Silent auto-save on background as per specs, do not show snackbar.
                    }
            }
        }
    }

    fun saveScrollState(cursorPos: Int, scrollOffset: Int) {
        val uri = _documentState.value.uri ?: return
        viewModelScope.launch {
            recentsRepository.updateScrollState(uri, cursorPos, scrollOffset)
        }
    }

    fun openFindBar() {
        _findBarState.update { it.copy(isVisible = true) }
        recomputeMatches()
    }

    fun onFindQueryChanged(query: String) {
        _findBarState.update { it.copy(query = query) }
        recomputeMatches()
    }

    fun onReplacementChanged(replacement: String) {
        _findBarState.update { it.copy(replacement = replacement) }
    }

    fun onCaseSensitiveToggled() {
        _findBarState.update { it.copy(caseSensitive = !it.caseSensitive) }
        recomputeMatches()
    }

    fun findNext() {
        val state = _findBarState.value
        if (state.matchRanges.isEmpty()) return
        val nextIndex = (state.currentMatchIndex + 1) % state.matchRanges.size
        _findBarState.update { it.copy(currentMatchIndex = nextIndex) }
    }

    fun findPrevious() {
        val state = _findBarState.value
        if (state.matchRanges.isEmpty()) return
        val prevIndex = (state.currentMatchIndex - 1 + state.matchRanges.size) % state.matchRanges.size
        _findBarState.update { it.copy(currentMatchIndex = prevIndex) }
    }

    fun replace(replacement: String) {
        val state = _findBarState.value
        val range = state.matchRanges.getOrNull(state.currentMatchIndex) ?: return
        textFieldState.edit {
            replace(range.first, range.last + 1, replacement)
        }
        recomputeMatchesImmediate()
    }

    fun replaceAll(replacement: String) {
        val state = _findBarState.value
        if (state.matchRanges.isEmpty()) return
        textFieldState.edit {
            state.matchRanges.sortedByDescending { it.first }.forEach { range ->
                replace(range.first, range.last + 1, replacement)
            }
        }
        recomputeMatchesImmediate()
    }

    fun toggleFocusMode() {
        val active = !_isFocusMode.value
        _isFocusMode.value = active
        if (!active) {
            _isChromeHidden.value = false
            inactivityJob?.cancel()
        } else {
            _isChromeHidden.value = true
            resetInactivityTimer()
        }
    }

    private fun resetInactivityTimer() {
        inactivityJob?.cancel()
        inactivityJob = viewModelScope.launch {
            delay(120_000) // 2 minutes inactivity
            _isChromeHidden.value = false
        }
    }

    fun closeFindBar() {
        _findBarState.update { it.copy(isVisible = false) }
    }

    private fun createNewDocument() {
        lastSavedContent = ""
        textFieldState = TextFieldState()
        val ext = userPreferences.value.defaultExtension
        _documentState.value = DocumentState(
            uri = null,
            displayName = "Untitled.$ext",
            isSaved = true,
            isReadOnly = false,
            isLoading = false,
            hadBom = false
        )
        _isFocusMode.value = false
        _isChromeHidden.value = false
        inactivityJob?.cancel()
    }

    private fun recomputeMatches() {
        recomputeJob?.cancel()
        recomputeJob = viewModelScope.launch {
            delay(150)
            recomputeMatchesImmediate()
        }
    }

    private fun recomputeMatchesImmediate() {
        val text = textFieldState.text.toString()
        val query = _findBarState.value.query
        val caseSensitive = _findBarState.value.caseSensitive
        if (query.isEmpty()) {
            _findBarState.update { it.copy(matchRanges = emptyList(), currentMatchIndex = 0) }
            return
        }
        val ranges = mutableListOf<IntRange>()
        var index = 0
        while (true) {
            val foundIndex = text.indexOf(query, index, ignoreCase = !caseSensitive)
            if (foundIndex == -1) break
            ranges.add(foundIndex until (foundIndex + query.length))
            index = foundIndex + 1
        }
        _findBarState.update { state ->
            val newIndex = if (ranges.isEmpty()) 0 else {
                state.currentMatchIndex.coerceIn(0, ranges.size - 1)
            }
            state.copy(matchRanges = ranges, currentMatchIndex = newIndex)
        }
    }
}

val EditorViewModelFactory = viewModelFactory {
    initializer {
        val context = (this[APPLICATION_KEY] as Application).applicationContext
        val db = SkrivDatabase.getInstance(context)
        EditorViewModel(
            fileRepository = FileRepository(context),
            recentsRepository = RecentsRepository(db.recentFileDao()),
            prefs = UserPreferences(context)
        )
    }
}
