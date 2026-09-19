package com.tnyx.PasswordEntryTests;

import com.tnyx.vault.PasswordEntry;
import com.tnyx.vault.PasswordEntrySerializer;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class PasswordEntrySerializerTest {
    @Test void emptyFieldsSurviveSerialization() {
        PasswordEntry original = new PasswordEntry();
        PasswordEntry result = PasswordEntrySerializer.deserializePasswordEntry(PasswordEntrySerializer.serializePasswordEntry(original));
        assertEquals("", result.getName()); assertEquals("", result.getUsername()); assertEquals(0, result.getPassword().length); assertEquals("", result.getUrl());
        original.close(); result.close();
    }

    @Test void delimiterAndUnicodeSurviveSerialization() {
        PasswordEntry original = new PasswordEntry(); original.setPassword("This is || ✌️".toCharArray()); original.setName("\n");
        PasswordEntry result = PasswordEntrySerializer.deserializePasswordEntry(PasswordEntrySerializer.serializePasswordEntry(original));
        assertEquals("This is || ✌️", new String(result.getPassword())); assertEquals("\n", result.getName());
        original.close(); result.close();
    }

    @Test void oversizedUrlIsRejected() {
        PasswordEntry original = new PasswordEntry(); original.setUrl("a".repeat(16 * 1024 + 1));
        assertThrows(IllegalArgumentException.class, () -> PasswordEntrySerializer.serializePasswordEntry(original));
    }

    @Test void invalidUtf8IsRejected() {
        PasswordEntry entry = new PasswordEntry();
        byte[] corrupted = new byte[16 + 16 + 1];
        java.nio.ByteBuffer b = java.nio.ByteBuffer.wrap(corrupted); b.putLong(entry.getId().getMostSignificantBits()).putLong(entry.getId().getLeastSignificantBits()); b.putInt(1).putInt(0).putInt(0).putInt(0).put((byte)0xC3);
        assertThrows(IllegalArgumentException.class, () -> PasswordEntrySerializer.deserializePasswordEntry(corrupted));
    }
}
