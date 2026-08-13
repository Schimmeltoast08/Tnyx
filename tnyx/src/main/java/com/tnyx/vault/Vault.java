package com.tnyx.vault;

import java.util.ArrayList;
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

    
    private List<PasswordEntry> entries = new ArrayList<>();



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

    public void addEntries(PasswordEntry entry){
        entries.add(entry);
    }

    public void printVault(){
        StringBuilder sb = new StringBuilder();

        for (byte b : salt){
            sb.append(b);
        }
        String saltString = sb.toString();
        sb.setLength(0); // clear the sb

        for (byte b : nonce){
            sb.append(b);
        }
        String nonceString = sb.toString();
        sb.setLength(0);

        /*for (byte b : nonce2){
            sb.append(b);
        }*/

        String nonce2String = sb.toString();

        System.out.println(



            this.vaultFormatVersion + " " +
            this.KDF + " " +
            saltString + " " +
            this.encryptionAlgorithm + " " +
            nonceString + " " +
            this.creationTime + " " +
            this.lastEditedTime + " " +
            nonce2String
        );
    }


}
