package com.tnyx.vault;

import java.util.UUID;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import org.junit.jupiter.api.Test;

public class VaultTest {

    @Test
    void addEntryAddsEntry() {
        Vault vault = new Vault();
        PasswordEntry entry = new PasswordEntry();

        vault.addEntry(entry);

        assertEquals(1, vault.getEntries().size());
        assertEquals(entry, vault.getEntries().get(0));
    }

    @Test
    void getEntryFindsCorrectEntry() {
        Vault vault = new Vault();

        PasswordEntry first = new PasswordEntry();
        PasswordEntry second = new PasswordEntry();

        vault.addEntry(first);
        vault.addEntry(second);

        assertSame(second, vault.getEntry(second.getId()));
    }

    @Test
void removeEntryRemovesCorrectEntry() {
    Vault vault = new Vault();

    PasswordEntry first = new PasswordEntry();
    PasswordEntry second = new PasswordEntry();

    vault.addEntry(first);
    vault.addEntry(second);

    vault.removeEntry(first.getId());

    assertEquals(1, vault.getEntries().size());

}

@Test
void removingNonexistentEntryThrows() {
    Vault vault = new Vault();

    UUID id = UUID.randomUUID();

    assertThrows(
        IllegalArgumentException.class,
        () -> vault.removeEntry(id)
    );
}








}
