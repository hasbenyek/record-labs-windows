# Record Labs Windows

Desktop port foundation for Record Labs. This project is intentionally isolated
from the Android application in the parent directory.

## Current state

- Kotlin/JVM desktop application
- Compose Desktop window shell
- Record Labs navigation and playback-bar foundation
- Windows `.exe` and `.msi` packaging configuration
- JDK/runtime and Compose dependencies are bundled by the native distribution
- Platform-neutral contracts for catalog, lyrics, downloads, and playback

## Run on Windows

From the parent project directory:

```powershell
.\gradlew.bat -p .\windows-recordlabs run
```

## Build installers

```powershell
.\gradlew.bat -p .\windows-recordlabs packageExe packageMsi
```

The next stage is implementing the contracts with the existing Record Labs
networking sources, then replacing the placeholder playback layer with a
Windows media backend.

## Building from this repository

This repository has its own Gradle wrapper, so from a clone:

```powershell
.\gradlew.bat run
.\gradlew.bat packageMsi
```

Releases on this repository's GitHub page (tag `vX.Y.Z` with the `.msi` attached)
are picked up by the app's in-app updater.

## Listen Together

Listen Together uses the Record Labs server, which only accepts official builds.
Official builds put the private key in `local.properties` (never committed):

```properties
LISTEN_TOGETHER_KEY=...
```

Builds without the key work normally, except that the Record Labs Listen Together
server refuses them. To use Listen Together in your own build, run your own server
(github.com/hasbenyek/record-labs-listen-server) and change
`WindowsListenTogetherClient.serverUrl`.
