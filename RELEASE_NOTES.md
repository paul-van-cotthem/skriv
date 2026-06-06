# Skriv Release Notes — Version 1.5.13

## Improvements & Bug Fixes
* **Clickable Resource Links in Settings**:
  * Added direct, clickable links to the online User Manual and Privacy Policy within the Settings screen's "About" section for better accessibility and quick reference.

---

# Skriv Release Notes — Version 1.5.12

## Improvements & Bug Fixes
* **Instant Missing-File Dialog**:
  * Tapping a missing or inaccessible file from the Recents list now shows the "Find file" locate dialog immediately, rather than showing a "Cannot open file" error toast first and requiring a second tap.
  * When "Open last file on startup" is enabled, if the last accessed file is missing on app startup, the app now shows the "Find file" locate dialog immediately instead of navigating to a blank/error screen and popping back.

---

# Skriv Release Notes — Version 1.5.11

## Improvements & Bug Fixes
* **Missing-File Recovery Improvements**:
  * Added a "Remove" button option in the "Find file" missing-file dialog for files in the Recents list that cannot be found. This allows users to delete the entry individually from the Recents list if the file has been permanently deleted or moved.

---

# Skriv Release Notes — Version 1.5.10

## Improvements & Bug Fixes
* **Sentence Case Recovery Dialogs**:
  * Updated "Find File" and "Locate File" button labels inside the missing-file recovery dialog to sentence case ("Find file" and "Locate file") for typographical consistency.

---

# Skriv Release Notes — Version 1.5.9

## Improvements & Bug Fixes
* **Read-Only Educational Dialog**:
  * Added a "Why?" information link button to the "Save a copy to edit?" dialog.
  * Tapping the link shows an explanatory modal detailing Android's SAF security rules and temporary file permissions to prevent confusion and explain the root cause.

---

# Skriv Release Notes — Version 1.5.8

## Improvements & Bug Fixes
* **Neutral Read-Only Dialog Text**:
  * Updated the "Save a copy to edit?" dialog text to neutrally state the file is opened in read-only mode, avoiding confusing references to "another app" for read-only files opened from Skriv's Recents list.

---

# Skriv Release Notes — Version 1.5.7

## Improvements & Bug Fixes
* **Robust Collision Copy Naming**:
  * Added proactive suggested file naming (`_copy` suffix) when saving copies of read-only files.
  * Implemented transparent auto-correction for buggy OS document providers that append conflict numbering after the extension (e.g. `filename.txt (1)`), automatically rearranging it to `filename (1).txt` and relaunching the picker to preserve file type recognition.

---

# Skriv Release Notes — Version 1.5.6

## Improvements & Bug Fixes
* **Sentence Case Warning Banner Text**:
  * Updated the "Read-Only Mode" warning header inside the banner to sentence case ("Read-only mode") for full visual styling consistency.

---

# Skriv Release Notes — Version 1.5.5

## Improvements & Bug Fixes
* **Sentence Case UI Alignment**:
  * Updated the "Enable editing" and "Save copy" button labels to sentence case to match the rest of the application's typography and interface standards.

---

# Skriv Release Notes — Version 1.5.4

## Improvements & Bug Fixes
* **Improved Read-Only Banner Affordance**:
  * Replaced the flat text link button in the Read-Only banner with an **Outlined Button** styled with a border. This gives the button distinct visual affordance so users clearly know it is clickable.

---

# Skriv Release Notes — Version 1.5.3

## Improvements & Bug Fixes
* **Cleaner Read-Only Loading Screen**:
  * Removed the redundant bottom snackbar warning when opening a read-only file, keeping the top warning banner as the single, clean source of truth.

---

# Skriv Release Notes — Version 1.5.2

## Improvements & Bug Fixes
* **Friendly Read-Only Explanation**:
  * Improved the "Enable Editing" dialog text to explicitly explain *why* the file is read-only (e.g. opened from another app that restricts editing).
  * Relabeled the confirm button to "Save Copy" to explicitly define the resulting Save As action.

---

# Skriv Release Notes — Version 1.5.1

## Improvements & Bug Fixes
* **Simplified Read-Only Enable Editing Flow**:
  * Replaced the confusing OS file-open picker with an intuitive warning dialog when the user clicks "Enable Editing" on a read-only file.
  * The dialog prompts the user to save a copy of the read-only file using the standard "Save As" flow to begin editing, avoiding picker confusion.

---

# Skriv Release Notes — Version 1.5.0

## Improvements & Bug Fixes
* **Polished Auto-save Setting Wording**:
  * Renamed the "Auto-save when in background" setting to a cleaner **"Auto-save in background"**.
  * Simplified the descriptive text under the auto-save setting by removing the redundant "Auto-save is on/off" status prefix, making the layout cleaner.

---

# Skriv Release Notes — Version 1.4.9

## Improvements & Bug Fixes
* **Lowercase Preference Labels**:
  * Updated the "Line spacing" and "Margin" preference items in the Settings screen to display their dynamic values in lowercase (e.g. "Line spacing: normal" and "Margin: normal" instead of "Line spacing: Normal" and "Margin: Normal") to match all other switch-based preference suffixes.

---

# Skriv Release Notes — Version 1.4.8

## Improvements & Bug Fixes
* **Eliminated File-Open Filename Flickering**:
  * Fixed a bug where opening a `.txt` file when the default extension preference was set to `.md` would briefly display `"Untitled.md"` in the toolbar during loading. The correct filename is now resolved and updated immediately when the loading phase starts.

---

# Skriv Release Notes — Version 1.4.7

## Improvements & Bug Fixes
* **Intuitive Save As Suggested Extension**:
  * Updated the "Save As" flow to propose filenames with the user's preferred default extension (configured in Settings) instead of the original file's extension. For example, if a `.txt` document is open and the preferred extension is set to `.md`, the Save As picker will now suggest `.md` automatically, preventing unnecessary extensionless warning triggers or naming confusion.

---

# Skriv Release Notes — Version 1.4.6

## Improvements & Bug Fixes
* **Simplified About Screen Layout**:
  * Removed the redundant descriptive text block under the "About" section in settings, leaving only the application name and version number. This reduces clutter and cleans up the settings screen.

---

# Skriv Release Notes — Version 1.4.5

## Improvements & Bug Fixes
* **Save As File Extension Validation**:
  * Implemented an interactive warning dialog when a user saves a file without a `.txt` or `.md` extension in the system Save As picker.
  * The dialog prompts the user with three clear choices: **Save again** (which automatically cleans up the extensionless file and re-opens the picker with the default extension appended), **Keep as is** (which retains the extensionless file name), or **Cancel** (which deletes the file and returns to the editor).
  * This prevents physical duplicate or abandoned extensionless files while protecting documents from becoming unrecognized by Android or other editors.

---

# Skriv Release Notes — Version 1.4.4

## Improvements & Bug Fixes
* **UI & Settings Polish**:
  * **Dynamic Line Numbers Gutter**: Improved the gutter width calculation to dynamically fit the digit count (up to 5 digits max), preventing excessive gutter space.
  * **Dialog Formatting**: Put the second sentence of the clear recent files confirmation dialog on a new line for better readability.
  * **Settings Text Adjustments**:
    * Removed "visible" from the "Word count" setting.
    * Removed the "sp" label after the font size value.
    * Renamed "Theme mode" to "Theme".
    * Added state suffixes (e.g. `: on` / `: off`) dynamically to all toggle-based settings.
    * Adjusted the "Word wrap" description to include the word "Long" when enabled.

---

# Skriv Release Notes — Version 1.4.3

## Improvements & Bug Fixes
* **Robust SAF File Handling & Error Recovery**:
  * **Safe Path Handling**: Prevented potential crashes when launching `file://` URIs by checking URI schemes in the permission persistence layer, and improved error messaging to explain direct file path limitations on modern Android.
  * **Read-Only Mode & Banner**: Added a top-positioned warning banner in the editor when a file is opened as read-only. Clicking "Enable Editing" launches the file picker pre-focused on the document's folder to quickly re-establish lifetime edit access.
  * **Recents Re-connection Flow**: Clickable recent list rows now trigger a "Find File" dialog if the document's temporary permission expires or the file is moved. Selecting the file re-connects the database entry, preserving previous cursor and scroll positions.

---

# Skriv Release Notes — Version 1.4.2

## Improvements
* **Smooth Overflow Menu Transition**: Optimized the overflow menu opening behavior to eliminate the layout "flash" (double-draw) that occurred when the soft keyboard was active. The menu now waits to open until the keyboard has fully finished dismissing, ensuring it renders smoothly and directly in its final, stable position.

---

# Skriv Release Notes — Version 1.4.1

## Bug Fixes
* **Overflow Menu Alignment**: Fixed a bug where the editor screen overflow menu was cut off at the top when first opened while the software keyboard was visible. Tapping the three-dots menu now clears focus from the editor, and the menu content is keyed on `WindowInsets.isImeVisible` to dynamically remeasure and align itself correctly after the keyboard has dismissed.

---

# Skriv Release Notes — Version 1.4.0

## New Feature: Focus Mode (Distraction-Free Writing)

This release introduces **Focus Mode**, a dedicated distraction-free writing environment designed to help you concentrate on your content.

### What is Focus Mode?
* **Toggle it On/Off**: Access Focus Mode via the new target-square icon in the top-right toolbar (next to the search icon). 
* **Dynamic Chrome Fading**: Once toggled ON, starting to type will automatically fade out all in-app UI chrome (TopAppBar, filename/saved status header, and word count footer) to leave only your writing canvas.
* **Auto-Restore**: The UI chrome will automatically fade back in after 2 minutes of typing inactivity, or immediately when you tap the Focus icon again.
* **More Screen space**: When the headers and footers hide, the editing canvas dynamically resizes and expands to fill the top and bottom areas, revealing extra lines of text.
* **Smart Gutter Collapse**: If line numbers are enabled, the left gutter dynamically excludes itself from the layout in Focus Mode, giving your text the full width of your reading margins.
* **Always Accessible**: The Focus icon remains at 100% opacity in its static position in the top-right corner, ensuring you can exit Focus Mode with a single tap at any time.

## Other Improvements
* System-driven dropdown menu positioning to handle safe areas, screen heights, and keyboard states dynamically.
