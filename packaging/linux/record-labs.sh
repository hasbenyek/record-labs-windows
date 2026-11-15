#!/bin/sh
# Starts Record Labs installed under /opt/record-labs.
# Java reads file names with the system locale; desktops set a UTF-8 one, but
# a bare session may not, which would break non-ASCII song and folder names.
if [ -z "${LANG:-}${LC_ALL:-}${LC_CTYPE:-}" ]; then export LANG=C.UTF-8; fi
# Memory: glibc gives every busy thread (Java, VLC, the graphics) its own memory pool,
# and with many threads most of those pools stay large and nearly empty (hundreds of MB
# that show as the app's own). Two pools are plenty for a music player.
export MALLOC_ARENA_MAX="${MALLOC_ARENA_MAX:-2}"
exec "/opt/record-labs/bin/Record Labs" "$@"
