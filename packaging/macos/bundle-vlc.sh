#!/usr/bin/env bash
# Copies libVLC and its plugins from a VLC.app into
# src/appResources/macos-arm64/vlc or macos-x64/vlc, so the Mac app plays
# without VLC installed. The VLC.app must match this Mac's architecture
# (the arm64 build on Apple Silicon, the Intel build on Intel).
#
#   VLC_APP=/path/to/VLC.app packaging/macos/bundle-vlc.sh   (default /Applications/VLC.app)
#
# Layout (the same as inside VLC.app, so the plugins' @loader_path/../lib
# links keep working):
#   vlc/lib/libvlc.dylib, libvlccore.dylib
#   vlc/plugins/*.dylib (+ plugins.dat cache)
set -euo pipefail

ROOT="$(cd "$(dirname "$0")/../.." && pwd)"
APP="${VLC_APP:-/Applications/VLC.app}"
SRC="$APP/Contents/MacOS"
ARCH="${MAC_ARCH:-$(uname -m)}"
case "$ARCH" in
  arm64|aarch64) DIR=macos-arm64; NEED=arm64 ;;
  x86_64)        DIR=macos-x64;   NEED=x86_64 ;;
  *) echo "Unsupported Mac architecture: $ARCH"; exit 1 ;;
esac
DEST="$ROOT/src/appResources/$DIR/vlc"

[ -f "$SRC/lib/libvlc.dylib" ] || { echo "libvlc.dylib not found in $SRC/lib (install VLC, e.g. brew install --cask vlc)"; exit 1; }
[ -d "$SRC/plugins" ] || { echo "VLC plugins not found in $SRC/plugins"; exit 1; }
archs="$(lipo -archs "$SRC/lib/libvlc.dylib")"
case " $archs " in
  *" $NEED "*) ;;
  *) echo "This VLC.app is built for '$archs', but this Mac needs $NEED"; exit 1 ;;
esac

rm -rf "$DEST"
mkdir -p "$DEST/lib" "$DEST/plugins"
cp -a "$SRC/lib/." "$DEST/lib/"
cp -a "$SRC/plugins/." "$DEST/plugins/"

echo "VLC $(defaults read "$APP/Contents/Info.plist" CFBundleShortVersionString 2>/dev/null || echo '?') ($archs) bundled into $DEST"
echo "  lib: $(ls "$DEST/lib" | tr '\n' ' ')"
echo "  plugins: $(ls "$DEST/plugins" | wc -l | tr -d ' ') files, $(du -sh "$DEST" | cut -f1) total"
otool -L "$DEST/lib/libvlc.dylib" | sed -n '1,4p'
