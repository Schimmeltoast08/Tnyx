package com.tnyx.crypto;

import java.security.GeneralSecurityException;
import java.security.SecureRandom;
import java.util.Arrays;

import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.spec.GCMParameterSpec;

public final class Encryption {
    private static final SecureRandom RANDOM = new SecureRandom();

    private Encryption() {}

    public static SecretKey generateDEK() throws GeneralSecurityException {
        KeyGenerator keyGenerator = KeyGenerator.getInstance("AES");
        keyGenerator.init(256);
        return keyGenerator.generateKey();
    }

    public static byte[] generateNonce() {
        byte[] nonce = new byte[CryptoConstants.NONCE_LENGTH];
        RANDOM.nextBytes(nonce);
        return nonce;
    }

    public static byte[] encryptGcm(byte[] plaintext, SecretKey key, byte[] nonce)
            throws GeneralSecurityException {
        validateKeyAndNonce(key, nonce);
        if (plaintext == null) {
            throw new IllegalArgumentException("Plaintext must not be null");
        }
        Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding");
        cipher.init(Cipher.ENCRYPT_MODE, key,
                new GCMParameterSpec(CryptoConstants.GCM_TAG_LENGTH, nonce));
        return cipher.doFinal(plaintext);
    }

    public static byte[] decryptGcm(byte[] ciphertext, SecretKey key, byte[] nonce)
            throws GeneralSecurityException {
        validateKeyAndNonce(key, nonce);
        if (ciphertext == null || ciphertext.length < CryptoConstants.GCM_TAG_BYTES) {
            throw new IllegalArgumentException("Ciphertext is too short");
        }
        Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding");
        cipher.init(Cipher.DECRYPT_MODE, key,
                new GCMParameterSpec(CryptoConstants.GCM_TAG_LENGTH, nonce));
        return cipher.doFinal(ciphertext);
    }

    private static void validateKeyAndNonce(SecretKey key, byte[] nonce) {
        if (key == null || !"AES".equalsIgnoreCase(key.getAlgorithm())) {
            throw new IllegalArgumentException("Invalid AES key");
        }
        byte[] encoded = key.getEncoded();
        try {
            if (encoded == null || encoded.length != CryptoConstants.DEK_LENGTH) {
                throw new IllegalArgumentException("AES-256 key required");
            }
        } finally {
            if (encoded != null) {
                Arrays.fill(encoded, (byte) 0);
            }
        }
        if (nonce == null || nonce.length != CryptoConstants.NONCE_LENGTH) {
            throw new IllegalArgumentException("Invalid GCM nonce length");
        }
    }
}
