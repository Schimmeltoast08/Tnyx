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

## Maven limitation

Maven is not installed in the audit execution environment, and outbound dependency downloads are unavailable. Therefore a real `mvn clean verify` / Surefire execution and OWASP Dependency-Check database scan could not be run here. The POM, test sources, and security profiles were still statically validated, and the application source was compiled and exercised with the integration harness above.

This is intentionally documented rather than presenting stale `target/surefire-reports` from the original upload as evidence. Those generated artifacts were removed from the hardened archive.

## Build-fix validation after Java 26 feedback

The Enforcer configuration was corrected after validation on a JDK 26.0.2 environment. The accepted JDK range is now `[21,27)`, so Java 26 is explicitly supported while Java 27+ requires an intentional project update. Maven lifecycle plugins that were previously supplied only by default lifecycle bindings are now explicitly pinned: clean 3.5.0, resources 3.5.0, install 3.1.4, deploy 3.1.4, and site 3.22.0. This satisfies the `requirePluginVersions` rule for the plugins reported by Maven.

Maven itself was not available in this audit environment, so this specific correction was validated statically against the reported Enforcer failures rather than by claiming a local `mvn clean verify` run.
