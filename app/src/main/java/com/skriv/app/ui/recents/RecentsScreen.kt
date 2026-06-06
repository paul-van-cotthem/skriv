package com.skriv.app.ui.recents

import android.text.format.DateUtils
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContract
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.net.toUri
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.provider.DocumentsContract
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import com.skriv.app.util.SkrivIcons
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.material3.*
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.rememberCoroutineScope
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import com.skriv.app.navigation.SettingsRoute
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import com.skriv.app.data.db.RecentFileEntity
import com.skriv.app.navigation.EditorRoute
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RecentsScreen(
    navController: NavHostController,
    viewModel: RecentsViewModel = viewModel(factory = RecentsViewModelFactory)
) {
    val recentFiles by viewModel.recentFiles.collectAsStateWithLifecycle()
    val userPrefs by viewModel.userPrefs.collectAsStateWithLifecycle()
    val scope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }

    var hasAttemptedRedirect by rememberSaveable { mutableStateOf(false) }
    var showClearDialog by remember { mutableStateOf(false) }
    var pendingLocateFile by remember { mutableStateOf<RecentFileEntity?>(null) }
    var locatingFileUriString by remember { mutableStateOf<String?>(null) }
    var checkingFileUri by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(Unit) {
        if (!hasAttemptedRedirect) {
            combine(viewModel.recentFiles, viewModel.userPrefs) { files, prefs ->
                Pair(files, prefs)
            }.first().let { (files, prefs) ->
                hasAttemptedRedirect = true
                if (prefs.openLastFileOnStartup && files.isNotEmpty()) {
                    val lastFile = files.first()
                    if (lastFile.isAvailable) {
                        viewModel.checkFileAvailability(lastFile) { available ->
                            if (available) {
                                navController.navigate(EditorRoute(lastFile.uri))
                            } else {
                                pendingLocateFile = lastFile
                            }
                        }
                    }
                }
            }
        }
    }

    val openLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri ->
        uri?.let { navController.navigate(EditorRoute(it.toString())) }
    }

    val locateLauncher = rememberLauncherForActivityResult(
        contract = OpenDocumentWithInitialUri()
    ) { uri ->
        val oldUri = locatingFileUriString
        locatingFileUriString = null
        if (uri != null && oldUri != null) {
            viewModel.reconnectFile(oldUri, uri) {
                navController.navigate(EditorRoute(uri.toString()))
            }
        }
    }

    val isDark = isSystemInDarkTheme()
    val scaffoldBgColor = MaterialTheme.colorScheme.background
    val cardBgColor = Color.Transparent

    Scaffold(
        containerColor = scaffoldBgColor,
        snackbarHost = {
            SnackbarHost(hostState = snackbarHostState) { data ->
                Snackbar(snackbarData = data)
            }
        },
        topBar = {
            TopAppBar(
                title = {
                    Text("Recents")
                },
                actions = {
                    IconButton(onClick = {
                        openLauncher.launch(
                            arrayOf("text/plain", "text/markdown", "application/octet-stream")
                        )
                    }) {
                        Icon(SkrivIcons.FolderOpen, contentDescription = "Open file")
                    }
                    var showMenu by remember { mutableStateOf(false) }
                    Box {
                        IconButton(onClick = { showMenu = true }) {
                            Icon(SkrivIcons.MoreVert, contentDescription = "Menu")
                        }
                        DropdownMenu(
                            expanded = showMenu,
                            onDismissRequest = { showMenu = false }
                        ) {
                            DropdownMenuItem(
                                text = { Text("Settings") },
                                onClick = {
                                    showMenu = false
                                    navController.navigate(SettingsRoute)
                                }
                            )
                            DropdownMenuItem(
                                text = { Text("Clear recent files") },
                                onClick = {
                                    showMenu = false
                                    showClearDialog = true
                                }
                            )
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = scaffoldBgColor
                )
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = {
                    navController.navigate(EditorRoute(uriString = null))
                },
                shape = FloatingActionButtonDefaults.shape
            ) {
                Icon(SkrivIcons.Add, contentDescription = "New document")
            }
        }
    ) { innerPadding ->
        if (recentFiles.isEmpty()) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text = "No recent files.",
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = "Tap the folder icon at the top to choose a file.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                        )
                    }
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
                contentPadding = PaddingValues(
                    bottom = 16.dp
                )
            ) {
                items(recentFiles, key = { it.uri }) { file ->
                    RecentFileRow(
                        file = file,
                        onClick = {
                            if (checkingFileUri != null) return@RecentFileRow
                            if (file.isAvailable) {
                                checkingFileUri = file.uri
                                viewModel.checkFileAvailability(file) { available ->
                                    checkingFileUri = null
                                    if (available) {
                                        navController.navigate(EditorRoute(file.uri))
                                    } else {
                                        pendingLocateFile = file
                                    }
                                }
                            } else {
                                pendingLocateFile = file
                            }
                        }
                    )
                }
            }
        }
    }

    if (showClearDialog) {
        AlertDialog(
            onDismissRequest = { showClearDialog = false },
            icon = { Icon(SkrivIcons.Delete, contentDescription = null, tint = MaterialTheme.colorScheme.error) },
            title = { Text("Clear recent files?") },
            text = { Text("This will clear your list of recently opened files.\nYour actual files will not be deleted.") },
            shape = AlertDialogDefaults.shape,
            containerColor = AlertDialogDefaults.containerColor,
            confirmButton = {
                TextButton(
                    onClick = {
                        viewModel.clearRecentFiles()
                        showClearDialog = false
                    },
                    colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.error)
                ) {
                    Text("Clear")
                }
            },
            dismissButton = {
                TextButton(onClick = { showClearDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }

    if (pendingLocateFile != null) {
        val file = pendingLocateFile!!
        AlertDialog(
            onDismissRequest = { pendingLocateFile = null },
            icon = { Icon(SkrivIcons.FolderOpen, contentDescription = null) },
            title = { Text("Find file") },
            text = { Text("Skriv needs you to locate \"${file.displayName}\" to open it again.") },
            shape = AlertDialogDefaults.shape,
            containerColor = AlertDialogDefaults.containerColor,
            confirmButton = {
                TextButton(
                    onClick = {
                        val initialUri = try { file.uri.toUri() } catch (e: Exception) { null }
                        locatingFileUriString = file.uri
                        pendingLocateFile = null
                        locateLauncher.launch(
                            Pair(
                                arrayOf("text/plain", "text/markdown", "application/octet-stream"),
                                initialUri
                            )
                        )
                    }
                ) {
                    Text("Locate file")
                }
            },
            dismissButton = {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    TextButton(
                        onClick = {
                            viewModel.remove(file.uri)
                            pendingLocateFile = null
                        },
                        colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.error)
                    ) {
                        Text("Remove")
                    }
                    TextButton(onClick = { pendingLocateFile = null }) {
                        Text("Cancel")
                    }
                }
            }
        )
    }
}

@Composable
fun RecentFileRow(
    file: RecentFileEntity,
    onClick: () -> Unit
) {
    val alpha = if (file.isAvailable) 1f else 0.4f
    Surface(
        color = Color.Transparent,
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Icon(
                imageVector = if (file.displayName.endsWith(".md")) {
                    SkrivIcons.EditDocument
                } else {
                    SkrivIcons.Document
                },
                contentDescription = "File icon",
                tint = MaterialTheme.colorScheme.primary.copy(alpha = alpha),
                modifier = Modifier.size(24.dp)
            )

            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    text = file.displayName,
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = alpha),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                val relativeTime = DateUtils.getRelativeTimeSpanString(
                    file.lastAccessedAt,
                    System.currentTimeMillis(),
                    DateUtils.MINUTE_IN_MILLIS
                ).toString()
                val location = getReadableLocation(file.uri)
                val subtitleText = if (location.isNotEmpty()) "$location  •  $relativeTime" else relativeTime
                Text(
                    text = subtitleText,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = alpha * 0.7f),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }

            if (!file.isAvailable) {
                Icon(
                    imageVector = SkrivIcons.Warning,
                    contentDescription = "Unavailable",
                    tint = MaterialTheme.colorScheme.error
                )
            }
        }
    }
}

private fun getReadableLocation(uriString: String): String {
    val uri = try { uriString.toUri() } catch (e: Exception) { return "" }
    val authority = uri.authority ?: return ""
    return when {
        authority == "com.android.externalstorage.documents" -> {
            val docId = uri.pathSegments.lastOrNull() ?: ""
            val parts = docId.split(":")
            if (parts.size > 1) {
                val path = parts[1]
                val parentDir = path.substringBeforeLast("/", "")
                if (parentDir.isNotEmpty()) {
                    "Local › " + parentDir.substringAfterLast("/")
                } else {
                    "Local"
                }
            } else {
                "Local"
            }
        }
        authority == "com.android.providers.downloads.documents" -> "Downloads"
        authority.contains("com.google.android.apps.docs", ignoreCase = true) || authority.contains("docs", ignoreCase = true) -> "Google Drive"
        authority.contains("skydrive", ignoreCase = true) || authority.contains("onedrive", ignoreCase = true) -> "OneDrive"
        authority.contains("dropbox", ignoreCase = true) -> "Dropbox"
        else -> {
            authority.removeSuffix(".documents")
                .removeSuffix(".storage")
                .removeSuffix(".provider")
                .split(".")
                .lastOrNull()
                ?.replaceFirstChar { it.uppercase() } ?: "External"
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
