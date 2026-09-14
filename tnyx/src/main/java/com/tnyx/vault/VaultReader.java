package com.tnyx.vault;

import java.io.DataInputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

import com.tnyx.crypto.EncryptedVault;
import com.tnyx.util.Log;

public class VaultReader {

    private static final int VAULT_FORMAT_VERSION = 3;

    public static Vault readVault(String filepath) throws IOException {
        Vault vault = new Vault();
        try (DataInputStream dataIn = new DataInputStream(new FileInputStream(filepath))) {

            //
            byte[] formatMagic = new byte["[Format]".getBytes().length];
            dataIn.readFully(formatMagic);
            String formatHeader = new String(formatMagic, StandardCharsets.UTF_8);

            if (!formatHeader.equals("[Format]")) {
                Log.log("Format Error! missing [Format] header", 4);
                throw new IOException("Invalid vault file: missing [Format] header");

            }

            int formatVersion = dataIn.readInt();

            if (formatVersion != VAULT_FORMAT_VERSION) {
                Log.log("Format Versions do not match. Expected: " + VAULT_FORMAT_VERSION + " recieved: " + formatVersion, 3);
            }
//

            byte[] kdfMagic = new byte["[KDF]".getBytes().length];
            dataIn.readFully(kdfMagic);
            String kdfHeader = new String(kdfMagic, StandardCharsets.UTF_8);

            if (!kdfHeader.equals("[KDF]")) {
                Log.log("Format Error! Missing [KDF] section header", 4);
                throw new IOException("Invalid vault file: missing [KDF] section header");
            }

            int kdfLength = dataIn.readInt();
            byte[] kdfBytes = new byte[kdfLength];
            dataIn.readFully(kdfBytes);

            String KDF = new String(kdfBytes, StandardCharsets.UTF_8);

            int saltLength = dataIn.readInt();
            byte[] saltBytes = new byte[saltLength];
            dataIn.readFully(saltBytes);

            byte[] encryptionMagic = new byte["[Encryption]".getBytes().length];
            dataIn.readFully(encryptionMagic);
            String encryptionHeader = new String(encryptionMagic, StandardCharsets.UTF_8);

            if (!encryptionHeader.equals("[Encryption]")) {
                Log.log("Format Error! Missing [Encryption] section header", 4);
                throw new IOException("Invalid vault file: missing [Encryption] section header");
            }

            int encryptionAlgorithmLength = dataIn.readInt();
            byte[] encryptionAlgorithmBytes = new byte[encryptionAlgorithmLength];
            dataIn.readFully(encryptionAlgorithmBytes);
            String encryptionAlgorithm = new String(encryptionAlgorithmBytes, StandardCharsets.UTF_8);

            int nonceLength = dataIn.readInt();
            byte[] nonceBytes = new byte[nonceLength];
            dataIn.readFully(nonceBytes);

            byte[] timeMagic = new byte["[Time]".getBytes().length];
            dataIn.readFully(timeMagic);
            String timeHeader = new String(timeMagic, StandardCharsets.UTF_8);

            if (!timeHeader.equals("[Time]")) {
                Log.log("Format Error! Missing [Time] section header", 4);
                throw new IOException("Invalid vault file: missing [Time] section header");
            }

            long creationTime = dataIn.readLong();
            long lastEditedTime = dataIn.readLong();

            byte[] dataMagic = new byte["[Data]".getBytes().length];
            dataIn.readFully(dataMagic);
            String dataHeader = new String(dataMagic, StandardCharsets.UTF_8);

            if (!dataHeader.equals("[Data]")) {
                Log.log("Format Error! Missing [Data] section header", 4);
                throw new IOException("Invalid vault file: missing [Data] section header");
            }

            int nonce2Length = dataIn.readInt();
            byte[] nonce2Bytes = new byte[nonce2Length];
            dataIn.readFully(nonce2Bytes);

            int entryCount = dataIn.readInt();

            for (int i = 0; i < entryCount; i++) {

                int entryLength = dataIn.readInt();

                if (entryLength < 0) {
                    Log.log("Invalid entry length!", 4);
                    throw new IOException("Invalid entry length: " + entryLength);
                }

                byte[] entryBytes = new byte[entryLength];

                dataIn.readFully(entryBytes);

                PasswordEntry entry = PasswordEntrySerializer.deserializePasswordEntry(entryBytes);
                vault.addEntry(entry);

                if (entryLength <= 0) {
                    Log.log("Entry length smaller then zero (0), corrupted format", 4);
                    throw new IOException("Entry length smaller then zero (0), corrupted format");
                }

            }

            vault.setVaultFormatVersion(formatVersion);
            vault.setKDF(KDF);
            vault.setSalt(saltBytes);
            vault.setEncryptionAlgorithm(encryptionAlgorithm);
            vault.setNonce(nonceBytes);
            vault.setCreationTime(creationTime);
            vault.setLastEditedTime(lastEditedTime);
            vault.setNonce2(nonce2Bytes);

        } catch (Exception e) {
            Log.log("Error reading vault: " + e.getMessage(), 4);
            throw new IOException("Could not read vault");
        }

        return vault;
    }

    public static byte[] readBytes(String filepath) throws IOException {
        return Files.readAllBytes(Path.of(filepath)); // wtf why so simple? Incredible
    }

    public static EncryptedVault readEncryptedVault(String filepath) throws IOException {

        byte[] encryptedVaultData = readBytes(filepath);

        try {
            return EncryptedVaultSerializer.deserializeEncryptedVault(encryptedVaultData);
        } catch (IllegalArgumentException e) {
            String message = "Could not parse encrypted vault: " + e.getMessage();
            Log.log(message, 4);
            throw new IOException(message, e);
        }
    }
}
