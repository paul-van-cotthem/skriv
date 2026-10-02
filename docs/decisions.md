# Decisions

Every decision taken for Skriv, newest first, with its reason and source. `task.md` holds what
is still open and points here once an item is decided; the spec points here for why it says what it
says.

Only the orchestrator writes this file. An entry is never rewritten: a decision that is reversed
gets a new entry naming the one it replaces, and the old one gains a line saying so.

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
