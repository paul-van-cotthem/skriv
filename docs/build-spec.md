# Skriv — Android app build specification
*Agent prompt v1.8*

---

## What you are building

An Android plain text file editor called **Skriv**. It opens, edits, and saves `.txt` and `.md` files stored anywhere on the Android file system or in cloud storage. It does not store files internally, render Markdown, or require an account. Every file the user edits remains a real file in the real file system.

The app is written in **Kotlin 2.x + Jetpack Compose**. Minimum SDK: **API 29 (Android 10)**. Target SDK: **API 35**. Use **Material 3** components and theming throughout.

---

## Hard constraints — never violate these

1. **No broad storage permissions.** Do not declare `READ_EXTERNAL_STORAGE`, `WRITE_EXTERNAL_STORAGE`, or `MANAGE_APP_ALL_FILES_ACCESS_PERMISSION` in the manifest. File access is exclusively via Android's Storage Access Framework (SAF) using `content://` URIs.
2. **No network permissions.** The app never connects to the internet. Do not declare `INTERNET` or any network permission.
3. **No proprietary file format.** Every document is a `.txt` or `.md` file. Nothing is stored in a private app database except the recents list and user preferences.
4. **No ads, no analytics, no third-party SDKs** beyond what is listed in the dependencies section below.
5. **UTF-8 only.** All files are read and written as UTF-8. Never silently substitute or drop characters.
6. **All file I/O off the main thread.** Use coroutines. Never block the UI thread with file operations.

---

## Gradle and Kotlin configuration

### Kotlin version and Compose compiler

Use **Kotlin 2.x**. With Kotlin 2.0+, the Compose compiler is a standalone Gradle plugin — there is no `kotlinCompilerExtensionVersion` and no `composeOptions` block.

Root `build.gradle.kts`:

```kotlin
plugins {
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.kotlin.android) apply false
    alias(libs.plugins.kotlin.compose) apply false
    alias(libs.plugins.kotlin.serialization) apply false
    alias(libs.plugins.ksp) apply false
}
```

`app/build.gradle.kts`:

```kotlin
plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)        // Compose compiler; replaces composeOptions
    alias(libs.plugins.kotlin.serialization)  // required for @Serializable nav routes
    alias(libs.plugins.ksp)                   // required for Room
}

android {
    compileSdk = 35
    defaultConfig { minSdk = 29; targetSdk = 35 }
    buildFeatures { compose = true }
}
```

### Gradle Version Catalog (`libs.versions.toml`)

```toml
[versions]
kotlin = "2.1.0"
agp = "8.7.0"
compose-bom = "2024.09.00"
navigation = "2.8.0"
lifecycle = "2.8.7"
room = "2.6.1"
datastore = "1.1.1"
coroutines = "1.9.0"
core-ktx = "1.15.0"
activity = "1.9.3"
window = "1.3.0"
serialization-json = "1.7.3"
ksp = "2.1.0-1.0.29"

[plugins]
android-application = { id = "com.android.application", version.ref = "agp" }
kotlin-android = { id = "org.jetbrains.kotlin.android", version.ref = "kotlin" }
kotlin-compose = { id = "org.jetbrains.kotlin.plugin.compose", version.ref = "kotlin" }
kotlin-serialization = { id = "org.jetbrains.kotlin.plugin.serialization", version.ref = "kotlin" }
ksp = { id = "com.google.devtools.ksp", version.ref = "ksp" }

[libraries]
compose-bom = { group = "androidx.compose", name = "compose-bom", version.ref = "compose-bom" }
compose-ui = { group = "androidx.compose.ui", name = "ui" }
compose-material3 = { group = "androidx.compose.material3", name = "material3" }
compose-ui-tooling-preview = { group = "androidx.compose.ui", name = "ui-tooling-preview" }
compose-animation = { group = "androidx.compose.animation", name = "animation" }
compose-ui-tooling = { group = "androidx.compose.ui", name = "ui-tooling" }
navigation-compose = { group = "androidx.navigation", name = "navigation-compose", version.ref = "navigation" }
lifecycle-viewmodel-compose = { group = "androidx.lifecycle", name = "lifecycle-viewmodel-compose", version.ref = "lifecycle" }
lifecycle-runtime-compose = { group = "androidx.lifecycle", name = "lifecycle-runtime-compose", version.ref = "lifecycle" }
room-runtime = { group = "androidx.room", name = "room-runtime", version.ref = "room" }
room-ktx = { group = "androidx.room", name = "room-ktx", version.ref = "room" }
room-compiler = { group = "androidx.room", name = "room-compiler", version.ref = "room" }
datastore-preferences = { group = "androidx.datastore", name = "datastore-preferences", version.ref = "datastore" }
coroutines-android = { group = "org.jetbrains.kotlinx", name = "kotlinx-coroutines-android", version.ref = "coroutines" }
core-ktx = { group = "androidx.core", name = "core-ktx", version.ref = "core-ktx" }
activity-compose = { group = "androidx.activity", name = "activity-compose", version.ref = "activity" }
window = { group = "androidx.window", name = "window", version.ref = "window" }
serialization-json = { group = "org.jetbrains.kotlinx", name = "kotlinx-serialization-json", version.ref = "serialization-json" }
```

---

## Project structure

```
com.skriv.app
├── MainActivity.kt
├── navigation/
│   └── SkrivNavGraph.kt
├── ui/
│   ├── theme/
│   │   └── SkrivTheme.kt
│   ├── editor/
│   │   ├── EditorScreen.kt
│   │   ├── EditorViewModel.kt
│   │   ├── FindBarState.kt
│   │   └── FindBar.kt
│   ├── recents/
│   │   ├── RecentsScreen.kt
│   │   └── RecentsViewModel.kt
│   └── settings/
│       ├── SettingsScreen.kt
│       └── SettingsViewModel.kt
├── data/
│   ├── db/
│   │   ├── SkrivDatabase.kt
│   │   ├── RecentFileDao.kt
│   │   └── RecentFileEntity.kt
│   ├── prefs/
│   │   └── UserPreferences.kt
│   └── repository/
│       ├── FileRepository.kt
│       └── RecentsRepository.kt
├── model/
│   ├── DocumentState.kt
│   ├── UiEvent.kt
│   └── PendingAction.kt
└── util/
    ├── UriPermissionHelper.kt
    └── EncodingHelper.kt
```

---

## Screen inventory and navigation

### Type-safe route definitions

```kotlin
@Serializable object RecentsRoute
@Serializable data class EditorRoute(val uriString: String?)  // null = new document
@Serializable object SettingsRoute
```

Navigate with `navController.navigate(EditorRoute(uri.toString()))`. Retrieve with `navBackStackEntry.toRoute<EditorRoute>()`.

### Navigation graph

```
LauncherIcon / External Intent
        │
        ▼
  RecentsScreen  ◄──────────────────────────────┐
        │                                        │
        │ tap recent / Open picker               │ back
        ▼                                        │
  EditorScreen ──── overflow ──────────► SettingsScreen
```

- `RecentsScreen` is always the start destination.
- External intents: start at `RecentsScreen`, immediately navigate to `EditorRoute(uri.toString())`.
- Navigation Compose 2.8+ handles predictive back animations automatically.

### `SkrivNavGraph`

```kotlin
@Composable
fun SkrivNavGraph(navController: NavHostController) {
    NavHost(
        navController = navController,
        startDestination = RecentsRoute,
        enterTransition = { fadeIn(tween(200)) + scaleIn(tween(200), initialScale = 0.95f) },
        exitTransition = { fadeOut(tween(200)) + scaleOut(tween(200), targetScale = 0.95f) },
        popEnterTransition = { fadeIn(tween(200)) + scaleIn(tween(200), initialScale = 0.95f) },
        popExitTransition = { fadeOut(tween(200)) + scaleOut(tween(200), targetScale = 0.95f) }
    ) {
        composable<RecentsRoute> {
            RecentsScreen(navController = navController)
        }
        composable<EditorRoute>(
            enterTransition = { slideInHorizontally(tween(300)) { it } },
            exitTransition = { slideOutHorizontally(tween(300)) { it } },
            popEnterTransition = { slideInHorizontally(tween(300)) { -it } },
            popExitTransition = { slideOutHorizontally(tween(300)) { -it } }
        ) { backStackEntry ->
            EditorScreen(navController = navController, navBackStackEntry = backStackEntry)
        }
        composable<SettingsRoute>(
            enterTransition = { slideInHorizontally(tween(300)) { it } },
            exitTransition = { slideOutHorizontally(tween(300)) { -it } },
            popEnterTransition = { slideInHorizontally(tween(300)) { -it } },
            popExitTransition = { slideOutHorizontally(tween(300)) { it } }
        ) {
            SettingsScreen(navController = navController)
        }
    }
}
```

### Back behaviour

- No unsaved changes: pop back to `RecentsScreen`.
- Unsaved changes: `BackHandler(enabled = !documentState.isSaved)` calls `viewModel.requestBack()`, which sets `pendingAction = PendingAction.BACK`. The screen shows the unsaved-changes `AlertDialog` (see Pending actions).
- From `SettingsScreen`: pop back to `EditorScreen`.

---

## Theme: SkrivTheme

```kotlin
@Composable
fun SkrivTheme(darkTheme: Boolean, content: @Composable () -> Unit) {
    val colorScheme = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
        val context = LocalContext.current
        if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
    } else {
        if (darkTheme) darkColorScheme() else lightColorScheme()
    }
    MaterialTheme(colorScheme = colorScheme, content = content)
}
```

`MainActivity` collects `UserPreferences` inside `setContent` (composable scope). `isSystemInDarkTheme()` is `@Composable` and must be called here.

The `navController` is created inside `setContent` (composable scope). External intent handling must also live inside `setContent` with access to the same `navController`. Use `LaunchedEffect(intent)` so that intent-driven navigation fires after the NavHost has composed:

```kotlin
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val navController = rememberNavController()
            val userPreferences = remember { UserPreferences(applicationContext) }
            val userPrefs by userPreferences.data.collectAsStateWithLifecycle(UserPrefsData())
            val darkTheme = when (userPrefs.darkMode) {
                "dark"  -> true
                "light" -> false
                else    -> isSystemInDarkTheme()
            }
            // Handle ACTION_VIEW / ACTION_EDIT intents (e.g. opened from a file manager)
            val fileRepository = remember { FileRepository(applicationContext) }
            LaunchedEffect(intent) {
                val action = intent?.action
                if (action == Intent.ACTION_VIEW || action == Intent.ACTION_EDIT) {
                    intent.data?.let { uri ->
                        fileRepository.persistPermission(uri)
                        navController.navigate(EditorRoute(uri.toString()))
                    }
                }
            }
            SkrivTheme(darkTheme = darkTheme) {
                SkrivNavGraph(navController = navController)
            }
        }
    }
}
```

---

## Events and pending actions

### `UiEvent`

One-shot events emitted by ViewModels and consumed by screens via `LaunchedEffect`.

```kotlin
sealed interface UiEvent {
    data class Snackbar(val message: String, val actionLabel: String? = null) : UiEvent
    // ErrorDialog navigates back to recents after the user dismisses it.
    data class ErrorDialog(val title: String, val message: String) : UiEvent
    data object NavigateBack : UiEvent
}
```

In each screen:
```kotlin
val snackbarHostState = remember { SnackbarHostState() }
LaunchedEffect(Unit) {
    viewModel.events.collect { event ->
        when (event) {
            is UiEvent.Snackbar    -> snackbarHostState.showSnackbar(event.message, event.actionLabel)
            is UiEvent.ErrorDialog -> { /* show AlertDialog, then navController.popBackStack() */ }
            UiEvent.NavigateBack   -> navController.popBackStack()
        }
    }
}
Scaffold(snackbarHost = { SnackbarHost(snackbarHostState) }) { ... }
```

### `PendingAction`

Controls the unsaved-changes `AlertDialog`, which is shared between the back gesture and the "New" action.

```kotlin
enum class PendingAction { BACK, NEW }
```

In `EditorViewModel`:
```kotlin
val pendingAction: StateFlow<PendingAction?>

fun requestBack()  // called by BackHandler; sets pendingAction = BACK if unsaved
fun requestNew()   // called by "New" overflow item; sets pendingAction = NEW if unsaved

// Called by AlertDialog buttons
fun confirmPendingAction(save: Boolean)  // if save=true, saves then executes action
fun cancelPendingAction()                // clears pendingAction
```

In `EditorScreen`, show the dialog when `pendingAction != null`:
```kotlin
val pendingAction by viewModel.pendingAction.collectAsStateWithLifecycle()
if (pendingAction != null) {
    AlertDialog(
        title = { Text("Save changes?") },
        confirmButton = { TextButton({ viewModel.confirmPendingAction(save = true) }) { Text("Save") } },
        dismissButton = {
            TextButton({ viewModel.confirmPendingAction(save = false) }) { Text("Discard") }
            TextButton({ viewModel.cancelPendingAction() }) { Text("Cancel") }
        },
        onDismissRequest = { viewModel.cancelPendingAction() }
    )
}
BackHandler(enabled = !documentState.isSaved) { viewModel.requestBack() }
```

---

## Data model

### Room database: `SkrivDatabase`

```kotlin
@Database(entities = [RecentFileEntity::class], version = 1, exportSchema = false)
abstract class SkrivDatabase : RoomDatabase() {
    abstract fun recentFileDao(): RecentFileDao

    companion object {
        @Volatile private var INSTANCE: SkrivDatabase? = null
        fun getInstance(context: Context): SkrivDatabase =
            INSTANCE ?: synchronized(this) {
                INSTANCE ?: Room.databaseBuilder(
                    context.applicationContext, SkrivDatabase::class.java, "skriv.db"
                ).build().also { INSTANCE = it }
            }
    }
}
```

### `RecentFileEntity`

```kotlin
@Entity(tableName = "recent_files")
data class RecentFileEntity(
    @PrimaryKey val uri: String,
    val displayName: String,
    val lastAccessedAt: Long,
    val lastModifiedAt: Long?,
    val cursorPosition: Int,
    val scrollOffset: Int,
    val isAvailable: Boolean = true
)
```

### `RecentFileDao`

```kotlin
@Dao
interface RecentFileDao {
    @Query("SELECT * FROM recent_files ORDER BY lastAccessedAt DESC LIMIT 20")
    fun observeAll(): Flow<List<RecentFileEntity>>

    @Query("SELECT * FROM recent_files WHERE uri = :uri LIMIT 1")
    suspend fun getByUri(uri: String): RecentFileEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(file: RecentFileEntity)

    @Query("DELETE FROM recent_files WHERE uri = :uri")
    suspend fun delete(uri: String)

    @Query("DELETE FROM recent_files")
    suspend fun deleteAll()

    @Query("SELECT COUNT(*) FROM recent_files")
    suspend fun count(): Int

    @Query("DELETE FROM recent_files WHERE uri = (SELECT uri FROM recent_files ORDER BY lastAccessedAt ASC LIMIT 1)")
    suspend fun deleteOldest()

    @Query("UPDATE recent_files SET cursorPosition = :pos, scrollOffset = :offset WHERE uri = :uri")
    suspend fun updateScrollState(uri: String, pos: Int, offset: Int)

    @Query("UPDATE recent_files SET isAvailable = :available WHERE uri = :uri")
    suspend fun updateAvailability(uri: String, available: Boolean)
}
```

### User preferences

| Key | Type | Default |
|---|---|---|
| `font_monospace` | Boolean | true |
| `font_size_sp` | Int | 16 |
| `line_spacing` | String | `"normal"` |
| `reading_margin_dp` | Int | 24 |
| `word_wrap` | Boolean | true |
| `dark_mode` | String | `"system"` |
| `default_extension` | String | `"txt"` |
| `auto_hide_toolbar` | Boolean | true |
| `line_numbers` | Boolean | false |
| `word_count_visible` | Boolean | false |

```kotlin
data class UserPrefsData(
    val fontMonospace: Boolean = true,
    val fontSizeSp: Int = 16,
    val lineSpacing: String = "normal",
    val readingMarginDp: Int = 24,
    val wordWrap: Boolean = true,
    val darkMode: String = "system",
    val defaultExtension: String = "txt",
    val autoHideToolbar: Boolean = true,
    val lineNumbers: Boolean = false,
    val wordCountVisible: Boolean = false
)
```

`UserPreferences` wraps DataStore and exposes `val data: Flow<UserPrefsData>` plus individual `suspend fun set*()` mutators.

**Important:** DataStore must be a singleton per process. Declare the DataStore via the `preferencesDataStore` top-level delegate (which internally enforces one instance per file name), then access it from `UserPreferences(context)`:

```kotlin
private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "user_prefs")

class UserPreferences(private val context: Context) {
    val data: Flow<UserPrefsData> = context.dataStore.data.map { prefs -> /* map to UserPrefsData */ }
    suspend fun setFontMonospace(v: Boolean) { context.dataStore.edit { it[Keys.FONT_MONOSPACE] = v } }
    // ... etc.
}
```

Multiple `UserPreferences(context)` instances are safe because they all access the same DataStore singleton via `context.dataStore`.

### `DocumentState`

```kotlin
data class DocumentState(
    val uri: Uri?,
    val displayName: String,
    val isSaved: Boolean,
    val isReadOnly: Boolean,
    val isLoading: Boolean,
    val hadBom: Boolean = false
)
```

Transient errors (save failures, encoding errors) are emitted as `UiEvent`s, not stored in `DocumentState`. `isReadOnly` is the only persistent error-adjacent state.

---

## EditorViewModel

### Factory

```kotlin
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
```

### Class definition

```kotlin
class EditorViewModel(
    private val fileRepository: FileRepository,
    private val recentsRepository: RecentsRepository,
    private val prefs: UserPreferences
) : ViewModel() {

    val documentState: StateFlow<DocumentState>
    val textFieldState: TextFieldState = TextFieldState()
    val findBarState: StateFlow<FindBarState>
    val userPreferences: StateFlow<UserPrefsData>
    val events: SharedFlow<UiEvent>
    val pendingAction: StateFlow<PendingAction?>
    val createDocumentRequest: SharedFlow<String>    // suggested filename
    val restoredScrollOffset: SharedFlow<Int>        // one-shot: scroll to this offset after file load

    // canUndo/canRedo as StateFlow so Compose recomposes correctly
    val canUndo: StateFlow<Boolean>  // = snapshotFlow { textFieldState.undoState.canUndo }.stateIn(...)
    val canRedo: StateFlow<Boolean>  // = snapshotFlow { textFieldState.undoState.canRedo }.stateIn(...)

    // Word count, debounced 300ms
    val wordCount: StateFlow<Int>  // snapshotFlow { text }.debounce(300).map { count words }.stateIn(...)

    fun loadFile(uri: Uri)
    fun onCreateDocumentResult(uri: Uri)

    fun undo() = textFieldState.undoState.undo()
    fun redo() = textFieldState.undoState.redo()

    fun save()            // emits createDocumentRequest if uri is null
    fun requestNew()      // checks unsaved state; sets pendingAction = NEW or creates document
    fun requestBack()     // checks unsaved state; sets pendingAction = BACK or emits NavigateBack
    fun confirmPendingAction(save: Boolean)
    // confirmPendingAction behaviour:
    //   save=false → clear textFieldState (if NEW) or emit NavigateBack (if BACK); clear pendingAction
    //   save=true, uri != null → save inline; then execute action; clear pendingAction
    //   save=true, uri == null → store pendingAction in _savedPendingAction; call save()
    //     → onCreateDocumentResult() completes the save, then checks _savedPendingAction and executes it
    fun cancelPendingAction()

    // Silent auto-save on background. No-op when uri is null (new unsaved document).
    // Launches viewModelScope coroutine internally; does not suspend the caller.
    fun onBackground()

    fun saveScrollState(cursorPos: Int, scrollOffset: Int)

    fun openFindBar()
    fun onFindQueryChanged(query: String)
    fun onCaseSensitiveToggled()
    fun findNext()
    fun findPrevious()
    fun replace(replacement: String)
    fun replaceAll(replacement: String)
    fun closeFindBar()
}
```

`isSaved` is tracked by `snapshotFlow { textFieldState.text.toString() }` compared against a private `lastSavedContent: String`.

`canUndo` and `canRedo` implementation:
```kotlin
val canUndo: StateFlow<Boolean> = snapshotFlow { textFieldState.undoState.canUndo }
    .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), false)
val canRedo: StateFlow<Boolean> = snapshotFlow { textFieldState.undoState.canRedo }
    .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), false)
```

---

## FileRepository

`FileRepository` uses `context.contentResolver` internally. No `ContentResolver` parameter on any method.

```kotlin
class FileRepository(private val context: Context) {
    suspend fun readFile(uri: Uri): Result<Pair<String, Boolean>>  // content, hadBom
    suspend fun writeFile(uri: Uri, content: String, hadBom: Boolean): Result<Unit>
    fun persistPermission(uri: Uri)
    fun hasWritePermission(uri: Uri): Boolean
    fun getDisplayName(uri: Uri): String
    fun getLastModified(uri: Uri): Long?
}
```

- `readFile`: `openInputStream(uri)`, decode UTF-8, strip BOM (`﻿`), return `hadBom = true` if present. `Result.failure` on any error.
- `writeFile`: `openOutputStream(uri, "wt")`, prepend BOM only if `hadBom`. UTF-8.
- `persistPermission`: `takePersistableUriPermission(READ or WRITE)`. try/catch, log, do not crash.
- `hasWritePermission`: scan `persistedUriPermissions`.

---

## RecentsRepository

```kotlin
class RecentsRepository(private val dao: RecentFileDao) {

    val recentFiles: Flow<List<RecentFileEntity>> = dao.observeAll()

    // Preserves existing cursor/scroll when re-opening a known file.
    suspend fun recordOpen(uri: Uri, displayName: String, lastModifiedAt: Long?) {
        val existing = dao.getByUri(uri.toString())
        if (existing == null && dao.count() >= 20) dao.deleteOldest()
        dao.upsert(RecentFileEntity(
            uri = uri.toString(),
            displayName = displayName,
            lastAccessedAt = System.currentTimeMillis(),
            lastModifiedAt = lastModifiedAt,
            cursorPosition = existing?.cursorPosition ?: 0,
            scrollOffset = existing?.scrollOffset ?: 0,
            isAvailable = true
        ))
    }

    suspend fun getEntry(uri: Uri): RecentFileEntity? = dao.getByUri(uri.toString())

    suspend fun updateScrollState(uri: Uri, cursorPos: Int, scrollOffset: Int) =
        dao.updateScrollState(uri.toString(), cursorPos, scrollOffset)

    suspend fun markUnavailable(uri: Uri) = dao.updateAvailability(uri.toString(), false)
    suspend fun remove(uri: Uri) = dao.delete(uri.toString())
    suspend fun clearAll() = dao.deleteAll()
}
```

---

## RecentsViewModel

```kotlin
class RecentsViewModel(
    private val recentsRepository: RecentsRepository
) : ViewModel() {

    val recentFiles: StateFlow<List<RecentFileEntity>> = recentsRepository.recentFiles
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun remove(uri: String) {
        viewModelScope.launch { recentsRepository.remove(Uri.parse(uri)) }
    }
}

val RecentsViewModelFactory = viewModelFactory {
    initializer {
        val context = (this[APPLICATION_KEY] as Application).applicationContext
        RecentsViewModel(RecentsRepository(SkrivDatabase.getInstance(context).recentFileDao()))
    }
}
```

---

## SettingsViewModel

```kotlin
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
    fun setAutoHideToolbar(v: Boolean)  { viewModelScope.launch { prefs.setAutoHideToolbar(v) } }
    fun setLineNumbers(v: Boolean)      { viewModelScope.launch { prefs.setLineNumbers(v) } }
    fun setWordCountVisible(v: Boolean) { viewModelScope.launch { prefs.setWordCountVisible(v) } }
    fun clearRecentFiles()              { viewModelScope.launch { recentsRepository.clearAll() } }
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
```

---

## File picker launchers

**RecentsScreen** — opens a file then navigates to the editor:
```kotlin
val openLauncher = rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()) { uri ->
    uri?.let { navController.navigate(EditorRoute(it.toString())) }
}
// launched from "Open" button
openLauncher.launch(arrayOf("text/plain", "text/markdown", "application/octet-stream"))
```

**EditorScreen** — opens a different file into the same editor instance:
```kotlin
val openLauncher = rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()) { uri ->
    uri?.let { viewModel.loadFile(it) }
}
openLauncher.launch(arrayOf("text/plain", "text/markdown", "application/octet-stream"))
```

**Creating a new document** — use `"text/*"` so both `.txt` and `.md` are valid choices regardless of `defaultExtension`. The suggested filename (including the correct extension from `userPrefs.defaultExtension`) is enough to guide the picker.

```kotlin
val createLauncher = rememberLauncherForActivityResult(
    ActivityResultContracts.CreateDocument("text/*")
) { uri ->
    uri?.let { viewModel.onCreateDocumentResult(it) }
}
LaunchedEffect(Unit) {
    viewModel.createDocumentRequest.collect { suggestedFilename ->
        createLauncher.launch(suggestedFilename)
    }
}
```

---

## EditorScreen layout

```
┌─────────────────────────────────────────────┐
│ TopAppBar (auto-hides on scroll up)          │
│  [filename]  [saved indicator]  [⟲] [⟳] [⋮] │
├─────────────────────────────────────────────┤
│                                             │
│   [line numbers gutter] │ [text field]      │
│                                             │
├─────────────────────────────────────────────┤
│ FindBar (above keyboard, when active)       │
└─────────────────────────────────────────────┘
```

Wrap the screen in `Scaffold` with a `SnackbarHost`:
```kotlin
Scaffold(snackbarHost = { SnackbarHost(snackbarHostState) }) { innerPadding ->
    // editor content with innerPadding applied
}
```

Trigger initial file load and collect events:
```kotlin
val uriString: String? = navBackStackEntry.toRoute<EditorRoute>().uriString
LaunchedEffect(uriString) {
    uriString?.let { viewModel.loadFile(Uri.parse(it)) }
}

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
            is UiEvent.ErrorDialog -> { /* show AlertDialog; on dismiss navController.popBackStack() */ }
            UiEvent.NavigateBack   -> navController.popBackStack()
        }
    }
}
val pendingAction by viewModel.pendingAction.collectAsStateWithLifecycle()
if (pendingAction != null) { /* AlertDialog with Save / Discard / Cancel */ }
BackHandler(enabled = !documentState.isSaved) { viewModel.requestBack() }
```

**Text field:** `BasicTextField` with `state = viewModel.textFieldState`, `outputTransformation` from `findBarState`. Padding from `effectiveMarginDp`, `lineHeight` from `lineSpacing`, font from `fontMonospace`.

**Undo/redo buttons:** observe `canUndo` and `canRedo` as state:
```kotlin
val canUndo by viewModel.canUndo.collectAsStateWithLifecycle()
val canRedo by viewModel.canRedo.collectAsStateWithLifecycle()
```

**Scrolling and word wrap:**
```kotlin
val scrollState = rememberScrollState()
val horizontalScrollState = if (!userPrefs.wordWrap) rememberScrollState() else null

Row(Modifier.weight(1f)) {
    if (userPrefs.lineNumbers) LineNumbersGutter(scrollState)  // scrolls in sync via shared scrollState
    Box(
        Modifier.weight(1f).then(
            if (horizontalScrollState != null) Modifier.horizontalScroll(horizontalScrollState)
            else Modifier
        )
    ) {
        BasicTextField(
            state = viewModel.textFieldState,
            scrollState = scrollState,  // vertical scroll passed as parameter, NOT via Modifier
            lineLimits = TextFieldLineLimits.MultiLine(),
            modifier = if (userPrefs.wordWrap) Modifier.fillMaxWidth() else Modifier.wrapContentWidth()
        )
    }
}
```

**Reading width:**
```kotlin
val windowSizeClass = currentWindowAdaptiveInfo().windowSizeClass
val effectiveMarginDp = when (windowSizeClass.windowWidthSizeClass) {
    WindowWidthSizeClass.EXPANDED -> maxOf(userPrefs.readingMarginDp, 48)
    WindowWidthSizeClass.MEDIUM   -> maxOf(userPrefs.readingMarginDp, 32)
    else                          -> userPrefs.readingMarginDp
}
```

**Word count:** `Text` below the text field, visible when `wordCountVisible`. Debounced 300ms via `snapshotFlow + debounce`.

**Floating checkmark:** 32x32dp, top-end, 12dp inset-aware padding. Grey when saved, amber when unsaved. Tap amber → `viewModel.save()`. Tap grey → reset `scrollBehavior` state.

**Lifecycle / auto-save:**

Import `LocalLifecycleOwner` from `androidx.lifecycle.compose` (not from `androidx.compose.ui.platform`, which deprecated it in Compose 1.7). The `lifecycle-runtime-compose` artifact is already declared in the dependency list.

```kotlin
val lifecycleOwner = LocalLifecycleOwner.current  // import androidx.lifecycle.compose.LocalLifecycleOwner
DisposableEffect(lifecycleOwner) {
    val observer = LifecycleEventObserver { _, event ->
        if (event == Lifecycle.Event.ON_PAUSE) viewModel.onBackground()
    }
    lifecycleOwner.lifecycle.addObserver(observer)
    onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
}
```

---

## RecentsScreen layout

```
┌─────────────────────────────────────────────┐
│  "Skriv"              [+ New]  [📂 Open]    │
├─────────────────────────────────────────────┤
│  LazyColumn of recent files                 │
│  (greyed + warning icon if unavailable)     │
│  Swipe to dismiss (SwipeToDismissBox)       │
│                                             │
│  Empty state: "No recent files.             │
│   Tap Open to choose a file."               │
└─────────────────────────────────────────────┘
```

- Tap row → navigate to `EditorRoute(uriString)`.
- Tap unavailable row → show snackbar directly in `RecentsScreen` via `CoroutineScope` + `SnackbarHostState` (no ViewModel event needed for this case): `scope.launch { snackbarHostState.showSnackbar("File unavailable. Swipe to remove.") }`.
- Swipe to dismiss → `viewModel.remove(uri)`.
- Open → fire `openLauncher`.
- New → `navController.navigate(EditorRoute(uriString = null))`.

---

## SettingsScreen layout

`LazyColumn` of `ListItem` rows, sections separated by `HorizontalDivider`.

- **Typography:** font segmented button, font size segmented button, line spacing segmented button, reading width `Slider` (8–64dp, snap at 8/24/48).
- **Editor:** word wrap `Switch`, line numbers `Switch`, word count `Switch`, auto-hide toolbar `Switch`.
- **Appearance:** dark mode segmented button (System / Light / Dark).
- **Files:** default extension segmented button, "Clear all recent files" `TextButton` with `AlertDialog` confirmation.

---

## Intent filters (AndroidManifest.xml)

```xml
<activity android:name=".MainActivity" ...>
    <intent-filter>
        <action android:name="android.intent.action.MAIN"/>
        <category android:name="android.intent.category.LAUNCHER"/>
    </intent-filter>
    <intent-filter>
        <action android:name="android.intent.action.VIEW"/>
        <action android:name="android.intent.action.EDIT"/>
        <category android:name="android.intent.category.DEFAULT"/>
        <data android:mimeType="text/plain"/>
    </intent-filter>
    <intent-filter>
        <action android:name="android.intent.action.VIEW"/>
        <action android:name="android.intent.action.EDIT"/>
        <category android:name="android.intent.category.DEFAULT"/>
        <data android:mimeType="text/markdown"/>
    </intent-filter>
    <!-- application/octet-stream catches SAF documents where the provider assigns a generic MIME.
         No pathPattern here: SAF content:// URIs don't carry file paths, so pathPattern
         would never match and only makes this filter confusingly broad. -->
    <intent-filter>
        <action android:name="android.intent.action.VIEW"/>
        <action android:name="android.intent.action.EDIT"/>
        <category android:name="android.intent.category.DEFAULT"/>
        <data android:mimeType="application/octet-stream"/>
    </intent-filter>
</activity>
```

Intent handling is done via `LaunchedEffect(intent)` inside `setContent` — see the `MainActivity` code in the Theme section above.

### App icon

Placeholder adaptive icon during development. Final icon (plus monochrome variant for Android 13+ themed icons) is a pre-launch design deliverable.

---

## Find and replace

### `FindBarState`

```kotlin
data class FindBarState(
    val isVisible: Boolean = false,
    val query: String = "",
    val replacement: String = "",
    val matchRanges: List<IntRange> = emptyList(),
    val currentMatchIndex: Int = 0,
    val caseSensitive: Boolean = false
) {
    val matchCount: Int get() = matchRanges.size
}
```

### Highlighting

`OutputTransformation` computed with `remember(findBarState.matchRanges, findBarState.currentMatchIndex)`. Annotate with background spans: amber (`Color(0xFFFFA000)`) for the current match, tint (`Color(0x33FFA000)`) for others. When `isVisible = false`, pass `null` (the parameter is `outputTransformation: OutputTransformation?`; there is no `None` constant). Match positions recomputed in the ViewModel on every query change, debounced at 150ms.

### Replace / Replace All

```kotlin
fun replace(replacement: String) {
    val range = findBarState.value.matchRanges
        .getOrNull(findBarState.value.currentMatchIndex) ?: return
    textFieldState.edit { replace(range.first, range.last + 1, replacement) }
    recomputeMatches()
}

fun replaceAll(replacement: String) {
    textFieldState.edit {
        findBarState.value.matchRanges
            .sortedByDescending { it.first }
            .forEach { range -> replace(range.first, range.last + 1, replacement) }
    }
    recomputeMatches()
}
```

Each `edit {}` block is one undoable step.

---

## Error states and UI

| Situation | Response |
|---|---|
| Save fails | `UiEvent.Snackbar("Could not save: [reason]", actionLabel = "Retry")` |
| File opened read-only | `UiEvent.Snackbar("File opened read-only. Use Open from menu to enable editing.")` + `documentState.isReadOnly = true` |
| Encoding error | `UiEvent.ErrorDialog("Cannot open file", "This file does not appear to be a UTF-8 text file.")` → navigates to recents |
| File too large (>5MB) | `UiEvent.ErrorDialog("File too large", "This file is over 5MB. Skriv is designed for text files.")` → navigates to recents |
| Recent file unavailable | Row greyed + warning icon. `UiEvent.Snackbar("File unavailable. Swipe to remove.")` on tap. |
| Back / New with unsaved changes | `AlertDialog` via `pendingAction` (see Pending actions section) |

---

## Save flow

```
viewModel.save() called
    │
    ├── uri is null
    │       └── emit createDocumentRequest(suggestedFilename)
    │           └── launcher returns uri → onCreateDocumentResult(uri):
    │                 persistPermission(uri)
    │                 writeFile(uri, textFieldState.text.toString(), hadBom) → success/failure
    │                 recordOpen(uri, ...)
    │                 lastSavedContent = content; isSaved = true
    │
    └── uri is not null
            ├── hasWritePermission == true
            │       └── writeFile(uri, textFieldState.text.toString(), hadBom)
            │               ├── success: lastSavedContent updated; isSaved = true
            │               └── failure: UiEvent.Snackbar with "Retry"
            └── hasWritePermission == false
                    └── UiEvent.Snackbar("File opened read-only...")
```

---

## Open flow

```
openLauncher fires → uri received
    1. persistPermission(uri)                    ← always first
    2. readFile(uri) on IO dispatcher
    3. Success — all steps that touch TextFieldState or DocumentState must run on Main dispatcher
       (use withContext(Dispatchers.Main) after the IO read):
       a. recentsRepository.recordOpen(uri, displayName, lastModified)
          (preserves existing cursor/scroll for known URIs)
       b. withContext(Dispatchers.Main) {
              textFieldState.edit { replace(0, length, content) }
              textFieldState.undoState.clearHistory()
          }
       c. documentState updated:
          - uri, displayName, hadBom
          - isSaved = true
          - isReadOnly = !fileRepository.hasWritePermission(uri)
          - isLoading = false
       d. val entry = recentsRepository.getEntry(uri)
          Set cursor: textFieldState.edit { selectCharsIn(TextRange(entry.cursorPosition)) }
          Scroll: expose restoredScrollOffset as a one-shot SharedFlow<Int>; EditorScreen
          collects it with LaunchedEffect and calls scrollState.scrollTo(offset)
    4. Failure: UiEvent.ErrorDialog (encoding) or UiEvent.ErrorDialog (too large)
```

---

## Share implementation

```kotlin
fun shareDocument(context: Context, uri: Uri?, content: String, displayName: String) {
    val intent = Intent(Intent.ACTION_SEND).apply {
        type = "text/plain"
        putExtra(Intent.EXTRA_TEXT, content)   // text body for apps that can't handle URIs
        putExtra(Intent.EXTRA_SUBJECT, displayName)
        uri?.let {
            putExtra(Intent.EXTRA_STREAM, it)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
    }
    context.startActivity(Intent.createChooser(intent, null))
}
```

Call from overflow menu "Share" item. Pass `textFieldState.text.toString()` as `content`.

---

## Print implementation

```kotlin
fun printDocument(context: Context, textFieldState: TextFieldState, displayName: String) {
    val content = textFieldState.text.toString()
    val printManager = context.getSystemService(Context.PRINT_SERVICE) as PrintManager
    // android.text.Html.escapeHtml() is the correct API (no escapeHtml extension on String)
    val htmlContent = "<html><body><pre>${Html.escapeHtml(content)}</pre></body></html>"
    val webView = WebView(context)
    webView.loadDataWithBaseURL(null, htmlContent, "text/html", "UTF-8", null)
    webView.webViewClient = object : WebViewClient() {
        override fun onPageFinished(view: WebView, url: String) {
            val printAdapter = webView.createPrintDocumentAdapter(displayName)
            printManager.print(displayName, printAdapter, PrintAttributes.Builder().build())
        }
    }
}
```

---

## Toolbar auto-hide

`TopAppBarDefaults.enterAlwaysScrollBehavior()`. Connect via `Modifier.nestedScroll(scrollBehavior.nestedScrollConnection)` on the root layout. Show floating checkmark when `collapsedFraction == 1f`.

---

## Transitions

- `RecentsScreen` → `EditorScreen`: `fadeIn() + scaleIn(initialScale = 0.95f)` enter, `fadeOut() + scaleOut(targetScale = 0.95f)` exit. Configure via `NavHost`'s `enterTransition`, `exitTransition`, `popEnterTransition`, `popExitTransition` parameters.
- `EditorScreen` → `SettingsScreen`: `slideInHorizontally { it }` enter, `slideOutHorizontally { -it }` exit, reversed on pop.
- Do **not** use `SharedTransitionLayout`/`sharedElement` — that API is still `@ExperimentalSharedTransitionApi` in Compose Animation 1.7 and requires an `@OptIn` annotation at every call site.
- Toolbar: `TopAppBarScrollBehavior`, 200ms, `FastOutSlowInEasing`.
- Find bar: `AnimatedVisibility` + `slideInVertically { it }` from bottom, `slideOutVertically { it }` to bottom.

---

## Edge-to-edge and insets

```kotlin
enableEdgeToEdge()  // in onCreate, before setContent
```

Use `Scaffold`'s `innerPadding` (which already includes `WindowInsets.systemBars`) as the root `Modifier.padding(innerPadding)` on screen content. Do **not** also apply `Modifier.windowInsetsPadding(WindowInsets.systemBars)` — that would double-pad.

For the IME (keyboard), apply `Modifier.imePadding()` to the `Column` that wraps the text field and FindBar (i.e., below the TopAppBar, above nothing). This lets the FindBar slide above the keyboard naturally.

---

## Haptic feedback

```kotlin
val haptic = LocalHapticFeedback.current
haptic.performHapticFeedback(HapticFeedbackType.Confirm)  // in FloatingCheckmarkButton onClick
```

---

## Dependencies

```kotlin
dependencies {
    implementation(platform(libs.compose.bom))
    implementation(libs.compose.ui)
    implementation(libs.compose.material3)
    implementation(libs.compose.ui.tooling.preview)
    implementation(libs.compose.animation)
    implementation(libs.navigation.compose)
    implementation(libs.lifecycle.viewmodel.compose)
    implementation(libs.lifecycle.runtime.compose)
    implementation(libs.room.runtime)
    implementation(libs.room.ktx)
    ksp(libs.room.compiler)
    implementation(libs.datastore.preferences)
    implementation(libs.coroutines.android)
    implementation(libs.core.ktx)
    implementation(libs.activity.compose)
    implementation(libs.window)
    implementation(libs.serialization.json)
    debugImplementation(libs.compose.ui.tooling)  // required for @Preview rendering in Android Studio
}
```

---

## What not to build

- Markdown rendering or preview
- Folder browser or file tree
- File management (rename, delete, duplicate)
- Multiple open files or tabs
- Syntax highlighting
- Custom color themes
- Cloud sync
- Widgets
- Any network call
- Any analytics or crash reporting

If a feature is not described in this document, do not build it.
