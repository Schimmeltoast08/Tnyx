package com.tnyx.vault;

import com.tnyx.crypto.CryptoConstants;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

public final class Vault implements AutoCloseable {
    public static final int CURRENT_FORMAT_VERSION = 4;

    private int vaultFormatVersion = CURRENT_FORMAT_VERSION;
    private long creationTime;
    private long lastEditedTime;
    private final List<PasswordEntry> entries = new ArrayList<>();

    public int getVaultFormatVersion() { return vaultFormatVersion; }
    public void setVaultFormatVersion(int version) { this.vaultFormatVersion = version; }
    public long getCreationTime() { return creationTime; }
    public void setCreationTime(long value) { this.creationTime = value; }
    public long getLastEditedTime() { return lastEditedTime; }
    public void setLastEditedTime(long value) { this.lastEditedTime = value; }

    public List<PasswordEntry> getEntries() { return Collections.unmodifiableList(entries); }

    public PasswordEntry getEntry(UUID uuid) {
        if (uuid == null) throw new IllegalArgumentException("entry id must not be null");
        for (PasswordEntry entry : entries) {
            if (entry.getId().equals(uuid)) return entry;
        }
        throw new IllegalArgumentException("No entry found with UUID " + uuid);
    }

    public void addEntry(PasswordEntry entry) {
        if (entry == null) throw new IllegalArgumentException("entry must not be null");
        if (entries.size() >= CryptoConstants.MAX_ENTRIES) throw new IllegalArgumentException("Too many entries");
        if (entries.stream().anyMatch(existing -> existing.getId().equals(entry.getId()))) {
            throw new IllegalArgumentException("Duplicate entry UUID");
        }
        entries.add(entry);
    }

    public void removeEntry(UUID id) {
        if (id == null) throw new IllegalArgumentException("entry id must not be null");
        boolean removed = entries.removeIf(entry -> entry.getId().equals(id));
        if (!removed) throw new IllegalArgumentException("No entry found with ID: " + id);
    }

    public void printVault() {
        Instant creation = Instant.ofEpochSecond(creationTime);
        Instant edited = Instant.ofEpochSecond(lastEditedTime);
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm:ss");
        System.out.println("======================================== VAULT ========================================");
        System.out.printf("Format Version : %-10d%n", vaultFormatVersion);
        System.out.printf("Created        : %-10s%n", ZonedDateTime.ofInstant(creation, ZoneId.of("UTC")).format(formatter));
        System.out.printf("Last Edited    : %-10s%n", ZonedDateTime.ofInstant(edited, ZoneId.of("UTC")).format(formatter));
        System.out.println();
        System.out.println("======================================= ENTRIES =======================================");
        System.out.printf("%-4s %-25s %-25s %-25s %-35s%n", "ID", "Name", "Username", "Password", "URL");
        System.out.println("---- ------------------------- ------------------------- ------------------------- -----------------------------------");
        int id = 1;
        for (PasswordEntry entry : entries) {
            System.out.printf("%-4d %-25s %-25s %-25s %-35s%n", id++, entry.getName(), entry.getUsername(), "*".repeat(entry.passwordLength()), entry.getUrl());
        }
        System.out.println("========================================================================================");
    }

    @Override
    public void close() {
        for (PasswordEntry entry : entries) entry.close();
        entries.clear();
    }
}
