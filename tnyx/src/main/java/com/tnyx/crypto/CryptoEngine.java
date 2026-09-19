package com.tnyx.crypto;

import com.tnyx.util.Log;

import javax.crypto.SecretKey;
import java.security.GeneralSecurityException;
import java.util.Arrays;

public final class CryptoEngine {
    private CryptoEngine() {}

    public static SecretKey deriveKek(char[] masterPW, byte[] salt) {
        return deriveKek(masterPW, salt,
                CryptoConstants.ARGON2_MEMORY_KIB,
                CryptoConstants.ARGON2_ITERATIONS,
                CryptoConstants.ARGON2_PARALLELISM,
                CryptoConstants.DEK_LENGTH);
    }

    public static SecretKey deriveKek(
            char[] masterPW, byte[] salt, int memoryKib, int iterations,
            int parallelism, int outputLength) {
        byte[] kekBytes = KeyDerivation.deriveKEK(
                masterPW, salt, memoryKib, iterations, parallelism, outputLength);
        try {
            return new DestroyableSecretKey(kekBytes, "AES");
        } finally {
            Arrays.fill(kekBytes, (byte) 0);
        }
    }

    public static byte[] encryptDek(SecretKey dek, SecretKey kek, byte[] dekNonce)
            throws GeneralSecurityException {
        byte[] encoded = dek.getEncoded();
        try {
            if (encoded == null || encoded.length != CryptoConstants.DEK_LENGTH) {
                throw new IllegalArgumentException("Invalid DEK");
            }
            return Encryption.encryptGcm(encoded, kek, dekNonce);
        } finally {
            if (encoded != null) Arrays.fill(encoded, (byte) 0);
        }
    }

    public static SecretKey decryptDek(byte[] encryptedDek, SecretKey kek, byte[] dekNonce)
            throws GeneralSecurityException {
        if (encryptedDek == null || encryptedDek.length != CryptoConstants.ENCRYPTED_DEK_LENGTH) {
            throw new IllegalArgumentException("Invalid encrypted DEK length");
        }
        byte[] dekBytes = Encryption.decryptGcm(encryptedDek, kek, dekNonce);
        try {
            if (dekBytes.length != CryptoConstants.DEK_LENGTH) {
                throw new GeneralSecurityException("Invalid DEK length");
            }
            return new DestroyableSecretKey(dekBytes, "AES");
        } finally {
            Arrays.fill(dekBytes, (byte) 0);
        }
    }

    public static byte[] encryptData(byte[] plaintextData, SecretKey dek, byte[] dataNonce)
            throws GeneralSecurityException {
        if (plaintextData == null || plaintextData.length > CryptoConstants.MAX_VAULT_PLAINTEXT_SIZE) {
            throw new IllegalArgumentException("Vault data is too large");
        }
        return Encryption.encryptGcm(plaintextData, dek, dataNonce);
    }

    public static byte[] decryptData(byte[] encryptedData, SecretKey dek, byte[] dataNonce)
            throws GeneralSecurityException {
        if (encryptedData == null
                || encryptedData.length < CryptoConstants.GCM_TAG_BYTES
                || encryptedData.length > CryptoConstants.MAX_VAULT_PLAINTEXT_SIZE + CryptoConstants.GCM_TAG_BYTES) {
            throw new IllegalArgumentException("Encrypted vault data is invalid or too large");
        }
        return Encryption.decryptGcm(encryptedData, dek, dataNonce);
    }

    public static EncryptedVault encryptVault(byte[] plaintextData, char[] masterPW)
            throws GeneralSecurityException {
        byte[] salt = KeyDerivation.generateSalt();
        SecretKey kek = deriveKek(masterPW, salt);
        SecretKey dek = Encryption.generateDEK();
        try {
            byte[] dekNonce = Encryption.generateNonce();
            byte[] encryptedDek = encryptDek(dek, kek, dekNonce);
            byte[] dataNonce = Encryption.generateNonce();
            byte[] encryptedData = encryptData(plaintextData, dek, dataNonce);
            return new EncryptedVault(salt, CryptoConstants.ARGON2_MEMORY_KIB,
                    CryptoConstants.ARGON2_ITERATIONS, CryptoConstants.ARGON2_PARALLELISM,
                    CryptoConstants.DEK_LENGTH, dekNonce, encryptedDek, dataNonce, encryptedData);
        } finally {
            destroy(dek);
            destroy(kek);
        }
    }

    public static byte[] decryptVault(EncryptedVault encryptedVault, char[] masterPW)
            throws GeneralSecurityException {
        validateContainer(encryptedVault);
        SecretKey kek = deriveKek(masterPW, encryptedVault.getSalt(),
                encryptedVault.getArgon2MemoryKib(), encryptedVault.getArgon2Iterations(),
                encryptedVault.getArgon2Parallelism(), encryptedVault.getArgon2OutputLength());
        try {
            SecretKey dek = decryptDek(encryptedVault.getEncryptedDek(), kek, encryptedVault.getDekNonce());
            try {
                return decryptData(encryptedVault.getEncryptedData(), dek, encryptedVault.getDataNonce());
            } finally {
                destroy(dek);
            }
        } finally {
            destroy(kek);
        }
    }

    public static EncryptedVault encryptVault(
            byte[] plaintextData, VaultSession session, EncryptedVault previous)
            throws GeneralSecurityException {
        if (session == null || session.isClosed() || previous == null) {
            throw new IllegalArgumentException("Invalid vault session");
        }
        validateContainer(previous);
        byte[] dekNonce = Encryption.generateNonce();
        byte[] encryptedDek = session.rewrapDek(dekNonce);
        byte[] dataNonce = Encryption.generateNonce();
        byte[] encryptedData = session.encryptData(plaintextData, dataNonce);
        return new EncryptedVault(
                previous.getSalt(), previous.getArgon2MemoryKib(), previous.getArgon2Iterations(),
                previous.getArgon2Parallelism(), previous.getArgon2OutputLength(),
                dekNonce, encryptedDek, dataNonce, encryptedData);
    }

    public static OpenedVaultData encryptNewVault(byte[] plaintextData, char[] masterPW)
            throws GeneralSecurityException {
        byte[] salt = KeyDerivation.generateSalt();
        SecretKey kek = deriveKek(masterPW, salt);
        SecretKey dek = Encryption.generateDEK();
        VaultSession session = null;
        try {
            byte[] dekNonce = Encryption.generateNonce();
            byte[] encryptedDek = encryptDek(dek, kek, dekNonce);
            byte[] dataNonce = Encryption.generateNonce();
            byte[] encryptedData = encryptData(plaintextData, dek, dataNonce);
            EncryptedVault encryptedVault = new EncryptedVault(
                    salt, CryptoConstants.ARGON2_MEMORY_KIB, CryptoConstants.ARGON2_ITERATIONS,
                    CryptoConstants.ARGON2_PARALLELISM, CryptoConstants.DEK_LENGTH,
                    dekNonce, encryptedDek, dataNonce, encryptedData);
            session = new VaultSession(kek, dek);
            return new OpenedVaultData(encryptedVault, session);
        } catch (RuntimeException | GeneralSecurityException e) {
            if (session != null) session.close();
            throw e;
        } finally {
            destroy(dek);
            destroy(kek);
        }
    }

    public static VaultSession openSession(EncryptedVault encryptedVault, char[] masterPW)
            throws GeneralSecurityException {
        validateContainer(encryptedVault);
        SecretKey kek = deriveKek(masterPW, encryptedVault.getSalt(),
                encryptedVault.getArgon2MemoryKib(), encryptedVault.getArgon2Iterations(),
                encryptedVault.getArgon2Parallelism(), encryptedVault.getArgon2OutputLength());
        try {
            SecretKey dek = decryptDek(encryptedVault.getEncryptedDek(), kek, encryptedVault.getDekNonce());
            try {
                return new VaultSession(kek, dek);
            } finally {
                destroy(dek);
            }
        } finally {
            destroy(kek);
        }
    }

    private static void validateContainer(EncryptedVault v) {
        if (v == null
                || v.getSalt().length != CryptoConstants.SALT_LENGTH
                || v.getDekNonce().length != CryptoConstants.NONCE_LENGTH
                || v.getEncryptedDek().length != CryptoConstants.ENCRYPTED_DEK_LENGTH
                || v.getDataNonce().length != CryptoConstants.NONCE_LENGTH
                || v.getEncryptedData().length < CryptoConstants.GCM_TAG_BYTES
                || v.getEncryptedData().length > CryptoConstants.MAX_VAULT_PLAINTEXT_SIZE + CryptoConstants.GCM_TAG_BYTES
                || v.getArgon2MemoryKib() != CryptoConstants.ARGON2_MEMORY_KIB
                || v.getArgon2Iterations() != CryptoConstants.ARGON2_ITERATIONS
                || v.getArgon2Parallelism() != CryptoConstants.ARGON2_PARALLELISM
                || v.getArgon2OutputLength() != CryptoConstants.DEK_LENGTH) {
            Log.log("Invalid encrypted vault parameters", 4);
            throw new IllegalArgumentException("Invalid encrypted vault parameters");
        }
    }

    private static void destroy(SecretKey key) {
        if (key instanceof DestroyableSecretKey destroyable) {
            destroyable.close();
        }
    }
}
