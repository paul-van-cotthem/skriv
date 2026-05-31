# Skriv — Android app build specification
*Agent prompt v1.1*

---

## What you are building

An Android plain text file editor called **Skriv**. It opens, edits, and saves `.txt` and `.md` files stored anywhere on the Android file system or in cloud storage. It does not store files internally, render Markdown, or require an account. Every file the user edits remains a real file in the real file system.

The app is written in **Kotlin + Jetpack Compose**. Minimum SDK: **API 29 (Android 10)**. Target SDK: **API 35**. Use **Material 3** components and theming throughout.

---

## Hard constraints — never violate these

1. **No broad storage permissions.** Do not declare `READ_EXTERNAL_STORAGE`, `WRITE_EXTERNAL_STORAGE`, or `MANAGE_APP_ALL_FILES_ACCESS_PERMISSION` in the manifest. File access is exclusively via Android's Storage Access Framework (SAF) using `content://` URIs.
2. **No network permissions.** The app never connects to the internet. Do not declare `INTERNET` or any network permission.
3. **No proprietary file format.** Every document is a `.txt` or `.md` file. Nothing is stored in a private app database except the recents list and user preferences.
4. **No ads, no analytics, no third-party SDKs** beyond what is listed in the dependencies section below.
5. **UTF-8 only.** All files are read and written as UTF-8. Never silently substitute or drop characters.
6. **All file I/O off the main thread.** Use coroutines. Never block the UI thread with file operations.

---

## Project structure

```
com.skriv.app
├── MainActivity.kt                  // single Activity, hosts NavHost
├── navigation/
│   └── SkrivNavGraph.kt             // NavHost with three destinations
├── ui/
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
- When launched via an external intent (tapping a `.txt` or `.md` file in Files, Drive, etc.), the app starts at `RecentsScreen` and immediately navigates forward to `EditorScreen` with the incoming URI. This ensures the back stack is always rooted at `RecentsScreen` and back navigation is consistent.
- `SettingsScreen` is pushed onto the back stack from `EditorScreen` via the overflow menu.
- Navigation uses `NavHost` with `composable()` destinations. Pass `Uri` as a string argument to `EditorScreen`.

### Back behaviour

- From `EditorScreen` with no unsaved changes: navigate back to `RecentsScreen`.
- From `EditorScreen` with unsaved changes: intercept back with `BackHandler`, show an `AlertDialog` ("Save" / "Discard" / "Cancel"). "Save" writes the file then navigates; "Discard" navigates without saving; "Cancel" dismisses the dialog and keeps the user in the editor.
- From `SettingsScreen`: pop back to `EditorScreen`.
- Android 13+ predictive back: register `OnBackPressedCallback` for the editor. The system shows its standard swipe-back animation preview; the `AlertDialog` fires on commit if unsaved changes exist. No custom preview UI is implemented.

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

Use Jetpack DataStore (Proto or Preferences) for all user settings. Key names:

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

---

## EditorViewModel

`EditorViewModel` holds all editor state. Inject `FileRepository`, `RecentsRepository`, and `UserPreferences`.

Key responsibilities:

```kotlin
class EditorViewModel(
    private val fileRepository: FileRepository,
    private val recentsRepository: RecentsRepository,
    private val prefs: UserPreferences
) : ViewModel() {

    val documentState: StateFlow<DocumentState>

    // Owns current text content and full undo/redo history.
    // Use textFieldState.text.toString() when the raw string is needed (save, print).
    val textFieldState: TextFieldState

    val findBarState: StateFlow<FindBarState>
    val userPreferences: StateFlow<UserPrefsData>

    // Called on launch with URI from intent or recents.
    // Populates textFieldState and clears undo history so the user
    // cannot undo past the point of file load.
    fun loadFile(uri: Uri, contentResolver: ContentResolver)

    // Undo/redo delegate to the platform; no snapshot stack needed.
    fun undo() = textFieldState.undoState.undo()
    fun redo() = textFieldState.undoState.redo()
    val canUndo: Boolean get() = textFieldState.undoState.canUndo
    val canRedo: Boolean get() = textFieldState.undoState.canRedo

    // Save to original URI
    fun save(contentResolver: ContentResolver)

    // Save As: fires ACTION_CREATE_DOCUMENT result handling
    fun saveAs(uri: Uri, contentResolver: ContentResolver)

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
    // Detects and strips UTF-8 BOM (﻿); sets hadBom = true in the returned pair.
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
- `readFile`: use `contentResolver.openInputStream(uri)`, read fully, decode as UTF-8. Check for BOM (`﻿`) at the start; strip it from the returned content and return `hadBom = true`. Return `Result.failure` with a descriptive exception on any error.
- `writeFile`: use `contentResolver.openOutputStream(uri, "wt")` (truncate mode). If `hadBom` is true, prepend `﻿` before writing. Encode as UTF-8. Files without a BOM are written without one.
- `persistPermission`: call `context.contentResolver.takePersistableUriPermission(uri, Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_GRANT_WRITE_URI_PERMISSION)`. Wrap in try/catch; log but do not crash if the permission cannot be persisted.
- `hasWritePermission`: check `context.contentResolver.persistedUriPermissions` for a matching URI with write flag.

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
- "Open" button fires `ACTION_OPEN_DOCUMENT` launcher.
- "New" button calls `viewModel.newDocument()` and navigates to `EditorScreen` with null URI.

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

In `MainActivity.onCreate`, check `intent.action == Intent.ACTION_VIEW || ACTION_EDIT`. If so, extract the URI, call `persistPermission(uri)` immediately, then navigate to `RecentsScreen` and immediately forward-navigate to `EditorScreen` with the URI, so that RecentsScreen is always in the back stack.

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
| New document back with unsaved changes | `AlertDialog`: "Save changes?". Buttons: "Save", "Discard", "Cancel". |

---

## Save flow (complete)

```
User taps save (or checkmark, or back gesture confirms save)
    │
    ├── uri is null (new document)
    │       └── launch ACTION_CREATE_DOCUMENT
    │               └── on result: persistPermission(uri)
    │                             writeFile(uri, textFieldState.text.toString(), hadBom)
    │                             upsert to recents
    │                             documentState.isSaved = true
    │
    └── uri is not null
            ├── hasWritePermission(uri) == true
            │       └── writeFile(uri, textFieldState.text.toString(), hadBom)
            │               ├── success: documentState.isSaved = true
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
    ├── From in-app menu: launch ACTION_OPEN_DOCUMENT
    │       flags: FLAG_GRANT_READ_URI_PERMISSION | FLAG_GRANT_WRITE_URI_PERMISSION
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

In `MainActivity`:
```kotlin
WindowCompat.setDecorFitsSystemWindows(window, false)
```

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
// Fallback for API 29:
if (Build.VERSION.SDK_INT < 30) {
    view.performHapticFeedback(HapticFeedbackConstants.VIRTUAL_KEY)
}
```

---

## Dependencies (gradle)

```kotlin
dependencies {
    // Jetpack Compose BOM
    implementation(platform("androidx.compose:compose-bom:2024.09.00"))
    implementation("androidx.compose.ui:ui")
    implementation("androidx.compose.material3:material3")
    implementation("androidx.compose.ui:ui-tooling-preview")

    // Navigation (2.8+ required for SharedTransitionLayout / shared element support)
    implementation("androidx.navigation:navigation-compose:2.8.0")

    // Lifecycle + ViewModel
    implementation("androidx.lifecycle:lifecycle-viewmodel-compose:2.7.0")
    implementation("androidx.lifecycle:lifecycle-runtime-compose:2.7.0")

    // Room
    implementation("androidx.room:room-runtime:2.6.1")
    implementation("androidx.room:room-ktx:2.6.1")
    ksp("androidx.room:room-compiler:2.6.1")

    // DataStore
    implementation("androidx.datastore:datastore-preferences:1.1.1")

    // Coroutines
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.8.0")

    // Window insets animation
    implementation("androidx.core:core-ktx:1.13.1")

    // Activity result APIs
    implementation("androidx.activity:activity-compose:1.9.0")
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
