#!/usr/bin/env bash
# ============================================================
# Skriv — Play Store screenshot capture script
# Requires: adb connected device (wireless or USB)
# Usage: bash scripts/take_store_screenshots.sh
# ============================================================

set -e

OUT_DIR="$(dirname "$0")/../store_screenshots"
PACKAGE="com.skriv.app"
ACTIVITY="com.skriv.app.MainActivity"

mkdir -p "$OUT_DIR"

# ── Helpers ──────────────────────────────────────────────────

check_device() {
    if ! adb devices | grep -q "device$"; then
        echo "❌  No ADB device found. Connect your S25 Ultra via wireless debugging and try again."
        exit 1
    fi
    echo "✅  Device connected: $(adb devices | grep 'device$' | head -1 | awk '{print $1}')"
}

capture() {
    local name="$1"
    local path="$OUT_DIR/${name}.png"
    adb exec-out screencap -p > "$path"
    echo "📸  Saved: store_screenshots/${name}.png"
}

wait_for_user() {
    echo ""
    echo "👉  $1"
    echo "    Press ENTER when the screen looks right…"
    read -r
}

launch_app() {
    adb shell am start -n "${PACKAGE}/${ACTIVITY}" > /dev/null 2>&1
    sleep 1
}

press_back() {
    adb shell input keyevent KEYCODE_BACK > /dev/null 2>&1
    sleep 0.5
}

# ── Main ─────────────────────────────────────────────────────

echo ""
echo "================================================="
echo "  Skriv — Play Store Screenshot Capture"
echo "================================================="
echo ""

check_device

# ── PRE-SETUP ────────────────────────────────────────────────
echo ""
echo "📋  BEFORE WE START — please do this once on your phone:"
echo ""
echo "    1. Open Skriv manually"
echo "    2. Open at least 3 files so Recents shows a mix:"
echo "       - at least one .txt file"
echo "       - at least one .md file"
echo "    3. Come back here and press ENTER when ready."
read -r

# ────────────────────────────────────────────────────────────
# 1. RECENTS — with files (light theme)
# ────────────────────────────────────────────────────────────
wait_for_user "SCREEN 1/11 — Recents list with .txt AND .md files visible (light theme).
    Make sure Settings > Theme is set to 'Light'.
    Navigate to the Recents screen (tap back until you see the file list)."
capture "01_recents_light"

# ────────────────────────────────────────────────────────────
# 2. RECENTS — with files (dark theme)
# ────────────────────────────────────────────────────────────
wait_for_user "SCREEN 2/11 — Same Recents list, but DARK theme.
    Go to: overflow menu → Settings → Theme → Dark.
    Then press back to return to the Recents screen."
capture "02_recents_dark"

# ────────────────────────────────────────────────────────────
# 3. RECENTS — empty state
# ────────────────────────────────────────────────────────────
wait_for_user "SCREEN 3/11 — Recents empty state.
    Go to Settings → scroll down → Clear recent files → confirm.
    Then back to Recents. Also switch theme back to Light."
capture "03_recents_empty"

# ────────────────────────────────────────────────────────────
# 4. EDITOR — document open (light theme)
# ────────────────────────────────────────────────────────────
wait_for_user "SCREEN 4/11 — Editor with a .txt or .md document open (light theme).
    Tap 'Open' and open any text file. Put a few lines of text in it."
capture "04_editor_light"

# ────────────────────────────────────────────────────────────
# 5. EDITOR — document open (dark theme)
# ────────────────────────────────────────────────────────────
wait_for_user "SCREEN 5/11 — Same editor, DARK theme.
    Overflow menu → Settings → Theme → Dark → back to editor."
capture "05_editor_dark"

# ────────────────────────────────────────────────────────────
# 6. EDITOR — overflow menu open
# ────────────────────────────────────────────────────────────
wait_for_user "SCREEN 6/11 — Editor overflow (3-dot) menu open.
    Tap the 3-dot menu in the top-right so it is fully visible.
    (Theme: whichever looks best — try light)"
capture "06_editor_overflow_menu"

# ────────────────────────────────────────────────────────────
# 7. EDITOR — Find bar open
# ────────────────────────────────────────────────────────────
wait_for_user "SCREEN 7/11 — Find/Replace bar open.
    Close the menu (back), then tap the search icon.
    Type a word in the search field so it shows a result."
capture "07_editor_find_bar"

# ────────────────────────────────────────────────────────────
# 8. EDITOR — Focus mode active
# ────────────────────────────────────────────────────────────
wait_for_user "SCREEN 8/11 — Focus mode active (UI chrome hidden).
    Close the find bar (X). Tap the target/crosshair icon (top right).
    Start typing a few characters — the toolbar/footer should fade out.
    Capture when only the text canvas + focus icon are visible."
capture "08_editor_focus_mode"

# ────────────────────────────────────────────────────────────
# 9. SETTINGS screen
# ────────────────────────────────────────────────────────────
wait_for_user "SCREEN 9/11 — Settings screen.
    Exit focus mode (tap the target icon). 
    Overflow menu → Settings. Let the full settings list be visible."
capture "09_settings"

# ────────────────────────────────────────────────────────────
# 10. EDITOR — Read-only banner
# ────────────────────────────────────────────────────────────
wait_for_user "SCREEN 10/11 — Read-only warning banner.
    Open a file from another app (e.g. Files app, or Google Drive)
    so Skriv shows the yellow 'Read-only mode' banner at the top.
    If you can't reproduce this, skip with Ctrl+C and just press ENTER."
capture "10_editor_readonly_banner"

# ────────────────────────────────────────────────────────────
# 11. UNSAVED CHANGES dialog
# ────────────────────────────────────────────────────────────
wait_for_user "SCREEN 11/11 — Unsaved changes dialog.
    In the editor, type something new (so the document is unsaved),
    then press the back button. The 'Save changes?' dialog should appear."
capture "11_unsaved_changes_dialog"

# ── Done ─────────────────────────────────────────────────────
echo ""
echo "================================================="
echo "  ✅  All done!"
echo "================================================="
echo ""
echo "  Screenshots saved to:"
echo "  $(cd "$OUT_DIR" && pwd)"
echo ""
echo "  Files:"
ls -1 "$OUT_DIR/"
echo ""
echo "  Upload the phone screenshots (01–11) to Play Console."
echo "  You still need to create:"
echo "    • Feature graphic  (1024 × 500 px)"
echo "    • App icon PNG     (512 × 512 px, no alpha)"
echo ""
