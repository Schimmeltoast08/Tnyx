package com.tnyx.crypto;

import java.security.GeneralSecurityException;

import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;

public class CryptoEngine {

    public static SecretKey deriveKek(char[] password, byte[] salt) {
        byte[] kekBytes = KeyDerivation.deriveKEK(password, salt);
        return new SecretKeySpec(kekBytes, "AES");
    }

    public static byte[] encryptDek(SecretKey dek, SecretKey kek, byte[] dekNonce) throws GeneralSecurityException {
        return Encryption.encryptGcm(dek.getEncoded(), kek, dekNonce);
    }

    public static SecretKey decryptDek(byte[] encryptedDek, SecretKey kek, byte[] dekNonce) throws GeneralSecurityException {
        byte[] dekBytes = Encryption.decryptGcm(encryptedDek, kek, dekNonce);
        return new SecretKeySpec(dekBytes, "AES");
    }

    public static byte[] encryptData(byte[] plaintextData, SecretKey dek, byte[] dataNonce) throws GeneralSecurityException {
        return Encryption.encryptGcm(plaintextData, dek, dataNonce);
    }

    public static byte[] decryptData(byte[] encryptedData, SecretKey dek, byte[] dataNonce) throws GeneralSecurityException {
        return Encryption.decryptGcm(encryptedData, dek, dataNonce);
    }

    
}