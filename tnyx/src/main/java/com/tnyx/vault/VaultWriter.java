package com.tnyx.vault;

import com.tnyx.util.Log;
import com.tnyx.vault.Password.PasswordEntrySerializer;
import java.io.DataOutputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.AtomicMoveNotSupportedException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.security.SecureRandom;
import java.time.Instant;

public class VaultWriter {

    private static final int VAULT_FORMAT_VERSION = 1;
    private static final String KDF = "Argon2id"; // replace \w name later
    private static final String ENCRYPTION_ALGORITHM = "AES"; // same as KDF

    public static void writeVaultAtomic(String vaultpath, Vault vault, boolean atomic) {

        String outputPath = atomic ? vaultpath + ".tmp" : vaultpath;

        try (DataOutputStream vaultOut = new DataOutputStream(new FileOutputStream(outputPath))) {

            //   Write Block   \\
            vaultOut.write("[Format]".getBytes(StandardCharsets.UTF_8));
            vaultOut.writeInt(vault.getVaultFormatVersion());

            vaultOut.write("[KDF]".getBytes(StandardCharsets.UTF_8));
            byte[] kdfBytes = vault.getKDF().getBytes(StandardCharsets.UTF_8);
            vaultOut.writeInt(kdfBytes.length);
            vaultOut.write(kdfBytes);

            vaultOut.writeInt(vault.getSalt().length);
            vaultOut.write(vault.getSalt());
            //vaultOut.write(parameters);

            vaultOut.write("[Encryption]".getBytes(StandardCharsets.UTF_8));
            byte[] encryptionAlgorithmBytes = vault.getEncryptionAlgorithm().getBytes(StandardCharsets.UTF_8);
            vaultOut.writeInt(encryptionAlgorithmBytes.length);
            vaultOut.write(encryptionAlgorithmBytes);

            vaultOut.writeInt(vault.getNonce().length);
            vaultOut.write(vault.getNonce());
            // DEK u KEK, more research
            //vaultOut.write(Data);

            vaultOut.write("[Time]".getBytes(StandardCharsets.UTF_8));
            vaultOut.writeLong(vault.getCreationTime());
            vaultOut.writeLong(vault.getLastEditedTime());

            vaultOut.write("[Data]".getBytes(StandardCharsets.UTF_8));

            // nonce
            vaultOut.writeInt(vault.getNonce2().length);
            vaultOut.write(vault.getNonce2());

            // number of entries
            vaultOut.writeInt(vault.getEntries().size());

            // entries
            for (PasswordEntry entry : vault.getEntries()) {
                

                byte[] entryData = PasswordEntrySerializer.serializePasswordEntry(entry);
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

        VaultWriter.writeVaultAtomic(filepath, vault, false);
        Log.log("Created new empty Vault", 2);

    }

}
