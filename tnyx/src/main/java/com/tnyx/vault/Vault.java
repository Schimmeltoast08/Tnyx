package com.tnyx.vault;

import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

public class Vault {

    int vaultFormatVersion = 0;
    String KDF = "";
    byte[] salt = {};
    String encryptionAlgorithm = "";
    byte[] nonce = {};
    long creationTime = 0;
    long lastEditedTime = 0;
    byte[] nonce2 = {}; // in case a field is empty, avoid nullpointer exception //hope this does not break things


    
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

// EpochSecond to Human Readable format in Print
        Instant creationInstant = Instant.ofEpochSecond(this.creationTime);
        Instant lastEditedInstant = Instant.ofEpochSecond(lastEditedTime);

        ZonedDateTime CreationZonedDateTime = creationInstant.atZone(ZoneId.of("UTC"));
        ZonedDateTime lastEditedZonedDateTime = lastEditedInstant.atZone(ZoneId.of("UTC"));
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm:ss");
 
        String humanReadableCreationTime = CreationZonedDateTime.format(formatter);
        String humanReadableLastEditedTime = lastEditedZonedDateTime.format(formatter);
//

        System.out.println(
            this.vaultFormatVersion + " " +
            this.KDF + " " +
            saltString + " " +
            this.encryptionAlgorithm + " " +
            nonceString + " " +
            "created: " + humanReadableCreationTime + " | " +
            "last edited: " + humanReadableLastEditedTime + " " +
            nonce2String +
            "\nName  Username  Password   URL"
        );
        for (PasswordEntry n : entries){
            System.out.println(n.getPasswordEntryData());
        }
    }


}
