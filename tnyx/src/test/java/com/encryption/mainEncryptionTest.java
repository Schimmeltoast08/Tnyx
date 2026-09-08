package com.encryption;

import java.nio.charset.StandardCharsets;
import java.util.Arrays;

import javax.crypto.SecretKey;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import org.junit.jupiter.api.Test;

import com.tnyx.crypto.CryptoEngine;
import com.tnyx.crypto.EncryptedVault;
import com.tnyx.crypto.Encryption;
import com.tnyx.crypto.KeyDerivation;
import com.tnyx.crypto.OpenedVaultData;
import com.tnyx.vault.PasswordEntry;
import com.tnyx.vault.Vault;
import com.tnyx.vault.VaultSerializer;

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

    @Test
    void encryptedVaultLifecycle() throws Exception {

        Vault vault = new Vault();

        vault.setVaultFormatVersion(3);
        vault.setKDF("Argon2id");
        vault.setSalt(KeyDerivation.generateSalt());
        vault.setEncryptionAlgorithm("AES");
        vault.setNonce(Encryption.generateNonce());
        vault.setCreationTime(System.currentTimeMillis());
        vault.setLastEditedTime(System.currentTimeMillis());
        vault.setNonce2(Encryption.generateNonce());

        PasswordEntry entry = new PasswordEntry();

        entry.setName("Example");
        entry.setUsername("user");
        entry.setPassword("secret");
        entry.setUrl("https://example.com");

        vault.addEntries(entry);

        byte[] serialized = VaultSerializer.serializeVault(vault);

        char[] password = "correct horse battery staple".toCharArray();

        OpenedVaultData encrypted = CryptoEngine.encryptNewVault(
                serialized,
                password
        );

        byte[] decrypted = CryptoEngine.decryptData(
                encrypted.getEncryptedVault().getEncryptedData(),
                encrypted.getSession().getDek(),
                encrypted.getEncryptedVault().getDataNonce()
        );

        Vault result = VaultSerializer.deserializeVault(decrypted);

        assertEquals(1, result.getEntries().size());
        assertEquals("Example", result.getEntries().get(0).getName());
        assertEquals("secret", result.getEntries().get(0).getPassword());
    }

    @Test
    void savingKeepsDekButChangesDataNonce() throws Exception {

        byte[] plaintext = "first vault state".getBytes(StandardCharsets.UTF_8);

        OpenedVaultData first = CryptoEngine.encryptNewVault(
                plaintext,
                "test password".toCharArray()
        );

        EncryptedVault firstVault = first.getEncryptedVault();

        byte[] firstDek = first.getSession().getDek().getEncoded();

        byte[] secondPlaintext = "second vault state".getBytes(StandardCharsets.UTF_8);

        EncryptedVault secondVault = CryptoEngine.encryptVault(
                secondPlaintext,
                first.getSession(),
                firstVault.getSalt(),
                firstVault.getArgon2MemoryKib(),
                firstVault.getArgon2Iterations(),
                firstVault.getArgon2Parallelism(),
                firstVault.getArgon2OutputLength(),
                firstVault.getEncryptedDek(),
                firstVault.getDekNonce()
        );

        byte[] secondDek = first.getSession().getDek().getEncoded();

        assertArrayEquals(firstDek, secondDek);

        assertArrayEquals(
                firstVault.getEncryptedDek(),
                secondVault.getEncryptedDek()
        );

        assertArrayEquals(
                firstVault.getDekNonce(),
                secondVault.getDekNonce()
        );

        assertFalse(
                Arrays.equals(
                        firstVault.getDataNonce(),
                        secondVault.getDataNonce()
                )
        );
    }

    @Test
    void modifiedCiphertextFailsAuthentication() throws Exception {

        byte[] plaintext = "secret vault data".getBytes(StandardCharsets.UTF_8);

        OpenedVaultData encrypted = CryptoEngine.encryptNewVault(
                plaintext,
                "test password".toCharArray()
        );

        byte[] ciphertext = encrypted.getEncryptedVault().getEncryptedData();

        ciphertext[0] ^= 1;

        assertThrows(
                Exception.class,
                () -> CryptoEngine.decryptData(
                        ciphertext,
                        encrypted.getSession().getDek(),
                        encrypted.getEncryptedVault().getDataNonce()
                )
        );
    }
}
