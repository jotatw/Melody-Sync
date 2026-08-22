#!/usr/bin/env bash
#
# Product Validation — automated workflow chain.
# Builds a realistic library in a temporary directory and drives the CLI
# end-to-end: scan -> health -> metadata/write-test -> duplicates -> organize.
# See docs/planning/product-validation.md (Track 1).
#
# Usage:
#   scripts/validate-workflow.sh          # run and clean up
#   scripts/validate-workflow.sh --keep   # keep the work dir for inspection
#
# Exit code 0 if all checks pass, 1 otherwise.

set -uo pipefail

ROOT="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
FIXTURES="$ROOT/melody-sync-core/src/test/resources/fixtures/audio"

KEEP=0
[ "${1:-}" = "--keep" ] && KEEP=1

BASE="$(mktemp -d)"
WORK="$BASE/lib"
DB="$BASE/db.sqlite"
mkdir -p "$WORK"

PASS=0
FAIL=0
ok()  { echo "  [PASS] $1"; PASS=$((PASS + 1)); }
bad() { echo "  [FAIL] $1"; FAIL=$((FAIL + 1)); }

check() { # check <label> <haystack> <needle>
  if printf '%s' "$2" | grep -qF "$3"; then ok "$1"; else bad "$1"; fi
}

run_cli() {
  (cd "$ROOT" && ./gradlew -q :melody-sync-cli:run --args="$1") 2>&1
}

echo "=== Building realistic library at $WORK ==="

for fmt in mp3 flac m4a ogg opus wav; do
  for f in "$FIXTURES/$fmt/"*".$fmt"; do
    [ -e "$f" ] && cp "$f" "$WORK/"
  done
done
[ -e "$FIXTURES/aac/no_tags.aac" ] && cp "$FIXTURES/aac/no_tags.aac" "$WORK/"

# Non-audio file must be treated as non-audio.
printf 'not an audio file\n' > "$WORK/readme.txt"

# A duplicate pair: two byte-identical copies of the same song.
cp "$FIXTURES/mp3/with_tags.mp3" "$WORK/copy1.mp3"
cp "$FIXTURES/mp3/with_tags.mp3" "$WORK/copy2.mp3"

# A file with a messy filename.
cp "$FIXTURES/flac/no_tags.flac" "$WORK/_MIXED_CASE_.Flac"

echo
echo "=== 1. Scan ==="
SCAN="$(run_cli "scan $WORK --persist --db $DB")"
check "scan discovers audio files" "$SCAN" "Database now holds"
SCC="$(printf '%s' "$SCAN" | sed -n 's/.*Database now holds \([0-9][0-9]*\) songs.*/\1/p' | head -1)"
echo "      (scanned songs: ${SCC:-unknown})"

echo
echo "=== 2. Health ==="
HEALTH="$(run_cli "health $WORK")"
check "health analyzes the library" "$HEALTH" "Files:"
check "health reports issue counts" "$HEALTH" "Metadata issues:"

echo
echo "=== 3. Metadata + write-test per format ==="
for fmt in mp3 flac m4a ogg opus; do
  OUT="$(run_cli "metadata --write-test $WORK/with_tags.$fmt")"
  check "write-test $fmt persists" "$OUT" "Write test:   passed"
done
WAV="$(run_cli "metadata --write-test $WORK/with_tags.wav")"
check "WAV write refused (read-only)" "$WAV" "Write:        no"

echo
echo "=== 4. Duplicates ==="
DUP="$(run_cli "duplicates $WORK --db $DB")"
check "duplicates detection runs" "$DUP" "Suggestions (for your review)"

echo
echo "=== 5. Organize (plan, dry-run) ==="
ORG="$(run_cli "organize $WORK --db $DB")"
check "organize produces a plan" "$ORG" "Nothing was moved. Run with --apply"

echo
echo "=== 6. Doctor ==="
DOC="$(run_cli "doctor")"
if printf '%s' "$DOC" | grep -qE "Everything looks healthy|issue"; then ok "doctor completes"; else bad "doctor completes"; fi

echo
echo "=== Summary: $PASS passed, $FAIL failed ==="
if [ "$KEEP" = "1" ]; then
  echo "Work dir kept at: $WORK"
  echo "Database at:      $DB"
else
  rm -rf "$BASE"
fi
[ "$FAIL" -eq 0 ]
