package com.tnyx.vault;

import com.tnyx.crypto.CryptoEngine;
import com.tnyx.crypto.EncryptedVault;
import com.tnyx.crypto.Encryption;
import com.tnyx.crypto.KeyDerivation;
import com.tnyx.crypto.OpenedVault;
import com.tnyx.crypto.OpenedVaultData;

import java.io.Console;
import java.io.IOException;
import java.nio.file.Path;
import java.security.MessageDigest;
import java.time.Instant;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;
import java.util.Scanner;

import static com.tnyx.util.Log.log;

public final class VaultHandler {
    private VaultHandler() {}

    public static void addVaultEntry(String filepath, String name, String username, char[] password, String url, char[] masterPassword) throws IOException {
        PasswordEntry entry = new PasswordEntry();
        entry.setName(name); entry.setUsername(username); entry.setUrl(url);
        entry.setPassword(password);
        try (OpenedVault opened = openVault(filepath, masterPassword)) {
            opened.getVault().addEntry(entry);
            saveVault(filepath, opened);
            log("Added new vault entry", 2);
        } finally {
            entry.close();
        }
    }

    public static void removeEntry(String filepath, char[] password) throws IOException {
        try (OpenedVault opened = openVault(filepath, password)) {
            Vault vault = opened.getVault();
            List<PasswordEntry> entries = vault.getEntries();
            if (entries.isEmpty()) { System.out.println("There are no password entries in this vault"); return; }
            HashMap<Integer, UUID> map = new HashMap<>();
            int i = 1;
            for (PasswordEntry entry : entries) {
                System.out.println(i + " | " + entry.getName() + " | " + entry.getUsername() + " | " + entry.getId());
                map.put(i++, entry.getId());
            }
            Console console = requireConsole();
            String input = console.readLine("Choose ID to remove: ");
            int choice;
            try { choice = Integer.parseInt(input); } catch (NumberFormatException e) { throw new IOException("Invalid entry selection", e); }
            UUID choiceUUID = map.get(choice);
            if (choiceUUID == null) throw new IOException("Invalid entry selection");
            vault.removeEntry(choiceUUID);
            saveVault(filepath, opened);
        }
    }

    public static void editEntry(String filepath, char[] masterPassword) throws IOException {
        try (OpenedVault opened = openVault(filepath, masterPassword)) {
            Vault vault = opened.getVault();
            List<PasswordEntry> entries = vault.getEntries();
            HashMap<Integer, UUID> map = new HashMap<>();
            int i = 1;
            for (PasswordEntry entry : entries) {
                System.out.printf("%-4d %-25s %-25s %-36s%n", i, entry.getName(), entry.getUsername(), entry.getId());
                map.put(i++, entry.getId());
            }
            Console console = requireConsole();
            int choice;
            try { choice = Integer.parseInt(console.readLine("Choose ID to edit: ")); } catch (NumberFormatException e) { throw new IOException("Invalid entry selection", e); }
            UUID id = map.get(choice);
            if (id == null) throw new IOException("Invalid entry selection");
            PasswordEntry entry = vault.getEntry(id);
            String name = console.readLine("Name (blank = unchanged): ");
            String username = console.readLine("Username (blank = unchanged): ");
            String url = console.readLine("URL (blank = unchanged): ");
            char[] pw = console.readPassword("New password (blank = unchanged): ");
            try {
                char[] pw2 = pw.length == 0 ? new char[0] : console.readPassword("Enter password again: ");
                try {
                    if (!Arrays.equals(pw, pw2)) throw new IOException("Passwords do not match");
                    entry.editEntry(name, username, url, pw);
                } finally { Arrays.fill(pw2, '\0'); }
            } finally { Arrays.fill(pw, '\0'); }
            saveVault(filepath, opened);
        }
    }

    public static Vault decryptVault(String filepath, char[] password) throws IOException {
        EncryptedVault encryptedVault = VaultReader.readEncryptedVault(filepath);
        try {
            byte[] serialized = CryptoEngine.decryptVault(encryptedVault, password);
            return VaultSerializer.deserializeVault(serialized);
        } catch (Exception e) {
            throw new IOException("Could not decrypt vault: " + e.getMessage(), e);
        }
    }

    public static OpenedVault createEncryptedVault(String filepath, char[] password) throws IOException {
        Path path = normalize(filepath);
        VaultFileLock lock = VaultFileLock.acquire(path);
        try {
            Vault vault = new Vault();
            vault.setCreationTime(Instant.now().getEpochSecond());
            vault.setLastEditedTime(vault.getCreationTime());
            byte[] serialized = VaultSerializer.serializeVault(vault);
            OpenedVaultData data = CryptoEngine.encryptNewVault(serialized, password);
            VaultWriter.createNewVault(path.toString(), data.getEncryptedVault());
            byte[] fingerprint = fingerprint(VaultReader.readBytes(path.toString()));
            return new OpenedVault(vault, data.getSession(), data.getEncryptedVault(), path, lock, fingerprint);
        } catch (Exception e) {
            lock.close();
            throw new IOException("Could not create encrypted vault: " + e.getMessage(), e);
        }
    }

    public static OpenedVault openVault(String filepath, char[] password) throws IOException {
        Path path = normalize(filepath);
        VaultFileLock lock = VaultFileLock.acquire(path);
        try {
            byte[] fileBytes = VaultReader.readBytes(path.toString());
            EncryptedVault encryptedVault = EncryptedVaultSerializer.deserializeEncryptedVault(fileBytes);
            var session = CryptoEngine.openSession(encryptedVault, password);
            try {
                byte[] serialized = session.decryptData(encryptedVault.getEncryptedData(), encryptedVault.getDataNonce());
                Vault vault = VaultSerializer.deserializeVault(serialized);
                return new OpenedVault(vault, session, encryptedVault, path, lock, fingerprint(fileBytes));
            } catch (Exception e) {
                session.close();
                throw e;
            }
        } catch (Exception e) {
            lock.close();
            throw new IOException("Could not open vault: " + e.getMessage(), e);
        }
    }

    public static void saveVault(String filepath, OpenedVault openedVault) throws IOException {
        if (openedVault == null) throw new IOException("Vault is not open");
        Path requested = normalize(filepath);
        if (!requested.equals(openedVault.getPath())) throw new IOException("Vault path changed while open");
        try {
            byte[] currentBytes = VaultReader.readBytes(requested.toString());
            byte[] currentFingerprint = fingerprint(currentBytes);
            if (!MessageDigest.isEqual(currentFingerprint, openedVault.getFingerprint())) {
                throw new IOException("Vault changed on disk since it was opened; refusing to overwrite external changes");
            }
            Vault vault = openedVault.getVault();
            long oldTimestamp = vault.getLastEditedTime();
            vault.setLastEditedTime(Instant.now().getEpochSecond());
            try {
                byte[] serialized = VaultSerializer.serializeVault(vault);
                EncryptedVault newEncryptedVault = CryptoEngine.encryptVault(serialized, openedVault.getSession(), openedVault.getEncryptedVault());
                VaultWriter.writeEncryptedVault(requested.toString(), newEncryptedVault);
                byte[] newFingerprint = fingerprint(VaultReader.readBytes(requested.toString()));
                openedVault.setEncryptedVault(newEncryptedVault, newFingerprint);
            } catch (Exception e) {
                vault.setLastEditedTime(oldTimestamp);
                throw e;
            }
        } catch (IOException e) {
            throw e;
        } catch (Exception e) {
            throw new IOException("Could not save vault: " + e.getMessage(), e);
        }
    }

    private static Path normalize(String filepath) throws IOException {
        if (filepath == null || filepath.isBlank()) throw new IOException("Vault path is empty");
        return Path.of(filepath).toAbsolutePath().normalize();
    }

    private static byte[] fingerprint(byte[] data) throws IOException {
        try { return MessageDigest.getInstance("SHA-256").digest(data); }
        catch (Exception e) { throw new IOException("Could not fingerprint vault", e); }
    }

    private static Console requireConsole() throws IOException {
        Console console = System.console();
        if (console == null) throw new IOException("Password entry requires a real console");
        return console;
    }
}
