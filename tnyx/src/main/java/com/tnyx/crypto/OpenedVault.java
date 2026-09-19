package com.tnyx.crypto;

import com.tnyx.vault.Vault;

public final class OpenedVault implements AutoCloseable {
    private final Vault vault;
    private final VaultSession session;
    private EncryptedVault encryptedVault;

    public OpenedVault(Vault vault, VaultSession session, EncryptedVault encryptedVault) {
        if (vault == null || session == null || encryptedVault == null) {
            throw new IllegalArgumentException("Opened vault components must not be null");
        }
        this.vault = vault;
        this.session = session;
        this.encryptedVault = encryptedVault;
    }

    public Vault getVault() { return vault; }
    public VaultSession getSession() { return session; }
    public EncryptedVault getEncryptedVault() { return encryptedVault; }

    public void setEncryptedVault(EncryptedVault encryptedVault) {
        if (encryptedVault == null) throw new IllegalArgumentException("Encrypted vault must not be null");
        this.encryptedVault = encryptedVault;
    }

    @Override
    public void close() {
        session.close();
    }
}
