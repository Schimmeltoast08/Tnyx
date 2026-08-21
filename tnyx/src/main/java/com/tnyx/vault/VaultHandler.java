package com.tnyx.vault;

import com.tnyx.crypto.CryptoEngine;
import com.tnyx.util.Log;
import java.io.IOException;
import java.time.Instant;
import java.util.HashMap;
import java.util.List;
import java.util.Scanner;
import java.util.UUID;

public class VaultHandler {

    @SuppressWarnings("unused")
    private static final int TNYX_MAIN_VERSION = 1;

    public static void createOpenVault(String filepath) throws IOException {
        VaultWriter.createOpenVault(filepath);
    }

    public static Vault readVault(String filepath) throws IOException {
        return VaultReader.readVault(filepath);
    }

    public static void removeEntry(String filepath) throws IOException {
        Vault vault = VaultReader.readVault(filepath);

        List<PasswordEntry> entries = vault.getEntries();
        //HashMap<int, UUID> map  = new HashMap<int, UUID>();
        HashMap<Integer, UUID> map = new HashMap<Integer, UUID>();

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
        VaultWriter.writeVaultAtomic(filepath, vault, true);
        scanner.close();
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

            vault.addEntries(pw);
            VaultWriter.writeVaultAtomic(filepath, vault, true);

            Log.log("Added new Vault entry", 2);
        } catch (IOException e) {
            Log.log("Could not Open Vault file when adding entry", 4);

        }

    }

    public static void editEntry(String filepath) throws IOException {
        // copy paste from removeEntry
        Vault vault = VaultReader.readVault(filepath);

        List<PasswordEntry> entries = vault.getEntries();
        //HashMap<int, UUID> map  = new HashMap<int, UUID>();
        HashMap<Integer, UUID> map = new HashMap<Integer, UUID>();

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
        
        
        VaultWriter.writeVaultAtomic(filepath, vault, true);
        scanner.close();

    }

    public static void encryptVault(String filepath, char[] password) throws IOException {
        Vault vault = readVault(filepath);
        byte[] serializedVault = VaultSerializer.serializeVault(vault);
        
        for (byte b : serializedVault){
            System.out.printf("%02x ", b & 0xFF); //TODO Temporary, security risk
        }



    }




}
