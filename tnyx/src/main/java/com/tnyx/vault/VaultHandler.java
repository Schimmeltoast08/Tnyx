package com.tnyx.vault;

import com.tnyx.util.Log;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.AtomicMoveNotSupportedException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.security.SecureRandom;
import java.time.Instant;

public class VaultHandler {

    @SuppressWarnings("unused")
    private static final int TNYX_MAIN_VERSION = 1;
    private static final int VAULT_FORMAT_VERSION = 1;
    private static final String KDF = "Argon2id"; // replace \w name later
    private static final String ENCRYPTION_ALGORITHM = "AES"; // same as KDF

    public static void createOpenVault(String filepath) throws IOException {

        Vault vault = new Vault();

        SecureRandom random = new SecureRandom();
        //temporary
        byte[] salt = new byte[16];
        random.nextBytes(salt);
        vault.setSalt(salt);

        byte[] nonce = new byte[12];
        random.nextBytes(nonce);
        vault.setNonce(nonce);

        //byte[] dek = new byte[32];
        //random.nextBytes(dek);
        vault.setEncryptionAlgorithm(ENCRYPTION_ALGORITHM);
        vault.setVaultFormatVersion(VAULT_FORMAT_VERSION);
        vault.setKDF(KDF);
        vault.setCreationTime(Instant.now().getEpochSecond());
        vault.setLastEditedTime(Instant.now().getEpochSecond());

        byte[] nonce2 = new byte[12];
        random.nextBytes(nonce2);
        vault.setNonce2(nonce2);

        writeVaultAtomic(filepath, vault, false);
        Log.log("Created new empty Vault", 2);

    }

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

            int formatVersion = dataIn.readUnsignedByte();
//

            byte[] kdfMagic = new byte["[KDF]".getBytes().length];
            dataIn.readFully(kdfMagic);
            String kdfHeader = new String(kdfMagic, StandardCharsets.UTF_8);

            if (!kdfHeader.equals("[KDF]")) {
                Log.log("Format Error! Missing [KDF] section header", 4);
                throw new IOException("Invalid vault file: missing [KDF] section header");
            }

            int kdfLength = dataIn.readUnsignedByte();
            byte[] kdfBytes = new byte[kdfLength];
            dataIn.readFully(kdfBytes);

            String KDF = new String(kdfBytes, StandardCharsets.UTF_8);

            int saltLength = dataIn.readUnsignedByte();
            byte[] saltBytes = new byte[saltLength];
            dataIn.readFully(saltBytes);

            byte[] encryptionMagic = new byte["[Encryption]".getBytes().length];
            dataIn.readFully(encryptionMagic);
            String encryptionHeader = new String(encryptionMagic, StandardCharsets.UTF_8);

            if (!encryptionHeader.equals("[Encryption]")) {
                Log.log("Format Error! Missing [Encryption] section header", 4);
                throw new IOException("Invalid vault file: missing [Encryption] section header");
            }

            int encryptionAlgorithmLength = dataIn.readUnsignedByte();
            byte[] encryptionAlgorithmBytes = new byte[encryptionAlgorithmLength];
            dataIn.readFully(encryptionAlgorithmBytes);
            String encryptionAlgorithm = new String(encryptionAlgorithmBytes, StandardCharsets.UTF_8);

            int nonceLength = dataIn.readUnsignedByte();
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
            long lastModifiedTime = dataIn.readLong();

            byte[] dataMagic = new byte["[Data]".getBytes().length];
            dataIn.readFully(dataMagic);
            String dataHeader = new String(dataMagic, StandardCharsets.UTF_8);

            if (!dataHeader.equals("[Data]")) {
                Log.log("Format Error! Missing [Data] section header", 4);
                throw new IOException("Invalid vault file: missing [Data] section header");
            }

            int nonce2Length = dataIn.readUnsignedByte();
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

                String entryData = new String(entryBytes, StandardCharsets.UTF_8);

                String[] parts = entryData.split("\\|\\|", -1);
                if (parts.length != 4) {
                    throw new IOException("Invalid password entry");
                }

                PasswordEntry entry = new PasswordEntry();
                entry.setName(parts[0]);
                entry.setUsername(parts[1]);
                entry.setPassword(parts[2]);
                entry.setUrl(parts[3]);

                vault.addEntries(entry);
            }

            vault.setVaultFormatVersion(formatVersion);
            vault.setKDF(KDF);
            vault.setSalt(saltBytes);
            vault.setEncryptionAlgorithm(encryptionAlgorithm);
            vault.setNonce(nonceBytes);
            vault.setCreationTime(creationTime);
            vault.setLastEditedTime(lastModifiedTime);
            vault.setNonce2(nonce2Bytes);
            ;

        } catch (Exception e) {
            Log.log("Error writing vault: " + e.getMessage(), 4);
        }
        return vault;
    }

    public static void addVaultEntry(String filepath, String name, String username, String password, String url) {
        PasswordEntry pw = new PasswordEntry();
        pw.setName(name);
        pw.setPassword(password);
        pw.setUrl(url);
        pw.setUsername(username);

        try {
            Vault vault = readVault(filepath);

            vault.getEntries().add(pw);

            writeVaultAtomic(filepath, vault, true);
            Log.log("Added new Vault entry", 2);
        } catch (IOException e) {
            Log.log("Could not Open Vault file when adding entry", 4);
        }

    }

    private static void writeVaultAtomic(String vaultpath, Vault vault, boolean atomic) {

        String outputPath = atomic ? vaultpath + ".tmp" : vaultpath;

        try (DataOutputStream vaultOut = new DataOutputStream(new FileOutputStream(outputPath))) {

            //   Write Block   \\
            vaultOut.write("[Format]".getBytes(StandardCharsets.UTF_8));
            vaultOut.write(vault.getVaultFormatVersion());

            vaultOut.write("[KDF]".getBytes(StandardCharsets.UTF_8));
            byte[] kdfBytes = vault.getKDF().getBytes(StandardCharsets.UTF_8);
            vaultOut.write(kdfBytes.length);
            vaultOut.write(kdfBytes);

            vaultOut.write(vault.getSalt().length);
            vaultOut.write(vault.getSalt());
            //vaultOut.write(parameters);

            vaultOut.write("[Encryption]".getBytes(StandardCharsets.UTF_8));
            byte[] encryptionAlgorithmBytes = vault.getEncryptionAlgorithm().getBytes(StandardCharsets.UTF_8);
            vaultOut.write(encryptionAlgorithmBytes.length);
            vaultOut.write(encryptionAlgorithmBytes);

            vaultOut.write(vault.getNonce().length);
            vaultOut.write(vault.getNonce());
            // DEK u KEK, more research
            //vaultOut.write(Data);

            vaultOut.write("[Time]".getBytes(StandardCharsets.UTF_8));
            vaultOut.writeLong(vault.getCreationTime());
            vaultOut.writeLong(vault.getLastEditedTime());

            vaultOut.write("[Data]".getBytes(StandardCharsets.UTF_8));

            // nonce
            vaultOut.writeByte(vault.getNonce2().length);
            vaultOut.write(vault.getNonce2());

            // number of entries
            vaultOut.writeInt(vault.getEntries().size());

            // entries
            for (PasswordEntry entry : vault.getEntries()) {

                byte[] entryData = entry.getPasswordEntryData().getBytes(StandardCharsets.UTF_8);
                vaultOut.writeInt(entryData.length); // first length of this entry
                vaultOut.write(entryData); // then entry
            }

            if (atomic) {
                Path temp = Path.of(vaultpath + ".tmp");
                Path target = Path.of(vaultpath);

                try {
                    Files.move(temp, target, StandardCopyOption.ATOMIC_MOVE, StandardCopyOption.REPLACE_EXISTING);
                } catch (AtomicMoveNotSupportedException e) {
                    Log.log("Atomic move is not supported for vault: " + vaultpath, 4);
                    throw new IOException("Atomic replacement is not supported", e);
                }
            }

        } catch (Exception e) {
            Log.log("Error writing vault: " + e.getMessage(), 4);
        }

    }

}
