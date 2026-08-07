#!/usr/bin/env bash
set -euo pipefail

# Release helper: bump the version in gradle.properties (or reuse the
# current one), commit, tag v<version> and push. Pushing the tag triggers
# .github/workflows/release.yml which builds and publishes the jar.
#
# Usage:
#   ./scripts/release.sh            # tag the current version
#   ./scripts/release.sh 0.14.0     # bump to 0.14.0 and tag

SCRIPT_DIR="$(dirname "$(readlink -f "$0")")"
PROJECT_DIR="$(dirname "$SCRIPT_DIR")"
PROP_FILE="$PROJECT_DIR/gradle.properties"
BRANCH="$(git -C "$PROJECT_DIR" rev-parse --abbrev-ref HEAD)"

NEW_VERSION="${1:-}"
if [ -z "$NEW_VERSION" ]; then
    NEW_VERSION="$(grep -E '^melodySyncVersion=' "$PROP_FILE" | cut -d= -f2- | tr -d '[:space:]')"
    echo "==> Releasing current version $NEW_VERSION"
else
    echo "==> Bumping version to $NEW_VERSION"
    sed -i -E "s/^melodySyncVersion=.*/melodySyncVersion=$NEW_VERSION/" "$PROP_FILE"
fi

git -C "$PROJECT_DIR" add gradle.properties
git -C "$PROJECT_DIR" commit -m "chore: release v$NEW_VERSION"
git -C "$PROJECT_DIR" tag "v$NEW_VERSION"
git -C "$PROJECT_DIR" push origin "$BRANCH"
git -C "$PROJECT_DIR" push origin "v$NEW_VERSION"

echo ""
echo "🎵 Released v$NEW_VERSION — GitHub Actions is building and publishing the jar."
echo "Watch it at: https://github.com/jotatw/Melody-Sync/actions"
