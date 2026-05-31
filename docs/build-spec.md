# Skriv — Android app build specification
*Agent prompt v1.2*

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
    alias(libs.plugins.kotlin.compose) apply false   // Compose compiler plugin
    alias(libs.plugins.ksp) apply false
}
```

In `app/build.gradle.kts`:

```kotlin
plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)   // replaces composeOptions block entirely
    alias(libs.plugins.ksp)
}

android {
    compileSdk = 35
    defaultConfig { minSdk = 29; targetSdk = 35 }
    buildFeatures { compose = true }
    // No composeOptions block needed with Kotlin 2.x
}
```

### Gradle Version Catalog (`libs.versions.toml`)

Android Studio generates projects with a version catalog by default. Use it — do not hardcode dependency strings in build files.

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
ksp = "2.1.0-1.0.29"

[plugins]
android-application = { id = "com.android.application", version.ref = "agp" }
kotlin-android = { id = "org.jetbrains.kotlin.android", version.ref = "kotlin" }
kotlin-compose = { id = "org.jetbrains.kotlin.plugin.compose", version.ref = "kotlin" }
kotlin-serialization = { id = "org.jetbrains.kotlin.plugin.serialization", version.ref = "kotlin" }
ksp = { id = "com.google.devtools.ksp", version.ref = "ksp" }

[libraries]
compose-bom = { group = "androidx.compose", name = "compose-bom", version.ref = "compose-bom" }
# ... individual compose libraries reference the BOM, no version needed
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

Apply `kotlin.plugin.serialization` Gradle plugin and add `kotlinx-serialization-json` to enable `@Serializable` on route classes.

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
- When launched via an external intent (tapping a `.txt` or `.md` file in Files, Drive, etc.), the app starts at `RecentsScreen` and immediately navigates forward to `EditorScreen` with the incoming URI, so the back stack is always rooted at `RecentsScreen`.
- `SettingsScreen` is pushed onto the back stack from `EditorScreen` via the overflow menu.
- Navigation Compose 2.8+ handles predictive back animations between destinations automatically — no manual setup required for nav transitions.

### Back behaviour

- From `EditorScreen` with no unsaved changes: navigate back to `RecentsScreen`.
- From `EditorScreen` with unsaved changes: use `BackHandler(enabled = !documentState.isSaved)` to intercept, then show an `AlertDialog` ("Save" / "Discard" / "Cancel"). "Save" writes the file then navigates; "Discard" navigates without saving; "Cancel" dismisses the dialog.
- From `SettingsScreen`: pop back to `EditorScreen`.
- `BackHandler` is composable-native and integrates with the Android 13+ predictive back system without additional wiring.

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

In `MainActivity`, resolve `darkTheme` from the user's `dark_mode` preference before calling `SkrivTheme`:

```kotlin
val darkTheme = when (userPrefs.darkMode) {
    "dark"  -> true
    "light" -> false
    else    -> isSystemInDarkTheme()
}
SkrivTheme(darkTheme = darkTheme) {
    SkrivNavGraph(navController = rememberNavController(), ...)
}
```

Dynamic color (`dynamicLightColorScheme` / `dynamicDarkColorScheme`) is available on API 31+ and derives colors from the user's wallpaper automatically. On API 29–30, `darkColorScheme()` / `lightColorScheme()` with neutral defaults is used as a fallback.

---

## Data model

### Room database: `SkrivDatabase`

Single table: `recent_files`

```kotlin
@Entity(tableName = "recent_files")
data class RecentFileEntity(
    @PrimaryKey val uri: String,          // content:// URI as string
    val displayName: String,              // filename shown in UI
    val lastAccessedAt: Long,             // epoch millis
    val lastModifiedAt: Long?,            // epoch millis, nullable
    val cursorPosition: Int,              // restore on reopen
    val scrollOffset: Int,               // restore on reopen
    val isAvailable: Boolean = true       // false if permission expired
)
```

Max 20 rows. On insert, if count exceeds 20, delete the oldest by `lastAccessedAt`. On open, update `lastAccessedAt` and `isAvailable`. On permission error, set `isAvailable = false`.

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

    @Query("UPDATE recent_files SET cursorPosition = :pos, scrollOffset = :offset WHERE uri = :uri")
    suspend fun updateScrollState(uri: String, pos: Int, offset: Int)

    @Query("UPDATE recent_files SET isAvailable = :available WHERE uri = :uri")
    suspend fun updateAvailability(uri: String, available: Boolean)
}
```

### User preferences: DataStore

Use Jetpack DataStore (Preferences) for all user settings. Key names:

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

### Document state: `DocumentState`

```kotlin
data class DocumentState(
    val uri: Uri?,                        // null for new unsaved document
    val displayName: String,              // shown in toolbar
    val isSaved: Boolean,                 // false when editing content differs from last save
    val isReadOnly: Boolean,              // true if URI has no write permission
    val isLoading: Boolean,
    val error: String?,                   // non-null when a save/load error occurred
    val hadBom: Boolean = false           // true if original file began with a UTF-8 BOM
)
```

Current editing content lives in `viewModel.textFieldState` (a `TextFieldState`), not in `DocumentState`. The ViewModel tracks a private `lastSavedContent: String`; `isSaved` is recomputed whenever `textFieldState.text` changes by comparing against that value.

### User preferences data class: `UserPrefsData`

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

---

## EditorViewModel

`EditorViewModel` holds all editor state. Inject `FileRepository`, `RecentsRepository`, and `UserPreferences`.

```kotlin
class EditorViewModel(
    private val fileRepository: FileRepository,
    private val recentsRepository: RecentsRepository,
    private val prefs: UserPreferences
) : ViewModel() {

    val documentState: StateFlow<DocumentState>

    // Owns current text content and full undo/redo history.
    // Use textFieldState.text.toString() when the raw string is needed (save, print).
    val textFieldState: TextFieldState = TextFieldState()

    val findBarState: StateFlow<FindBarState>
    val userPreferences: StateFlow<UserPrefsData>

    // Expose createDocumentRequest as SharedFlow; EditorScreen collects and launches picker.
    val createDocumentRequest: SharedFlow<String>  // suggested filename

    // Called on launch with URI from intent or recents.
    // Populates textFieldState and clears undo history so the user
    // cannot undo past the point of file load.
    fun loadFile(uri: Uri, contentResolver: ContentResolver)

    // Called when EditorScreen receives a URI from the create-document picker
    fun onCreateDocumentResult(uri: Uri, contentResolver: ContentResolver)

    // Undo/redo delegate to the platform; no snapshot stack needed.
    fun undo() = textFieldState.undoState.undo()
    fun redo() = textFieldState.undoState.redo()
    val canUndo: Boolean get() = textFieldState.undoState.canUndo
    val canRedo: Boolean get() = textFieldState.undoState.canRedo

    // Save to original URI; emits createDocumentRequest if uri is null
    fun save(contentResolver: ContentResolver)

    // New document
    fun newDocument()

    // Called when app goes to background
    fun onBackground(contentResolver: ContentResolver)

    // Persist cursor and scroll position
    fun saveScrollState(cursorPos: Int, scrollOffset: Int)

    // Find and replace
    fun onFindQueryChanged(query: String)
    fun findNext()
    fun findPrevious()
    fun replace(replacement: String)
    fun replaceAll(replacement: String)
    fun closeFindBar()
}
```

`isSaved` is computed by observing `textFieldState` via `snapshotFlow { textFieldState.text.toString() }` and comparing against a private `lastSavedContent: String`. It is `true` on file load and after a successful save; `false` whenever the current text differs from that value.

---

## FileRepository: SAF operations

```kotlin
class FileRepository(private val context: Context) {

    // Read file content as UTF-8 string.
    // Detects and strips UTF-8 BOM; sets hadBom = true in the returned pair.
    suspend fun readFile(uri: Uri): Result<Pair<String, Boolean>>  // content, hadBom

    // Write string to URI in truncate mode, UTF-8.
    // Prepends BOM if hadBom is true.
    suspend fun writeFile(uri: Uri, content: String, hadBom: Boolean): Result<Unit>

    // Persist URI permissions for future sessions
    fun persistPermission(uri: Uri)

    // Check whether write permission is still valid
    fun hasWritePermission(uri: Uri): Boolean

    // Get display name from URI
    fun getDisplayName(uri: Uri): String

    // Get last modified timestamp
    fun getLastModified(uri: Uri): Long?
}
```

Implementation notes:
- `readFile`: use `contentResolver.openInputStream(uri)`, read fully, decode as UTF-8. Check for BOM (`﻿`) at the start; strip it and return `hadBom = true`. Return `Result.failure` with a descriptive exception on any error.
- `writeFile`: use `contentResolver.openOutputStream(uri, "wt")` (truncate mode). If `hadBom` is true, prepend `﻿` before writing. Files without a BOM are written without one.
- `persistPermission`: call `context.contentResolver.takePersistableUriPermission(uri, FLAG_GRANT_READ_URI_PERMISSION or FLAG_GRANT_WRITE_URI_PERMISSION)`. Wrap in try/catch; log but do not crash if the permission cannot be persisted.
- `hasWritePermission`: check `context.contentResolver.persistedUriPermissions` for a matching URI with write flag.

---

## File picker launchers

Use `rememberLauncherForActivityResult` with typed contracts. Never call `startActivityForResult` directly.

**Opening a file** (in `RecentsScreen` and editor overflow menu):

```kotlin
val openLauncher = rememberLauncherForActivityResult(
    ActivityResultContracts.OpenDocument()
) { uri ->
    uri?.let { viewModel.loadFile(it, context.contentResolver) }
}
// Launch:
openLauncher.launch(arrayOf("text/plain", "text/markdown", "application/octet-stream"))
```

**Saving a new document** — the ViewModel cannot hold a launcher directly. Use a `SharedFlow` signal:

```kotlin
// In EditorScreen
val createLauncher = rememberLauncherForActivityResult(
    ActivityResultContracts.CreateDocument("text/plain")
) { uri ->
    uri?.let { viewModel.onCreateDocumentResult(it, context.contentResolver) }
}
LaunchedEffect(Unit) {
    viewModel.createDocumentRequest.collect { suggestedFilename ->
        createLauncher.launch(suggestedFilename)
    }
}
```

When the user taps Save on a new document, the ViewModel emits `createDocumentRequest`. The screen collects it, launches the picker, and returns the chosen URI via `onCreateDocumentResult`.

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

- `TopAppBar` uses `TopAppBarScrollBehavior` with `enterAlwaysScrollBehavior()` so it hides on scroll up and reappears on scroll down.
- When toolbar is hidden, show a floating `FloatingCheckmarkButton` composable: 32x32dp, positioned top-end with 12dp padding, respecting window insets. Grey (`MaterialTheme.colorScheme.outline`) when saved, amber (`Color(0xFFFFA000)`) when unsaved. On tap when amber: call `viewModel.save()`. On tap when grey: show toolbar by resetting scroll behavior state.
- Text field: `BasicTextField` with `state = viewModel.textFieldState`. Pass `outputTransformation` derived from `findBarState` for find/replace highlight spans (see Find and replace). Apply horizontal padding from `readingMarginDp`, `lineHeight` from `lineSpacing` preference, font family from `fontMonospace` preference.
- Scrolling: the `BasicTextField` uses `Modifier.verticalScroll(scrollState)` where `scrollState = rememberScrollState()`. Do **not** wrap it in a `LazyColumn`. This is a single non-lazy scrollable, which is correct for files up to 1MB.
- Line numbers gutter: a `Column` aligned left, driven by the **same `scrollState` instance** as the `BasicTextField`. The gutter scrolls in lockstep with the text field. Visible only when `lineNumbers` preference is true. Width adapts to digit count.
- Word count bar: a `Text` composable below the text field (above the FindBar), visible only when `wordCountVisible` is true. Shows "Words: 1,234  Characters: 6,789". Updates debounced at 300ms using `snapshotFlow` + `debounce`.

### Reading width and WindowSizeClass

Use `currentWindowAdaptiveInfo().windowSizeClass` from `androidx.window` to determine the active window size class. Apply it to the reading margin:

```kotlin
val windowSizeClass = currentWindowAdaptiveInfo().windowSizeClass
val effectiveMarginDp = when {
    windowSizeClass.windowWidthSizeClass == WindowWidthSizeClass.EXPANDED ->
        maxOf(userPrefs.readingMarginDp, 48)  // enforce minimum wide margin on tablets
    windowSizeClass.windowWidthSizeClass == WindowWidthSizeClass.MEDIUM ->
        maxOf(userPrefs.readingMarginDp, 32)
    else -> userPrefs.readingMarginDp
}
```

This replaces any orientation-check heuristics. `WindowWidthSizeClass.EXPANDED` covers tablets and landscape phones; `MEDIUM` covers unfolded foldables and some landscape phones.

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

- Tapping a row navigates to `EditorScreen` with that URI.
- Unavailable files (permission expired) shown with greyed text and a warning icon. Tapping shows a snackbar: "File unavailable. Swipe to remove."
- Swipe-to-dismiss uses `SwipeToDismissBox` from Material 3.
- "Open" button fires the `openLauncher` (see File picker launchers).
- "New" button calls `viewModel.newDocument()` and navigates to `EditorScreen(uriString = null)`.

---

## SettingsScreen layout

Standard `LazyColumn` of settings items using Material 3 `ListItem`. Sections separated by `HorizontalDivider` with a section label.

**Typography section**
- Font: two-option segmented button (Monospace / Proportional)
- Font size: segmented button with options 12, 14, 16, 18, 20, 24
- Line spacing: three-option segmented button (Compact / Normal / Relaxed)
- Reading width: `Slider` from 8dp to 64dp, labelled Narrow → Wide, with three snap points at 8, 24, 48dp for Narrow/Medium/Wide

**Editor section**
- Word wrap: `Switch`
- Line numbers: `Switch`
- Word count: `Switch`
- Auto-hide toolbar: `Switch`

**Appearance section**
- Dark mode: three-option segmented button (System / Light / Dark)

**Files section**
- Default save extension: two-option segmented button (.txt / .md)
- Clear recent files: `TextButton` labelled "Clear all recent files", triggers confirmation `AlertDialog`

---

## Intent filters (AndroidManifest.xml)

```xml
<activity android:name=".MainActivity" ...>

    <!-- Launcher -->
    <intent-filter>
        <action android:name="android.intent.action.MAIN"/>
        <category android:name="android.intent.category.LAUNCHER"/>
    </intent-filter>

    <!-- Open .txt files -->
    <intent-filter>
        <action android:name="android.intent.action.VIEW"/>
        <action android:name="android.intent.action.EDIT"/>
        <category android:name="android.intent.category.DEFAULT"/>
        <data android:mimeType="text/plain"/>
    </intent-filter>

    <!-- Open .md files -->
    <intent-filter>
        <action android:name="android.intent.action.VIEW"/>
        <action android:name="android.intent.action.EDIT"/>
        <category android:name="android.intent.category.DEFAULT"/>
        <data android:mimeType="text/markdown"/>
    </intent-filter>

    <!-- Catch-all for files that report octet-stream -->
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

In `MainActivity.onCreate`, check `intent.action == Intent.ACTION_VIEW || ACTION_EDIT`. If so, extract the URI, call `persistPermission(uri)` immediately, then navigate to `RecentsRoute` and immediately forward-navigate to `EditorRoute(uri.toString())`, so that `RecentsScreen` is always in the back stack.

### App icon

Use a simple placeholder adaptive icon (document glyph with a cursor mark) during development. The final icon — including the monochrome variant for Android 13+ themed icons — is a separate design deliverable required before Play Store submission. Do not block development on it.

---

## Find and replace

`FindBarState` data class:

```kotlin
data class FindBarState(
    val isVisible: Boolean = false,
    val query: String = "",
    val replacement: String = "",
    val matchCount: Int = 0,
    val currentMatchIndex: Int = 0,
    val caseSensitive: Boolean = false
)
```

Find bar appears above the system keyboard. Implement using `imePadding()` on the root layout so the find bar lifts with the keyboard. The find bar is a `Row` pinned to the bottom of the editor area, visible only when `FindBarState.isVisible` is true.

Highlight matches using `OutputTransformation` on `BasicTextField`. In `EditorScreen`, compute the transformation with `remember(findBarState.query, findBarState.currentMatchIndex, findBarState.caseSensitive)`. Inside the transformation, annotate the raw text with background spans: amber (`Color(0xFFFFA000)`) for the current match, a lighter tint (`Color(0x33FFA000)`) for all other matches. The transformation is purely visual — it never alters `textFieldState.text`. When the find bar is closed, pass `OutputTransformation.None`. Match positions are recomputed in the ViewModel on every query change, debounced at 150ms, and exposed via `findBarState`.

---

## Error states and UI

| Situation | UI response |
|---|---|
| Save fails (any reason) | `Snackbar` with message "Could not save: [reason]" and "Retry" action. Never silent. |
| File opened read-only (no write permission) | `Snackbar` on open: "File opened read-only. Use Open from menu to enable editing." Toolbar save button disabled. Checkmark always grey. |
| Encoding error on open | `AlertDialog`: "This file cannot be opened. It does not appear to be a UTF-8 text file." Single "OK" button. Navigate back to recents. |
| File too large (>5MB) | `AlertDialog`: "This file is too large to edit (over 5MB). Skriv is designed for text files." Single "OK" button. |
| Recent file unavailable | Row shown greyed with warning icon. Snackbar on tap. Swipe to remove. |
| Back with unsaved changes | `AlertDialog`: "Save changes?". Buttons: "Save", "Discard", "Cancel". |

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
    │                         upsert to recents
    │                         lastSavedContent = textFieldState.text.toString()
    │                         documentState.isSaved = true
    │
    └── uri is not null
            ├── hasWritePermission(uri) == true
            │       └── writeFile(uri, textFieldState.text.toString(), hadBom)
            │               ├── success: lastSavedContent updated; isSaved = true
            │               └── failure: show snackbar with error
            │
            └── hasWritePermission(uri) == false
                    └── show snackbar: "File opened read-only.
                                       Use Open from menu to enable editing."
```

---

## Open flow (complete)

```
User taps Open (in-app menu or recents screen)
    │
    ├── From in-app menu: launch openLauncher
    │       (ActivityResultContracts.OpenDocument)
    │       mimeTypes: ["text/plain", "text/markdown", "application/octet-stream"]
    │
    └── On result (uri received):
            1. Call takePersistableUriPermission(uri, READ | WRITE)  ← first, always
            2. Call readFile(uri) on IO dispatcher
            3. On success:
                   a. Upsert to recents (displayName, lastAccessedAt, isAvailable=true)
                   b. Populate textFieldState: textFieldState.edit { replace(0, length, content) }
                      then call textFieldState.undoState.clearHistory()
                   c. Set documentState with uri, hadBom, isSaved=true
                   d. Restore cursorPosition and scrollOffset from recents DB
            4. On failure:
                   a. Show appropriate error dialog (see Error states table)
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

Call from overflow menu "Print" item.

---

## Toolbar auto-hide implementation

Use `TopAppBarDefaults.enterAlwaysScrollBehavior()` with a `NestedScrollConnection`. Connect the `scrollBehavior` to both the `TopAppBar` and the `BasicTextField` scroll state via `Modifier.nestedScroll(scrollBehavior.nestedScrollConnection)`.

When toolbar is collapsed (`scrollBehavior.state.collapsedFraction == 1f`), show the floating checkmark button. When expanded, hide it.

---

## Transitions

- `RecentsScreen` → `EditorScreen`: use `SharedTransitionLayout` with `sharedElement` modifier on the tapped list item and the editor surface. Material 3 container transform pattern.
- `EditorScreen` → `SettingsScreen`: `slideInHorizontally` from end, `slideOutHorizontally` to start. Reverse on back.
- Toolbar show/hide: animated via `TopAppBarScrollBehavior`, 200ms, `FastOutSlowInEasing`.
- Find bar appear/disappear: `AnimatedVisibility` with `slideInVertically` from bottom.

---

## Edge-to-edge and insets

In `MainActivity.onCreate`:

```kotlin
enableEdgeToEdge()  // Activity 1.9+; replaces WindowCompat.setDecorFitsSystemWindows()
```

On API 35 (our target SDK), edge-to-edge is enforced by the platform regardless — `enableEdgeToEdge()` ensures consistent behaviour on API 29–34 as well.

Apply insets on all screens:

```kotlin
Modifier.windowInsetsPadding(WindowInsets.systemBars)
```

The text field area uses `Modifier.imePadding()` so content is not obscured by the keyboard. The find bar uses `imePadding()` on its container so it lifts with the keyboard.

---

## Haptic feedback

In the floating checkmark button `onClick` when saving:

```kotlin
view.performHapticFeedback(HapticFeedbackConstants.CONFIRM) // API 30+
if (Build.VERSION.SDK_INT < 30) {
    view.performHapticFeedback(HapticFeedbackConstants.VIRTUAL_KEY)
}
```

---

## Dependencies

```kotlin
dependencies {
    // Jetpack Compose BOM — pins all compose library versions
    implementation(platform(libs.compose.bom))
    implementation("androidx.compose.ui:ui")
    implementation("androidx.compose.material3:material3")
    implementation("androidx.compose.ui:ui-tooling-preview")

    // Navigation (2.8+ for type-safe routes and SharedTransitionLayout)
    implementation(libs.navigation.compose)

    // Lifecycle + ViewModel
    implementation(libs.lifecycle.viewmodel.compose)
    implementation(libs.lifecycle.runtime.compose)

    // Room (ksp, not kapt)
    implementation(libs.room.runtime)
    implementation(libs.room.ktx)
    ksp(libs.room.compiler)

    // DataStore
    implementation(libs.datastore.preferences)

    // Coroutines
    implementation(libs.coroutines.android)

    // Core KTX + Activity (enableEdgeToEdge, rememberLauncherForActivityResult)
    implementation(libs.core.ktx)
    implementation(libs.activity.compose)

    // Window size classes (WindowSizeClass, currentWindowAdaptiveInfo)
    implementation(libs.window)

    // Serialization for type-safe nav routes
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.7.3")
}
```

No other third-party dependencies. No Firebase, no Crashlytics, no analytics SDK.

---

## What not to build

Do not build any of the following. They are explicitly out of scope:

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
