# Validation performed for this hardened source archive

Date: 2026-09-19

## Static/source validation

- All Java main sources compile successfully with `javac 21.0.11` against the dependency classes available in the original upload's shaded reference JAR. This validates Java/API compatibility; it does not claim that Bouncy Castle 1.86 itself was executed in this environment.
- All JUnit test sources compile successfully against temporary JUnit API stubs (syntax/API validation only).
- `pom.xml` parses as valid XML.
- No `target/` directory is included.
- The stale nested `src/main/java/com/tnyx.zip` archive is absent.
- The old `util/Config` package is absent.
- Removed APIs (`VaultSession.getDek`, legacy `VaultReader.readVault`, duplicated Vault crypto metadata accessors, `PasswordEntry.printEntry`, `getPasswordEntryData`) have no source references.

## Behavioral validation

A dedicated Java integration harness was run against the hardened source and the available reference dependency JAR from the original upload. It passed all of these checks:

1. AES-GCM encryption/decryption round trip.
2. Wrong master password fails authentication.
3. Independent vaults use independent salt/nonces.
4. Closing `VaultSession` revokes subsequent encryption/rewrap operations.
5. Save generates a fresh DEK-wrapping nonce and ciphertext as well as a fresh data nonce.
6. Plaintext vault serialization round-trips as v4.
7. Legacy plaintext v3 migration parsing succeeds.
8. New vault creation refuses to overwrite an existing file.
9. Cooperative concurrent-open locking rejects a second opener.
10. External modification is detected before save.
11. Encrypted ciphertext tampering fails authentication.


