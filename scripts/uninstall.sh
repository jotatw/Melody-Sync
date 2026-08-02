#!/usr/bin/env bash
set -euo pipefail

NAME="melody-sync"
BIN_DIR="$HOME/.local/bin"
DATA_DIR="$HOME/.local/share/$NAME"
DESKTOP_DIR="$HOME/.local/share/applications"

echo "==> Uninstalling Melody Sync..."

rm -f "$BIN_DIR/$NAME"
rm -f "$DESKTOP_DIR/$NAME.desktop"
rm -rf "$DATA_DIR"

echo "🎵 Melody Sync uninstalled."
echo "Your library database (~/.config/melody-sync/library.db) was kept."
