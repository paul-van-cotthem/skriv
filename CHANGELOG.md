# Changelog

All notable changes to Skriv are documented here.

The format follows [Keep a Changelog](https://keepachangelog.com/en/1.1.0/), and this project
adheres to [Semantic Versioning](https://semver.org/spec/v2.0.0.html).

This file is the source of truth for what changed in a release. Entries are written for the
person using the app, not as a summary of the diff.

Releases up to and including 1.4.9 are archived in [RELEASE_NOTES.md](RELEASE_NOTES.md) in the
older format.

## [1.5.13]

### Added
- Clickable links to the online user manual and privacy policy in the Settings "About" section.

## [1.5.12]

### Changed
- Tapping a missing file in Recents now opens the "Find file" dialog immediately, instead of
  showing a "Cannot open file" error first and requiring a second tap.
- With "Open last file on startup" enabled, a missing last file now opens the "Find file" dialog
  directly rather than navigating to a blank screen and popping back.

## [1.5.11]

### Added
- A "Remove" option in the missing-file dialog, so an entry can be deleted from Recents when the
  file has been permanently moved or deleted.

## [1.5.10]

### Changed
- "Find file" and "Locate file" button labels in the missing-file dialog set in sentence case.

## [1.5.9]

### Added
- A "Why?" link on the "Save a copy to edit?" dialog, explaining Android's storage-access rules
  and temporary permissions rather than leaving the restriction unexplained.

## [1.5.8]

### Changed
- The "Save a copy to edit?" dialog now states plainly that the file is read-only, instead of
  referring to "another app" for files opened from Skriv's own Recents list.

## [1.5.7]

### Added
- A suggested `_copy` filename when saving a copy of a read-only file.

### Fixed
- Some document providers append conflict numbering after the extension (`filename.txt (1)`),
  which breaks file-type recognition. Skriv now rewrites this to `filename (1).txt` and reopens
  the picker.

## [1.5.6]

### Changed
- "Read-only mode" banner heading set in sentence case.

## [1.5.5]

### Changed
- "Enable editing" and "Save copy" button labels set in sentence case.

## [1.5.4]

### Changed
- The action in the read-only banner is now an outlined button rather than flat text, so it reads
  as clickable.

## [1.5.3]

### Changed
- Removed the duplicate snackbar warning when opening a read-only file; the top banner is now the
  single indicator.

## [1.5.2]

### Changed
- The "Enable editing" dialog now explains *why* a file is read-only, and the confirm button is
  labelled "Save Copy" to describe what it actually does.

## [1.5.1]

### Changed
- "Enable editing" on a read-only file now shows a warning dialog leading to Save As, replacing
  the OS file picker that appeared without explanation.

## [1.5.0]

### Changed
- "Auto-save when in background" renamed to "Auto-save in background", and its description
  simplified by dropping the redundant on/off status prefix.

## [1.4.9] and earlier

See [RELEASE_NOTES.md](RELEASE_NOTES.md).
