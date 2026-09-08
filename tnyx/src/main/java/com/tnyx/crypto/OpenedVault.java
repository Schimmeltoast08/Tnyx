package com.tnyx.crypto;

import com.tnyx.vault.Vault;

public class OpenedVault {

    private final Vault vault;
    private final VaultSession session;
    private EncryptedVault encryptedVault;

    public OpenedVault(Vault vault, VaultSession session, EncryptedVault encryptedVault) {
        this.vault = vault;
        this.session = session;
        this.encryptedVault = encryptedVault;
    }

    public Vault getVault() {
        return vault;
    }

    public VaultSession getSession() {
        return session;
    }

    public EncryptedVault getEncryptedVault() {
        return encryptedVault;
    }

    public void setEncryptedVault(EncryptedVault encryptedVault) {
        this.encryptedVault = encryptedVault;
    }
}
