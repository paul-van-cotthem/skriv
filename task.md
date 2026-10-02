# Open points

The single list of what is open in this repository. Read it before starting work here, tick what you
finish, and move a closed item down with its commit. If your work makes a claim here untrue, correct
it in the same commit.

**Created 02/10/2026** by moving `Skriv-Wishlist.md` here (written 01/09/2026, `207faab`), so its
history follows this file. The items below are that wishlist, unchanged in substance and not
re-checked against the app.

## Decisions waiting on Paul

None. Decision 1 (which wishlist items the spec allows) was settled on 02/10/2026; see
*Recently closed*.

## Open

### Bugs

- [ ] **4. Black screen after saving.** After saving a file, swiping back too soon, while the
  Snackbar message is still displayed, leaves Skriv on a black screen. (Wishlist, 01/09/2026; not
  reproduced since.)

### Features and polish

- [ ] **2. Remove an entry from the recent files list** by swipe or long press. Removes the entry,
  not the file. Allowed by the spec as it stands: Settings already clears the whole list, and since
  1.5.11 the missing-file dialog removes one entry.
- [ ] **5. New and Save As open the file picker in the last folder saved to.** Paul's choice,
  02/10/2026, over the original Skriv-Files folder in Documents, which would need a folder-access
  grant and a spec change. The picker's starting folder is only a hint, so check that Google Drive
  and local storage both honour it.
- [ ] **6. Tighten the spacing of Recents.**
- [ ] **7. Fix the responsive design of the manual.**

## Recently closed

- **02/10/2026, decision 1:** Paul decided. Item 3, Markdown preview, is **dropped**: the build
  spec says Skriv does not render Markdown, and Share already hands a file to a viewer. Item 5 is
  narrowed to remembering the last folder, which needs no spec change. Item 2 was never blocked.
- **02/10/2026:** `Skriv-Wishlist.md` became this file (`5ff6590`, `0837470`).
