package com.tnyx.crypto;

import com.tnyx.vault.Vault;
import com.tnyx.vault.VaultFileLock;

import java.nio.file.Path;
import java.util.Arrays;

public final class OpenedVault implements AutoCloseable {
    private final Vault vault;
    private final VaultSession session;
    private final Path path;
    private final VaultFileLock fileLock;
    private byte[] encryptedFileFingerprint;
    private EncryptedVault encryptedVault;
    private boolean closed;

    public OpenedVault(Vault vault, VaultSession session, EncryptedVault encryptedVault,
                        Path path, VaultFileLock fileLock, byte[] encryptedFileFingerprint) {
        if (vault == null || session == null || encryptedVault == null || path == null || fileLock == null || encryptedFileFingerprint == null) {
            throw new IllegalArgumentException("Opened vault components must not be null");
        }
        this.vault = vault;
        this.session = session;
        this.encryptedVault = encryptedVault;
        this.path = path.toAbsolutePath().normalize();
        this.fileLock = fileLock;
        this.encryptedFileFingerprint = encryptedFileFingerprint.clone();
    }

    public Vault getVault() { ensureOpen(); return vault; }
    public VaultSession getSession() { ensureOpen(); return session; }
    public EncryptedVault getEncryptedVault() { ensureOpen(); return encryptedVault; }
    public Path getPath() { return path; }
    public byte[] getFingerprint() { ensureOpen(); return encryptedFileFingerprint.clone(); }

    public void setEncryptedVault(EncryptedVault encryptedVault, byte[] fingerprint) {
        ensureOpen();
        if (encryptedVault == null || fingerprint == null) throw new IllegalArgumentException("Encrypted vault data must not be null");
        this.encryptedVault = encryptedVault;
        Arrays.fill(this.encryptedFileFingerprint, (byte) 0);
        this.encryptedFileFingerprint = fingerprint.clone();
    }

    @Override
    public synchronized void close() {
        if (closed) return;
        closed = true;
        try { vault.close(); } finally {
            try { session.close(); } finally {
                Arrays.fill(encryptedFileFingerprint, (byte) 0);
                fileLock.close();
            }
        }
    }

    private synchronized void ensureOpen() {
        if (closed) throw new IllegalStateException("Opened vault is closed");
    }
}
