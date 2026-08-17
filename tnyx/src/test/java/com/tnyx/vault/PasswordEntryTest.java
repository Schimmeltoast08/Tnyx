package com.tnyx.vault;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import org.junit.jupiter.api.Test;

class PasswordEntryTest {

    @Test
    void newEntryHasUUID() {
        PasswordEntry entry = new PasswordEntry();

        assertNotNull(entry.getId());
    }

    @Test
    void differentEntriesHaveDifferentUUIDs() {
        PasswordEntry first = new PasswordEntry();
        PasswordEntry second = new PasswordEntry();

        assertNotEquals(first.getId(), second.getId());
    }

    @Test
    void manyEntriesHaveDifferentUUIDs() {
        Set<UUID> ids = new HashSet<>();

        for (int i = 0; i < 999; i++) {
            ids.add(new PasswordEntry().getId());
        }

        assertEquals(999, ids.size());
    }

    @Test
    void editEntryChangesFields() {
        PasswordEntry entry = new PasswordEntry();
        UUID originalUUID = entry.getId();

        entry.setName("Gitlab");
        entry.setUsername("Ada Lovelace");
        entry.setUrl("https://www.stackoverflow.com");
        entry.setPassword("weakPassword");

        entry.editEntry("GitHub", "Person", "https://github.com", "password123");

        assertEquals("GitHub", entry.getName());
        assertEquals("Person", entry.getUsername());
        assertEquals("password123", entry.getPassword());
        assertEquals("https://github.com", entry.getUrl());
        assertEquals(originalUUID, entry.getId());
    }

}
