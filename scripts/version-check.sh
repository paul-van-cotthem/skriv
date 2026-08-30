#!/usr/bin/env bash
# Verifies the app version is coherent before a release.
#
# Skriv has only one version location — `verName` in app/build.gradle.kts, with versionCode
# derived from it — so there is no mirror to drift. What *can* drift is the changelog: bumping
# verName without recording the release, or writing a CHANGELOG entry that was never shipped.
set -euo pipefail

root="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
gradle="$root/app/build.gradle.kts"
changelog="$root/CHANGELOG.md"

ver_name="$(grep -oE '^val verName = "[^"]+"' "$gradle" | grep -oE '[0-9]+\.[0-9]+\.[0-9]+')"
if [[ -z "$ver_name" ]]; then
  echo "✗ Could not read verName from app/build.gradle.kts"
  exit 1
fi

# versionCode is computed in Gradle as major*10000 + minor*100 + patch. Recompute it here so the
# number that reaches the Play Store is visible before uploading — Play rejects a duplicate or
# lower versionCode, and diagnosing that after a failed upload is slow.
IFS='.' read -r major minor patch <<< "$ver_name"
ver_code=$(( major * 10000 + minor * 100 + patch ))

changelog_version=""
if [[ -f "$changelog" ]]; then
  changelog_version="$(grep -oE '^## \[[0-9]+\.[0-9]+\.[0-9]+\]' "$changelog" | head -1 | grep -oE '[0-9]+\.[0-9]+\.[0-9]+' || true)"
fi

echo "  $ver_name  app/build.gradle.kts (verName)"
echo "  $ver_code  derived versionCode"
echo "  ${changelog_version:-none}  CHANGELOG.md (latest entry)"

if [[ -z "$changelog_version" ]]; then
  echo ""
  echo "✗ CHANGELOG.md has no version entry. Record the release before shipping it."
  exit 1
fi

if [[ "$changelog_version" != "$ver_name" ]]; then
  echo ""
  echo "✗ verName ($ver_name) and the latest CHANGELOG entry ($changelog_version) disagree."
  echo "  Either the release was not written up, or the changelog describes an unshipped version."
  exit 1
fi

echo ""
echo "✓ Version $ver_name is consistent (versionCode $ver_code)."
