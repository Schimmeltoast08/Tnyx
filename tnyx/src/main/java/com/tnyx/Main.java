package com.tnyx;

import java.io.Console;
import java.io.IOException;
import java.util.Arrays;

import com.tnyx.ui.ThemeManager;
import com.tnyx.ui.UiManager;
import com.tnyx.vault.Vault;
import com.tnyx.vault.VaultHandler;


import javax.swing.*;

import static com.tnyx.util.Log.log;


public class Main {

    @SuppressWarnings("unused")
    private static final int TNYX_MAIN_VERSION = 1;
    

    public static void main(String[] args) {
        log("=== Starting application ===", 2);
        
    if (args.length > 0){
        switch (args[0]){
            case "--new" -> newVault(args);
            case "--edit" -> edit(args);
            case "--remove" -> remove(args);
            case "--add" -> add(args);
            case "--open" -> open(args);
            case "--gui" -> gui(args);
            default -> gui(args);
        }



    }


    }

    private static void edit(String[] args) {
        if (args.length < 2) {
            log("Missing vault path for --edit", 3);
            return;
        }
        try {
            char[] password = getPassword();
            try {
                VaultHandler.editEntry(args[1], password);
            } finally {
                Arrays.fill(password, '\0');
            }
        } catch (IOException e) {
            log("Could not edit password entry", 4);
        }
    }

    private static void remove(String[] args) {
        try{
            char[] password = getPassword();
            VaultHandler.removeEntry(args[1], password);
            Arrays.fill(password, '\n');
        } catch (IOException e){log("Could not remove Password entry", 3);}
    }

    private static void add(String[] args) {
        Console console = requireConsole();

        String filepath = args.length > 1 ? args[1] : console.readLine("Filepath: ");
        char[] masterPW = console.readPassword("Vault Master Password: ");
        char[] pw = console.readPassword("Password: ");
        try {
            String name = console.readLine("Entry name: ");
            String username = console.readLine("Username: ");
            String url = console.readLine("Url: ");

            // The current PasswordEntry model stores passwords as String. This is
            // still a memory-exposure limitation; the char[] is cleared here.
            String password = new String(pw);
            VaultHandler.addVaultEntry(filepath, name, username, password, url, masterPW);
        } finally {
            Arrays.fill(masterPW, '\0');
            Arrays.fill(pw, '\0');
        }
    }

    private static void open(String[] args) {
        if (args.length < 2) {
            log("Missing vault path for --open", 3);
            return;
        }
        char[] password = getPassword();
        try {
            Vault vault = VaultHandler.decryptVault(args[1], password);
            vault.printVault();
        } catch (IOException e) {
            log("General error reading vault", 4);
            System.out.println("Error reading vault. Possibly wrong password or corrupted vault.");
        } finally {
            Arrays.fill(password, '\0');
        }
    }

    private static void newVault(String[] args) {
        if (args.length < 2) {
            log("Missing vault path for --new", 3);
            return;
        }
        char[] password = getPassword();
        try {
            log("Creating new vault", 2);
            VaultHandler.createEncryptedVault(args[1], password);
        } catch (IOException e) {
            log("Failed to create vault", 4);
        } finally {
            Arrays.fill(password, '\0');
        }
    }

    private static char[] getPassword() {
        Console console = requireConsole();
        char[] password = console.readPassword("Master password: ");
        if (password == null) {
            throw new IllegalStateException("Could not read master password");
        }
        return password;
    }

    private static Console requireConsole() {
        Console console = System.console();
        if (console == null) {
            throw new IllegalStateException("Password entry requires a real console");
        }
        return console;
    }

    private static void gui(String[] args){

        SwingUtilities.invokeLater(() -> {

            try {
                ThemeManager.setDark();
            } catch (Exception e) {
                log("Could not set theme to dark", 3);
            }

            new UiManager();
            //uiManager.showLockScreen();

    });

    }

    public static void exitApplication(int code){
        System.exit(code);
    }


}
