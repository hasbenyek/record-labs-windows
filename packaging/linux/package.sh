#!/usr/bin/env bash
# Builds the Linux installers from the Compose app image (./gradlew createDistributable):
#
#   record-labs_<v>_amd64.deb            Ubuntu, Debian, Linux Mint, Pop!_OS
#   record-labs-<v>-1.x86_64.rpm         Fedora, openSUSE
#   RecordLabs-<v>-x86_64.AppImage       any distribution (no install needed)
#   record-labs-<v>-linux-x86_64.tar.xz  + PKGBUILD for Arch Linux (AUR)
#
# Usage: packaging/linux/package.sh [version]
# Needs: dpkg-deb, rpmbuild, xz; appimagetool (on PATH or $APPIMAGETOOL) for the AppImage.
set -euo pipefail

ROOT="$(cd "$(dirname "$0")/../.." && pwd)"
HERE="$ROOT/packaging/linux"
VERSION="${1:-$(sed -n 's/^version = "\(.*\)"/\1/p' "$ROOT/build.gradle.kts" | tr -d '\r' | head -1)}"
APP_IMAGE="${APP_IMAGE_DIR:-$(find "$ROOT/build" -maxdepth 6 -type d -path '*binaries/main/app/*' -name 'Record Labs' | head -1)}"
OUT="$ROOT/build/linux-packages"
WORK="$ROOT/build/linux-packaging"
NAME="record-labs"
INSTALL_DIR="/opt/$NAME"
LAUNCHER="Record Labs"

[ -n "$APP_IMAGE" ] && [ -x "$APP_IMAGE/bin/$LAUNCHER" ] || { echo "App image not found. Run ./gradlew createDistributable first."; exit 1; }
[ -f "$APP_IMAGE/lib/app/resources/vlc/libvlc.so.5" ] || echo "warning: no bundled VLC in the app image (run packaging/linux/bundle-vlc.sh before createDistributable)"
echo "Packaging Record Labs $VERSION from: $APP_IMAGE"

rm -rf "$OUT" "$WORK"
mkdir -p "$OUT" "$WORK"

# ---- shared file tree (what lands on the user's disk) -------------------------
stage_tree() {
  local dest="$1"
  mkdir -p "$dest$INSTALL_DIR" "$dest/usr/bin" "$dest/usr/share/applications" \
    "$dest/usr/share/icons/hicolor/256x256/apps" "$dest/usr/share/metainfo"
  cp -a "$APP_IMAGE/." "$dest$INSTALL_DIR/"
  install -m 755 "$HERE/record-labs.sh" "$dest/usr/bin/$NAME"
  install -m 644 "$HERE/record-labs.desktop" "$dest/usr/share/applications/$NAME.desktop"
  install -m 644 "$ROOT/src/main/resources/recordlabs.png" "$dest/usr/share/icons/hicolor/256x256/apps/$NAME.png"
  sed "s/@VERSION@/$VERSION/; s/@DATE@/$(date -u +%Y-%m-%d)/" "$HERE/record-labs.metainfo.xml" \
    > "$dest/usr/share/metainfo/$NAME.metainfo.xml"
  # Marks the installation type for the in-app updater.
  echo "${2:-tar}" > "$dest$INSTALL_DIR/lib/app/package-type"
}

installed_kb() { du -sk "$1" | cut -f1; }

# ---- .deb ---------------------------------------------------------------------
build_deb() {
  local root="$WORK/deb"
  stage_tree "$root" deb
  mkdir -p "$root/DEBIAN"
  # Libraries the app takes from the system (everything else is bundled).
  # Alternatives cover Debian/Ubuntu before and after the 64-bit time_t rename.
  cat > "$root/DEBIAN/control" <<EOF
Package: $NAME
Version: $VERSION
Section: sound
Priority: optional
Architecture: amd64
Maintainer: Record Labs <hasbenyek@users.noreply.github.com>
Installed-Size: $(installed_kb "$root")
Depends: libc6 (>= 2.35), libasound2t64 | libasound2, libpulse0, libx11-6, libx11-xcb1, libxext6, libxi6, libxrender1, libxtst6, libxcb1, libxcb-render0, libxcb-shm0, libgl1, libfontconfig1, libfreetype6, libharfbuzz0b, libfribidi0, libglib2.0-0t64 | libglib2.0-0, libdbus-1-3, libdrm2, zlib1g, xdg-utils
Recommends: pipewire-pulse | pulseaudio
Homepage: https://github.com/hasbenyek/record-labs-windows
Description: Record Labs desktop music player
 Music player for YouTube Music with synced lyrics, music videos,
 Listen Together and Record Labs Connect.
EOF
  cat > "$root/DEBIAN/postinst" <<'EOF'
#!/bin/sh
set -e
command -v update-desktop-database >/dev/null 2>&1 && update-desktop-database -q /usr/share/applications || true
command -v gtk-update-icon-cache >/dev/null 2>&1 && gtk-update-icon-cache -q -t -f /usr/share/icons/hicolor || true
exit 0
EOF
  cp "$root/DEBIAN/postinst" "$root/DEBIAN/postrm"
  chmod 755 "$root/DEBIAN/postinst" "$root/DEBIAN/postrm"
  dpkg-deb --root-owner-group -Zxz -z9 --build "$root" "$OUT/${NAME}_${VERSION}_amd64.deb"
}

# ---- .rpm ---------------------------------------------------------------------
build_rpm() {
  local top="$WORK/rpm"
  mkdir -p "$top"/{BUILD,RPMS,SOURCES,SPECS,SRPMS,BUILDROOT}
  local tree="$WORK/rpm-tree"
  stage_tree "$tree" rpm
  # Requirements by library name (soname) work on both Fedora and openSUSE,
  # whose package names differ.
  cat > "$top/SPECS/$NAME.spec" <<EOF
Name:           $NAME
Version:        $VERSION
Release:        1
Summary:        Record Labs desktop music player
License:        Proprietary
URL:            https://github.com/hasbenyek/record-labs-windows
AutoReqProv:    no
Requires:       libasound.so.2()(64bit), libpulse.so.0()(64bit), libX11.so.6()(64bit), libX11-xcb.so.1()(64bit), libXext.so.6()(64bit), libXi.so.6()(64bit), libXrender.so.1()(64bit), libXtst.so.6()(64bit), libxcb.so.1()(64bit), libGL.so.1()(64bit), libfontconfig.so.1()(64bit), libfreetype.so.6()(64bit), libharfbuzz.so.0()(64bit), libfribidi.so.0()(64bit), libglib-2.0.so.0()(64bit), libdbus-1.so.3()(64bit), libdrm.so.2()(64bit), libz.so.1()(64bit), xdg-utils
%global __os_install_post %{nil}
%global debug_package %{nil}
%global _build_id_links none
%define _binary_payload w9.xzdio

%description
Music player for YouTube Music with synced lyrics, music videos,
Listen Together and Record Labs Connect.

%install
cp -a "$tree/." %{buildroot}/

%post
update-desktop-database -q /usr/share/applications >/dev/null 2>&1 || :
gtk-update-icon-cache -q -t -f /usr/share/icons/hicolor >/dev/null 2>&1 || :

%postun
update-desktop-database -q /usr/share/applications >/dev/null 2>&1 || :

%files
$INSTALL_DIR
/usr/bin/$NAME
/usr/share/applications/$NAME.desktop
/usr/share/icons/hicolor/256x256/apps/$NAME.png
/usr/share/metainfo/$NAME.metainfo.xml
EOF
  rpmbuild --quiet --define "_topdir $top" -bb "$top/SPECS/$NAME.spec"
  find "$top/RPMS" -name '*.rpm' -exec cp {} "$OUT/" \;
}

# ---- tar.xz + PKGBUILD (Arch) ---------------------------------------------------
build_tarball() {
  local tree="$WORK/tar-tree"
  stage_tree "$tree" tar
  local tarball="$NAME-$VERSION-linux-x86_64.tar.xz"
  tar -C "$tree" --owner=0 --group=0 -cf - . | xz -T0 -9 > "$OUT/$tarball"
  local sum
  sum=$(sha256sum "$OUT/$tarball" | cut -d' ' -f1)
  sed "s/@VERSION@/$VERSION/; s/@SHA256@/$sum/" "$HERE/PKGBUILD.in" > "$OUT/PKGBUILD"
}

# ---- AppImage -----------------------------------------------------------------
build_appimage() {
  local appdir="$WORK/RecordLabs.AppDir"
  stage_tree "$appdir" appimage
  install -m 755 "$HERE/AppRun" "$appdir/AppRun"
  cp "$appdir/usr/share/applications/$NAME.desktop" "$appdir/$NAME.desktop"
  cp "$appdir/usr/share/icons/hicolor/256x256/apps/$NAME.png" "$appdir/$NAME.png"
  ln -sf "$NAME.png" "$appdir/.DirIcon"
  local tool="${APPIMAGETOOL:-$(command -v appimagetool || true)}"
  if [ -z "$tool" ]; then
    echo "appimagetool not found: AppDir left at $appdir (no .AppImage built)"
    return 0
  fi
  # The update information lets AppImage tools (and the in-app updater) find newer releases.
  ARCH=x86_64 "$tool" --no-appstream --comp zstd \
    -u "gh-releases-zsync|hasbenyek|record-labs-windows|latest|RecordLabs-*-x86_64.AppImage.zsync" \
    "$appdir" "$OUT/RecordLabs-$VERSION-x86_64.AppImage"
  # zsync file is written next to the working directory by appimagetool.
  find . -maxdepth 1 -name "RecordLabs-$VERSION-x86_64.AppImage.zsync" -exec mv {} "$OUT/" \; || true
}

build_deb
build_rpm
build_tarball
build_appimage

(cd "$OUT" && sha256sum ./* > SHA256SUMS)
echo
ls -la "$OUT"
