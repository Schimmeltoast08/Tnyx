package com.tnyx;

import java.io.Console;
import java.io.IOException;
import java.util.Arrays;

import com.tnyx.vault.Vault;
import com.tnyx.vault.VaultHandler;


import static com.tnyx.util.Log.log;


public class Main {

    @SuppressWarnings("unused")
    private static final int TNYX_MAIN_VERSION = 1;
    

    public static void main(String[] args) {
        log("[x][x][x] Starting application", 2);
        
    if (args.length > 0){
        switch (args[0]){
            case "--new" -> newVault(args);
            case "--edit" -> edit(args);
            case "--remove" -> remove(args);
            case "--add" -> add(args);
            case "--open" -> open(args);
        }


    }


    }

    private static void edit(String[] args) {
        try{
        VaultHandler.editEntry(args[1], getPassword());
        } catch (Exception e){}
    }

    private static void remove(String[] args) {
        try{
            char[] password = getPassword();
            VaultHandler.removeEntry(args[1], password);
            Arrays.fill(password, '\n');
        } catch (IOException e){log("Could not remove Password entry", 3);}
    }

    private static void add(String[] args) {
        Console console = System.console();

        String filepath;
        if (args.length > 1) {
            filepath = args[1];
        } else {
            filepath = console.readLine("Filepath: ");
        }

        char[] masterPW = console.readPassword("Vault Master Password: ");
        String name = console.readLine("Entry name: ");
        String username = console.readLine("Username: ");
        char[] pw = console.readPassword("Password: ");
        String password = new String(pw); // IMMUTABLE! FIX
        String url = console.readLine("Url: ");


        VaultHandler.addVaultEntry(filepath, name, username, password, url, masterPW);
    }

    private static void open(String[] args) {
        try {
            char[] password = getPassword();
            Vault vault = VaultHandler.decryptVault(args[1], password);
            vault.printVault(); // temporary for development
            Arrays.fill(password, '\n');
        } catch (IOException e) {
            log("General Error at reading vault", 4);
            System.out.println("Error at reading vault. Possibly wrong password or corrupted vault.");

        }
    }

    private static void newVault(String[] args) {
        try {
            log("Creating new Vault", 2);
            char[] password = getPassword();
            VaultHandler.createEncryptedVault(args[1], password);
            Arrays.fill(password, '\n');
        } catch (IOException e) {
            log("Failed to create Vault", 4);
        }
    }

    private static char[] getPassword() {
        Console console = System.console();

        if (console == null){
            log("Could not acquire console for password entry", 4);
        }

        char[] password = console.readPassword("Master password: ");
        assert password != null; // just in case
        return password;
    }


}
