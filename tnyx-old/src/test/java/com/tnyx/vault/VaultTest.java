package com.tnyx.vault;

import org.junit.jupiter.api.Test;
import java.util.UUID;
import static org.junit.jupiter.api.Assertions.*;

class VaultTest {
    @Test void addEntryAddsEntry() { Vault v=new Vault(); PasswordEntry e=new PasswordEntry(); v.addEntry(e); assertSame(e,v.getEntries().get(0)); v.close(); }
    @Test void duplicateIdRejected() { Vault v=new Vault(); UUID id=UUID.randomUUID(); v.addEntry(new PasswordEntry(id)); assertThrows(IllegalArgumentException.class, () -> v.addEntry(new PasswordEntry(id))); v.close(); }
    @Test void removeEntryRemovesCorrectEntry() { Vault v=new Vault(); PasswordEntry a=new PasswordEntry(), b=new PasswordEntry(); v.addEntry(a); v.addEntry(b); v.removeEntry(a.getId()); assertEquals(1,v.getEntries().size()); v.close(); }
    @Test void removingNonexistentEntryThrows() { Vault v=new Vault(); assertThrows(IllegalArgumentException.class, () -> v.removeEntry(UUID.randomUUID())); v.close(); }
}
