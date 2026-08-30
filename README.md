# Skriv

An Android app for writing and editing plain text and Markdown files. Opens files from anywhere
on the device through Android's storage access framework, edits them in place, and never converts
or reformats them.

## Stack

- Kotlin, Jetpack Compose
- Gradle (Kotlin DSL), `compileSdk` 37, `minSdk` 31, `targetSdk` 36
- Distributed via the Google Play Store

## Prerequisites

- JDK 17+ and Android Studio (or the Android SDK command-line tools)
- `local.properties` pointing at your Android SDK
- A signing keystore for release builds (not in the repository)

## Getting started

Open the project in Android Studio and run the `app` configuration on a device or emulator.

From the command line:

```bash
./gradlew assembleDebug
```

```bash
./gradlew installDebug
```

## Build

```bash
./gradlew test
```

```bash
./gradlew bundleRelease
```

`bundleRelease` produces the signed App Bundle (`.aab`) for the Play Store. Build output lands in
`app/build/`, which is gitignored.

## Versioning and releases

`verName` at the top of [app/build.gradle.kts](app/build.gradle.kts) is the **single** source of
truth. `versionCode` is *derived* from it (`major * 10000 + minor * 100 + patch`), so the two
cannot drift apart — this is the pattern the other projects on this machine were changed to match.

| Command | Purpose |
| :-- | :-- |
| `./scripts/version-check.sh` | Confirms `verName` and the latest CHANGELOG entry agree, and prints the `versionCode` that will reach Play |
| `./scripts/version-tag.sh` | Tags the release commit `vX.Y.Z` — run **after** committing |

`version-tag.sh` refuses a dirty tree and refuses to move an existing tag. Tag *after* the release
commit: tagging earlier points the tag at the previous commit.

Release order: edit `verName` → write the [CHANGELOG.md](CHANGELOG.md) entry → `version-check.sh`
→ commit → `version-tag.sh` → build and upload the bundle.

The version is shown to users in Settings → About, read from the installed package's own
metadata, so it always reflects what actually shipped.

Releases up to 1.4.9 are archived in [RELEASE_NOTES.md](RELEASE_NOTES.md) in the older format.

## Project structure

```
app/src/main/java/com/skriv/app/
  ui/                  Compose screens, including ui/settings
app/build.gradle.kts   verName lives here
docs/                  build spec, PRD, privacy policy, screenshots
scripts/               version tooling
```

## Documentation

| Topic | File |
| :-- | :-- |
| Product requirements | [docs/prd.md](docs/prd.md) |
| Build specification | [docs/build-spec.md](docs/build-spec.md) |
| Release history | [CHANGELOG.md](CHANGELOG.md) |
| Archive (≤ 1.4.9) | [RELEASE_NOTES.md](RELEASE_NOTES.md) |

Contributor and AI-assistant conventions: [AGENTS.md](AGENTS.md).
