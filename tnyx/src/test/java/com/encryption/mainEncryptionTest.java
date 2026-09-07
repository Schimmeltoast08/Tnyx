package com.encryption;

import java.nio.charset.StandardCharsets;

import javax.crypto.SecretKey;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import org.junit.jupiter.api.Test;

import com.tnyx.crypto.Encryption;

public class mainEncryptionTest {

    @Test
    void aesGcmRoundTrip() throws Exception {
        SecretKey key = Encryption.generateDEK();

        byte[] nonce = Encryption.generateNonce();

        byte[] plaintext = "Hello Tnyx".getBytes(StandardCharsets.UTF_8);

        byte[] ciphertext = Encryption.encryptGcm(plaintext, key, nonce);
        byte[] decrypted = Encryption.decryptGcm(ciphertext, key, nonce);

        assertArrayEquals(plaintext, decrypted);
    }
}
