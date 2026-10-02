# Open points

The single list of what is open in this repository. Read it before starting work here, tick what you
finish, and move a closed item down with its commit. If your work makes a claim here untrue, correct
it in the same commit.

**Created 02/10/2026** by moving `Skriv-Wishlist.md` here (written 01/09/2026, `207faab`), so its
history follows this file. The items below are that wishlist, unchanged in substance and not
re-checked against the app.

## Decisions waiting on Paul

### 1. Two wishlist items are ruled out by the spec as it stands

`AGENTS.md` (*Don't*) forbids Markdown preview and file-management features "unless the spec is
changed", and `docs/build-spec.md` governs. So items 3 and 5 below need a spec change first. Item 2
may count as file management too. Decide per item: change the spec, or drop the item.

## Open

### Bugs

- [ ] **4. Black screen after saving.** After saving a file, swiping back too soon, while the
  Snackbar message is still displayed, leaves Skriv on a black screen. (Wishlist, 01/09/2026; not
  reproduced since.)

### Features and polish

- [ ] **2. Delete an entry from the recent files list** by swipe or long press.
- [ ] **3. Markdown preview.** Blocked by decision 1.
- [ ] **5. A default Skriv-Files folder** in Documents, proposed for new files unless the last file
  was saved elsewhere, in which case that folder. Alternative: a setting for the default folder.
  Blocked by decision 1.
- [ ] **6. Tighten the spacing of Recents.**
- [ ] **7. Fix the responsive design of the manual.**

## Recently closed

- **02/10/2026:** `Skriv-Wishlist.md` became this file.
