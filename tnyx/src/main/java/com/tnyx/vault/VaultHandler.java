package com.tnyx.vault;

import com.tnyx.crypto.CryptoEngine;
import com.tnyx.crypto.EncryptedVault;
import com.tnyx.crypto.Encryption;
import com.tnyx.crypto.KeyDerivation;
import com.tnyx.crypto.OpenedVault;
import com.tnyx.crypto.OpenedVaultData;
import com.tnyx.crypto.VaultSession;


import java.io.Console;
import java.io.IOException;
import java.time.Instant;
import java.util.HashMap;
import java.util.List;
import java.util.Scanner;
import java.util.UUID;

import javax.crypto.SecretKey;

import static com.tnyx.util.Log.log;


public class VaultHandler {





    public static void removeEntry(String filepath, char[] password) throws IOException {
        OpenedVault openedVault = openVault(filepath, password);
        Vault vault = openedVault.getVault();

        //Vault vault = openVault(filepath, password).getVault();
        List<PasswordEntry> entries = vault.getEntries();

        if (entries.isEmpty()){
            log("Entry password list in vault, nothing to remove", 3);
            System.out.println("There are no password entries in this Vault");
            return;
        }

        HashMap<Integer, UUID> map = new HashMap<>();

        Integer i = 1; // would like it to be 0, but bad UX. Users are not programmers.
        System.out.println("ID  name    username    UUID");
        for (PasswordEntry entry : entries) {
            System.out.println(i + " | " + entry.getName() + " | " + entry.getUsername() + " | " + entry.getId());
            map.put(i, entry.getId());
            i++;
        }

        System.out.print("choose ID to remove: ");
        Scanner scanner = new Scanner(System.in);

        int choice = scanner.nextInt();
        UUID choiceUUID = map.get(choice);

        vault.removeEntry(choiceUUID);

        //VaultWriter.writeVaultAtomic(filepath, vault, true);
        saveVault(filepath, openedVault);

        scanner.close();
    }


    public static void addVaultEntry(String filepath, String name, String username, String password, String url, char[] masterpassword) {

        PasswordEntry pw = new PasswordEntry();

        pw.setName(name);
        pw.setPassword(password);
        pw.setUrl(url);
        pw.setUsername(username);

        try {
            OpenedVault openedVault = openVault(filepath, masterpassword);
            Vault vault = openedVault.getVault();

            vault.setLastEditedTime(Instant.now().getEpochSecond());

            vault.addEntries(pw);
            //VaultWriter.writeVaultAtomic(filepath, vault, true); //TODO!!!!!!!!!!
            saveVault(filepath, openedVault);

            log("Added new Vault entry", 2);
        } catch (IOException e) {
            log("Could not Open Vault file when adding entry", 4);

        }

    }

    public static void editEntry(String filepath, char[] masterpassword) throws IOException {

        OpenedVault openedVault = openVault(filepath, masterpassword);
        Vault vault = openedVault.getVault();

        List<PasswordEntry> entries = vault.getEntries();
        HashMap<Integer, UUID> map = new HashMap<>();

        Integer i = 1; // would like it to be 0, but bad UX. Users are not programmers.
        System.out.println("======================================== EDIT ENTRY ========================================");

        // Table header
        System.out.printf(
                "%-4s %-25s %-25s %-36s%n", // disclosure: this printf call was beautified by Ai. I wrote it, it looked like shit and asked Ai to format it.
                "ID",
                "Name",
                "Username",
                "UUID"
        );

        System.out.println(
                "---- ------------------------- ------------------------- ------------------------------------"
        );

        for (PasswordEntry entry : entries) {

            System.out.printf(
                    "%-4d %-25s %-25s %-36s%n",
                    i,
                    entry.getName(),
                    entry.getUsername(),
                    entry.getId()
            );

            map.put(i, entry.getId());
            i++;
        }

        System.out.println(
                "============================================================================================"
        );

        System.out.print("choose ID to edit: ");
        Console console = System.console();


        int choice = Integer.parseInt(console.readLine());
        UUID choiceUUID = map.get(choice);


        PasswordEntry entry = vault.getEntry(choiceUUID);

        System.out.println("");
        System.out.println("Enter nothing if you do not want them changed"); // maby change this away from unix to if (!isNull)
        String name = console.readLine("Name: ");
        String username = console.readLine("Username: ");
        String url = console.readLine("Url: ");

        String password;
        String password2;
        do {
            password = new String(console.readPassword("Password: "));
            password2 = new String(console.readPassword("Enter Password again: "));

            if (!(password.equals(password2))){
                System.out.println("Passwords do not match. Please try again");
            }

        } while (!(password.equals(password2)));

        entry.editEntry(name, username, url, password);
        System.out.println("");

        System.out.println("Entry successfully changed to:");
        entry.printEntry();

        //VaultWriter.writeVaultAtomic(filepath, vault, true);
        saveVault(filepath, openedVault);

    }


    public static Vault decryptVault(String filepath, char[] password) throws IOException {

        EncryptedVault encryptedVault = VaultReader.readEncryptedVault(filepath);

        try {
            byte[] serializedVault = CryptoEngine.decryptVault(encryptedVault, password);

            Vault vault = VaultSerializer.deserializeVault(serializedVault);

            log("Vault decrypted successfully", 2);

            return vault;

        } catch (Exception e) {
            String message = "Could not decrypt vault: " + e.getMessage();
            log(message, 4);
            throw new IOException(message, e);
        }
    }

    public static OpenedVault createEncryptedVault(String filepath, char[] password) throws IOException {

        Vault vault = new Vault();

        vault.setVaultFormatVersion(3);
        vault.setKDF("Argon2id");
        vault.setSalt(KeyDerivation.generateSalt());
        vault.setEncryptionAlgorithm("AES");
        vault.setNonce(Encryption.generateNonce());
        vault.setCreationTime(Instant.now().getEpochSecond());
        vault.setLastEditedTime(Instant.now().getEpochSecond());
        vault.setNonce2(Encryption.generateNonce());

        try {
            byte[] serializedVault = VaultSerializer.serializeVault(vault);

            OpenedVaultData openedVaultData = CryptoEngine.encryptNewVault(
                    serializedVault,
                    password
            );

            EncryptedVault encryptedVault = openedVaultData.getEncryptedVault();
            VaultSession session = openedVaultData.getSession();

            VaultWriter.writeEncryptedVault(
                    filepath,
                    encryptedVault,
                    true
            );

            log("Created encrypted vault successfully", 2);

            return new OpenedVault(
                    vault,
                    session,
                    encryptedVault
            );

        } catch (Exception e) {
            String message = "Could not create encrypted vault: " + e.getMessage();
            log(message, 4);
            throw new IOException(message, e);
        }
    }

    public static OpenedVault openVault(String filepath, char[] password) throws IOException {

        EncryptedVault encryptedVault = VaultReader.readEncryptedVault(filepath);

        try {
            SecretKey kek = CryptoEngine.deriveKek(
                    password,
                    encryptedVault.getSalt(),
                    encryptedVault.getArgon2MemoryKib(),
                    encryptedVault.getArgon2Iterations(),
                    encryptedVault.getArgon2Parallelism(),
                    encryptedVault.getArgon2OutputLength()
            );

            SecretKey dek = CryptoEngine.decryptDek(
                    encryptedVault.getEncryptedDek(),
                    kek,
                    encryptedVault.getDekNonce()
            );

            byte[] serializedVault = CryptoEngine.decryptData(
                    encryptedVault.getEncryptedData(),
                    dek,
                    encryptedVault.getDataNonce()
            );

            Vault vault = VaultSerializer.deserializeVault(serializedVault);

            VaultSession session = new VaultSession(dek);

            log("Vault opened successfully", 2);

            return new OpenedVault(
                    vault,
                    session,
                    encryptedVault
            );

        } catch (Exception e) {
            String message = "Could not open vault: " + e.getMessage();
            log(message, 4);
            throw new IOException(message, e);
        }
    }

    public static void saveVault(String filepath, OpenedVault openedVault) throws IOException {

        try {
            Vault vault = openedVault.getVault();

            vault.setLastEditedTime(Instant.now().getEpochSecond());

            byte[] serializedVault = VaultSerializer.serializeVault(vault);

            EncryptedVault oldEncryptedVault = openedVault.getEncryptedVault();

            EncryptedVault newEncryptedVault = CryptoEngine.encryptVault(
                    serializedVault,
                    openedVault.getSession(),
                    oldEncryptedVault.getSalt(),
                    oldEncryptedVault.getArgon2MemoryKib(),
                    oldEncryptedVault.getArgon2Iterations(),
                    oldEncryptedVault.getArgon2Parallelism(),
                    oldEncryptedVault.getArgon2OutputLength(),
                    oldEncryptedVault.getEncryptedDek(),
                    oldEncryptedVault.getDekNonce()
            );

            VaultWriter.writeEncryptedVault(
                    filepath,
                    newEncryptedVault,
                    true
            );

            openedVault.setEncryptedVault(newEncryptedVault);

            log("Vault saved successfully", 2);

        } catch (Exception e) {
            String message = "Could not save vault: " + e.getMessage();
            log(message, 4);
            throw new IOException(message, e);
        }
    }


}
