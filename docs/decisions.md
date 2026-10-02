# Decisions

Every decision taken for Skriv, newest first, with its reason and source. `task.md` holds what
is still open and points here once an item is decided; the spec points here for why it says what it
says.

Only the orchestrator writes this file. An entry is never rewritten: a decision that is reversed
gets a new entry naming the one it replaces, and the old one gains a line saying so.

## 02/10/2026: Work on Skriv is paused until Paul restarts it

- **Decided:** All work on Skriv stops now, with no end date. Paul will wake the orchestrator when he
  wants it to continue.
- **Why:** This Mac has no Android SDK, so no session can compile Skriv. The Recents worker wrote the
  code for version 1.5.14 but could not build it. Paul was offered three ways to get an SDK and chose
  to stop instead. He gave no further reason.
- **Source:** Paul, 02/10/2026, with the question tool in session "Skriv Orchestrator".
- **Recorded in:** the orchestrator commit that adds this entry.

## 02/10/2026: Removing a Recents entry is a long press with Undo, no swipe

- **Decided:** A long press on a Recents row opens a small "Remove from Recents" menu. The same
  action sits in TalkBack's actions menu for that row. There is no swipe. Removing an entry shows a
  "Removed from Recents" message with Undo, and Undo restores the whole row, cursor and scroll
  position included. That adds one small restore function to the Recents repository.
- **Why:** A swipe alone has no alternative for TalkBack or switch users, and `AGENTS.md` requires
  WCAG 2.2 AA. The worker recommended this and Paul took the recommendation.
- **Source:** Paul, 02/10/2026, with the question tool in session "Skriv: remove one entry from
  Recents", relayed by that worker and recorded here.
- **Recorded in:** the orchestrator commit that adds this entry.

## 02/10/2026: Release notes go in CHANGELOG.md only

- **Decided:** A worker records each release in `CHANGELOG.md` and no longer adds to
  `RELEASE_NOTES.md`, which becomes a frozen archive. The `AGENTS.md` line that said otherwise is
  changed to match.
- **Why:** `AGENTS.md` told workers to document each bump in `RELEASE_NOTES.md`, while the header of
  `CHANGELOG.md` and the `publish-play-store` skill call `CHANGELOG.md` the source of truth and
  `RELEASE_NOTES.md` a frozen archive up to 1.4.9. In practice both were updated up to 1.5.13.
  Two places drift. Turned down: updating both, which is what `AGENTS.md` said.
- **Source:** Paul, 02/10/2026, with the question tool in session "Skriv Orchestrator".
- **Recorded in:** the orchestrator commit that changes `AGENTS.md`.

## 02/10/2026: No reading-page kit for Skriv

- **Decided:** Skriv does not get the hub page and the to do list page from `baseline-app`. Paul is
  asked in the chat with the question tool.
- **Why:** Five open items and no other sessions: the pages would be more upkeep than the list they
  show. Paul can still ask for the kit later.
- **Source:** Paul, 02/10/2026, with the question tool in session "Skriv Orchestrator".
- **Recorded in:** the orchestrator commit that adds this entry.

## 02/10/2026: Skriv gets an orchestrator

- **Decided:** Skriv works to the way of working in `~/Code/baseline-app/setup/way-of-working.md`,
  with one orchestrator session per the plan in `orchestration-plan.md`.
- **Why:** Paul decided on 01/10/2026 that every repository under `~/Code` gets an orchestrator at
  once, quiet ones included. Skriv is quiet: one branch, no other session, no open pull request.
- **Source:** Paul, 01/10/2026, recorded in `way-of-working.md` section 10 and in the brief that
  started this session.
- **Recorded in:** the first orchestrator commit (the one that adds this file).

## 02/10/2026: The picker opens in the last folder saved to

- **Decided:** New and Save As open the file picker in the last folder the user saved to. The
  original idea, a fixed Skriv-Files folder in Documents, is not built.
- **Why:** A fixed folder would need a folder-access grant and a spec change. The picker's starting
  folder is only a hint, so remembering the last folder needs neither. The cost is that a worker
  must still add a stored preference, with its migration, and describe the behaviour in the spec's
  picker section in the same commit.
- **Source:** Paul, 02/10/2026, as written into `task.md` in commit `bd26a71`. The session in which he
  answered was not recorded.
- **Recorded in:** `bd26a71`.

## 02/10/2026: No Markdown preview

- **Decided:** The wishlist item for a Markdown preview is dropped.
- **Why:** The build spec says Skriv does not render Markdown (`docs/build-spec.md`, opening
  paragraph, and *What not to build*). Share already hands a file to another app that can render it.
- **Source:** Paul, 02/10/2026, as written into `task.md` in commit `bd26a71`. The session in which he
  answered was not recorded.
- **Recorded in:** `bd26a71`.
