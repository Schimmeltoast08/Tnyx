package com.encryption;

import com.tnyx.crypto.CryptoEngine;
import com.tnyx.crypto.EncryptedVault;
import com.tnyx.crypto.OpenedVaultData;
import com.tnyx.vault.PasswordEntry;
import com.tnyx.vault.Vault;
import com.tnyx.vault.VaultSerializer;
import org.junit.jupiter.api.Test;

import java.nio.charset.StandardCharsets;
import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.*;

class mainEncryptionTest {
    @Test void aesGcmRoundTrip() throws Exception {
        var key = com.tnyx.crypto.Encryption.generateDEK();
        byte[] nonce = com.tnyx.crypto.Encryption.generateNonce();
        byte[] plaintext = "Hello Tnyx".getBytes(StandardCharsets.UTF_8);
        assertArrayEquals(plaintext, com.tnyx.crypto.Encryption.decryptGcm(com.tnyx.crypto.Encryption.encryptGcm(plaintext, key, nonce), key, nonce));
        if (key instanceof com.tnyx.crypto.DestroyableSecretKey d) d.close();
    }

    @Test void encryptionHierarchyRoundTrip() throws Exception {
        char[] password = "correct horse battery staple".toCharArray();
        try {
            byte[] plaintext = "Hello Tnyx, this is secret!".getBytes(StandardCharsets.UTF_8);
            EncryptedVault encrypted = CryptoEngine.encryptVault(plaintext, password);
            assertArrayEquals(plaintext, CryptoEngine.decryptVault(encrypted, password));
        } finally { Arrays.fill(password, '\0'); }
    }

    @Test void wrongPasswordFails() throws Exception {
        char[] good = "correct password".toCharArray(); char[] bad = "wrong password".toCharArray();
        try {
            EncryptedVault encrypted = CryptoEngine.encryptVault("Secret".getBytes(StandardCharsets.UTF_8), good);
            assertThrows(Exception.class, () -> CryptoEngine.decryptVault(encrypted, bad));
        } finally { Arrays.fill(good, '\0'); Arrays.fill(bad, '\0'); }
    }

    @Test void independentVaultsUseIndependentNoncesAndSalts() throws Exception {
        char[] password = "test password".toCharArray();
        try {
            EncryptedVault a = CryptoEngine.encryptVault("same".getBytes(StandardCharsets.UTF_8), password);
            EncryptedVault b = CryptoEngine.encryptVault("same".getBytes(StandardCharsets.UTF_8), password);
            assertFalse(Arrays.equals(a.getDataNonce(), b.getDataNonce()));
            assertFalse(Arrays.equals(a.getDekNonce(), b.getDekNonce()));
            assertFalse(Arrays.equals(a.getSalt(), b.getSalt()));
        } finally { Arrays.fill(password, '\0'); }
    }

    @Test void sessionCloseRevokesAllKeyOperations() throws Exception {
        char[] password = "session password".toCharArray();
        try {
            OpenedVaultData data = CryptoEngine.encryptNewVault("secret".getBytes(StandardCharsets.UTF_8), password);
            assertFalse(data.getSession().isClosed());
            data.getSession().close();
            assertTrue(data.getSession().isClosed());
            assertThrows(IllegalStateException.class, () -> data.getSession().encryptData(new byte[]{1}, new byte[12]));
            assertThrows(IllegalStateException.class, () -> data.getSession().rewrapDek(new byte[12]));
        } finally { Arrays.fill(password, '\0'); }
    }

    @Test void saveRewrapsDekWithFreshNonce() throws Exception {
        OpenedVaultData first = CryptoEngine.encryptNewVault("first".getBytes(StandardCharsets.UTF_8), "test password".toCharArray());
        EncryptedVault old = first.getEncryptedVault();
        EncryptedVault second = CryptoEngine.encryptVault("second".getBytes(StandardCharsets.UTF_8), first.getSession(), old);
        assertFalse(Arrays.equals(old.getDekNonce(), second.getDekNonce()));
        assertFalse(Arrays.equals(old.getEncryptedDek(), second.getEncryptedDek()));
        assertFalse(Arrays.equals(old.getDataNonce(), second.getDataNonce()));
        first.getSession().close();
    }

    @Test void modifiedCiphertextFailsAuthentication() throws Exception {
        char[] password = "test password".toCharArray();
        try {
            EncryptedVault original = CryptoEngine.encryptVault("secret vault data".getBytes(StandardCharsets.UTF_8), password);
            byte[] ciphertext = original.getEncryptedData(); ciphertext[0] ^= 1;
            EncryptedVault tampered = new EncryptedVault(original.getSalt(), original.getArgon2MemoryKib(), original.getArgon2Iterations(), original.getArgon2Parallelism(), original.getArgon2OutputLength(), original.getDekNonce(), original.getEncryptedDek(), original.getDataNonce(), ciphertext);
            assertThrows(Exception.class, () -> CryptoEngine.decryptVault(tampered, password));
        } finally { Arrays.fill(password, '\0'); }
    }

    @Test void vaultLifecycle() {
        Vault vault = new Vault();
        vault.setCreationTime(1); vault.setLastEditedTime(2);
        PasswordEntry entry = new PasswordEntry();
        entry.setName("Example"); entry.setUsername("user"); entry.setPassword("secret".toCharArray()); entry.setUrl("https://example.com");
        vault.addEntry(entry);
        byte[] serialized = VaultSerializer.serializeVault(vault);
        Vault result = VaultSerializer.deserializeVault(serialized);
        assertEquals(4, result.getVaultFormatVersion());
        assertEquals("secret", new String(result.getEntries().get(0).getPassword()));
        result.close(); vault.close();
    }
}
