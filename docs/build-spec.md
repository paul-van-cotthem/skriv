# Skriv — Android app build specification
*Agent prompt v1.3*

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

Use **Kotlin 2.x**. With Kotlin 2.0+, the Compose compiler is delivered as a standalone Gradle plugin — there is no separate `kotlinCompilerExtensionVersion` to manage. The old `composeOptions` block in `build.gradle` is gone.

In the root-level `build.gradle.kts` plugins block:

```kotlin
plugins {
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.kotlin.android) apply false
    alias(libs.plugins.kotlin.compose) apply false
    alias(libs.plugins.kotlin.serialization) apply false
    alias(libs.plugins.ksp) apply false
}
```

In `app/build.gradle.kts`:

```kotlin
plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)        // replaces composeOptions block entirely
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
├── MainActivity.kt                  // single Activity, hosts NavHost
├── navigation/
│   └── SkrivNavGraph.kt             // NavHost, type-safe route definitions
├── ui/
│   ├── theme/
│   │   └── SkrivTheme.kt            // MaterialTheme wrapper, dynamic color
│   ├── editor/
│   │   ├── EditorScreen.kt          // full-screen editor Composable
│   │   ├── EditorViewModel.kt       // editor state and file operations
│   │   ├── FindBarState.kt          // find/replace state holder
│   │   └── FindBar.kt               // find/replace bar Composable
│   ├── recents/
│   │   ├── RecentsScreen.kt         // recents list Composable
│   │   └── RecentsViewModel.kt      // recents list state
│   └── settings/
│       ├── SettingsScreen.kt        // settings Composable
│       └── SettingsViewModel.kt     // settings state
├── data/
│   ├── db/
│   │   ├── SkrivDatabase.kt         // Room database definition
│   │   ├── RecentFileDao.kt         // DAO for recent files
│   │   └── RecentFileEntity.kt      // Room entity
│   ├── prefs/
│   │   └── UserPreferences.kt       // DataStore preferences wrapper
│   └── repository/
│       ├── FileRepository.kt        // SAF file operations
│       └── RecentsRepository.kt     // recents CRUD
├── model/
│   └── DocumentState.kt             // data class for current document
└── util/
    ├── UriPermissionHelper.kt       // takePersistableUriPermission logic
    └── EncodingHelper.kt            // UTF-8 read/write helpers
```

---

## Screen inventory and navigation

The app has **three screens** and a **single Activity**.

### Type-safe route definitions

Define routes in `SkrivNavGraph.kt` using `@Serializable` (nav-compose 2.8+). No string-based routes.

```kotlin
@Serializable object RecentsRoute
@Serializable data class EditorRoute(val uriString: String?)  // null = new document
@Serializable object SettingsRoute
```

Navigate using `navController.navigate(EditorRoute(uri.toString()))`. Retrieve arguments with `navBackStackEntry.toRoute<EditorRoute>()`.

### Navigation graph

```
LauncherIcon / External Intent
        │
        ▼
  RecentsScreen  ◄──────────────────────────────┐
        │                                        │
        │ tap recent file                        │ back gesture (predictive back)
        │ or Open picker selects file            │
        ▼                                        │
  EditorScreen ──── overflow menu ──────► SettingsScreen
```

- `RecentsScreen` is **always** the start destination, regardless of how the app is launched.
- When launched via an external intent, the app starts at `RecentsScreen` and immediately navigates forward to `EditorScreen` with the incoming URI, so the back stack is always rooted at `RecentsScreen`.
- `SettingsScreen` is pushed onto the back stack from `EditorScreen` via the overflow menu.
- Navigation Compose 2.8+ handles predictive back animations between destinations automatically — no manual setup required.

### Back behaviour

- From `EditorScreen` with no unsaved changes: navigate back to `RecentsScreen`.
- From `EditorScreen` with unsaved changes: use `BackHandler(enabled = !documentState.isSaved)` to intercept, then show an `AlertDialog` ("Save" / "Discard" / "Cancel"). "Save" writes the file then navigates; "Discard" navigates without saving; "Cancel" dismisses the dialog.
- From `SettingsScreen`: pop back to `EditorScreen`.
- `BackHandler` is composable-native and integrates with Android 13+ predictive back without additional wiring.

---

## Theme: SkrivTheme

`SkrivTheme.kt` wraps the entire `NavHost`. Individual screens do not set their own theme.

```kotlin
@Composable
fun SkrivTheme(
    darkTheme: Boolean,
    content: @Composable () -> Unit
) {
    val colorScheme = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
        val context = LocalContext.current
        if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
    } else {
        if (darkTheme) darkColorScheme() else lightColorScheme()
    }
    MaterialTheme(colorScheme = colorScheme, content = content)
}
```

`MainActivity` collects `UserPreferences` from DataStore inside `setContent` (a composable scope), resolves the dark mode preference, then calls `SkrivTheme`. `isSystemInDarkTheme()` is a `@Composable` function and must be called here, not before `setContent`.

```kotlin
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val userPreferences = remember { UserPreferences(applicationContext) }
            val userPrefs by userPreferences.data.collectAsStateWithLifecycle(
                initialValue = UserPrefsData()
            )
            val darkTheme = when (userPrefs.darkMode) {
                "dark"  -> true
                "light" -> false
                else    -> isSystemInDarkTheme()
            }
            SkrivTheme(darkTheme = darkTheme) {
                SkrivNavGraph(navController = rememberNavController(), userPrefs = userPrefs)
            }
        }
    }
}
```

Dynamic color is available on API 31+. On API 29–30, `darkColorScheme()` / `lightColorScheme()` with neutral defaults is used as a fallback.

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
                    context.applicationContext,
                    SkrivDatabase::class.java,
                    "skriv.db"
                ).build().also { INSTANCE = it }
            }
    }
}
```

Single table: `recent_files`

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

### DAO: `RecentFileDao`

```kotlin
@Dao
interface RecentFileDao {
    @Query("SELECT * FROM recent_files ORDER BY lastAccessedAt DESC LIMIT 20")
    fun observeAll(): Flow<List<RecentFileEntity>>

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

### User preferences: DataStore

Use Jetpack DataStore (Preferences) for all user settings.

| Key | Type | Default |
|---|---|---|
| `font_monospace` | Boolean | true |
| `font_size_sp` | Int | 16 |
| `line_spacing` | String | `"normal"` (`"compact"`, `"normal"`, `"relaxed"`) |
| `reading_margin_dp` | Int | 24 |
| `word_wrap` | Boolean | true |
| `dark_mode` | String | `"system"` (`"system"`, `"light"`, `"dark"`) |
| `default_extension` | String | `"txt"` (`"txt"`, `"md"`) |
| `auto_hide_toolbar` | Boolean | true |
| `line_numbers` | Boolean | false |
| `word_count_visible` | Boolean | false |

### `UserPrefsData`

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

### Document state: `DocumentState`

```kotlin
data class DocumentState(
    val uri: Uri?,
    val displayName: String,
    val isSaved: Boolean,
    val isReadOnly: Boolean,
    val isLoading: Boolean,
    val error: String?,
    val hadBom: Boolean = false
)
```

Current editing content lives in `viewModel.textFieldState` (a `TextFieldState`), not in `DocumentState`. The ViewModel tracks a private `lastSavedContent: String`; `isSaved` is recomputed whenever `textFieldState.text` changes by comparing against that value.

---

## EditorViewModel

`EditorViewModel` holds all editor state. Inject `FileRepository`, `RecentsRepository`, and `UserPreferences`. All constructor dependencies come from the ViewModel factory — do not use Hilt or Koin.

### ViewModel factory

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

Obtain in composables via `viewModel(factory = EditorViewModelFactory)`.

### Class definition

```kotlin
class EditorViewModel(
    private val fileRepository: FileRepository,
    private val recentsRepository: RecentsRepository,
    private val prefs: UserPreferences
) : ViewModel() {

    val documentState: StateFlow<DocumentState>

    // Owns current text content and full undo/redo history.
    val textFieldState: TextFieldState = TextFieldState()

    val findBarState: StateFlow<FindBarState>
    val userPreferences: StateFlow<UserPrefsData>

    // EditorScreen collects this and launches the CreateDocument picker.
    val createDocumentRequest: SharedFlow<String>  // suggested filename

    // Load file; populates textFieldState and clears undo history.
    fun loadFile(uri: Uri)

    // Called when EditorScreen receives a URI from the CreateDocument picker.
    fun onCreateDocumentResult(uri: Uri)

    // Undo/redo delegate to the platform.
    fun undo() = textFieldState.undoState.undo()
    fun redo() = textFieldState.undoState.redo()
    val canUndo: Boolean get() = textFieldState.undoState.canUndo
    val canRedo: Boolean get() = textFieldState.undoState.canRedo

    // Save to original URI; emits createDocumentRequest if uri is null.
    fun save()

    // New document.
    fun newDocument()

    // Called from onPause via DisposableEffect. Auto-saves only if uri is not null;
    // new unsaved documents are kept in memory — never prompt from background.
    fun onBackground()

    // Persist cursor and scroll position to the recents DB.
    fun saveScrollState(cursorPos: Int, scrollOffset: Int)

    // Find and replace
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

`isSaved` is computed via `snapshotFlow { textFieldState.text.toString() }` compared against `lastSavedContent`. It is `true` on file load and after a successful save.

**`onBackground()` behaviour for null URI:** if `documentState.uri` is null (new unsaved document), do nothing — the content survives in `textFieldState` for the duration of the process. Do not auto-save and do not prompt. The user will be asked on the next back gesture.

---

## FileRepository: SAF operations

`FileRepository` takes `Context` in its constructor and uses `context.contentResolver` internally. ViewModel methods do not pass `ContentResolver`.

```kotlin
class FileRepository(private val context: Context) {

    // Read file content as UTF-8 string.
    // Detects and strips UTF-8 BOM; returns hadBom = true if one was present.
    suspend fun readFile(uri: Uri): Result<Pair<String, Boolean>>

    // Write string to URI in truncate mode, UTF-8.
    // Prepends BOM only if hadBom is true.
    suspend fun writeFile(uri: Uri, content: String, hadBom: Boolean): Result<Unit>

    // Call takePersistableUriPermission immediately on any received URI.
    fun persistPermission(uri: Uri)

    // Returns true if a persisted write permission exists for this URI.
    fun hasWritePermission(uri: Uri): Boolean

    fun getDisplayName(uri: Uri): String
    fun getLastModified(uri: Uri): Long?
}
```

Implementation notes:
- `readFile`: `context.contentResolver.openInputStream(uri)`, read fully, decode as UTF-8. Strip BOM (`﻿`) if present; return `hadBom = true`. Return `Result.failure` on any error.
- `writeFile`: `context.contentResolver.openOutputStream(uri, "wt")`. Prepend `﻿` only if `hadBom`. Encode UTF-8.
- `persistPermission`: `context.contentResolver.takePersistableUriPermission(uri, FLAG_GRANT_READ_URI_PERMISSION or FLAG_GRANT_WRITE_URI_PERMISSION)`. Wrap in try/catch; log, do not crash.
- `hasWritePermission`: check `context.contentResolver.persistedUriPermissions`.

---

## RecentsRepository

```kotlin
class RecentsRepository(private val dao: RecentFileDao) {

    val recentFiles: Flow<List<RecentFileEntity>> = dao.observeAll()

    suspend fun recordOpen(uri: Uri, displayName: String, lastModifiedAt: Long?) {
        if (dao.count() >= 20) dao.deleteOldest()
        dao.upsert(RecentFileEntity(
            uri = uri.toString(),
            displayName = displayName,
            lastAccessedAt = System.currentTimeMillis(),
            lastModifiedAt = lastModifiedAt,
            cursorPosition = 0,
            scrollOffset = 0,
            isAvailable = true
        ))
    }

    suspend fun updateScrollState(uri: Uri, cursorPos: Int, scrollOffset: Int) =
        dao.updateScrollState(uri.toString(), cursorPos, scrollOffset)

    suspend fun markUnavailable(uri: Uri) =
        dao.updateAvailability(uri.toString(), false)

    suspend fun remove(uri: Uri) = dao.delete(uri.toString())

    suspend fun clearAll() = dao.deleteAll()

    suspend fun getEntry(uri: Uri): RecentFileEntity? =
        dao.observeAll().first().find { it.uri == uri.toString() }
}
```

---

## File picker launchers

Use `rememberLauncherForActivityResult` with typed contracts. Never call `startActivityForResult` directly.

**Opening a file** (in `RecentsScreen` and editor overflow menu):

```kotlin
val openLauncher = rememberLauncherForActivityResult(
    ActivityResultContracts.OpenDocument()
) { uri ->
    uri?.let { viewModel.loadFile(it) }
}
openLauncher.launch(arrayOf("text/plain", "text/markdown", "application/octet-stream"))
```

**Saving a new document** — ViewModels cannot hold launchers. Use a `SharedFlow` signal from the ViewModel:

```kotlin
// In EditorScreen
val createLauncher = rememberLauncherForActivityResult(
    ActivityResultContracts.CreateDocument("text/plain")
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
│                         │                  │
│                         │  (full height,   │
│                         │   scrollable)    │
│                         │                  │
├─────────────────────────────────────────────┤
│ FindBar (visible only when active,          │
│ sits above system keyboard)                 │
│  [search input] [↑] [↓] [3 of 12] [Replace]│
│  [replace input] [Replace] [Replace All]   │
└─────────────────────────────────────────────┘
```

- `TopAppBar` uses `enterAlwaysScrollBehavior()` so it hides on scroll up and reappears on scroll down.
- When toolbar is hidden, show a floating `FloatingCheckmarkButton`: 32x32dp, top-end, 12dp padding, respecting insets. Grey when saved, amber (`Color(0xFFFFA000)`) when unsaved. Tap when amber: `viewModel.save()`. Tap when grey: reset scroll behavior to reveal toolbar.
- Text field: `BasicTextField` with `state = viewModel.textFieldState` and `outputTransformation` from `findBarState`. Apply horizontal padding from `effectiveMarginDp`, `lineHeight` from `lineSpacing`, font family from `fontMonospace`.
- Line numbers gutter: a `Column` aligned left, driven by the **same `scrollState`** as the text field, visible only when `lineNumbers` is true.
- Word count bar: `Text` below the text field, visible only when `wordCountVisible` is true. "Words: 1,234  Characters: 6,789". Debounced 300ms via `snapshotFlow` + `debounce`.

### Word wrap on vs off

```kotlin
val scrollState = rememberScrollState()
val horizontalScrollState = if (!userPrefs.wordWrap) rememberScrollState() else null

Row(modifier = Modifier.weight(1f)) {
    if (userPrefs.lineNumbers) {
        LineNumbersGutter(scrollState = scrollState)  // vertical scroll only
    }
    Box(
        modifier = Modifier
            .weight(1f)
            .then(
                if (horizontalScrollState != null)
                    Modifier.horizontalScroll(horizontalScrollState)
                else Modifier
            )
    ) {
        BasicTextField(
            state = viewModel.textFieldState,
            modifier = Modifier
                .verticalScroll(scrollState)
                .then(
                    if (userPrefs.wordWrap) Modifier.fillMaxWidth()
                    else Modifier.wrapContentWidth()
                ),
            ...
        )
    }
}
```

When word wrap is off, the text field expands to its natural width and the outer `Box` scrolls horizontally. The line numbers gutter participates only in vertical scroll — it has no horizontal content.

### Reading width and WindowSizeClass

```kotlin
val windowSizeClass = currentWindowAdaptiveInfo().windowSizeClass
val effectiveMarginDp = when (windowSizeClass.windowWidthSizeClass) {
    WindowWidthSizeClass.EXPANDED -> maxOf(userPrefs.readingMarginDp, 48)
    WindowWidthSizeClass.MEDIUM   -> maxOf(userPrefs.readingMarginDp, 32)
    else                          -> userPrefs.readingMarginDp
}
```

### Auto-hide lifecycle

Call `viewModel.onBackground()` when the composable pauses:

```kotlin
val lifecycleOwner = LocalLifecycleOwner.current
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
│ TopAppBar                                    │
│  "Skriv"              [+ New]  [📂 Open]    │
├─────────────────────────────────────────────┤
│                                             │
│  LazyColumn of recent files:                │
│  ┌───────────────────────────────────────┐  │
│  │ 📄 filename.txt          2 Jun 2026  │  │
│  │    /Documents/           (greyed if   │  │
│  │                           unavailable)│  │
│  └───────────────────────────────────────┘  │
│  (swipe to dismiss removes from list)       │
│                                             │
│  Empty state (no recents):                  │
│  "No recent files.                          │
│   Tap Open to choose a file."               │
└─────────────────────────────────────────────┘
```

- Tapping a row navigates to `EditorRoute(uriString)`.
- Unavailable files shown with greyed text and a warning icon. Tapping shows a snackbar: "File unavailable. Swipe to remove."
- Swipe-to-dismiss uses `SwipeToDismissBox` from Material 3.
- "Open" button fires `openLauncher`.
- "New" button navigates to `EditorRoute(uriString = null)`.

---

## SettingsScreen layout

Standard `LazyColumn` of `ListItem` rows. Sections separated by `HorizontalDivider`.

**Typography section**
- Font: two-option segmented button (Monospace / Proportional)
- Font size: segmented button (12, 14, 16, 18, 20, 24)
- Line spacing: three-option segmented button (Compact / Normal / Relaxed)
- Reading width: `Slider` 8–64dp, snap points at 8/24/48dp

**Editor section**
- Word wrap: `Switch`
- Line numbers: `Switch`
- Word count: `Switch`
- Auto-hide toolbar: `Switch`

**Appearance section**
- Dark mode: three-option segmented button (System / Light / Dark)

**Files section**
- Default save extension: two-option segmented button (.txt / .md)
- Clear recent files: `TextButton`, confirmation `AlertDialog`

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
    <intent-filter>
        <action android:name="android.intent.action.VIEW"/>
        <action android:name="android.intent.action.EDIT"/>
        <category android:name="android.intent.category.DEFAULT"/>
        <data android:mimeType="application/octet-stream"/>
        <data android:pathPattern=".*\\.txt"/>
        <data android:pathPattern=".*\\.md"/>
    </intent-filter>
</activity>
```

In `MainActivity.onCreate`, if `intent.action` is `ACTION_VIEW` or `ACTION_EDIT`, extract the URI, call `fileRepository.persistPermission(uri)` immediately, then navigate to `RecentsRoute` and immediately forward-navigate to `EditorRoute(uri.toString())`.

### App icon

Use a placeholder adaptive icon during development. The final icon — including the monochrome variant for Android 13+ themed icons — is a separate design deliverable before Play Store submission.

---

## Find and replace

### `FindBarState`

```kotlin
data class FindBarState(
    val isVisible: Boolean = false,
    val query: String = "",
    val replacement: String = "",
    val matchRanges: List<IntRange> = emptyList(), // character offsets into textFieldState.text
    val currentMatchIndex: Int = 0,                // index into matchRanges
    val caseSensitive: Boolean = false
) {
    val matchCount: Int get() = matchRanges.size
}
```

### Find bar layout

The find bar is a `Row` pinned to the bottom of the editor area, visible only when `isVisible` is true. It uses `imePadding()` so it lifts above the keyboard.

### Highlighting

In `EditorScreen`, compute `OutputTransformation` with `remember(findBarState.matchRanges, findBarState.currentMatchIndex)`. Annotate using `matchRanges`: amber (`Color(0xFFFFA000)`) for `matchRanges[currentMatchIndex]`, lighter tint (`Color(0x33FFA000)`) for all others. The transformation is visual-only — it never alters `textFieldState.text`. When the find bar is closed, pass `OutputTransformation.None`.

Match positions are recomputed in the ViewModel on every query change, debounced at 150ms, by scanning `textFieldState.text.toString()` for occurrences of `query` (respecting `caseSensitive`) and storing the results as `List<IntRange>` in `findBarState`.

### Replace and Replace All

Both use `textFieldState.edit {}` — each `edit {}` block becomes a single undoable step.

```kotlin
fun replace(replacement: String) {
    val range = findBarState.value.matchRanges
        .getOrNull(findBarState.value.currentMatchIndex) ?: return
    textFieldState.edit { replace(range.first, range.last + 1, replacement) }
    recomputeMatches()
}

fun replaceAll(replacement: String) {
    val ranges = findBarState.value.matchRanges
    textFieldState.edit {
        // apply in reverse order to preserve earlier offsets
        ranges.sortedByDescending { it.first }.forEach { range ->
            replace(range.first, range.last + 1, replacement)
        }
    }
    recomputeMatches()
}
```

---

## Error states and UI

| Situation | UI response |
|---|---|
| Save fails | `Snackbar`: "Could not save: [reason]" with "Retry" action. Never silent. |
| File opened read-only | `Snackbar` on open: "File opened read-only. Use Open from menu to enable editing." Save disabled. Checkmark always grey. |
| Encoding error on open | `AlertDialog`: "This file cannot be opened. It does not appear to be a UTF-8 text file." OK → navigate to recents. |
| File too large (>5MB) | `AlertDialog`: "This file is too large to edit (over 5MB). Skriv is designed for text files." OK → navigate to recents. |
| Recent file unavailable | Row greyed with warning icon. Snackbar on tap. Swipe to remove. |
| Back with unsaved changes | `AlertDialog`: "Save changes?" — "Save" / "Discard" / "Cancel". |

---

## Save flow (complete)

```
User taps save (or checkmark, or back gesture confirms save)
    │
    ├── uri is null (new document)
    │       └── emit createDocumentRequest(suggestedFilename)
    │               └── EditorScreen launches CreateDocument picker
    │                   └── onCreateDocumentResult(uri):
    │                         persistPermission(uri)
    │                         writeFile(uri, textFieldState.text.toString(), hadBom)
    │                         recentsRepository.recordOpen(uri, ...)
    │                         lastSavedContent = textFieldState.text.toString()
    │                         documentState.isSaved = true
    │
    └── uri is not null
            ├── hasWritePermission(uri) == true
            │       └── writeFile(uri, textFieldState.text.toString(), hadBom)
            │               ├── success: lastSavedContent updated; isSaved = true
            │               └── failure: Snackbar with error
            │
            └── hasWritePermission(uri) == false
                    └── Snackbar: "File opened read-only. Use Open from menu to enable editing."
```

---

## Open flow (complete)

```
User taps Open (in-app menu or recents screen)
    │
    ├── From in-app menu: launch openLauncher
    │       mimeTypes: ["text/plain", "text/markdown", "application/octet-stream"]
    │
    └── On result (uri received):
            1. persistPermission(uri)                              ← first, always
            2. readFile(uri) on IO dispatcher
            3. On success:
                   a. recentsRepository.recordOpen(uri, displayName, lastModified)
                   b. textFieldState.edit { replace(0, length, content) }
                      textFieldState.undoState.clearHistory()
                   c. documentState = DocumentState(uri, displayName, isSaved=true, hadBom=hadBom, ...)
                   d. Restore cursorPosition and scrollOffset from recents DB
            4. On failure: show error dialog (see Error states table)
```

---

## Print implementation

```kotlin
fun printDocument(context: Context, textFieldState: TextFieldState, displayName: String) {
    val content = textFieldState.text.toString()
    val printManager = context.getSystemService(Context.PRINT_SERVICE) as PrintManager
    val htmlContent = "<html><body><pre>${content.escapeHtml()}</pre></body></html>"
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

## Toolbar auto-hide implementation

Use `TopAppBarDefaults.enterAlwaysScrollBehavior()`. Connect `scrollBehavior.nestedScrollConnection` to the root layout via `Modifier.nestedScroll(...)` and to the `TopAppBar`.

When `scrollBehavior.state.collapsedFraction == 1f`, show the floating checkmark button and hide it otherwise.

---

## Transitions

- `RecentsScreen` → `EditorScreen`: `SharedTransitionLayout` with `sharedElement` on the tapped list item and editor surface.
- `EditorScreen` → `SettingsScreen`: `slideInHorizontally` from end / `slideOutHorizontally` to start. Reverse on back.
- Toolbar show/hide: animated via `TopAppBarScrollBehavior`, 200ms, `FastOutSlowInEasing`.
- Find bar: `AnimatedVisibility` with `slideInVertically` from bottom.

---

## Edge-to-edge and insets

```kotlin
// MainActivity.onCreate — before setContent
enableEdgeToEdge()
```

On API 35 (target SDK), edge-to-edge is platform-enforced. `enableEdgeToEdge()` ensures consistent behaviour on API 29–34.

Apply on all screens:

```kotlin
Modifier.windowInsetsPadding(WindowInsets.systemBars)
```

Text field uses `Modifier.imePadding()`. Find bar container uses `imePadding()` so it lifts with the keyboard.

---

## Haptic feedback

Use Compose's haptic API — no `View` reference needed:

```kotlin
val haptic = LocalHapticFeedback.current
// In FloatingCheckmarkButton onClick when saving:
haptic.performHapticFeedback(HapticFeedbackType.Confirm)  // maps to CONFIRM on API 30+
```

`HapticFeedbackType.Confirm` falls back gracefully on API 29 — no manual version check required.

---

## Dependencies

```kotlin
dependencies {
    implementation(platform(libs.compose.bom))
    implementation("androidx.compose.ui:ui")
    implementation("androidx.compose.material3:material3")
    implementation("androidx.compose.ui:ui-tooling-preview")

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
}
```

No Firebase, no Crashlytics, no analytics SDK.

---

## What not to build

- Markdown rendering or preview
- A folder browser or file tree
- File management (rename, delete, duplicate)
- Multiple open files or tabs
- Syntax highlighting
- Custom color themes
- Cloud sync
- Widgets
- Any network call of any kind
- Any analytics or crash reporting

If a feature is not described in this document, do not build it.
