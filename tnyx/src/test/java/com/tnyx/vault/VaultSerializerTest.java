package com.tnyx.vault;

import java.io.IOException;
import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import org.junit.jupiter.api.Test;

public class VaultSerializerTest {

    @Test
    void emptyVaultRoundTrip() throws IOException {
        Vault original = new Vault();

        original.setVaultFormatVersion(0);
        original.setKDF("Argon2id");
        original.setSalt(new byte[]{1, 2, 3, 4});
        original.setEncryptionAlgorithm("AES/GCM/NoPadding");
        original.setNonce(new byte[]{5, 6, 7, 8});
        original.setCreationTime(123456789L);
        original.setLastEditedTime(987654321L); // unix epoch second
        original.setNonce2(new byte[]{9, 10, 11, 12});

        byte[] serialized = VaultSerializer.serializeVault(original);
        Vault restored = VaultSerializer.deserializeVault(serialized);

        assertEquals(0, restored.getEntries().size());
        assertEquals(original.getVaultFormatVersion(), restored.getVaultFormatVersion());
        assertEquals(original.getKDF(), restored.getKDF());
        assertArrayEquals(original.getSalt(), restored.getSalt());
        assertEquals(original.getEncryptionAlgorithm(), restored.getEncryptionAlgorithm());
        assertArrayEquals(original.getNonce(), restored.getNonce());
        assertEquals(original.getCreationTime(), restored.getCreationTime());
        assertEquals(original.getLastEditedTime(), restored.getLastEditedTime());
        assertArrayEquals(original.getNonce2(), restored.getNonce2());
    }

    @Test
    void nonEmptyRoundTrip() throws IOException {
        Vault original = new Vault();

        original.setVaultFormatVersion(0);
        original.setKDF("Argon2id");
        original.setSalt(new byte[]{1, 2, 3, 4});
        original.setEncryptionAlgorithm("AES/GCM/NoPadding");
        original.setNonce(new byte[]{5, 6, 7, 8});
        original.setCreationTime(123456789L);
        original.setLastEditedTime(987654321L);
        original.setNonce2(new byte[]{9, 10, 11, 12});

        PasswordEntry entry = new PasswordEntry();
        entry.setName("testEntry");
        entry.setPassword("testPassword");
        entry.setUrl("https://www.test.url.com/tnyx");
        entry.setUsername("testUsername");

        original.getEntries().add(entry);

        byte[] serialized = VaultSerializer.serializeVault(original);
        Vault restored = VaultSerializer.deserializeVault(serialized);

        assertEquals(1, restored.getEntries().size());
    }

    @Test
    void testErrorHandelingOfVaultSerializer() throws IOException {
        Vault original = new Vault();

        original.setVaultFormatVersion(0);
        original.setKDF("Argon2id");
        original.setSalt(new byte[]{1, 2, 3, 4});
        original.setEncryptionAlgorithm("AES/GCM/NoPadding");
        original.setNonce(new byte[]{5, 6, 7, 8});
        original.setCreationTime(123456789L);
        original.setLastEditedTime(987654321L);
        original.setNonce2(new byte[]{9, 10, 11, 12});

        byte[] serialized = VaultSerializer.serializeVault(original); // last 4 bytes are 00 00 00 00
        byte[] corrupted = Arrays.copyOf(serialized, serialized.length - 4); // remove last 4 bytes

        assertThrows(IllegalArgumentException.class, () -> VaultSerializer.deserializeVault(corrupted)); // check if it throws an error (it should)

    }

    @Test
    void testTruncatedEntry() throws IOException {
        Vault original = new Vault();

        original.setVaultFormatVersion(0);
        original.setKDF("Argon2id");
        original.setSalt(new byte[]{1, 2, 3, 4});
        original.setEncryptionAlgorithm("AES/GCM/NoPadding");
        original.setNonce(new byte[]{5, 6, 7, 8});
        original.setCreationTime(123456789L);
        original.setLastEditedTime(987654321L);
        original.setNonce2(new byte[]{9, 10, 11, 12});

        byte[] serialized = VaultSerializer.serializeVault(original);
        byte[] corrupted = Arrays.copyOf(serialized, serialized.length - 5);
        assertThrows(IllegalArgumentException.class, () -> VaultSerializer.deserializeVault(corrupted));

    }

}
