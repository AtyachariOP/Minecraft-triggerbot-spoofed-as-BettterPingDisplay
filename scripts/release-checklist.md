# Release checklist for Better Ping Display Fabric

1. Confirm the repository is at the intended release tag or commit.
2. Ensure Java 21 is active.
3. Run the clean release build:
   - ./gradlew clean build
4. Verify the output jar exists at build/libs/.
5. Record the SHA-256 hash of the built jar.
6. Compare the hash against the official Modrinth artifact if one is available.
7. Do not modify source files between build runs unless intentionally preparing a new release.
8. Keep the same Git revision and build environment for reproducible output.

## Deterministic build notes
- The build uses Fabric Loom and the standard Gradle jar task.
- Archive timestamps are disabled and file ordering is made reproducible.
- Rebuilding from the same source revision should yield the same SHA-256 hash.
