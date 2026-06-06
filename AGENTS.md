# AGENTS.md

Instructions for AI coding assistants working in this repository.

## Project constraints

- This is an Android app named Skriv.
- Skriv is a plain text file editor for real `.txt` and `.md` files stored on the Android file system or in cloud storage through Android's Storage Access Framework.
- The product source of truth is `docs/build-spec.md`; read it before implementation work.
- The product behavior source of truth is `docs/prd.md`; read it when product scope, terminology, or user-facing behavior matters.
- Build with Kotlin 2.2, Jetpack Compose, Material 3, Room, DataStore, Navigation Compose, and Android SAF as specified in `docs/build-spec.md`.
- Minimum SDK is API 31 because dynamic color is a core product requirement. Target SDK is API 36.
- Do not add dependencies beyond the dependency list in `docs/build-spec.md` unless the user explicitly approves.
- Keep Kotlin strictness and Android lint/build health green.
- Accessibility is mandatory: WCAG 2.2 AA minimum where applicable to Android UI.
- If persisted Room or DataStore state shape changes after initial implementation, add the required migration or versioning work.

## Setup and commands

- Before the Android project is scaffolded, do not assume `./gradlew`, `settings.gradle.kts`, or `app/build.gradle.kts` exists.
- After scaffolding Gradle files, use the checked-in Gradle wrapper from the repository root.
- Default build validation: `./gradlew assembleDebug`.
- Use `./gradlew lint` only after confirming the Android Gradle project defines the task.
- Use `./gradlew clean` only when stale generated output or Gradle state is plausibly causing the problem.
- Do not add setup commands that install packages, SDKs, or dependencies globally without asking first.

## Hard constraints

- Do not declare `READ_EXTERNAL_STORAGE`, `WRITE_EXTERNAL_STORAGE`, or `MANAGE_EXTERNAL_STORAGE`.
- Do not declare `INTERNET` or any network permission.
- Do not add Firebase, Crashlytics, analytics SDKs, ads, telemetry, or third-party SDKs beyond the approved dependency list.
- Do not store document contents in app-private storage or a database. The app edits real files through SAF `content://` URIs.
- Use UTF-8 for all file reads and writes. Do not silently drop or substitute characters.
- Keep all file I/O off the main thread.
- Do not create test files under `src/test/` or `src/androidTest/` unless the build spec is intentionally changed.
- If a feature is not described in `docs/build-spec.md`, do not build it without asking first.

## Do

- Follow the implementation order in `docs/build-spec.md`.
- Scaffold the full project file structure first when starting the Android implementation.
- Increment the Android app version (`versionCode` and `versionName` in `app/build.gradle.kts`) whenever changes are made to the codebase.
- Whenever you bump the application version in `app/build.gradle.kts`, document the specific additions, changes, and deletions in `RELEASE_NOTES.md`.
- Run the real Gradle commands from the project after each implementation layer, especially `./gradlew assembleDebug`.
- Keep Gradle, Kotlin, Compose compiler, KSP, and Room versions aligned exactly as specified unless explicitly changed.
- Use Material 3 components and theming throughout.
- Centralize file access and SAF permission handling in the repository/data layer.
- Centralize Room entities, DAOs, and database code under the data layer.
- Centralize DataStore user preferences under the preferences layer.
- Keep UI state in ViewModels and one-shot UI events in the event model described by the spec.
- Use type-safe Navigation Compose routes as described in the spec.
- Keep diffs small and focused on the task at hand.

## Don't

- Do not introduce web app infrastructure, Firebase Hosting, Firebase Auth, Firebase App Check, Supabase, SQL migrations, edge functions, or analytics patterns into this Android app.
- Do not add Hilt, Koin, or another dependency injection framework.
- Do not add Markdown preview/rendering, a file tree, tabs, syntax highlighting, cloud sync, widgets, or file-management features unless the spec is changed.
- Do not block the UI thread with file, Room, or DataStore work.
- Do not scatter SAF permission, file repository, Room, or DataStore setup across rendering components.
- Do not add new broad architectural patterns when a clear spec pattern already exists.

## Working method
 
- Search the codebase for existing patterns before proposing or adding new ones.
- Prefer repo-local docs and current source over memory.
- Verify that a dependency, script, or convention exists before assuming it.
- When project behavior and generic best practice conflict, preserve the established project direction unless the user asks to change it.
- When generic repository guidance conflicts with `docs/build-spec.md`, follow `docs/build-spec.md` for this Android app and mention the conflict briefly.
- When the user requests a task, always comment on it (indicating whether you agree, disagree, have doubts, or think it is a good/bad idea). Do not hesitate to push back or offer alternative options.


## Start here when searching

- Build specification: `docs/build-spec.md`
- Product requirements: `docs/prd.md`
- Android entry point: `app/src/main/java/com/skriv/app/MainActivity.kt`
- Navigation: `app/src/main/java/com/skriv/app/navigation/`
- Theme: `app/src/main/java/com/skriv/app/ui/theme/`
- Screens and ViewModels: `app/src/main/java/com/skriv/app/ui/`
- Data layer: `app/src/main/java/com/skriv/app/data/`
- Shared models: `app/src/main/java/com/skriv/app/model/`
- Utilities: `app/src/main/java/com/skriv/app/util/`
- Android resources: `app/src/main/res/`

## Read the right docs

- Android build, architecture, dependencies, implementation order, and validation: read `docs/build-spec.md`.
- Product behavior, terminology, and scope: read `docs/prd.md`.

## Session rules

- When in doubt about which reference docs apply to a task, read them rather than skip them.
- After reading any reference doc, immediately output a line in the format: `✅ read docs/<filename>.md` - one line per file, before proceeding with the task.
- When a repo rule in `AGENTS.md` or guidance from `docs/` materially affects a decision, call it out explicitly in the user-facing response with `➔` and name the rule, guidance, or source you followed.
- Keep `➔` callouts brief and specific.
- Ask clarifying questions only when the risk of a wrong assumption is meaningful.
- If a new durable repo convention emerges, ask whether `AGENTS.md` should be updated.
- Do not take screenshots under any circumstances.
- Do not attempt to check if changes were implemented correctly on the device. Instead, provide clear, step-by-step instructions to the user on what to check manually to verify correctness.

## Validation commands

- Use the real repo commands from `package.json` or Gradle files when present.
- For Android implementation work, use `./gradlew assembleDebug` as the default validation command.
- After changes, run the smallest meaningful validation command for the work you touched.
- Before finishing UI work, compare the new UI against nearby existing UI for typography, color, spacing, component choice, focus behavior, and accessibility.
- For build completion, use the smoke test checklist in `docs/build-spec.md`.

## Ask first before

- Adding dependencies.
- Changing public APIs.
- Changing architecture or cross-cutting patterns.
- Changing the app's storage, network, telemetry, or document persistence model.
- Large refactors beyond the task.
- Pushing commits or taking destructive git actions the user did not request.
- Modifying deployment or CI/CD workflows unless the task requires it.

## Safety and editing reliability

- Never commit secrets, API keys, or production credentials.
- Read a file with line numbers immediately before editing it.
- Prefer small, targeted edits anchored on unique surrounding text.
- Follow the existing code style and patterns in the touched area.

## Android file handling

- File access must go through SAF and `content://` URIs.
- Persist URI permissions before reading or writing selected files.
- Use Android document APIs for display names, MIME types, and metadata.
- Treat unavailable recent files as recoverable UI states, not crashes.
- Keep recents and user preferences in app storage; keep document contents only in the user's chosen file.

## Git workflow

- Default `git pull` target is `origin main` unless the user says otherwise.
- Do not overwrite or revert user changes you did not make.
- Keep commits and diffs focused on the task at hand.
