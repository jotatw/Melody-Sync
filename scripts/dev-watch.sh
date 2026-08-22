#!/usr/bin/env bash
#
# Dev watch: rebuild (and re-install) the app automatically when sources change.
# Keeps the installed `melody-sync` launcher always on the latest build for testing.
#
# Usage:
#   scripts/dev-watch.sh            # watch and rebuild+install on change
#   scripts/dev-watch.sh --build    # watch and rebuild only (no install)
#   scripts/dev-watch.sh once       # rebuild+install once and exit
#   scripts/dev-watch.sh --help
#
# Notes:
#   - Polls source mtimes (no external watcher dependency).
#   - Debounces ~4s so intermediate saves don't trigger back-to-back builds.
#   - A running app keeps the old jar in memory; relaunch it to pick the new build.

set -uo pipefail

ROOT="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
cd "$ROOT"

MODE="watch"
INSTALL=1
case "${1:-}" in
  --build) MODE="watch"; INSTALL=0 ;;
  once)    MODE="once"; INSTALL=1 ;;
  --help|-h) sed -n '1,16p' "$0"; exit 0 ;;
  *) ;;
esac

rebuild() {
  echo ""
  echo "==> [$(date +%H:%M:%S)] Change detected — building..."
  if [ "$INSTALL" = "1" ]; then
    ./scripts/install.sh
  else
    ./gradlew build --no-configuration-cache 2>&1 | tail -3
  fi
  echo "==> [$(date +%H:%M:%S)] Build finished. Relaunch the app to test."
}

snapshot() {
  find \
    melody-sync-core/src \
    melody-sync-cli/src \
    melody-sync-desktop/src \
    gradle \
    -type f \( -name '*.kt' -o -name '*.kts' -o -name '*.toml' -o -name '*.properties' \) \
    2>/dev/null -print0 | sort -z | xargs -0 stat -c '%Y %n' 2>/dev/null | sha256sum | cut -d' ' -f1
}

if [ "$MODE" = "once" ]; then
  rebuild
  exit 0
fi

echo "==> Watching for changes in $ROOT (Ctrl+C to stop). Build+install on change: $([ "$INSTALL" = 1 ] && echo yes || echo no)"

prev="$(snapshot)"
stable=0
while true; do
  sleep 1
  cur="$(snapshot)"
  if [ "$cur" != "$prev" ]; then
    stable=$((stable + 1))
    if [ "$stable" -ge 4 ]; then
      rebuild
      prev="$(snapshot)"
      stable=0
    fi
  else
    stable=0
  fi
done
