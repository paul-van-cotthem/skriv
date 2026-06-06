package com.skriv.app.ui.editor

import android.content.Context
import android.content.Intent
import android.content.res.Configuration
import android.net.Uri
import androidx.core.net.toUri
import android.os.Bundle
import android.view.HapticFeedbackConstants
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContract
import androidx.activity.result.contract.ActivityResultContracts
import android.provider.DocumentsContract
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.animation.expandHorizontally
import androidx.compose.animation.shrinkHorizontally
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.ui.draw.alpha
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.input.OutputTransformation
import androidx.compose.foundation.text.input.TextFieldLineLimits
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.isSystemInDarkTheme
import com.skriv.app.util.SkrivIcons
import androidx.compose.material3.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.material3.adaptive.currentWindowAdaptiveInfo
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.KeyEventType
import androidx.compose.ui.input.key.key
import androidx.compose.ui.input.key.onPreviewKeyEvent
import androidx.compose.ui.input.key.type
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.TextLayoutResult
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LifecycleEventEffect
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavBackStackEntry
import androidx.navigation.NavHostController
import androidx.navigation.toRoute
import androidx.window.core.layout.WindowSizeClass
import com.skriv.app.model.DocumentState
import com.skriv.app.model.PendingAction
import com.skriv.app.model.UiEvent
import com.skriv.app.navigation.EditorRoute
import com.skriv.app.navigation.SettingsRoute
import kotlinx.coroutines.launch
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.runtime.saveable.rememberSaveable
import kotlinx.coroutines.delay

private fun getCorrectedFileName(displayName: String): String? {
    val regex = Regex("""^(.*)\.(txt|md)\s*\((\d+)\)\s*$""", RegexOption.IGNORE_CASE)
    val matchResult = regex.matchEntire(displayName) ?: return null
    val base = matchResult.groupValues[1]
    val ext = matchResult.groupValues[2]
    val num = matchResult.groupValues[3]
    return "$base ($num).$ext"
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun EditorScreen(
    navController: NavHostController,
    navBackStackEntry: NavBackStackEntry,
    viewModel: EditorViewModel = viewModel(factory = EditorViewModelFactory)
) {
    val context = LocalContext.current
    val view = LocalView.current
    val scope = rememberCoroutineScope()
    var showRevertDialog by remember { mutableStateOf(false) }
    var hasSavedThisSession by remember { mutableStateOf(false) }
    var extensionlessFileUri by remember { mutableStateOf<Uri?>(null) }
    var extensionlessFileName by remember { mutableStateOf("") }
    var showEnableEditingDialog by remember { mutableStateOf(false) }
    var showReadOnlyInfoDialog by remember { mutableStateOf(false) }

    val documentState by viewModel.documentState.collectAsStateWithLifecycle()
    val findBarState by viewModel.findBarState.collectAsStateWithLifecycle()
    val userPrefs by viewModel.userPreferences.collectAsStateWithLifecycle()
    val pendingAction by viewModel.pendingAction.collectAsStateWithLifecycle()

    val canUndo by viewModel.canUndo.collectAsStateWithLifecycle()
    val canRedo by viewModel.canRedo.collectAsStateWithLifecycle()
    val wordCount by viewModel.wordCount.collectAsStateWithLifecycle()
    val isFocusMode by viewModel.isFocusMode.collectAsStateWithLifecycle()
    val isChromeHidden by viewModel.isChromeHidden.collectAsStateWithLifecycle()

    val chromeAlpha by androidx.compose.animation.core.animateFloatAsState(
        targetValue = if (isChromeHidden) 0f else 1f,
        label = "ChromeAlpha"
    )

    val route = navBackStackEntry.toRoute<EditorRoute>()
    val uriString = route.uriString

    var hasLoadedInitial by rememberSaveable { mutableStateOf(false) }
    val focusRequester = remember { FocusRequester() }

    var showShareUnsavedDialog by remember { mutableStateOf(false) }
    var triggerShareAfterSave by remember { mutableStateOf(false) }

    LaunchedEffect(documentState.isSaved) {
        if (documentState.isSaved && triggerShareAfterSave) {
            triggerShareAfterSave = false
            shareDocument(context, documentState.uri, viewModel.textFieldState.text.toString(), documentState.displayName)
        }
    }

    // 1. Initial file loading
    LaunchedEffect(uriString) {
        if (!hasLoadedInitial) {
            hasSavedThisSession = false
            uriString?.let { viewModel.loadFile(it.toUri()) }
            hasLoadedInitial = true
        }
    }

    // Auto-focus when file changes
    LaunchedEffect(documentState.uri) {
        delay(50)
        try {
            focusRequester.requestFocus()
        } catch (e: Exception) {
            // ignore if view is not attached
        }
    }

    // 2. Snackbar and navigation events
    val snackbarHostState = remember { SnackbarHostState() }
    LaunchedEffect(Unit) {
        viewModel.events.collect { event ->
            when (event) {
                is UiEvent.Snackbar -> {
                    val result = snackbarHostState.showSnackbar(event.message, event.actionLabel)
                    if (result == SnackbarResult.ActionPerformed && event.actionLabel == "Retry") {
                        viewModel.save()
                    }
                }
                is UiEvent.ErrorDialog -> {
                    // Show message in toast or handle it with an overlay, then pop back
                    android.widget.Toast.makeText(context, "${event.title}: ${event.message}", android.widget.Toast.LENGTH_LONG).show()
                    navController.popBackStack()
                }
                UiEvent.NavigateBack -> {
                    navController.popBackStack()
                }
            }
        }
    }

    // 3. Document Save/SaveAs Launchers
    var createLauncherPtr: ActivityResultLauncher<String>? = null
    val createLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.CreateDocument("text/*")
    ) { uri ->
        if (uri != null) {
            val displayName = viewModel.getDisplayName(uri)
            val lowerName = displayName.lowercase()
            if (lowerName.endsWith(".txt") || lowerName.endsWith(".md")) {
                hasSavedThisSession = true
                viewModel.onCreateDocumentResult(uri)
            } else {
                val correctedName = getCorrectedFileName(displayName)
                if (correctedName != null) {
                    viewModel.deleteFile(uri)
                    createLauncherPtr?.launch(correctedName)
                } else {
                    extensionlessFileUri = uri
                    extensionlessFileName = displayName
                }
            }
        } else {
            viewModel.cancelPendingAction()
        }
    }
    createLauncherPtr = createLauncher
    LaunchedEffect(Unit) {
        viewModel.createDocumentRequest.collect { suggestedFilename ->
            createLauncher.launch(suggestedFilename)
        }
    }

    val openLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.OpenDocument()
    ) { uri ->
        uri?.let { viewModel.requestOpen(it) }
    }

    val isDark = isSystemInDarkTheme()
    val dialogBgColor = AlertDialogDefaults.containerColor
    val dialogShape = AlertDialogDefaults.shape

    // 4. Unsaved Alert Dialog
    if (pendingAction != null) {
        AlertDialog(
            onDismissRequest = { viewModel.cancelPendingAction() },
            icon = { Icon(SkrivIcons.Warning, contentDescription = null) },
            title = { Text("Save changes?") },
            text = { Text("You have unsaved changes in your document.") },
            shape = dialogShape,
            containerColor = dialogBgColor,
            confirmButton = {
                TextButton(onClick = {
                    hasSavedThisSession = true
                    viewModel.confirmPendingAction(save = true)
                }) {
                    Text("Save")
                }
            },
            dismissButton = {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    TextButton(onClick = { viewModel.confirmPendingAction(save = false) }) {
                        Text("Discard")
                    }
                    TextButton(onClick = { viewModel.cancelPendingAction() }) {
                        Text("Cancel")
                    }
                }
            }
        )
    }

    // Extensionless File Warning Dialog
    if (extensionlessFileUri != null) {
        AlertDialog(
            onDismissRequest = {
                extensionlessFileUri?.let { viewModel.deleteFile(it) }
                extensionlessFileUri = null
                extensionlessFileName = ""
                viewModel.cancelPendingAction()
            },
            icon = { Icon(SkrivIcons.Warning, contentDescription = null) },
            title = { Text("Save without extension?") },
            text = { Text("You named the file '$extensionlessFileName' without a .txt or .md extension. Without a text extension, other apps and Android might not recognize this file.") },
            shape = dialogShape,
            containerColor = dialogBgColor,
            confirmButton = {
                TextButton(onClick = {
                    val uri = extensionlessFileUri
                    if (uri != null) {
                        viewModel.deleteFile(uri)
                        val extension = userPrefs.defaultExtension
                        val correctedName = "$extensionlessFileName.$extension"
                        extensionlessFileUri = null
                        extensionlessFileName = ""
                        createLauncher.launch(correctedName)
                    }
                }) {
                    Text("Save again")
                }
            },
            dismissButton = {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    TextButton(onClick = {
                        val uri = extensionlessFileUri
                        if (uri != null) {
                            hasSavedThisSession = true
                            viewModel.onCreateDocumentResult(uri)
                        }
                        extensionlessFileUri = null
                        extensionlessFileName = ""
                    }) {
                        Text("Keep as is")
                    }
                    TextButton(onClick = {
                        extensionlessFileUri?.let { viewModel.deleteFile(it) }
                        extensionlessFileUri = null
                        extensionlessFileName = ""
                        viewModel.cancelPendingAction()
                    }) {
                        Text("Cancel")
                    }
                }
            }
        )
    }

    // Enable Editing Dialog
    if (showEnableEditingDialog) {
        AlertDialog(
            onDismissRequest = { showEnableEditingDialog = false },
            icon = { Icon(SkrivIcons.Warning, contentDescription = null) },
            title = { Text("Save a copy to edit?") },
            text = { Text("This file is opened in read-only mode.\n\nTo start editing, please save a copy of this file to your device or cloud storage.") },
            shape = dialogShape,
            containerColor = dialogBgColor,
            confirmButton = {
                TextButton(onClick = {
                    showEnableEditingDialog = false
                    viewModel.requestSaveAs()
                }) {
                    Text("Save copy")
                }
            },
            dismissButton = {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    TextButton(onClick = { showReadOnlyInfoDialog = true }) {
                        Text("Why?")
                    }
                    TextButton(onClick = { showEnableEditingDialog = false }) {
                        Text("Cancel")
                    }
                }
            }
        )
    }

    // Read-Only Explanation Dialog
    if (showReadOnlyInfoDialog) {
        AlertDialog(
            onDismissRequest = { showReadOnlyInfoDialog = false },
            title = { Text("Why is this read-only?") },
            text = { Text("Android's security rules only allow Skriv temporary access when opening files from some apps (like downloads, mail clients, or certain cloud folders).\n\nWhen this temporary access expires, or if the system doesn't grant edit access, the file opens as read-only. Saving a copy solves this by creating a fresh, writable file in your storage.") },
            shape = dialogShape,
            containerColor = dialogBgColor,
            confirmButton = {
                TextButton(onClick = { showReadOnlyInfoDialog = false }) {
                    Text("Got it")
                }
            }
        )
    }

    // Revert to Saved Confirmation Dialog
    if (showRevertDialog) {
        AlertDialog(
            onDismissRequest = { showRevertDialog = false },
            icon = { Icon(SkrivIcons.Warning, contentDescription = null, tint = MaterialTheme.colorScheme.error) },
            title = { Text("Revert to saved?") },
            text = { Text("All unsaved changes made in this session will be permanently discarded.") },
            shape = dialogShape,
            containerColor = dialogBgColor,
            confirmButton = {
                TextButton(
                    onClick = {
                        showRevertDialog = false
                        documentState.uri?.let { viewModel.loadFile(it, forceReload = true) }
                    },
                    colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.error)
                ) {
                    Text("Revert")
                }
            },
            dismissButton = {
                TextButton(onClick = { showRevertDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }

    if (showShareUnsavedDialog) {
        AlertDialog(
            onDismissRequest = { showShareUnsavedDialog = false },
            icon = { Icon(SkrivIcons.Warning, contentDescription = null) },
            title = { Text(if (documentState.uri == null) "Save required" else "Save changes?") },
            text = {
                Text(
                    if (documentState.uri == null) {
                        "Please save the document first to share it as a file."
                    } else {
                        "Please save your changes before sharing this document."
                    }
                )
            },
            shape = dialogShape,
            containerColor = dialogBgColor,
            confirmButton = {
                TextButton(
                    onClick = {
                        showShareUnsavedDialog = false
                        if (documentState.uri != null) {
                            triggerShareAfterSave = true
                        }
                        viewModel.save()
                    }
                ) {
                    Text(if (documentState.uri == null) "Save" else "Save & Share")
                }
            },
            dismissButton = {
                TextButton(onClick = { showShareUnsavedDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }

    // 5. Back gesture handling
    BackHandler(enabled = !documentState.isSaved) {
        viewModel.requestBack()
    }

    // 6. Scroll synchronisation and margins
    val scrollState = rememberScrollState()
    val horizontalScrollState = if (!userPrefs.wordWrap) rememberScrollState() else null
    var textLayoutResult by remember { mutableStateOf<TextLayoutResult?>(null) }

    // Restore scroll offset when a file is loaded
    LaunchedEffect(viewModel.restoredScrollOffset) {
        viewModel.restoredScrollOffset.collect { offset ->
            scrollState.scrollTo(offset)
        }
    }

    // Scroll to current search match
    LaunchedEffect(findBarState.currentMatchIndex, findBarState.matchRanges, textLayoutResult) {
        val matchRanges = findBarState.matchRanges
        val currentIndex = findBarState.currentMatchIndex
        val layout = textLayoutResult
        if (layout != null && matchRanges.isNotEmpty() && currentIndex in matchRanges.indices) {
            val range = matchRanges[currentIndex]
            val line = layout.getLineForOffset(range.first)
            val lineTop = layout.getLineTop(line).toInt()
            val lineBottom = layout.getLineBottom(line).toInt()
            val currentScroll = scrollState.value
            val viewportHeight = scrollState.viewportSize
            if (viewportHeight > 0) {
                if (lineTop < currentScroll || lineBottom > currentScroll + viewportHeight) {
                    val targetScroll = lineTop - (viewportHeight / 2) + ((lineBottom - lineTop) / 2)
                    scrollState.animateScrollTo(targetScroll.coerceIn(0, scrollState.maxValue))
                }
            }
        }
    }

    // 7. Reading Margin calculations
    val windowSizeClass = currentWindowAdaptiveInfo().windowSizeClass
    val configuration = LocalConfiguration.current
    val isLandscape = configuration.orientation == Configuration.ORIENTATION_LANDSCAPE

    val sizeClassMargin = when {
        windowSizeClass.isWidthAtLeastBreakpoint(WindowSizeClass.WIDTH_DP_EXPANDED_LOWER_BOUND) -> maxOf(userPrefs.readingMarginDp, 48)
        windowSizeClass.isWidthAtLeastBreakpoint(WindowSizeClass.WIDTH_DP_MEDIUM_LOWER_BOUND) -> maxOf(userPrefs.readingMarginDp, 32)
        else -> userPrefs.readingMarginDp
    }
    val effectiveMarginDp = if (isLandscape) maxOf(sizeClassMargin, 48) else sizeClassMargin


    // 9. Lifecycle hooks for backgrounding
    LifecycleEventEffect(Lifecycle.Event.ON_PAUSE) {
        viewModel.onBackground()
        viewModel.saveScrollState(
            cursorPos = viewModel.textFieldState.selection.start,
            scrollOffset = scrollState.value
        )
    }

    val transformation = remember(findBarState.matchRanges, findBarState.currentMatchIndex, findBarState.isVisible, isDark) {
        if (!findBarState.isVisible || findBarState.matchRanges.isEmpty()) null
        else OutputTransformation {
            findBarState.matchRanges.forEachIndexed { i, range ->
                val color = if (i == findBarState.currentMatchIndex) {
                    if (isDark) Color(0xFFFFB74D) else Color(0xFFFF9800)
                } else {
                    if (isDark) Color(0x4D00E5FF) else Color(0x9980DEEA)
                }
                addStyle(SpanStyle(background = color), range.first, range.last + 1)
            }
        }
    }

    val fontFamily = if (userPrefs.fontMonospace)
        FontFamily.Monospace
    else
        FontFamily.SansSerif
    val lineHeightMultiplier = when (userPrefs.lineSpacing) {
        "compact" -> 1.1f
        "relaxed" -> 1.6f
        "double" -> 2.0f
        else -> 1.3f
    }
    val lineHeight = (userPrefs.fontSizeSp * lineHeightMultiplier).sp

    val editorBarBg = MaterialTheme.colorScheme.surface

    Scaffold(
        snackbarHost = {
            SnackbarHost(hostState = snackbarHostState) { data ->
                Snackbar(snackbarData = data)
            }
        },
        topBar = {
            TopAppBar(
                title = {},
                navigationIcon = {
                    IconButton(
                        onClick = { viewModel.requestBack() },
                        enabled = !isChromeHidden,
                        modifier = Modifier.alpha(chromeAlpha)
                    ) {
                        Icon(
                            imageVector = SkrivIcons.ArrowBack,
                            contentDescription = "Back"
                        )
                    }
                },
                actions = {
                    // Undo/redo buttons
                    IconButton(
                        onClick = { viewModel.undo() },
                        enabled = canUndo && !isChromeHidden,
                        modifier = Modifier.alpha(chromeAlpha)
                    ) {
                        Icon(
                            imageVector = SkrivIcons.Undo,
                            contentDescription = "Undo"
                        )
                    }
                    IconButton(
                        onClick = { viewModel.redo() },
                        enabled = canRedo && !isChromeHidden,
                        modifier = Modifier.alpha(chromeAlpha)
                    ) {
                        Icon(
                            imageVector = SkrivIcons.Redo,
                            contentDescription = "Redo"
                        )
                    }
                    IconButton(
                        onClick = { viewModel.openFindBar() },
                        enabled = !isChromeHidden,
                        modifier = Modifier.alpha(chromeAlpha)
                    ) {
                        Icon(
                            imageVector = SkrivIcons.Search,
                            contentDescription = "Find"
                        )
                    }
                    IconButton(
                        onClick = { viewModel.toggleFocusMode() }
                    ) {
                        Icon(
                            imageVector = SkrivIcons.Focus,
                            contentDescription = if (isFocusMode) "Exit Focus Mode" else "Enter Focus Mode"
                        )
                    }

                    // Overflow Menu
                    var showMenu by remember { mutableStateOf(false) }
                    var pendingShowMenu by remember { mutableStateOf(false) }
                    val focusManager = LocalFocusManager.current
                    val isImeVisible = WindowInsets.isImeVisible

                    LaunchedEffect(isImeVisible) {
                        if (!isImeVisible && pendingShowMenu) {
                            pendingShowMenu = false
                            showMenu = true
                        }
                    }

                    Box {
                        IconButton(
                            onClick = {
                                if (isImeVisible) {
                                    focusManager.clearFocus()
                                    pendingShowMenu = true
                                } else {
                                    showMenu = true
                                }
                            },
                            enabled = !isChromeHidden,
                            modifier = Modifier.alpha(chromeAlpha)
                        ) {
                            Icon(SkrivIcons.MoreVert, contentDescription = "Menu")
                        }
                        DropdownMenu(
                            expanded = showMenu && !isChromeHidden,
                            onDismissRequest = { showMenu = false }
                        ) {
                            DropdownMenuItem(
                                text = { Text("New") },
                                leadingIcon = { Icon(SkrivIcons.Add, contentDescription = null) },
                                onClick = {
                                    showMenu = false
                                    hasSavedThisSession = false
                                    viewModel.requestNew()
                                }
                            )
                            DropdownMenuItem(
                                text = { Text("Open") },
                                leadingIcon = { Icon(SkrivIcons.FolderOpen, contentDescription = null) },
                                onClick = {
                                    showMenu = false
                                    openLauncher.launch(arrayOf("text/plain", "text/markdown", "application/octet-stream"))
                                }
                            )
                            DropdownMenuItem(
                                text = { Text("Save") },
                                leadingIcon = { Icon(SkrivIcons.Check, contentDescription = null) },
                                enabled = !documentState.isSaved && !documentState.isReadOnly,
                                onClick = {
                                    showMenu = false
                                    hasSavedThisSession = true
                                    viewModel.save()
                                }
                            )
                            if (documentState.uri != null) {
                                DropdownMenuItem(
                                    text = { Text("Revert to saved") },
                                    leadingIcon = { Icon(SkrivIcons.Undo, contentDescription = null) },
                                    enabled = !documentState.isSaved,
                                    onClick = {
                                        showMenu = false
                                        showRevertDialog = true
                                    }
                                )
                            }
                            DropdownMenuItem(
                                text = { Text("Save as") },
                                leadingIcon = { Icon(SkrivIcons.Document, contentDescription = null) },
                                onClick = {
                                    showMenu = false
                                    viewModel.requestSaveAs()
                                }
                            )

                            DropdownMenuItem(
                                text = { Text("Share") },
                                leadingIcon = { Icon(SkrivIcons.Share, contentDescription = null) },
                                onClick = {
                                    showMenu = false
                                    if (documentState.uri == null || !documentState.isSaved) {
                                        showShareUnsavedDialog = true
                                    } else {
                                        shareDocument(context, documentState.uri, viewModel.textFieldState.text.toString(), documentState.displayName)
                                    }
                                }
                            )
                            DropdownMenuItem(
                                text = { Text("Print") },
                                leadingIcon = { Icon(SkrivIcons.Print, contentDescription = null) },
                                onClick = {
                                    showMenu = false
                                    printDocument(context, viewModel.textFieldState.text.toString(), documentState.displayName)
                                }
                            )
                            DropdownMenuItem(
                                text = { Text("Settings") },
                                leadingIcon = { Icon(SkrivIcons.Settings, contentDescription = null) },
                                onClick = {
                                    showMenu = false
                                    navController.navigate(SettingsRoute)
                                }
                            )
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface.copy(alpha = chromeAlpha)
                )
            )
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .imePadding()
            ) {
                // Header (filename and save status)
                AnimatedVisibility(
                    visible = !isChromeHidden,
                    enter = expandVertically() + fadeIn(),
                    exit = shrinkVertically() + fadeOut()
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f))
                            .padding(
                                horizontal = 16.dp,
                                vertical = 8.dp
                            )
                    ) {
                        Text(
                            text = documentState.displayName,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f)
                        )
                        val statusText = when {
                            !documentState.isSaved -> "Unsaved changes"
                            hasSavedThisSession && documentState.uri != null -> "Saved"
                            else -> ""
                        }
                        if (statusText.isNotEmpty()) {
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = statusText,
                                style = MaterialTheme.typography.labelSmall,
                                color = if (documentState.isSaved) {
                                    MaterialTheme.colorScheme.onSurfaceVariant
                                } else {
                                    MaterialTheme.colorScheme.error
                                }
                            )
                        }
                    }
                }

                // Read-Only Banner
                AnimatedVisibility(
                    visible = documentState.isReadOnly && !isChromeHidden,
                    enter = expandVertically() + fadeIn(),
                    exit = shrinkVertically() + fadeOut()
                ) {
                    Surface(
                        color = MaterialTheme.colorScheme.errorContainer,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp, vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                modifier = Modifier.weight(1f)
                            ) {
                                Icon(
                                    imageVector = SkrivIcons.Warning,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.onErrorContainer
                                )
                                Text(
                                    text = "Read-only mode",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onErrorContainer
                                )
                            }
                            OutlinedButton(
                                onClick = {
                                    showEnableEditingDialog = true
                                },
                                colors = ButtonDefaults.outlinedButtonColors(
                                    contentColor = MaterialTheme.colorScheme.onErrorContainer
                                ),
                                border = BorderStroke(1.dp, MaterialTheme.colorScheme.onErrorContainer)
                            ) {
                                Text("Enable editing")
                            }
                        }
                    }
                }

                // Editor layout row
                Row(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                ) {
                    AnimatedVisibility(
                        visible = userPrefs.lineNumbers && !isChromeHidden,
                        enter = expandHorizontally() + fadeIn(),
                        exit = shrinkHorizontally() + fadeOut()
                    ) {
                        LineNumbersGutter(
                            scrollState = scrollState,
                            text = viewModel.textFieldState.text.toString(),
                            textLayoutResult = textLayoutResult,
                            fontFamily = fontFamily,
                            fontSize = userPrefs.fontSizeSp.sp,
                            lineHeight = lineHeight,
                            modifier = Modifier.padding(start = 16.dp)
                        )
                    }

                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxHeight()
                            .then(
                                if (horizontalScrollState != null) Modifier.horizontalScroll(horizontalScrollState)
                                else Modifier
                            )
                    ) {
                        val editorStartPadding = effectiveMarginDp.dp
                        BasicTextField(
                            state = viewModel.textFieldState,
                            scrollState = scrollState,
                            readOnly = documentState.isReadOnly,
                            lineLimits = TextFieldLineLimits.MultiLine(),
                            outputTransformation = transformation,
                            keyboardOptions = KeyboardOptions(
                                capitalization = KeyboardCapitalization.Sentences,
                                autoCorrectEnabled = true
                            ),
                            onTextLayout = { getResult ->
                                textLayoutResult = getResult()
                            },
                            textStyle = TextStyle(
                                fontFamily = fontFamily,
                                fontSize = userPrefs.fontSizeSp.sp,
                                lineHeight = lineHeight,
                                color = MaterialTheme.colorScheme.onSurface
                            ),
                            cursorBrush = SolidColor(MaterialTheme.colorScheme.primary),
                            modifier = if (userPrefs.wordWrap) {
                                Modifier
                                    .focusRequester(focusRequester)
                                    .fillMaxWidth()
                                    .fillMaxHeight()
                                    .padding(start = editorStartPadding, end = effectiveMarginDp.dp, top = 8.dp, bottom = 8.dp)
                            } else {
                                Modifier
                                    .focusRequester(focusRequester)
                                    .wrapContentWidth()
                                    .fillMaxHeight()
                                    .padding(start = editorStartPadding, end = effectiveMarginDp.dp, top = 8.dp, bottom = 8.dp)
                            }.onPreviewKeyEvent { keyEvent ->
                                if (keyEvent.key == Key.Enter || keyEvent.key == Key.NumPadEnter) {
                                    val defaultIme = android.provider.Settings.Secure.getString(
                                        context.contentResolver,
                                        android.provider.Settings.Secure.DEFAULT_INPUT_METHOD
                                    ) ?: ""
                                    val isSwiftKey = defaultIme.contains("swiftkey", ignoreCase = true)
                                    if (isSwiftKey) {
                                        // Workaround: SwiftKey's autospace dispatches KeyEvents for Enter (KEYCODE_ENTER)
                                        // inside a nested batch edit transaction, which drops or overrides manual text changes.
                                        // We intercept Enter keys, consume both KeyDown and KeyUp to prevent mismatched
                                        // processing, and post the edit asynchronously to escape the keyboard's batch edit window.
                                        if (keyEvent.type == KeyEventType.KeyDown) {
                                            view.post {
                                                viewModel.textFieldState.edit {
                                                    // Commit the active composition to prevent the IME from overriding our inserted newline.
                                                    try {
                                                        val committer = this::class.java.methods.firstOrNull { it.name.startsWith("commitComposition") }
                                                        committer?.isAccessible = true
                                                        committer?.invoke(this)
                                                    } catch (e: Exception) {
                                                        android.util.Log.w("EditorScreen", "Could not commit composition via reflection", e)
                                                    }
                                                    val start = selection.min
                                                    replace(start, selection.max, "\n")
                                                    selection = TextRange(start + 1)
                                                }
                                            }
                                        }
                                        true
                                    } else {
                                        false
                                    }
                                } else {
                                    false
                                }
                            },
                            decorator = { innerTextField ->
                                innerTextField()
                            }
                        )
                    }
                }

                // Debounced Word/Character count
                AnimatedVisibility(
                    visible = userPrefs.wordCountVisible && !isChromeHidden,
                    enter = expandVertically() + fadeIn(),
                    exit = shrinkVertically() + fadeOut()
                ) {
                    Surface(
                        tonalElevation = 1.dp,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp, vertical = 8.dp),
                            contentAlignment = Alignment.CenterEnd
                        ) {
                            Text(
                                text = "$wordCount words  |  ${viewModel.textFieldState.text.length} characters",
                                style = MaterialTheme.typography.bodySmall
                            )
                        }
                    }
                }

                // Find/Replace bar
                AnimatedVisibility(
                    visible = findBarState.isVisible,
                    enter = slideInVertically { it },
                    exit = slideOutVertically { it }
                ) {
                    FindBar(
                        state = findBarState,
                        onQueryChanged = { viewModel.onFindQueryChanged(it) },
                        onReplacementChanged = { viewModel.onReplacementChanged(it) },
                        onCaseSensitiveToggled = { viewModel.onCaseSensitiveToggled() },
                        onFindNext = { viewModel.findNext() },
                        onFindPrevious = { viewModel.findPrevious() },
                        onReplace = { viewModel.replace(it) },
                        onReplaceAll = { viewModel.replaceAll(it) },
                        onClose = { viewModel.closeFindBar() }
                    )
                }
            }

            }
    }
}

@Composable
fun LineNumbersGutter(
    scrollState: ScrollState,
    text: String,
    textLayoutResult: TextLayoutResult?,
    fontFamily: FontFamily,
    fontSize: androidx.compose.ui.unit.TextUnit,
    lineHeight: androidx.compose.ui.unit.TextUnit,
    modifier: Modifier = Modifier
) {
    val textMeasurer = rememberTextMeasurer()
    val lineNumbers = remember(textLayoutResult, text) {
        if (textLayoutResult == null) return@remember emptyList<Int>()
        val list = ArrayList<Int>(textLayoutResult.lineCount)
        var currentLogicalLine = 1
        for (i in 0 until textLayoutResult.lineCount) {
            val startOffset = textLayoutResult.getLineStart(i)
            val isStartOfLogical = if (i == 0) {
                true
            } else {
                text.getOrNull(startOffset - 1) == '\n'
            }
            if (isStartOfLogical) {
                list.add(currentLogicalLine)
                currentLogicalLine++
            } else {
                list.add(-1)
            }
        }
        list
    }

    val style = TextStyle(
        fontFamily = fontFamily,
        fontSize = fontSize,
        lineHeight = lineHeight,
        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
    )

    val longestLineNumber = lineNumbers.lastOrNull { it != -1 } ?: 1
    val numDigits = longestLineNumber.toString().length.coerceAtMost(5)
    val sampleText = "9".repeat(numDigits)
    val measuredWidth = remember(sampleText, style) {
        textMeasurer.measure(sampleText, style).size.width
    }
    val density = androidx.compose.ui.platform.LocalDensity.current
    val gutterWidth = remember(measuredWidth, density) {
        with(density) { (measuredWidth + 16.dp.toPx()).toDp() }
    }

    val isDark = isSystemInDarkTheme()
    val dividerColor = MaterialTheme.colorScheme.outlineVariant
    Box(
        modifier = modifier
            .fillMaxHeight()
            .width(gutterWidth)
            .padding(vertical = 8.dp)
            .drawBehind {
                if (textLayoutResult == null) return@drawBehind
                val scrollVal = scrollState.value
                val startLine = textLayoutResult.getLineForVerticalPosition(scrollVal.toFloat())
                val endLine = textLayoutResult.getLineForVerticalPosition(scrollVal + size.height)
                    .coerceAtMost(textLayoutResult.lineCount - 1)

                // Draw thin vertical divider on the right boundary
                drawLine(
                    color = dividerColor,
                    start = androidx.compose.ui.geometry.Offset(size.width, 0f),
                    end = androidx.compose.ui.geometry.Offset(size.width, size.height),
                    strokeWidth = 1.dp.toPx()
                )

                for (i in startLine..endLine) {
                    val logicalLine = lineNumbers.getOrNull(i) ?: -1
                    if (logicalLine != -1) {
                        val lineTop = textLayoutResult.getLineTop(i)
                        val textLayout = textMeasurer.measure(
                            text = logicalLine.toString(),
                            style = style
                        )
                        // Right-aligned against the separator line with 8.dp padding
                        val x = size.width - textLayout.size.width - 8.dp.toPx()
                        val y = lineTop - scrollVal
                        drawText(textLayout, topLeft = androidx.compose.ui.geometry.Offset(x, y))
                    }
                }
            }
    )
}

fun shareDocument(context: Context, uri: Uri?, content: String, displayName: String) {
    if (uri != null) {
        val mimeType = context.contentResolver.getType(uri) ?: when {
            displayName.endsWith(".md", ignoreCase = true) -> "text/markdown"
            else -> "text/plain"
        }
        val intent = Intent(Intent.ACTION_SEND).apply {
            type = mimeType
            putExtra(Intent.EXTRA_STREAM, uri)
            putExtra(Intent.EXTRA_SUBJECT, displayName)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        context.startActivity(Intent.createChooser(intent, "Share file"))
    } else {
        val intent = Intent(Intent.ACTION_SEND).apply {
            type = "text/plain"
            putExtra(Intent.EXTRA_TEXT, content)
            putExtra(Intent.EXTRA_SUBJECT, displayName)
        }
        context.startActivity(Intent.createChooser(intent, "Share text"))
    }
}

fun printDocument(context: Context, content: String, displayName: String) {
    val printManager = context.getSystemService(Context.PRINT_SERVICE) as android.print.PrintManager
    val htmlContent = "<html><body><pre>${android.text.Html.escapeHtml(content)}</pre></body></html>"
    
    val webView = android.webkit.WebView(context)
    var printAdapterWrapper: android.print.PrintDocumentAdapter? = null

    webView.loadDataWithBaseURL(null, htmlContent, "text/html", "UTF-8", null)
    webView.webViewClient = object : android.webkit.WebViewClient() {
        override fun onPageFinished(view: android.webkit.WebView, url: String) {
            val printAdapter = webView.createPrintDocumentAdapter(displayName)
            printAdapterWrapper = object : android.print.PrintDocumentAdapter() {
                private val keptWebView = webView

                override fun onStart() = printAdapter.onStart()
                override fun onLayout(
                    oldAttributes: android.print.PrintAttributes?,
                    newAttributes: android.print.PrintAttributes?,
                    cancellationSignal: android.os.CancellationSignal?,
                    callback: LayoutResultCallback?,
                    extras: Bundle?
                ) = printAdapter.onLayout(oldAttributes, newAttributes, cancellationSignal, callback, extras)

                override fun onWrite(
                    pages: Array<out android.print.PageRange>?,
                    destination: android.os.ParcelFileDescriptor?,
                    cancellationSignal: android.os.CancellationSignal?,
                    callback: WriteResultCallback?
                ) = printAdapter.onWrite(pages, destination, cancellationSignal, callback)

                override fun onFinish() {
                    printAdapter.onFinish()
                    printAdapterWrapper = null
                }
            }
            printManager.print(displayName, printAdapterWrapper!!, android.print.PrintAttributes.Builder().build())
        }
    }
}

class OpenDocumentWithInitialUri : ActivityResultContract<Pair<Array<String>, Uri?>, Uri?>() {
    override fun createIntent(context: Context, input: Pair<Array<String>, Uri?>): Intent {
        val intent = Intent(Intent.ACTION_OPEN_DOCUMENT)
            .putExtra(Intent.EXTRA_MIME_TYPES, input.first)
            .setType("*/*")
        input.second?.let { initialUri ->
            intent.putExtra(DocumentsContract.EXTRA_INITIAL_URI, initialUri)
        }
        return intent
    }

    override fun parseResult(resultCode: Int, intent: Intent?): Uri? {
        return if (intent == null || resultCode != android.app.Activity.RESULT_OK) null else intent.data
    }
}
