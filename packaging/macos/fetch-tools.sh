#!/usr/bin/env bash
# Downloads the macOS helper programs Record Labs bundles (yt-dlp, Deno) for
# this Mac's architecture into src/appResources/macos-arm64 or macos-x64,
# where `./gradlew run` and the packaging tasks pick them up. Not committed
# to git; CI runs this before building (the Gradle build also runs it).
set -euo pipefail

ROOT="$(cd "$(dirname "$0")/../.." && pwd)"
ARCH="${MAC_ARCH:-$(uname -m)}"
case "$ARCH" in
  arm64|aarch64) DIR=macos-arm64; DENO_TARGET=aarch64-apple-darwin ;;
  x86_64)        DIR=macos-x64;   DENO_TARGET=x86_64-apple-darwin ;;
  *) echo "Unsupported Mac architecture: $ARCH"; exit 1 ;;
esac
DEST="$ROOT/src/appResources/$DIR"
YTDLP_VERSION="${YTDLP_VERSION:-latest}"
DENO_VERSION="${DENO_VERSION:-latest}"
mkdir -p "$DEST"
TMP="$(mktemp -d)"
trap 'rm -rf "$TMP"' EXIT

# yt-dlp_macos is a universal build (Apple Silicon + Intel).
if [ "$YTDLP_VERSION" = latest ]; then
  YTDLP_URL="https://github.com/yt-dlp/yt-dlp/releases/latest/download/yt-dlp_macos"
else
  YTDLP_URL="https://github.com/yt-dlp/yt-dlp/releases/download/$YTDLP_VERSION/yt-dlp_macos"
fi
echo "Downloading yt-dlp ($YTDLP_VERSION) for macOS"
curl -fL --retry 3 -o "$DEST/yt-dlp" "$YTDLP_URL"
chmod +x "$DEST/yt-dlp"

if [ "$DENO_VERSION" = latest ]; then
  DENO_URL="https://github.com/denoland/deno/releases/latest/download/deno-$DENO_TARGET.zip"
else
  DENO_URL="https://github.com/denoland/deno/releases/download/$DENO_VERSION/deno-$DENO_TARGET.zip"
fi
echo "Downloading Deno ($DENO_VERSION) for $DENO_TARGET"
curl -fL --retry 3 -o "$TMP/deno.zip" "$DENO_URL"
unzip -o -q "$TMP/deno.zip" -d "$TMP"
install -m 755 "$TMP/deno" "$DEST/deno"

echo "Done ($DIR): $(ls "$DEST" | tr '\n' ' ')"
