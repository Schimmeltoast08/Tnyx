package com.tnyx.vault;

import com.tnyx.crypto.CryptoEngine;
import com.tnyx.crypto.EncryptedVault;
import com.tnyx.crypto.Encryption;
import com.tnyx.crypto.KeyDerivation;
import com.tnyx.crypto.OpenedVault;
import com.tnyx.crypto.OpenedVaultData;
import com.tnyx.crypto.VaultSession;
import com.tnyx.util.Log;
import java.io.IOException;
import java.time.Instant;
import java.util.HashMap;
import java.util.List;
import java.util.Scanner;
import java.util.UUID;

import javax.crypto.SecretKey;
import javax.crypto.spec.PSource;

public class VaultHandler {





    public static void removeEntry(String filepath, char[] password) throws IOException {
        OpenedVault openedVault = openVault(filepath, password);
        Vault vault = openedVault.getVault();

        //Vault vault = openVault(filepath, password).getVault();
        List<PasswordEntry> entries = vault.getEntries();

        if (entries.isEmpty()){
            Log.log("Entry password list in vault, nothing to remove", 3);
            System.out.println("There are no password entries in this Vault");
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
            vault.printVault();

            vault.setLastEditedTime(Instant.now().getEpochSecond());

            vault.addEntries(pw);
            //VaultWriter.writeVaultAtomic(filepath, vault, true); //TODO!!!!!!!!!!
            saveVault(filepath, openedVault);

            Log.log("Added new Vault entry", 2);
        } catch (IOException e) {
            Log.log("Could not Open Vault file when adding entry", 4);

        }

    }

    public static void editEntry(String filepath, char[] masterpassword) throws IOException {
        // copied and pasted from removeEntry
        //Vault vault = VaultReader.readVault(filepath);

        OpenedVault openedVault = openVault(filepath, masterpassword);
        Vault vault = openedVault.getVault();

        List<PasswordEntry> entries = vault.getEntries();
        //HashMap<int, UUID> map  = new HashMap<int, UUID>();
        HashMap<Integer, UUID> map = new HashMap<>();

        Integer i = 1; // would like it to be 0, but bad UX. Users are not programmers.
        System.out.println("ID  name    username    UUID");
        for (PasswordEntry entry : entries) {
            System.out.println(i + " | " + entry.getName() + " | " + entry.getUsername() + " | " + entry.getId());
            map.put(i, entry.getId());
            i++;
        }

        System.out.print("choose ID to edit: ");
        Scanner scanner = new Scanner(System.in);

        int choice = scanner.nextInt();
        UUID choiceUUID = map.get(choice);

        PasswordEntry entry = vault.getEntry(choiceUUID);

        String name = "";
        String username = "";
        String url = "";
        String password = "";

        scanner.nextLine(); // flush buffer, else the newline from int choice gets carried to the name

        System.out.println("Enter nothing if you do not want them changed"); // maby change this away from unix to if (!isNull)
        System.out.print("name: ");
        name = scanner.nextLine();
        System.out.print("username: ");
        username = scanner.nextLine();
        System.out.print("url: ");
        url = scanner.nextLine();
        System.out.print("password: ");
        password = scanner.nextLine();

        entry.editEntry(name, username, url, password);
        System.out.println("Entry successfully changed to:"
                + "\nname: " + entry.getName()
                + "\nusername: " + entry.getUsername()
                + "\nurl: " + entry.getUrl()
                + "\npassword: " + entry.getPassword()
        );

        //VaultWriter.writeVaultAtomic(filepath, vault, true);
        saveVault(filepath, openedVault);
        scanner.close();

    }

    public static void encryptVault(String filepath, char[] password) throws IOException {

        Vault vault = VaultReader.readVault(filepath);

        byte[] serializedVault = VaultSerializer.serializeVault(vault);

        try {
            EncryptedVault encryptedVault = CryptoEngine.encryptVault(serializedVault, password);

            VaultWriter.writeEncryptedVault(filepath, encryptedVault, true);

            Log.log("Vault encrypted successfully", 2);

        } catch (Exception e) {
            String message = "Could not encrypt vault: " + e.getMessage();
            Log.log(message, 4);
            throw new IOException(message, e);
        }
    }

    public static Vault decryptVault(String filepath, char[] password) throws IOException {

        EncryptedVault encryptedVault = VaultReader.readEncryptedVault(filepath);

        try {
            byte[] serializedVault = CryptoEngine.decryptVault(encryptedVault, password);

            Vault vault = VaultSerializer.deserializeVault(serializedVault);

            Log.log("Vault decrypted successfully", 2);

            return vault;

        } catch (Exception e) {
            String message = "Could not decrypt vault: " + e.getMessage();
            Log.log(message, 4);
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

            Log.log("Created encrypted vault successfully", 2);

            return new OpenedVault(
                    vault,
                    session,
                    encryptedVault
            );

        } catch (Exception e) {
            String message = "Could not create encrypted vault: " + e.getMessage();
            Log.log(message, 4);
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

            Log.log("Vault opened successfully", 2);

            return new OpenedVault(
                    vault,
                    session,
                    encryptedVault
            );

        } catch (Exception e) {
            String message = "Could not open vault: " + e.getMessage();
            Log.log(message, 4);
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

            Log.log("Vault saved successfully", 2);

        } catch (Exception e) {
            String message = "Could not save vault: " + e.getMessage();
            Log.log(message, 4);
            throw new IOException(message, e);
        }
    }


}
