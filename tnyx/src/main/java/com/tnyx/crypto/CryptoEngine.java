package com.tnyx.crypto;

import java.security.GeneralSecurityException;
import java.util.Arrays;

import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;

public class CryptoEngine {

    public static SecretKey deriveKek(char[] password, byte[] salt) {
        byte[] kekBytes = KeyDerivation.deriveKEK(password, salt);
        return new SecretKeySpec(kekBytes, "AES");
    }

    public static SecretKey deriveKek(char[] password, byte[] salt, int memoryKib, int iterations, int parallelism, int outputLength) {
        byte[] kekBytes = KeyDerivation.deriveKEK(password, salt, memoryKib, iterations, parallelism, outputLength);
        return new SecretKeySpec(kekBytes, "AES");
    }

    public static byte[] encryptDek(SecretKey dek, SecretKey kek, byte[] dekNonce) throws GeneralSecurityException {
        return Encryption.encryptGcm(dek.getEncoded(), kek, dekNonce);
    }

    public static SecretKey decryptDek(byte[] encryptedDek, SecretKey kek, byte[] dekNonce) throws GeneralSecurityException {
        byte[] dekBytes = Encryption.decryptGcm(encryptedDek, kek, dekNonce);
        SecretKey dek = new SecretKeySpec(dekBytes, "AES");
        Arrays.fill(dekBytes, (byte) 0);
        return dek;
    }

    public static byte[] encryptData(byte[] plaintextData, SecretKey dek, byte[] dataNonce) throws GeneralSecurityException {
        return Encryption.encryptGcm(plaintextData, dek, dataNonce);
    }

    public static byte[] decryptData(byte[] encryptedData, SecretKey dek, byte[] dataNonce) throws GeneralSecurityException {
        return Encryption.decryptGcm(encryptedData, dek, dataNonce);
    }


    public static EncryptedVault encryptVault(byte[] plaintextData, char[] password) throws GeneralSecurityException {

        byte[] salt = KeyDerivation.generateSalt();

        SecretKey kek = deriveKek(
                password,
                salt,
                CryptoConstants.ARGON2_MEMORY_KIB,
                CryptoConstants.ARGON2_ITERATIONS,
                CryptoConstants.ARGON2_PARALLELISM,
                CryptoConstants.DEK_LENGTH
        );

        SecretKey dek = Encryption.generateDEK();

        byte[] dekNonce = Encryption.generateNonce();

        byte[] encryptedDek = encryptDek(dek, kek, dekNonce);

        byte[] dataNonce = Encryption.generateNonce();

        byte[] encryptedData = encryptData(plaintextData, dek, dataNonce);

        return new EncryptedVault(
                salt,
                CryptoConstants.ARGON2_MEMORY_KIB,
                CryptoConstants.ARGON2_ITERATIONS,
                CryptoConstants.ARGON2_PARALLELISM,
                CryptoConstants.DEK_LENGTH,
                dekNonce,
                encryptedDek,
                dataNonce,
                encryptedData
        );
    }

    public static byte[] decryptVault(EncryptedVault encryptedVault, char[] password) throws GeneralSecurityException {

        SecretKey kek = deriveKek(
                password,
                encryptedVault.getSalt(),
                encryptedVault.getArgon2MemoryKib(),
                encryptedVault.getArgon2Iterations(),
                encryptedVault.getArgon2Parallelism(),
                encryptedVault.getArgon2OutputLength()
        );

        SecretKey dek = decryptDek(
                encryptedVault.getEncryptedDek(),
                kek,
                encryptedVault.getDekNonce()
        );

        return decryptData(
                encryptedVault.getEncryptedData(),
                dek,
                encryptedVault.getDataNonce()
        );
    }

    public static EncryptedVault encryptVault(byte[] plaintextData, VaultSession session, byte[] salt, int memoryKib, int iterations, int parallelism, int outputLength, byte[] encryptedDek, byte[] dekNonce) throws GeneralSecurityException {

        byte[] dataNonce = Encryption.generateNonce();

        byte[] encryptedData = encryptData(
                plaintextData,
                session.getDek(),
                dataNonce
        );

        return new EncryptedVault(
                salt,
                memoryKib,
                iterations,
                parallelism,
                outputLength,
                dekNonce,
                encryptedDek,
                dataNonce,
                encryptedData
        );
    }

    public static OpenedVaultData encryptNewVault(byte[] plaintextData, char[] password) throws GeneralSecurityException {

        byte[] salt = KeyDerivation.generateSalt();

        SecretKey kek = deriveKek(
                password,
                salt,
                CryptoConstants.ARGON2_MEMORY_KIB,
                CryptoConstants.ARGON2_ITERATIONS,
                CryptoConstants.ARGON2_PARALLELISM,
                CryptoConstants.DEK_LENGTH
        );

        SecretKey dek = Encryption.generateDEK();

        byte[] dekNonce = Encryption.generateNonce();

        byte[] encryptedDek = encryptDek(
                dek,
                kek,
                dekNonce
        );

        byte[] dataNonce = Encryption.generateNonce();

        byte[] encryptedData = encryptData(
                plaintextData,
                dek,
                dataNonce
        );

        EncryptedVault encryptedVault = new EncryptedVault(
                salt,
                CryptoConstants.ARGON2_MEMORY_KIB,
                CryptoConstants.ARGON2_ITERATIONS,
                CryptoConstants.ARGON2_PARALLELISM,
                CryptoConstants.DEK_LENGTH,
                dekNonce,
                encryptedDek,
                dataNonce,
                encryptedData
        );

        return new OpenedVaultData(
                encryptedVault,
                new VaultSession(dek)
        );
    }
}
