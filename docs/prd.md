# Product requirements document — Android plain text editor
*Version 1.1 — draft*

---

## 1. Purpose and context

This document defines the requirements for a plain text file editor for Android. The app targets writers, journalists, students, and technically-minded users who work with `.txt` and `.md` files as files — stored on the device file system or in cloud storage — and need to edit them reliably without friction, proprietary formats, or mandatory cloud accounts.

The primary competitor is Simple Text Editor by Maxistar (TextPad), which holds 500K+ downloads despite unresolved data-loss bugs, a broken recents list, and no write access when files are opened from the Android file picker. The opportunity is to build what TextPad is trying to be, executed correctly from the ground up.

---

## 2. Goals

**Product goals**

- Be the most reliable plain text editor on Android: no data loss, no silent corruption, no broken saves.
- Be a first-class Android file citizen: open, edit, and save back to any file from any source without workarounds.
- Be visually calm and well-designed: a writing environment, not a utility.

**Non-goals for v1**

- Markdown rendering or preview.
- Cloud sync (beyond what the OS file system already provides).
- Folder browser or file manager functionality.
- Proprietary file format or internal note store.
- Foldable-optimised layouts (post-v1).
- Collaboration or multi-user editing.

---

## 3. Target users

**Primary:** Writers and journalists who work across devices. They need files to be files — portable `.txt` or `.md` documents that open in any editor on any platform. They are frustrated by apps that silo content.

**Secondary:** Students who draft on mobile and need interoperability with desktop tools. They use Google Drive or local storage and switch between phone and laptop.

**Tertiary:** Technical users who maintain plain text for structured personal data (logs, diaries, notes, configs). They understand the file system, want direct access, and have zero tolerance for data loss or silent encoding errors.

All three groups are underserved by the current Play Store landscape, which offers either code editors (too complex), abandoned utilities (broken on modern Android), or apps with aggressive ads.

---

## 4. Core principles

**Reliability first.** Every save must succeed or fail loudly. Silent data loss or corruption is a fatal defect, not a bug to be triaged.

**File citizenship.** The app does not own files. It opens them, edits them, and saves them back where they came from. No proprietary format. No "export" instead of save.

**Minimal surface.** The writing surface is the product. Every UI element competes with it. Nothing ships unless it earns its screen space.

**No ads, ever.** A writing environment with ads is a broken product. Monetisation is a one-time purchase.

**SAF-native.** The app is built on Android's Storage Access Framework from day one. No `MANAGE_APP_ALL_FILES_ACCESS_PERMISSION`. No broad storage permission requests. No workarounds that break on future Android versions.

---

## 5. Functional requirements

### 5.1 File operations

**Open**

- The app registers intent filters for `text/plain`, `text/markdown`, and `application/octet-stream` MIME types, plus `.txt` and `.md` extensions explicitly. This ensures files open correctly regardless of how the file manager reports the MIME type.
- Tapping a `.txt` or `.md` file in any file manager, Google Drive, or cloud provider opens the app directly.
- The in-app menu provides an "Open" action that fires `ACTION_OPEN_DOCUMENT` with `FLAG_GRANT_READ_URI_PERMISSION | FLAG_GRANT_WRITE_URI_PERMISSION`. The system picker handles all provider navigation.
- On receiving a URI from any source, `takePersistableUriPermission()` is called immediately with both read and write flags before any other operation. This is non-negotiable; it is the mechanism that makes recents and write-back work across sessions.

**Save**

- The app saves in place to the original URI via `ContentResolver.openOutputStream()` in truncate mode.
- Save is triggered by: the save action in the overflow menu, the system back gesture when unsaved changes exist (with confirmation), and automatically when the app moves to the background with unsaved changes.
- Auto-save on backgrounding is silent: no toast, no dialog. The file is written and the checkmark updates when the app returns to the foreground.
- Auto-save does not run on a timer while the app is active. Writers may want to reverse edits before committing, or save the current content under a new name via Save As. Continuous auto-save would undermine both. Manual save and Save As remain the primary commit actions.
- Save status is displayed persistently in the toolbar: "Saved" or "Unsaved changes". The indicator updates immediately on every edit and on every successful save. It never lies.
- If a save fails (URI permission expired, provider unavailable, storage full), the app shows an explicit error message and does not silently discard the failure.

**Save as**

- "Save as" fires `ACTION_CREATE_DOCUMENT`, allowing the user to choose location and filename.
- On first save of a new (unsaved) document, the app prompts for save location using the same flow.
- The user chooses the file extension at save time: `.txt` or `.md`. The default is `.txt`.

**New**

- Creates a blank unsaved document. If a document with unsaved changes is already open, the app prompts to save or discard first.

**Recent files**

- Every successfully opened file has its URI persisted via `takePersistableUriPermission()` and stored in a local Room database.
- The recents list displays the 20 most recently accessed files, ordered by last access time descending.
- Each entry shows the filename and, where available, the last-modified date.
- Tapping a recent file reopens it immediately without requiring the file picker, provided the persisted permission is still valid. If the permission has expired (file deleted, provider removed), the entry is shown as unavailable and can be removed.
- The recents list is the primary navigation surface. There is no folder browser.

**Share and print**

- The overflow menu includes "Share", which fires `ACTION_SEND` with the current file's `content://` URI and MIME type. This allows sharing via any registered app (email, messaging, Drive, etc.).
- The current text content is shared, not just a file reference, to ensure compatibility with apps that do not handle `content://` URIs.
- The overflow menu includes "Print", which converts the current text to HTML and passes it to Android's `PrintManager` via `WebViewClient`. The system print dialog handles printer selection, page setup, and PDF export as a destination. No custom PDF exporter is needed; PDF export is a free by-product of this implementation.

### 5.2 Editing

**Text input**

- The editing surface is a full-screen `EditText` (or Compose equivalent) with no chrome beyond the toolbar.
- Word wrap is on by default. The user can toggle it in settings.
- The system soft keyboard is used. No custom keyboard row is added in v1.
- Spell checking is delegated entirely to the system keyboard and input method. The app sets `inputType` to include `TYPE_TEXT_FLAG_AUTO_CORRECT` and `TYPE_TEXT_FLAG_MULTI_LINE` so third-party keyboards (SwiftKey, Gboard, OpenBoard) can underline and suggest corrections natively. No custom spell-check layer is implemented.

**Undo / redo**

- Full undo and redo history for the current session.
- Undo and redo are accessible via toolbar icons and are never greyed out incorrectly.
- Undo history is preserved when the app is backgrounded and restored to the foreground within the same session.
- Undo and redo work correctly regardless of whether the file was opened via the in-app picker, the system file manager, or an external intent.

**Find and replace**

- Find is triggered from the overflow menu or a keyboard shortcut.
- The find bar appears above the system keyboard, not as a modal dialog.
- Find supports plain text matching. Case-sensitive toggle. No regex in v1.
- Replace and Replace All are available in the same bar.
- Match count is displayed ("3 of 12").
- The find bar is dismissed by the back gesture or a close button.

**Encoding**

- All files are read and written as UTF-8.
- On open, if the file contains a UTF-8 BOM, it is stripped from the display but the presence of the BOM is recorded in the document state.
- On save, a BOM is written only if the original file had one. Files without a BOM are saved without one.
- Characters outside the Basic Multilingual Plane (emoji, some CJK) are handled correctly by the underlying `EditText` without substitution or corruption.
- If a file cannot be decoded as UTF-8, the user is informed explicitly. The app does not silently substitute or drop characters.

**Line numbers**

- Line numbers are displayed in a left gutter when enabled.
- Line numbers are toggleable via the overflow menu and persist as a user preference.
- Line numbers update correctly during editing, including when lines are inserted or deleted.
- The gutter width adapts to the number of digits in the longest line number.

**Word and character count**

- Word count and character count are displayed in the toolbar or status area when enabled.
- The count updates in real time as the user types. Debounced at 300ms to avoid performance impact on large files.
- The user can toggle the count display via the overflow menu.

### 5.3 Interface

**Toolbar**

- The toolbar contains: filename (truncated if necessary), save status indicator, undo icon, redo icon, overflow menu icon (three dots).
- The overflow menu contains: Open, Recents, New, Save, Save As, Share, Print, Find & Replace, Line Numbers (toggle), Word Count (toggle), Font Size, Dark Mode (toggle), Settings.
- No other persistent chrome. The toolbar is the full extent of non-writing UI.

**Typography**

- Default font: system monospace (Roboto Mono or device equivalent). User-selectable in settings between monospace and proportional (system default sans-serif).
- Default font size: 16sp. User-selectable: 12, 14, 16, 18, 20, 24sp. Selection persists.
- Line spacing: user-configurable in settings. Three options: Compact (1.2x), Normal (1.6x), Relaxed (2.0x). Default is Normal. Selection persists.
- Horizontal reading margin: user-configurable via a slider in settings labelled "Reading width", ranging from Narrow to Wide. This maps to horizontal padding of 8dp (narrow) through 64dp (wide) on each side. Default is Medium (24dp). The text column is always left-aligned; wider margins centre the readable area naturally on larger screens. On phones in portrait this makes a modest difference; on tablets and in landscape it is the primary readability control.

**Tablet and landscape layout**

- The app is usable and tested on standard Android tablets (10-inch class) in v1. No bespoke tablet layout is required: the same single-column editor applies, with the reading width setting providing line length control.
- In landscape orientation on any device, the reading width default increases to Wide (48dp) to avoid excessively long line lengths.
- Navigation rail, split-pane, or multi-column layouts are deferred to post-v1.

**Dark mode**

- The app follows the system dark/light mode by default.
- The user can override to force light or dark via the overflow menu toggle. The preference persists.
- Dark mode uses a true dark background (not dark grey). Text is off-white, not pure white, to reduce eye strain.

**Color and theming**

- The app uses Material 3 dynamic color. Accent colors, surface tones, and interactive element colors are derived from the user's wallpaper palette via the Android system. The app adapts automatically to each device without a hardcoded brand color.
- On devices running Android 11 or earlier that do not support dynamic color, a neutral fallback palette is used.
- Dark and light mode surface colors follow Material 3 defaults: true dark background in dark mode, off-white text to reduce eye strain.

**Edge-to-edge rendering**

- The app draws behind the system status bar and navigation bar. Content scrolls under the status bar with correct inset handling. The navigation bar background is transparent or translucent. This is required behaviour on modern Android and is non-negotiable.
- All interactive elements respect window insets so nothing is obscured by system bars or the camera cutout.

**Keyboard-aware layout**

- The editor resizes and repositions smoothly when the system keyboard appears or disappears, using `WindowInsetsAnimationCompat`. No jarring jumps or layout flashes. The transition between keyboard-visible and keyboard-hidden states is animated at the same speed as the keyboard itself.

**Predictive back gesture**

- The app supports Android 13+ predictive back. Swiping back plays the standard system back animation preview.
- If unsaved changes exist, the gesture triggers an `AlertDialog` ("Save" / "Discard" / "Cancel") on commit rather than navigating away immediately. "Save" writes the file then navigates; "Discard" navigates without saving; "Cancel" dismisses the dialog and keeps the user in the editor.

**Transitions and animation**

- The transition between the recents list and the editor uses a Material 3 container transform animation: the selected recent file item expands into the full editor surface. Return transition is the reverse.
- Toolbar show/hide on scroll is animated with a smooth slide, not an instant snap. Duration: 200ms, standard Material easing curve.
- All other transitions (settings screen, find bar) use Material 3 default motion specs. No custom animation durations unless a specific interaction requires it.

**Haptic feedback**

- A single short haptic tick fires when the user taps the checkmark to save. No other haptic feedback in v1. The tick uses `HapticFeedbackConstants.CONFIRM` (API 30+); on older devices it falls back to `VIRTUAL_KEY`.

**App icon**

- The app icon is designed in two variants: a full-color adaptive icon for standard display, and a monochrome variant for Android 13+ themed icons. The monochrome variant adopts the user's dynamic color wallpaper palette automatically, making the icon feel native to the device.
- The icon should be simple enough to read at 48x48dp and distinctive enough to be identifiable in a crowded launcher.
- A placeholder adaptive icon is used during development. The final icon is a separate design deliverable required before Play Store submission.

**Toolbar auto-hide**

- When the user scrolls up (reading or reviewing), the toolbar slides out of view upward. The writing surface expands to fill the full screen.
- When the user scrolls down, or taps anywhere in the writing surface, the toolbar slides back into view.
- The toolbar also reappears immediately when the system keyboard appears.
- This behaviour is on by default. It can be disabled in settings for users who prefer persistent chrome.
- The save status indicator remains accessible even when the toolbar is hidden: a small checkmark icon in the top corner acts as a floating save button. It is grey when no unsaved changes exist, amber when unsaved changes are present. Tapping it when amber saves the file immediately and turns it grey. Tapping it when grey reveals the toolbar.

- The app saves and restores cursor position and scroll offset per file in the recents database.
- When a file is reopened from recents, the cursor and scroll position are restored to where the user left off.
- Switching between Android windows (split-screen or multi-instance) does not reset cursor or scroll position.

### 5.4 Settings

A dedicated settings screen (accessible from the overflow menu) contains:

- Default font (monospace / proportional)
- Default font size
- Line spacing (Compact / Normal / Relaxed)
- Reading width (Narrow / Medium / Wide / Custom slider)
- Auto-hide toolbar on scroll (on/off)
- Word wrap (on/off)
- Dark mode override (system / always light / always dark)
- Default save extension (.txt / .md)
- Clear recent files list (with confirmation)

Settings are stored in `SharedPreferences` and persist across sessions.

---

## 6. Non-functional requirements

**Performance**

- App cold start to editable document in under 2 seconds on mid-range hardware (2GB RAM, 2020-era processor).
- Files up to 1MB open without perceptible lag. Files between 1MB and 5MB open with a brief loading indicator. Files above 5MB are out of scope for v1 and may show a warning.
- Word count update is debounced and runs off the main thread to avoid jank during typing.

**Reliability**

- Zero data loss under normal operation. This is a hard requirement, not a quality target.
- The app handles `FileNotFoundException` from SAF explicitly (it does not mean the file is missing; it can mean permission expired or provider is temporarily unavailable).
- The app does not crash on any Android version from API 29 (Android 10) onwards.

**Compatibility**

- Minimum SDK: API 29 (Android 10). Required for reliable SAF behaviour.
- Target SDK: current stable (API 35 at time of writing).
- Tested on: stock Android (Pixel), Samsung One UI, at least one mid-range device from another OEM, and at least one 10-inch Android tablet.
- Cloud providers tested explicitly: Google Drive, OneDrive, local storage via Files app.

**Security and privacy**

- No network permissions. The app never phones home.
- No analytics, no crash reporting that transmits data, no third-party SDKs.
- No permissions beyond those required by SAF (`READ_EXTERNAL_STORAGE` not requested; scoped access only).
- No account required. No login screen.

**Accessibility**

- All interactive elements have content descriptions for TalkBack.
- The app is fully usable with TalkBack enabled.
- Minimum touch target size: 48x48dp throughout.
- Text contrast ratios meet WCAG AA in both light and dark modes.

---

## 7. Technical constraints

- Built on Android's Storage Access Framework. No `file://` URI handling. No broad storage permission.
- URI permissions persisted via `takePersistableUriPermission()` on every open. Stored in Room database with filename, last accessed timestamp, and last known display name.
- File I/O via `ContentResolver.openInputStream()` and `openOutputStream()`. All I/O off the main thread (coroutines or `WorkManager` for save operations).
- Stack: Kotlin + Jetpack Compose, generated via Google AI Studio. AI Studio produces native Android code that is fully portable and opens directly in Android Studio for post-export refinement. See Appendix A for the full build-to-publish lifecycle.
- Development workflow: build and iterate in Google AI Studio using the Build tab and embedded Android Emulator; export to Android Studio for release signing, Lint cleanup, and Play Store submission.
- No third-party dependencies beyond what is strictly necessary. Fewer dependencies means fewer attack surface, easier maintenance, and smaller APK.
- AI-generated code must be reviewed post-export for hardcoded strings, missing accessibility labels, and over-declared permissions before release. Lint must pass clean prior to any Play Store submission.

---

## 8. Monetisation

- The app is a paid one-time purchase. Suggested price: 2.99 EUR (or regional equivalent).
- No free tier with limitations. No subscription. No in-app purchases. No ads.
- Rationale: the core value proposition is a reliable, clean writing tool. Ads contradict the product. Subscriptions are not justified for a local file editor. A one-time price filters for users who value the tool and reduces support burden from users who installed it by accident.
- A free trial mechanism (if desired) is handled by Google Play's refund window, not by a feature-limited free version.

---

## 9. Out of scope (v1)

The following are explicitly deferred to post-v1 and must not be added during v1 development regardless of user requests during development:

- Markdown preview or rendering
- Folder browser
- File management (rename, delete, duplicate)
- Multiple open files / tabs
- Templates
- Syntax highlighting
- Custom themes beyond light/dark
- Foldable and multi-pane optimised layouts
- iOS version
- Cloud sync
- Backup / version history
- Widgets

---

## 10. Success metrics

- Zero one-star reviews citing data loss or inability to save within 90 days of launch.
- Average Play Store rating ≥ 4.2 at 100 reviews.
- Fewer than 5% of installs result in an uninstall within 7 days (Play Store cohort data).
- At least one unprompted positive review specifically mentioning reliability or simplicity within 30 days.

---

## 11. Known risks

**SAF write permission from external intents.** Files opened by tapping from a file manager may arrive without write permission if the sending app does not grant it. Mitigation: detect read-only URIs on open, inform the user clearly, and offer to re-open via the in-app picker which requests write permission explicitly.

**Cloud provider SAF reliability.** Dropbox and OneDrive have historically had flaky SAF document provider implementations. Write failures to these providers will be attributed to the app by users. Mitigation: explicit save error messages that name the provider; documentation noting that cloud provider reliability is outside the app's control.

**Android storage policy changes.** Google continues tightening storage access with each major Android release. Building on SAF aligns with the platform direction, but edge case behaviour may change. Mitigation: test on each major Android release in beta before shipping updates.

**Markor as free incumbent.** Markor is free, open source, and capable. Users who find Markor acceptable have no financial reason to pay. Mitigation: design and reliability are the only answer. The app must feel meaningfully better to use, not just differently positioned.

---

*End of document.*

---

## Appendix A: Android app release lifecycle (Google AI Studio to Google Play)

This appendix covers the full lifecycle for taking Skriv from Google AI Studio to publication on the Google Play Store.

---

### A.1 Overview

Google AI Studio generates production-quality, native Android code (Kotlin, Jetpack Compose) that is fully portable. The tool handles the build and prototype phase; the steps below handle everything from that point to store submission.

The lifecycle has four stages:

1. Build and iterate in AI Studio
2. Export the project
3. Prepare a signed release build
4. Submit to Google Play

---

### A.2 Stage 1: Build and iterate in Google AI Studio

**Who:** Any team member (technical or non-technical)

Use the Build tab in [Google AI Studio](https://aistudio.google.com) to generate the app from a prompt. Iterate using the embedded Android Emulator until the core user flows are stable.

Requirements before moving to export:

- All primary user flows have been tested in the emulator
- No placeholder content (dummy text, hardcoded test values) remains in user-facing screens
- App has been installed and verified on a physical Android device via the USB/ADB install option

> **Note:** Do not proceed to export until physical device testing is complete. Browser emulator behaviour and real device behaviour can differ, particularly for hardware features and file system access via SAF.

---

### A.3 Stage 2: Export the project

**Who:** Technical (developer)

AI Studio supports two export paths:

| Method | When to use |
|---|---|
| Download as ZIP | One-time handoff; no version control needed |
| Export to GitHub | Ongoing development; preferred |

After export, open the project in [Android Studio](https://developer.android.com/studio). Verify the project builds successfully before proceeding:

```
./gradlew assembleDebug
```

A clean debug build is the baseline. Resolve any Gradle sync errors or dependency conflicts before moving forward.

**Required: run Lint**

```
./gradlew lint
```

AI-generated projects tend to have hardcoded strings, missing accessibility labels, and over-declared permissions that Play review will flag. For Skriv specifically, verify that no broad storage permissions (`READ_EXTERNAL_STORAGE`, `WRITE_EXTERNAL_STORAGE`, `MANAGE_APP_ALL_FILES_ACCESS_PERMISSION`) have been declared in the manifest. Skriv uses SAF exclusively and requires none of these. Fix all Lint errors; review all warnings.

---

### A.4 Stage 3: Prepare a signed release build

**Who:** Technical (developer)

Google Play requires all apps to be signed with a release key and submitted as an Android App Bundle (AAB).

#### A.4.1 Generate a signing keystore

A keystore is a file that contains the app's signing key. It must be created once and stored securely. Loss of the keystore means loss of the ability to update the app on the Play Store.

Generate via Android Studio: **Build > Generate Signed Bundle / APK > Create new keystore**

Or via command line:

```
keytool -genkey -v -keystore release.keystore \
  -alias skriv-key \
  -keyalg RSA -keysize 2048 \
  -validity 10000
```

Store the keystore file and its passwords in a secure location (password manager or secrets vault). Do not commit it to version control.

#### A.4.2 Configure release signing in Gradle

In `app/build.gradle`, add a `signingConfigs` block and wire it to the release build type:

```groovy
android {
    signingConfigs {
        release {
            storeFile file("release.keystore")
            storePassword System.getenv("KEYSTORE_PASSWORD")
            keyAlias "skriv-key"
            keyPassword System.getenv("KEY_PASSWORD")
        }
    }
    buildTypes {
        release {
            signingConfig signingConfigs.release
            minifyEnabled true
            proguardFiles getDefaultProguardFile('proguard-android-optimize.txt'), 'proguard-rules.pro'
        }
    }
}
```

Passwords are read from environment variables to avoid hardcoding credentials in source files.

#### A.4.3 Build the release AAB

```
./gradlew bundleRelease
```

Output: `app/build/outputs/bundle/release/app-release.aab`

This is the file submitted to Google Play.

---

### A.5 Stage 4: Submit to Google Play

**Who:** Mixed (developer + product/business)

#### A.5.1 Prerequisites

Before submitting, the following must be in place:

- A [Google Play Developer account](https://play.google.com/console) (one-time registration fee: USD 25)
- A privacy policy hosted at a public URL (required for all apps; Skriv collects no data but a policy is still mandatory)
- Store listing assets: app icon (512x512 px, both full-colour adaptive and monochrome variants), feature graphic (1024x500 px), at least two screenshots per supported screen size
- Content rating questionnaire completed (done inside Play Console)

#### A.5.2 Create the app in Play Console

1. Open Play Console and select **Create app**
2. App name: **Skriv**, default language: English, type: app, pricing: paid
3. Complete all store listing fields: short description (80 chars max), full description (4000 chars max), screenshots, icon, feature graphic
4. Complete the content rating questionnaire
5. Set up target audience; confirm the app is not directed at children

#### A.5.3 Upload the AAB and release

1. Navigate to **Production > Releases > Create new release**
2. Upload the `.aab` file from Stage 3
3. Add release notes
4. Review and roll out

Google Play's review process typically takes a few hours to a few days for new apps.

#### A.5.4 Use a testing track first

Before releasing to production, use a **closed testing track** to validate the release build with a limited group of testers. This catches signing or runtime issues that do not surface in debug builds, and is particularly important for verifying SAF file access behaviour on real devices from multiple manufacturers.

---

### A.6 Ongoing updates

For subsequent releases, repeat Stages 3 and 4. The keystore from Stage 3 must be used for every update; switching signing keys requires a new app listing.

If development continues in AI Studio, re-export and merge changes into the Android Studio project rather than replacing it wholesale, to avoid overwriting manual changes made post-export.

---

### A.7 Checklist summary

| Stage | Task | Owner |
|---|---|---|
| Build | Core flows tested in emulator | Any |
| Build | SAF file open/save/recent verified in emulator | Dev |
| Build | Verified on physical Android device | Any |
| Build | Verified on physical Android tablet | Any |
| Build | Google Drive file open and save verified on real device | Dev |
| Export | Project exported and opens in Android Studio | Dev |
| Export | Debug build clean | Dev |
| Export | Lint errors resolved | Dev |
| Export | No broad storage permissions in manifest | Dev |
| Release build | Keystore generated and stored securely | Dev |
| Release build | Signing config added to Gradle | Dev |
| Release build | Release AAB built successfully | Dev |
| Play submission | Developer account active | Business |
| Play submission | Privacy policy published | Business |
| Play submission | Final app icon designed (adaptive + monochrome variants) | Design |
| Play submission | Store listing assets prepared | Design |
| Play submission | Content rating completed | Product |
| Play submission | AAB uploaded to closed testing track | Dev |
| Play submission | Closed testing passed | Dev + Product |
| Play submission | Production rollout approved | Product |
