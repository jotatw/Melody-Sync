#!/usr/bin/env bash
set -euo pipefail

SCRIPT_DIR="$(dirname "$(readlink -f "$0")")"
PROJECT_DIR="$(dirname "$SCRIPT_DIR")"
NAME="melody-sync"
BIN_DIR="$HOME/.local/bin"
DATA_DIR="$HOME/.local/share/$NAME"
DESKTOP_DIR="$HOME/.local/share/applications"
# Single source of truth for the version lives in gradle.properties.
VERSION="$(grep -E '^melodySyncVersion=' "$PROJECT_DIR/gradle.properties" | cut -d= -f2- | tr -d '[:space:]')"
if [ -z "$VERSION" ]; then
    echo "==> ERROR: melodySyncVersion not found in gradle.properties"
    exit 1
fi

echo "==> Building Melody Sync..."
cd "$PROJECT_DIR"
./gradlew :melody-sync-desktop:packageUberJarForCurrentOS --no-configuration-cache 2>&1 | tail -3

echo "==> Installing to $DATA_DIR..."
mkdir -p "$DATA_DIR"
cp -f "$PROJECT_DIR/melody-sync-desktop/build/compose/jars/melody-sync-linux-x64-$VERSION.jar" "$DATA_DIR/$NAME.jar"
cp -f "$PROJECT_DIR/melody-sync-desktop/src/main/resources/icon.png" "$DATA_DIR/icon.png"
printf '%s\n' "$VERSION" > "$DATA_DIR/VERSION"

cat > "$DATA_DIR/$NAME" <<'SCRIPT'
#!/bin/sh
DIR="$(dirname "$(readlink -f "$0")")"
exec java -jar "$DIR/melody-sync.jar" "$@"
SCRIPT
chmod +x "$DATA_DIR/$NAME"

mkdir -p "$BIN_DIR"
ln -sf "$DATA_DIR/$NAME" "$BIN_DIR/$NAME"

mkdir -p "$DESKTOP_DIR"
cat > "$DESKTOP_DIR/$NAME.desktop" <<DESKTOP
[Desktop Entry]
Version=$VERSION
Name=Melody Sync
Comment=Organize, analyze and explore your local music library
Exec=melody-sync
Icon=$DATA_DIR/icon.png
Terminal=false
Type=Application
Categories=Audio;Music;
Keywords=music;library;organizer;metadata;
DESKTOP

echo ""
echo "🎵 Melody Sync installed!"
echo ""
echo "Run:  melody-sync"
echo "CLI:  melody-sync scan ~/Music"
echo "      melody-sync health ~/Music"
echo "      melody-sync enrich ~/Music (needs YOUTUBE_API_KEY)"
echo ""
echo "To uninstall: $SCRIPT_DIR/uninstall.sh"
