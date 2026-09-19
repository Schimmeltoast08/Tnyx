package com.tnyx.vault;

import org.junit.jupiter.api.Test;
import java.nio.ByteBuffer;
import java.util.Arrays;
import static org.junit.jupiter.api.Assertions.*;

class VaultSerializerTest {
    @Test void roundTripUsesCanonicalV4() {
        Vault original = new Vault(); original.setCreationTime(123); original.setLastEditedTime(456);
        PasswordEntry entry = new PasswordEntry(); entry.setName("test"); entry.setUsername("user"); entry.setPassword("secret".toCharArray()); entry.setUrl("https://example.com"); original.addEntry(entry);
        byte[] serialized = VaultSerializer.serializeVault(original); Vault restored = VaultSerializer.deserializeVault(serialized);
        assertEquals(4, restored.getVaultFormatVersion()); assertEquals(1, restored.getEntries().size()); assertEquals("secret", new String(restored.getEntries().get(0).getPassword()));
        original.close(); restored.close();
    }

    @Test void truncatedDataIsRejected() {
        byte[] serialized = VaultSerializer.serializeVault(new Vault());
        assertThrows(IllegalArgumentException.class, () -> VaultSerializer.deserializeVault(Arrays.copyOf(serialized, serialized.length - 1)));
    }

    @Test void trailingDataIsRejected() {
        byte[] serialized = VaultSerializer.serializeVault(new Vault()); byte[] corrupted = Arrays.copyOf(serialized, serialized.length + 1); corrupted[corrupted.length - 1] = 1;
        assertThrows(IllegalArgumentException.class, () -> VaultSerializer.deserializeVault(corrupted));
    }

    @Test void excessiveEntryCountIsRejectedBeforeLoop() {
        byte[] serialized = VaultSerializer.serializeVault(new Vault());
        ByteBuffer b = ByteBuffer.wrap(serialized); b.position(serialized.length - 4); b.putInt(10_001);
        assertThrows(IllegalArgumentException.class, () -> VaultSerializer.deserializeVault(serialized));
    }
}
