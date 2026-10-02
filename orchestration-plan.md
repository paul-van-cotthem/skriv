# Orchestration plan

Who does what in Skriv, in which order, who installs what, who may edit which shared file, and how
work reaches `main`. `task.md` holds what is open and `docs/decisions.md` what was decided; this
file holds the coordination. Each fact lives in one of the three, and they point at each other.

**This file is the authority, not any session.** If the orchestrator stops, is restarted or runs out
of context, whoever opens this file next (a new orchestrator, or Paul) takes over from it. Update it
in the same commit as the change it describes. The rules behind it are in
`~/Code/baseline-app/setup/way-of-working.md`.

Skriv is an Android app, not a web app. Parts of the way of working that assume a web project (the
hub and to do pages, `npm` checks, end-to-end tests, a formatter) do not apply here and are marked
so below.

## Roles

| Role | Who | Does | Does not |
| :-- | :-- | :-- | :-- |
| Supervisor | Paul | Decides, approves, answers questions, starts cards | |
| Orchestrator | the session "Skriv Orchestrator", started 02/10/2026 | Keeps this file; decides when work starts and who does it; writes briefs; makes cards; reviews, checks and lands work; asks Paul every cross-cutting question | Write product code; build a workstream itself |
| Worker | every other session that touches this repository, on its own branch and worktree, including one started in another repository and a skill run from one | Builds one workstream; asks Paul about its own output; hands over a commit range | Push to `main`; make a card; edit a locked file without the lock; adopt a new library, tool or pattern without approval |

Background agents are the orchestrator's helpers (reviews, verification). Work Paul will want to see
or talk about runs as a card he starts.

The orchestrator session runs from a worktree the app made for it, because the app could not move
it into the main checkout. It lands its own bookkeeping by fast-forwarding `main` in the main
checkout (`git -C /Users/paul/Code/skriv merge --ff-only <commit>`). That worktree and its branch
are scaffolding: the orchestrator removes them when Paul archives this session.

## Questions and where answers go

- Every question to Paul uses the question tool, one decision per question, the recommendation
  first.
- A worker asks Paul about its own output. Cross-cutting questions (a shared file, a dependency, a
  technology, project-wide policy, another worker's work) go to the orchestrator, which asks Paul.
  A worker that escalates a question does not also ask Paul.
- A worker sends every answer Paul gives it to the orchestrator in the same turn; the orchestrator
  records it here, in `task.md` or in `docs/decisions.md`. A relayed decision binds once it is
  written.
- When more than ten items wait on Paul, the orchestrator tells him in plain words and proposes
  which to defer or drop.
- A new rule from Paul goes to every live worker in the same turn.

## Stages and their gates

Skriv is released (version 1.5.13 at 02/10/2026) and its build spec has been in the repository since
the first commit on 06/06/2026. The stages for a new project therefore ran before the way of working
existed. What is left is the loop of stages 8 and 9: each change is a small milestone, released as a
patch or minor version.

| Stage | State | Waits on |
| :-- | :-- | :-- |
| 1. Brief | Done before the model: `docs/prd.md` | |
| 2. Design interview | Done before the model | |
| 3. Research plan | Not used for Skriv | |
| 4. Spec | Done: `docs/build-spec.md` is the source of truth | |
| 5. Spec review | Not recorded | |
| 6. Approval | Taken as given: the app is released and the spec has driven its releases | |
| 7. Stage 0 | Not applicable: nothing to spike | |
| 8. Milestones | In progress: the open list in `task.md` | Paul choosing which item to start |
| 9. Release | Per change: the `publish-play-store` skill, with Paul uploading | |

**The scope is frozen at the build spec.** `AGENTS.md` already says a feature the spec does not
describe is not built without asking. A change that needs the spec to change goes to Paul first, and
the spec edit travels in the same commit as the code.

**Exceptions Paul granted to "nothing built before the scope freezes":** none needed.

## Workstreams

Status as of 02/10/2026, `main` at `bd26a71`. Rewrite this line; do not append to it.

| Workstream | Owner (session title and short id) | Branch and worktree | Status | Waits on | Next step |
| :-- | :-- | :-- | :-- | :-- | :-- |
| Coordination | Skriv Orchestrator (this session) | `claude/gracious-zhukovsky-590c95`, worktree `.claude/worktrees/gracious-zhukovsky-590c95` (scaffolding) | Setting up the plan, decision log and `AGENTS.md` section | Nothing | Land the first commit |
| Recents entry removal | none | none | Not started | Paul choosing what starts | See *Recents: remove one entry* in `task.md` |
| Picker opens in last folder | none | none | Not started | Paul; touches the spec and stored preferences | See *Open the picker in the last folder* in `task.md` |
| Recents spacing | none | none | Not started | Paul choosing what starts | See *Tighten the spacing of Recents* in `task.md` |
| Manual responsive design | none | none | Not started | Paul choosing what starts | See *Manual: fix the responsive design* in `task.md` |
| Black screen after saving | none | none | Not started, not reproduced since 01/09/2026 | A way to reproduce it | See *Black screen after saving* in `task.md` |

There is no other Skriv session, live or archived. The session list for 02/10/2026 shows none whose
folder is this repository. `main` equals `origin/main`, the only remote branch is `main`, there are
no open pull requests and no stashes.

## Shared-file locks

A worker asks the orchestrator before editing a file below. The orchestrator records the holder by
session title and short id, with the date, and releases the lock when the work lands or the holder
is archived. Files a worker creates for its own workstream need no lock.

| File | Why it is shared | Held by |
| :-- | :-- | :-- |
| `AGENTS.md`, `orchestration-plan.md`, `task.md`, `docs/decisions.md` | rules, coordination and the open list | orchestrator |
| `docs/build-spec.md`, `docs/prd.md` | the spec and the product behaviour; every workstream reads them | |
| `app/build.gradle.kts` (`verName`), `CHANGELOG.md`, `RELEASE_NOTES.md` | every release changes all three; two workers bumping at once collide | |
| `gradle/libs.versions.toml`, root and app Gradle files | the build; one dependency change at a time | |
| `app/src/main/AndroidManifest.xml` | permissions, intent filters, activity attributes | |
| `app/src/main/res/values/strings.xml` and `themes.xml` | every string and the theme | |
| Room entities, DAOs and the database class; the DataStore preferences class | persisted state: a change needs a migration or a version | |
| `docs/index.html`, `docs/privacy.html` | the published manual and privacy page | |
| `store_screenshots/`, `docs/screenshots/` | listing and manual images | |

**Version numbers.** The orchestrator names the next version in each brief. The worker bumps
`verName` to exactly that number and writes its entries in `CHANGELOG.md` and `RELEASE_NOTES.md`.
Work lands in version order. A coordination-only commit (this plan, `task.md`, the decision log)
does not bump the version, as the last three commits on `main` show.

## Install ledger

One install at a time. A package is installed by the worker whose milestone first imports it,
never earlier. Machine-wide installs (Homebrew, global npm packages, test browsers, Node) change
only through the package-update procedure or with Paul's yes.

| Package or tool | Version | State | Needed at | Installed by |
| :-- | :-- | :-- | :-- | :-- |
| Nothing pending | | The machine tools a build needs (JDK 17, the Android SDK) have not been inventoried. No code work has started | The first code workstream | The worker, which checks them first and tells the orchestrator |

## Technology register

The approved dependencies are the list in `docs/build-spec.md`, section *Dependencies*: Compose
(BOM, UI, Material 3, animation, tooling preview), Navigation Compose, lifecycle ViewModel and
runtime for Compose, Room with KSP, DataStore preferences, coroutines, core KTX, activity Compose,
window, Material 3 adaptive, and kotlinx serialization JSON. Kotlin 2.2, minimum SDK 31, target SDK
36. Android's Storage Access Framework carries all file access.

Anything not in that list is proposed to the orchestrator, which asks Paul. `AGENTS.md` bars the
internet and storage permissions, analytics, Firebase, dependency injection frameworks and test
files outright.

## Published pages

None. Skriv has no hub, no to do list page and no reports index. `docs/index.html` is the product
manual and `docs/privacy.html` the privacy page, both published with the project, not coordination
pages. The reading-page kit lives in `baseline-app` and is not installed here (see *Waiting on Paul*
in `task.md`).

## Landing protocol

1. The worker commits on its own branch, rebased onto `origin/main`, and sends the orchestrator the
   commit range, what it changed and which checks it ran.
2. The orchestrator checks the handed-over commit in a clean temporary worktree
   (`git worktree add --detach <scratch> <commit>`), never in the worker's own.
   - *Documents only* (Markdown, coordination files): `git diff --check`, and a read of the diff.
     There is no formatter and no `check:standard` here.
   - *Anything else*: `./gradlew assembleDebug`, then `scripts/version-check.sh`, then
     `./gradlew lint` only if the project defines the task. The repository forbids test files, so
     there is no test run. A build in a temporary worktree has not been tried yet. The first code
     landing confirms it and the result goes into the runbook.
   - The device cannot be checked by a session (`AGENTS.md`: no screenshots, no on-device checks).
     The worker hands Paul step-by-step manual checks instead, and the orchestrator does not land
     user-visible work as verified until Paul has run them, or says plainly that he has not.
3. It reads the diff against the house rules, `AGENTS.md`, the build spec and this plan. A larger
   branch also gets a fresh reviewer agent.
4. It fast-forwards `main` and pushes; a merge commit only when `main` moved for its own
   bookkeeping. It batches its bookkeeping after landings. From its worktree it fast-forwards
   `main` in the main checkout and pushes from there.
5. It deletes the branch on both sides, updates this plan and `task.md`, names the checks that ran,
   and tells the worker.
6. After Paul archives a session, it removes the worktree and branch in the same step, once both
   are clean and merged.

## Worker brief

Every brief the orchestrator writes is built from `~/Code/baseline-app/setup/orchestrator-brief.md`,
"The worker brief", with these Skriv parts added: the version number to bump to, the `✅ read
docs/<filename>.md` lines `AGENTS.md` asks for, the rule against screenshots and on-device checks,
and the manual check steps the worker must hand to Paul.

## Orchestrator runbook

What the orchestrator needs to pick the job up from this file alone.

- **Starting, or restarting:** read the house rules, `AGENTS.md`, `way-of-working.md`, this file and
  `task.md`; then check the live sessions, `git worktree list`, the branches and `main` against
  `origin/main`, and reconcile this file with what is there.
- **After its context is compacted:** reread Roles, Shared-file locks and this runbook. After the
  second compaction Paul starts a fresh orchestrator from this file.
- **Before stating that a worker is running, waiting or done:** check its session state.
- **Before messaging a session:** check it is not archived; a message unarchives it.
- **A session started in the main checkout `/Users/paul/Code/skriv`:** run `git branch
  --show-current` first. The app could not move the first orchestrator there, so a fresh one may
  have the same trouble and work from a worktree; land through the main checkout then.
- **Paul's decisions:** record each in `docs/decisions.md`, close its item in `task.md` with a
  pointer, and send it to every live worker if it is a rule.
- **No hub or to do page exists.** Paul is asked in the chat with the question tool. Add pages only
  if he installs the kit.

## Guards to automate later

| Guard | Built at |
| :-- | :-- |
| `scripts/version-check.sh` already exists: version and changelog agree | Built |
| A check that `task.md` stays under 300 lines and has no numbered items | Not built; only if the file grows |

Review this table when each milestone's plan is written; a guard planned for a milestone is built in
it.
