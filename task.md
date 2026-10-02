# Open points

What is open in Skriv: decisions waiting on Paul, open items, work in flight, and what recently
closed. Coordination is in `orchestration-plan.md`; decisions and their reasons are in
`docs/decisions.md`.

**Rules for this file** (`~/Code/baseline-app/setup/way-of-working.md`, section 2):

- Keep it under 300 lines.
- Items are bullets with a bold title, referred to by that title, never by a number. A formatter
  renumbers ordered lists.
- A decision, once made, goes to `docs/decisions.md`; the item here closes with a pointer to it.
- A closed item moves to *Recently closed* with its date and commit, and leaves after 14 days.
- Only the orchestrator edits this file. A worker sends it what to record.

**History.** Created 02/10/2026 by moving `Skriv-Wishlist.md` here (written 01/09/2026, `207faab`),
so its history follows this file. The items below are that wishlist, unchanged in substance and not
re-checked against the app, except where noted. The wishlist numbered its items; they are now
referred to by title.

## Now

Skriv 1.5.13 is released and nothing is in flight. Four features and one bug are open. The next step
is Paul choosing which one starts first, and whether the reading-page kit from `baseline-app` is
installed here.

## Decisions waiting on Paul

- **Which open item starts first.** The orchestrator makes a card for it, with a version number and
  manual check steps. Asked in chat on 02/10/2026.
- **Whether Skriv gets the reading-page kit.** The kit in `baseline-app` builds a hub page and a to
  do list page. Skriv has no `docs/design/pages/` folder, so none is built here. Asked in chat on
  02/10/2026.
- **Which release file a worker updates.** `AGENTS.md` says to document each version bump in
  `RELEASE_NOTES.md`. The `publish-play-store` skill and the header of `CHANGELOG.md` say
  `RELEASE_NOTES.md` is a frozen archive up to 1.4.9 and `CHANGELOG.md` is the source of truth. In
  practice `RELEASE_NOTES.md` has carried every entry since, version 1.5.13 included. Asked in chat
  on 02/10/2026.

## Open

### Bugs

- [ ] **Black screen after saving.** After saving a file, swiping back too soon, while the
  Snackbar message is still displayed, leaves Skriv on a black screen. (Wishlist, 01/09/2026; not
  reproduced since.) Needs a way to reproduce it before a worker can fix it.

### Features and polish

- [ ] **Recents: remove one entry** by swipe or long press. Removes the entry, not the file.
  Allowed by the spec as it stands: Settings already clears the whole list, and since 1.5.11 the
  missing-file dialog removes one entry (`RELEASE_NOTES.md`, version 1.5.11).
- [ ] **Open the picker in the last folder.** New and Save As open the file picker in the last
  folder saved to. Decided by Paul on 02/10/2026 (`docs/decisions.md`, *The picker opens in the last
  folder saved to*). The picker's starting folder is only a hint, so check that Google Drive and
  local storage both honour it. It adds a stored preference, so it needs a migration or a version
  and a line in the spec's picker section, in the same commit.
- [ ] **Tighten the spacing of Recents.**
- [ ] **Manual: fix the responsive design.** The manual is `docs/index.html`.

## In flight

| Work | Where | Started |
| :-- | :-- | :-- |
| Nothing | | |

## Parked on purpose

- **Markdown preview.** Dropped on 02/10/2026: the build spec says Skriv does not render Markdown
  (`docs/decisions.md`, *No Markdown preview*).

## Recently closed

- **02/10/2026:** Paul decided which wishlist items the spec allows. The Markdown preview is dropped,
  and the picker item is narrowed to remembering the last folder. Recorded in `bd26a71`, and in
  `docs/decisions.md` by the orchestrator's first commit.
- **02/10/2026:** `Skriv-Wishlist.md` became this file (`5ff6590`, `0837470`).
