package com.tnyx.vault;

import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.AtomicMoveNotSupportedException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;

import com.tnyx.crypto.EncryptedVault;
import com.tnyx.util.Log;

public class VaultWriter {

    private static final int VAULT_FORMAT_VERSION = 3;
    private static final String KDF = "Argon2id"; // replace \w name later
    private static final String ENCRYPTION_ALGORITHM = "AES"; // same as KDF


    public static void writeBytes(String vaultpath, byte[] vaultData, boolean atomic) throws IOException {

        String outputPath = atomic ? vaultpath + ".tmp" : vaultpath;

        try (FileOutputStream out = new FileOutputStream(outputPath)) {
            out.write(vaultData);
        }

        if (atomic) {

            Path temp = Path.of(vaultpath + ".tmp");
            Path target = Path.of(vaultpath);

            try {
                Files.move(
                        temp,
                        target,
                        StandardCopyOption.ATOMIC_MOVE,
                        StandardCopyOption.REPLACE_EXISTING
                );
            } catch (AtomicMoveNotSupportedException e) {
                Log.log(
                        "Atomic move not supported, falling back to normal move: " + vaultpath, 3);

                Files.move(
                        temp,
                        target,
                        StandardCopyOption.REPLACE_EXISTING
                );
            }
            }
        }


    public static void writeEncryptedVault(String filepath, EncryptedVault encryptedVault, boolean atomic) throws IOException {

        byte[] encryptedVaultData = EncryptedVaultSerializer.serializeEncryptedVault(encryptedVault);

        writeBytes(filepath, encryptedVaultData, atomic);
    }

}

