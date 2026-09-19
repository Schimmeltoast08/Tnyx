package com.tnyx.vault;

import com.tnyx.util.Log;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

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
        return salt.clone();
    }

    public void setSalt(byte[] salt) {
        if (salt == null) throw new IllegalArgumentException("salt must not be null");
        this.salt = salt.clone();
    }

    public String getEncryptionAlgorithm() {
        return encryptionAlgorithm;
    }

    public void setEncryptionAlgorithm(String encryptionAlgorithm) {
        this.encryptionAlgorithm = encryptionAlgorithm;
    }

    public byte[] getNonce() {
        return nonce.clone();
    }

    public void setNonce(byte[] nonce) {
        if (nonce == null) throw new IllegalArgumentException("nonce must not be null");
        this.nonce = nonce.clone();
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
        return nonce2.clone();
    }

    public void setNonce2(byte[] nonce2) {
        if (nonce2 == null) throw new IllegalArgumentException("nonce2 must not be null");
        this.nonce2 = nonce2.clone();
    }

    public List<PasswordEntry> getEntries() {
        return Collections.unmodifiableList(entries);
    }

    public PasswordEntry getEntry(UUID uuid){

        for (PasswordEntry entry : entries){
            if (entry.getId().equals(uuid)){
                return entry;
            }
        }

        Log.log("No entry found with UUID " + uuid, 3);
        throw new IllegalArgumentException("No entry found with UUID " + uuid);
    }


    public void addEntry(PasswordEntry entry) {
        if (entry == null) throw new IllegalArgumentException("entry must not be null");
        if (entries.stream().anyMatch(existing -> existing.getId().equals(entry.getId()))) {
            throw new IllegalArgumentException("Duplicate entry UUID");
        }
        if (entries.size() >= com.tnyx.crypto.CryptoConstants.MAX_ENTRIES) {
            throw new IllegalArgumentException("Too many entries");
        }
        entries.add(entry);
    }

    public void removeEntry(UUID id) {
        Log.log("Removing Entry " + id, 1);

        boolean removed = entries.removeIf(entry -> entry.getId().equals(id));

        if (!removed) {
            Log.log("Removal of entry " + id + " failed! No such entry", 4);
            throw new IllegalArgumentException("No entry found with ID: " + id);
        }
    }

    public void printVault() {
        StringBuilder sb = new StringBuilder();

        for (byte b : salt) {
            sb.append(b);
        }
        String saltString = sb.toString();
        sb.setLength(0); // clear the sb

        for (byte b : nonce) {
            sb.append(b);
        }
        String nonceString = sb.toString();
        sb.setLength(0);

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

        // full disclosure: From here up to the "[Ai]" are generated using ChatGPT. I did it all myself, chat did the pretty formatting, nothing more, nothing less.
        System.out.println("======================================== VAULT ========================================");
        System.out.printf("Format Version : %-10d%n", vaultFormatVersion);
        System.out.printf("KDF            : %-10s%n", KDF);
        System.out.printf("Salt           : %-10s%n", saltString);
        System.out.printf("Encryption     : %-10s%n", encryptionAlgorithm);
        System.out.printf("Nonce          : %-10s%n", nonceString);
        System.out.printf("Nonce 2        : %-10s%n", nonce2String);
        System.out.printf("Created        : %-10s%n", humanReadableCreationTime);
        System.out.printf("Last Edited    : %-10s%n", humanReadableLastEditedTime);

        System.out.println();
        System.out.println("======================================= ENTRIES =======================================");

        // Table header
        System.out.printf(
                "%-4s %-25s %-25s %-25s %-35s%n",
                "ID",
                "Name",
                "Username",
                "Password",
                "URL"
        );

        System.out.println(
                "---- ------------------------- ------------------------- ------------------------- -----------------------------------"
        );

        // Entries
        int id = 1;

        for (PasswordEntry entry : entries) {

            int pwlength = entry.getPassword().length();
            StringBuilder pwb = new StringBuilder();
            pwb.append("*".repeat(pwlength));


            System.out.printf(
                    "%-4d %-25s %-25s %-25s %-35s%n", // [Ai]
                    id,
                    entry.getName(),
                    entry.getUsername(),
                    pwb.toString(),
                    entry.getUrl()
            );

            id++;
        }

        System.out.println(
                "========================================================================================"
        );

    }

}
