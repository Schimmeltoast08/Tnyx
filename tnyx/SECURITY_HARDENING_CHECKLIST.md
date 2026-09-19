# Tnyx hardening checklist

This file is intentionally part of the source tree so the security work is auditable instead of being hidden in a binary.

## Key lifetime / memory

- [x] `VaultSession` owns the KEK and DEK and exposes operations, not raw keys (`crypto/VaultSession.java`).
- [x] Session close destroys both key buffers and revokes future operations (`crypto/DestroyableSecretKey.java`, `crypto/VaultSession.java`).
- [x] KEK/DEK temporary byte arrays are cleared in `CryptoEngine` (`crypto/CryptoEngine.java`).
- [x] DEK generation no longer relies on a non-destroyable `SecretKeySpec` (`crypto/Encryption.java`).
- [x] Repeated `getDek()` key-copy escape was removed entirely.
- [x] CLI opened vaults use try-with-resources and close their sessions (`Main.java`, `VaultHandler.java`).
- [x] Passwords in `PasswordEntry` are `char[]`, cloned at API boundaries and cleared on close (`vault/PasswordEntry.java`).
- [x] Password entry serialization never creates a password `String` (`vault/PasswordEntrySerializer.java`).
- [x] Master-password inputs are cleared by CLI/GUI callers; Argon2 input bytes are cleared after derivation.
- [x] Master-password UTF-8 size is bounded and malformed UTF-16 is rejected.

## GUI / secret exposure

- [x] Entry panels no longer preload passwords into `JPasswordField`.
- [x] Password text is only placed in the Swing field while focused/revealed and is cleared on focus loss.
- [x] Edit dialogs start with an empty password field; existing passwords are not copied into the dialog.
- [x] Dialog password arrays are cleared.
- [x] Clipboard contents are cleared after 30 seconds and on lock/exit (`ui/ClipboardManager.java`).
- [x] Clipboard clearing is ownership-safe: Tnyx only clears its own copied value.
- [x] Five-minute inactivity auto-lock is implemented with monotonic `System.nanoTime()` timing (`ui/UiManager.java`).
- [x] Manual lock/exit handles unsaved changes explicitly.
- [x] Auto-lock attempts to save dirty changes before locking; if the save cannot complete, it still locks rather than leaving plaintext unlocked.
- [x] Dead config flags for unimplemented sleep/screen-lock hooks and last-vault persistence were removed instead of pretending to work.

## File-system / save integrity

- [x] Vault reads are bounded and performed through a single open channel; no `size()` + later `readAllBytes()` reopen sequence.
- [x] Vault reads reject symlinks and request `NOFOLLOW_LINKS` on the actual file open.
- [x] Writes reject vault symlink paths.
- [x] Saves use a forced temporary file followed by an atomic move; there is no non-atomic fallback.
- [x] New-vault creation refuses to overwrite an existing target.
- [x] Parent directory metadata is force-attempted after atomic replacement where the platform permits it.
- [x] A per-vault sidecar file lock prevents two cooperative Tnyx processes from editing the same vault simultaneously.
- [x] The encrypted file is SHA-256 fingerprinted on open and checked again before save to detect external modification.
- [x] Save updates the in-memory container/fingerprint only after the file replacement succeeds.
- [x] Log directory/file permissions are restricted on POSIX systems and symlinked log files are refused.

## File format / parser

- [x] Unbounded legacy `VaultReader.readVault()` was removed.
- [x] Encrypted container parser has fixed format/KDF/algorithm/nonce/length checks and rejects trailing data.
- [x] Plaintext vault format v4 removes duplicated KDF/salt/encryption/nonce metadata.
- [x] Legacy plaintext v3 is accepted only as a strict migration input; new saves emit v4.
- [x] Entry count is bounded before parsing entries.
- [x] Entry lengths and all field lengths are bounded before allocation.
- [x] Duplicate entry UUIDs are rejected.
- [x] UTF-8 decoding uses `REPORT` for malformed/unmappable input.
- [x] Serialized vault size is bounded before the final output allocation is made.
- [x] `PasswordEntry` setters reject nulls and enforce model invariants.
- [x] Dead `printEntry()` / `getPasswordEntryData()` helpers and duplicate plaintext crypto metadata were removed.

## Crypto format / nonce handling

- [x] Argon2id work factors are fixed and validated; the file cannot select arbitrary cost parameters.
- [x] Each save generates a fresh DEK-wrapping nonce and data nonce.
- [x] The DEK ciphertext is therefore rewrapped on save instead of reusing the old GCM nonce/ciphertext pair.
- [x] AES-256-GCM authentication remains mandatory for vault ciphertext.
- [x] Outer encrypted format remains version 3 for container compatibility while the authenticated plaintext payload is migrated to v4.

## Build / supply chain

- [x] Bouncy Castle is pinned to 1.86.
- [x] JUnit is pinned to 6.1.3.
- [x] Compiler, Enforcer, Jar, Shade, Surefire, CycloneDX, Dependency-Check, and GPG plugin versions are pinned.
- [x] Maven Enforcer requires Java 21+ and dependency convergence and rejects unpinned/snapshot plugins.
- [x] CycloneDX SBOM generation is part of `package`.
- [x] OWASP Dependency-Check is available under `-Psecurity-scan` and fails on CVSS >= 7.
- [x] Reproducible-build timestamp is pinned with `project.build.outputTimestamp`.
- [x] Release signing is configured under `-Prelease-signing` using Maven GPG; a real signing key is intentionally not shipped in source.
- [x] Build artifacts and stale `target/` output are excluded from the source archive.
- [x] The stale nested `src/main/java/com/tnyx.zip` source archive was removed.

## Tests / malformed-input coverage

- [x] Round-trip encryption/decryption.
- [x] Wrong-password authentication failure.
- [x] Ciphertext tamper detection.
- [x] Session-close revocation.
- [x] Fresh save nonces / DEK rewrap.
- [x] Password entry size and malformed UTF-8 rejection.
- [x] Truncation, trailing-data, and entry-count parser rejection.
- [x] Duplicate UUID rejection.
- [x] Vault creation refuses overwrite.
- [x] Concurrent-open lock behavior.
- [x] External modification is detected before save.

## Deliberate limits

- OS memory scraping, swap, hibernation images, and crash dumps cannot be made impossible by Java code alone. Deployment guidance therefore recommends full-disk encryption and disabling/controlling crash dumps on sensitive systems.
- Sleep/screen-lock hooks were not retained as fake settings because a portable, reliable implementation was not present. Inactivity auto-lock is real and enforced.
- Directory `fsync` is attempted where the platform exposes a usable directory channel; some platforms/filesystems do not permit it.
- A Maven Wrapper binary was not fabricated without a Maven runtime/network in the audit environment. The POM is fully pinned and `BUILD.md` documents the exact build commands. A wrapper can be generated later with the project's Maven installation.
