#!/usr/bin/env bash
# Tags the current commit with the version in app/build.gradle.kts.
#
# Run AFTER the release commit. Tagging before the commit exists points the tag at the previous
# one — that bug silently mis-tagged three releases in a sibling project before it was noticed.
set -euo pipefail

root="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
cd "$root"

ver_name="$(grep -oE '^val verName = "[^"]+"' app/build.gradle.kts | grep -oE '[0-9]+\.[0-9]+\.[0-9]+')"
tag="v$ver_name"

# 1. Refuse a dirty tree — a tag must describe a committed state.
if [[ -n "$(git status --porcelain)" ]]; then
  echo "✗ Working tree has uncommitted changes. Commit the release first, then tag."
  exit 1
fi

# 2. Refuse to move an existing tag.
if git rev-parse -q --verify "refs/tags/$tag" >/dev/null; then
  tagged="$(git rev-list -n 1 "$tag")"
  head_sha="$(git rev-parse HEAD)"
  if [[ "$tagged" == "$head_sha" ]]; then
    echo "✓ $tag already tags this commit — nothing to do."
    exit 0
  fi
  echo "✗ $tag already exists on a different commit (${tagged:0:7})."
  echo "  Moving a published tag rewrites history for everyone. Resolve deliberately."
  exit 1
fi

git tag -a "$tag" -m "Release $tag"
echo "✓ Tagged $(git rev-parse --short HEAD) as $tag"
echo "  Push it with: git push origin $tag"
