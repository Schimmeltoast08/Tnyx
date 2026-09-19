package com.tnyx.vault;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.List;

import com.tnyx.util.Log;

public class VaultSerializer {

    public static byte[] serializeVault(Vault vault) throws IOException {

        List<PasswordEntry> entries = vault.getEntries();

        ByteArrayOutputStream out = new ByteArrayOutputStream();

        // [Format]
        out.write("[Format]".getBytes(StandardCharsets.UTF_8));

        ByteBuffer formatVersion = ByteBuffer.allocate(4);
        formatVersion.putInt(vault.getVaultFormatVersion());
        out.write(formatVersion.array());

        // [KDF]
        out.write("[KDF]".getBytes(StandardCharsets.UTF_8));

        byte[] kdfBytes
                = vault.getKDF().getBytes(StandardCharsets.UTF_8);

        ByteBuffer kdfLength = ByteBuffer.allocate(4);
        kdfLength.putInt(kdfBytes.length);
        out.write(kdfLength.array());

        out.write(kdfBytes);

        ByteBuffer saltLength = ByteBuffer.allocate(4);
        saltLength.putInt(vault.getSalt().length);
        out.write(saltLength.array());

        out.write(vault.getSalt());

        // [Encryption]
        out.write("[Encryption]".getBytes(StandardCharsets.UTF_8));

        byte[] encryptionAlgorithmBytes
                = vault.getEncryptionAlgorithm()
                        .getBytes(StandardCharsets.UTF_8);

        ByteBuffer encryptionAlgorithmLength
                = ByteBuffer.allocate(4);

        encryptionAlgorithmLength.putInt(
                encryptionAlgorithmBytes.length
        );

        out.write(encryptionAlgorithmLength.array());

        out.write(encryptionAlgorithmBytes);

        ByteBuffer nonceLength = ByteBuffer.allocate(4);
        nonceLength.putInt(vault.getNonce().length);
        out.write(nonceLength.array());

        out.write(vault.getNonce());

        // [Time]
        out.write("[Time]".getBytes(StandardCharsets.UTF_8));

        ByteBuffer creationTime = ByteBuffer.allocate(8);
        creationTime.putLong(vault.getCreationTime());
        out.write(creationTime.array());

        ByteBuffer lastEditedTime = ByteBuffer.allocate(8);
        lastEditedTime.putLong(vault.getLastEditedTime());
        out.write(lastEditedTime.array());

        // [Data]
        out.write("[Data]".getBytes(StandardCharsets.UTF_8));

        ByteBuffer nonce2Length = ByteBuffer.allocate(4);
        nonce2Length.putInt(vault.getNonce2().length);
        out.write(nonce2Length.array());

        out.write(vault.getNonce2());

        // Number of entries
        ByteBuffer entryCount = ByteBuffer.allocate(4);
        entryCount.putInt(entries.size());
        out.write(entryCount.array());

        // Entries
        for (PasswordEntry entry : entries) {

            byte[] entryData
                    = PasswordEntrySerializer.serializePasswordEntry(entry);

            // Length of entry
            ByteBuffer entryLength = ByteBuffer.allocate(4);
            entryLength.putInt(entryData.length);
            out.write(entryLength.array());

            // Entry itself
            out.write(entryData);
        }

        return out.toByteArray();
    }

    public static Vault deserializeVault(byte[] vaultData) {

        ByteBuffer buffer = ByteBuffer.wrap(vaultData);

        readMarker(buffer, "[Format]");

        int vaultFormatVersion = buffer.getInt();

        readMarker(buffer, "[KDF]");

        int kdfLength = buffer.getInt();
        validateLength(kdfLength, buffer, "KDF");

        byte[] kdfBytes = new byte[kdfLength];
        buffer.get(kdfBytes);
        String kdf = new String(kdfBytes, StandardCharsets.UTF_8);

        int saltLength = buffer.getInt();
        validateLength(saltLength, buffer, "salt");

        byte[] salt = new byte[saltLength];
        buffer.get(salt);

        readMarker(buffer, "[Encryption]");

        int encryptionAlgorithmLength = buffer.getInt();
        validateLength(encryptionAlgorithmLength, buffer, "encryption algorithm");

        byte[] encryptionAlgorithmBytes = new byte[encryptionAlgorithmLength];
        buffer.get(encryptionAlgorithmBytes);
        String encryptionAlgorithm = new String(encryptionAlgorithmBytes, StandardCharsets.UTF_8);

        int nonceLength = buffer.getInt();
        validateLength(nonceLength, buffer, "nonce");

        byte[] nonce = new byte[nonceLength];
        buffer.get(nonce);

        readMarker(buffer, "[Time]");

        if (buffer.remaining() < 16) { // 2 * long, a long has 8 bytes. 1 long creation date 1 long last edited date 
            Log.log("Corrupted Vault: incomplete time block.", 4);
            throw new IllegalArgumentException("Corrupted Vault: incomplete time block");
        }

        long creationTime = buffer.getLong();
        long lastEditedTime = buffer.getLong();

        readMarker(buffer, "[Data]");

        int nonce2Length = buffer.getInt();
        validateLength(nonce2Length, buffer, "nonce2");

        byte[] nonce2 = new byte[nonce2Length];
        buffer.get(nonce2);

// entry count. // on 0 entries, this has a "0" there (in 4 bits, so 0000) 
        if (buffer.remaining() < 4) {
            Log.log("Corrupted vault: missing entry count", 4);
            throw new IllegalArgumentException("Corrupted vault: missing entry count");
        }

        int entryCount = buffer.getInt();

        if (entryCount < 0) {
            Log.log("Corrupted vault: negative entry count", 4);
            throw new IllegalArgumentException("Corrupted vault: negative entry count");
        }

        Vault vault = new Vault();

        vault.setVaultFormatVersion(vaultFormatVersion);
        vault.setKDF(kdf);
        vault.setSalt(salt);
        vault.setEncryptionAlgorithm(encryptionAlgorithm);
        vault.setNonce(nonce);
        vault.setCreationTime(creationTime);
        vault.setLastEditedTime(lastEditedTime);
        vault.setNonce2(nonce2);

        for (int i = 0; i < entryCount; i++) {

            if (buffer.remaining() < 4) {
                Log.log("Corrupted vault: missing entry length", 4);
                throw new IllegalArgumentException("Corrupted vault: missing entry length");
            }

            int entryLength = buffer.getInt();

            validateLength(entryLength, buffer, "entry");

            byte[] entryData = new byte[entryLength];
            buffer.get(entryData);

            PasswordEntry entry = PasswordEntrySerializer.deserializePasswordEntry(entryData);

            vault.addEntry(entry);
        }

        // Ensure entire file was consumed
        if (buffer.hasRemaining()) {
            Log.log("Corrupted vault: unexpected data after entries {file was supposed to end, but did not}", 4);
            throw new IllegalArgumentException("Corrupted vault: unexpected data after entries");
        }

        return vault;
    }

// Scuffed name, but it reads markers like [Data] to validate the format. Can technically be used to validate any 2 pieces of data
    private static void readMarker(ByteBuffer buffer, String expected) { // carefull, this modifies / advances the buffer index
        byte[] expectedBytes = expected.getBytes(StandardCharsets.UTF_8);

        if (buffer.remaining() < expectedBytes.length) {
            Log.log("Corrupted vault: missing " + expected + " marker", 4);
            throw new IllegalArgumentException("Corrupted vault: missing " + expected + " marker");
        }

        byte[] actual = new byte[expectedBytes.length];
        buffer.get(actual);

        if (!Arrays.equals(actual, expectedBytes)) {
            Log.log("Corrupted vault: missing " + expected + " marker", 4);
            throw new IllegalArgumentException("Corrupted vault: expected " + expected + " marker");
        }
    }

    private static void validateLength(int length, ByteBuffer buffer, String field) {

        if (length < 0) {
            Log.log("Corrupted vault: negative " + field + " length", 4);
            throw new IllegalArgumentException("Corrupted vault: negative " + field + " length");
        }

        if (length > buffer.remaining()) {
            Log.log("Corrupted vault: " + field + " exceeds remaining data", 4);
            throw new IllegalArgumentException("Corrupted vault: " + field + " exceeds remaining data");
        }
    }

}
