package com.tnyx.PasswordEntryTests;

import com.tnyx.vault.PasswordEntry;
import org.junit.jupiter.api.Test;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;
import static org.junit.jupiter.api.Assertions.*;

class PasswordEntryTest {
    @Test void newEntryHasUUID() { assertNotNull(new PasswordEntry().getId()); }
    @Test void differentEntriesHaveDifferentUUIDs() { assertNotEquals(new PasswordEntry().getId(), new PasswordEntry().getId()); }
    @Test void manyEntriesHaveDifferentUUIDs() { Set<UUID> ids = new HashSet<>(); for (int i=0;i<999;i++) ids.add(new PasswordEntry().getId()); assertEquals(999, ids.size()); }
    @Test void editEntryChangesFields() {
        PasswordEntry entry = new PasswordEntry(); UUID id = entry.getId();
        entry.setName("Gitlab"); entry.setUsername("Ada Lovelace"); entry.setUrl("https://example.com"); entry.setPassword("weakPassword".toCharArray());
        entry.editEntry("GitHub", "Person", "https://github.com", "password123".toCharArray());
        assertEquals("GitHub", entry.getName()); assertEquals("Person", entry.getUsername()); assertEquals("password123", new String(entry.getPassword())); assertEquals("https://github.com", entry.getUrl()); assertEquals(id, entry.getId());
        entry.close();
    }
}
