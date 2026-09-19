# Building Tnyx

## Requirements

- Java 21 through 26.
- Maven 3.9.16 or newer.

The project is configured for Java 21 bytecode so the hardened build is usable on current LTS JDKs while remaining compatible with newer JDKs.

## Normal build

```text
mvn clean verify
```

`package` also generates a CycloneDX SBOM.

## Dependency security scan

```text
mvn clean verify -Psecurity-scan
```

The OWASP Dependency-Check profile fails the build for vulnerabilities at CVSS 7 or higher. Its vulnerability database is external and must be available to Maven/Dependency-Check.

## Release signing

Configure a trusted GPG signing key in your build environment, then run:

```text
mvn clean verify -Prelease-signing
```

