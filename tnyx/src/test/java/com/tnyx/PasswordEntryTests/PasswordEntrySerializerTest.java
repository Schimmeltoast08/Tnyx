package com.tnyx.PasswordEntryTests;

import com.tnyx.vault.Password.PasswordEntrySerializer;
import com.tnyx.vault.PasswordEntry;
import static org.junit.jupiter.api.Assertions.assertEquals;
import org.junit.jupiter.api.Test;

public class PasswordEntrySerializerTest {

    @Test
    void emptyFieldsSurviveSerialization() {
        PasswordEntry original = new PasswordEntry();

        original.setName("");
        original.setUsername("");
        original.setPassword("");
        original.setUrl("");

        PasswordEntry result = PasswordEntrySerializer.deserializePasswordEntry(PasswordEntrySerializer.serializePasswordEntry(original));

        assertEquals("", result.getName());
        assertEquals("", result.getUsername());
        assertEquals("", result.getPassword());
        assertEquals("", result.getUrl());
    }

    @Test
    void DoubleBarSurvivesSerialization() { // cuz old format used || as a separator of fields rather than length based delimiters
        PasswordEntry original = new PasswordEntry();
        original.setPassword("This is || a test");
        PasswordEntry result = PasswordEntrySerializer.deserializePasswordEntry(PasswordEntrySerializer.serializePasswordEntry(original));

        assertEquals("This is || a test", result.getPassword());

    }

    @Test
    void NewLineSurvivesSerialization() {
        PasswordEntry original = new PasswordEntry();
        original.setPassword("This is \\n a test");
        original.setName("\n");
        original.setUrl("a".repeat(10000000));
        PasswordEntry result = PasswordEntrySerializer.deserializePasswordEntry(PasswordEntrySerializer.serializePasswordEntry(original));

        assertEquals("This is \\n a test", result.getPassword());
        assertEquals("\n", original.getName());
        assertEquals("a".repeat(10000000), original.getUrl());
    }

    @Test
    void emojiAsPassword() {
        PasswordEntry original = new PasswordEntry();
        original.setPassword("✌️");
        PasswordEntry result = PasswordEntrySerializer.deserializePasswordEntry(PasswordEntrySerializer.serializePasswordEntry(original));

        assertEquals("✌️", result.getPassword());

    }

}
