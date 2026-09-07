package com.encryption;

import java.nio.charset.StandardCharsets;
import java.util.Arrays;

import javax.crypto.SecretKey;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import org.junit.jupiter.api.Test;

import com.tnyx.crypto.CryptoEngine;
import com.tnyx.crypto.EncryptedVault;
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

    @Test
    void encryptionHierarchyRoundTrip() throws Exception {

        char[] password = "correct horse battery staple".toCharArray();

        byte[] plaintext = "Hello Tnyx, this is secret!".getBytes(StandardCharsets.UTF_8);

        EncryptedVault encryptedVault = CryptoEngine.encryptVault(plaintext, password);

        byte[] decrypted = CryptoEngine.decryptVault(encryptedVault, "correct horse battery staple".toCharArray());

        assertArrayEquals(plaintext, decrypted);
    }

    @Test
    void wrongPasswordFails() throws Exception {

        char[] password = "correct password".toCharArray();

        byte[] plaintext = "Secret Tnyx data".getBytes(StandardCharsets.UTF_8);

        EncryptedVault encryptedVault = CryptoEngine.encryptVault(plaintext, password);

        assertThrows(
                Exception.class,
                () -> CryptoEngine.decryptVault(encryptedVault, "wrong password".toCharArray())
        );
    }


    @Test
void encryptionUsesDifferentNonces() throws Exception {

    char[] password = "test password".toCharArray();

    byte[] plaintext = "Hello Tnyx".getBytes(StandardCharsets.UTF_8);

    EncryptedVault first = CryptoEngine.encryptVault(plaintext, password);

    EncryptedVault second = CryptoEngine.encryptVault(plaintext, "test password".toCharArray());

    assertFalse(
            Arrays.equals(first.getDataNonce(), second.getDataNonce())
    );

    assertFalse(
            Arrays.equals(first.getDekNonce(), second.getDekNonce())
    );

    assertFalse(
            Arrays.equals(first.getSalt(), second.getSalt()) // idk why this throws errors when in one line???
    );
}
}
