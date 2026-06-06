---
name: publish-play-store
description: Use when building, incrementing the version, and preparing a signed release App Bundle (.aab) of the Skriv app for publication on the Google Play Store.
argument-hint: [versionName]
---

# Publish to Play Store

Automates the version bump, release build, and deployment instructions for submitting Skriv updates to the Google Play Store.

## Overview
To release a new version of Skriv, the app version must be incremented, a clean release build of the Android App Bundle (.aab) must be compiled, and the bundle must be uploaded to the Google Play Developer Console.

## When to Use
* When all code changes for a release/update are complete and tested.
* When you want to push a new version of the app to the Google Play Store (Internal testing, Closed testing, or Production).

## The Process

1. **Determine the Target Version**:
   * Read the current `verName` from [app/build.gradle.kts](file:///Users/paul/Library/CloudStorage/OneDrive-Personal/Documents/coding/skriv/app/build.gradle.kts).
   * By default, use the current `verName` (since codebase changes coded by the AI assistant will have already incremented it).
   * If a target version name is passed in `$ARGUMENTS` (e.g., `/publish-play-store 1.3.27`), or if you need to manually increment it, use that version.

2. **Update the Gradle Configuration (if version changed)**:
   * If updating the version, open [app/build.gradle.kts](file:///Users/paul/Library/CloudStorage/OneDrive-Personal/Documents/coding/skriv/app/build.gradle.kts).
   * Update the line `val verName = "[version]"` with the target version.
   * Verify that the dynamic `versionCode` calculation correctly updates.

3. **Build the Release App Bundle**:
   * Run a clean build using the Gradle wrapper:
     ```bash
     ./gradlew clean bundleRelease
     ```

4. **Provide Links and Deployment Instructions**:
   * Print BOTH the raw absolute path on the user's Mac:
     `/Users/paul/Library/CloudStorage/OneDrive-Personal/Documents/coding/skriv/app/build/outputs/bundle/release/app-release.aab`
   * And the clickable link with the full absolute path:
     `[/Users/paul/Library/CloudStorage/OneDrive-Personal/Documents/coding/skriv/app/build/outputs/bundle/release/app-release.aab](file:///Users/paul/Library/CloudStorage/OneDrive-Personal/Documents/coding/skriv/app/build/outputs/bundle/release/app-release.aab)`
   * Guide the user to upload this bundle to the [Google Play Console](https://play.google.com/console) under **Testing** > **Internal testing** and update their phone via the Play Store app.

## Quick Reference

| Action | Command / File | Purpose |
| --- | --- | --- |
| Update Version | [app/build.gradle.kts](file:///Users/paul/Library/CloudStorage/OneDrive-Personal/Documents/coding/skriv/app/build.gradle.kts) | Sets the version name & code |
| Build Release | `./gradlew clean bundleRelease` | Cleans and compiles the signed release bundle |
| Output AAB | `app/build/outputs/bundle/release/app-release.aab` | Output file for Play Store upload |

## Common Mistakes

* **Skip Clean**: Skipping `./gradlew clean` can sometimes cause stale resources or cache to be included in the release build.
* **Incorrect Keystore Passwords**: If building fails, ensure that [local.properties](file:///Users/paul/Library/CloudStorage/OneDrive-Personal/Documents/coding/skriv/local.properties) contains correct, unexpired signing keys.
* **No Version Bump**: Forgetting to increment the version will cause Google Play Console to reject the upload.

## Related Workflows

* [android-cli](file:///Users/paul/.gemini/config/plugins/android-cli-plugin/skills/SKILL.md) — For other general Android developer command line tools.
* [verification-before-completion](file:///Users/paul/.gemini/config/skills/verification-before-completion/SKILL.md) — To run standard validation.
