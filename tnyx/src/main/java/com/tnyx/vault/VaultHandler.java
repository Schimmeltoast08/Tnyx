package com.tnyx.vault;

import com.tnyx.util.Log;
import java.io.IOException;
import java.time.Instant;

public class VaultHandler {

    @SuppressWarnings("unused")
    private static final int TNYX_MAIN_VERSION = 1;

    public static void createOpenVault(String filepath) throws IOException {
        VaultWriter.createOpenVault(filepath);
    }

    public static Vault readVault(String filepath) throws IOException {
        return VaultReader.readVault(filepath);
    }

    public static void writeVaultAtomic(String filepath, Vault vault, Boolean atomic) throws IOException {
        VaultWriter.writeVaultAtomic(filepath, vault, atomic);
    }

    public static void addVaultEntry(String filepath, String name, String username, String password, String url) {

        PasswordEntry pw = new PasswordEntry();

        pw.setName(name);
        pw.setPassword(password);
        pw.setUrl(url);
        pw.setUsername(username);

        try {
            Vault vault = VaultReader.readVault(filepath);

            vault.setLastEditedTime(Instant.now().getEpochSecond());

            vault.getEntries().add(pw);
            VaultWriter.writeVaultAtomic(filepath, vault, true);

            Log.log("Added new Vault entry", 2);
        } catch (IOException e) {
            Log.log("Could not Open Vault file when adding entry", 4);

        }

    }

}
