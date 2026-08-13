package com.tnyx.vault;

import java.util.List;

public class Vault {

    int vaultFormatVersion;
    String KDF;
    byte[] salt;
    String encryptionAlgorithm;
    byte[] nonce;
    long creationTime;
    long lastEditedTime;
    byte[] nonce2;
    
    private List<PasswordEntry> entries;

    public int getVaultFormatVersion() {
        return vaultFormatVersion;
    }

    public void setVaultFormatVersion(int vaultFormatVersion) {
        this.vaultFormatVersion = vaultFormatVersion;
    }

    public String getKDF() {
        return KDF;
    }

    public void setKDF(String KDF) {
        this.KDF = KDF;
    }

    public byte[] getSalt() {
        return salt;
    }

    public void setSalt(byte[] salt) {
        this.salt = salt;
    }

    public String getEncryptionAlgorithm() {
        return encryptionAlgorithm;
    }

    public void setEncryptionAlgorithm(String encryptionAlgorithm) {
        this.encryptionAlgorithm = encryptionAlgorithm;
    }

    public byte[] getNonce() {
        return nonce;
    }

    public void setNonce(byte[] nonce) {
        this.nonce = nonce;
    }

    public long getCreationTime() {
        return creationTime;
    }

    public void setCreationTime(long creationTime) {
        this.creationTime = creationTime;
    }

    public long getLastEditedTime() {
        return lastEditedTime;
    }

    public void setLastEditedTime(long lastEditedTime) {
        this.lastEditedTime = lastEditedTime;
    }

    public byte[] getNonce2() {
        return nonce2;
    }

    public void setNonce2(byte[] nonce2) {
        this.nonce2 = nonce2;
    }

    public List<PasswordEntry> getEntries() {
        return entries;
    }

    public void setEntries(List<PasswordEntry> entries) {
        this.entries = entries;
    }

}
