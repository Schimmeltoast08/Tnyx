package com.tnyx.crypto;

public class OpenedVaultData {

    private final EncryptedVault encryptedVault;
    private final VaultSession session;

    public OpenedVaultData(EncryptedVault encryptedVault, VaultSession session) {
        this.encryptedVault = encryptedVault;
        this.session = session;
    }

    public EncryptedVault getEncryptedVault() {
        return encryptedVault;
    }

    public VaultSession getSession() {
        return session;
    }
}
