package com.tnyx;

import java.io.Console;
import java.io.IOException;
import java.util.Arrays;

import com.tnyx.util.Log;
import com.tnyx.vault.Vault;
import com.tnyx.vault.VaultHandler;



public class Main {

    @SuppressWarnings("unused")
    private static final int TNYX_MAIN_VERSION = 1;
    

    public static void main(String[] args) {
        Log.log("[x][x][x] Starting application", 2);
        

        if (args.length > 0 && args[0].equals("--new")) {
            try {
                Log.log("Creating new Vault", 2);
                char[] password = getPassword();
                VaultHandler.createEncryptedVault(args[1], password);
                Arrays.fill(password, '\n');
            } catch (IOException e) {
                Log.log("Failed to create Vault", 4);
            }
        }

        if (args.length > 0 && args[0].equals("--open")){
            try {
                char[] password = getPassword();
                Vault vault = VaultHandler.decryptVault(args[1], password);
                vault.printVault(); // temporary for development
                Arrays.fill(password, '\n');
            } catch (IOException e) {
                Log.log("General Error at reading vault", 4);
            }
        }

        if (args.length > 0 && args[0].equals("--add")){
            //String filepath = "";
            //String name = "";
            //String username = "";
            //String password = "";
            //String url = "";

            Console console = System.console();

            String filepath = console.readLine("Filepath: ");
            char[] masterPW = console.readPassword("Vault Master Password: ");
            String name = console.readLine("Entry name: ");
            String username = console.readLine("Username: ");
            char[] pw = console.readPassword("Password: ");
            String password = new String(pw); // IMMUTABLE! FIX
            String url = console.readLine("Url: ");

            /*
            try {
                filepath = args[1];
            } catch (Exception e) {}
            try {
                name = args[2];
            } catch (Exception e) {}
            try {
                username = args[3];
            } catch (Exception e) {}
            try {
                password = args[4];
            } catch (Exception e) {} // Seperate try so when one fails, the others still go through
            try {
                url = args[5];  // now url is optional, so are they all from right to left as seen down below at addVaultEntry
            } catch (Exception e) {}

             */


            VaultHandler.addVaultEntry(filepath, name, username, password, url, masterPW);
        }

        if (args.length > 0 && args[0].equals("--remove")){
            try{
                char[] password = getPassword();
                VaultHandler.removeEntry(args[1], password);
                Arrays.fill(password, '\n');
            } catch (IOException e){Log.log("Could not remove Password entry", 3);}
        }

        if (args.length > 0 && args[0].equals("--edit")){
            try{
            VaultHandler.editEntry(args[1], getPassword());
            } catch (Exception e){}
        }


    }

    private static char[] getPassword() {
        Console console = System.console();

        if (console == null){
            Log.log("Could not acquire console for password entry", 4);
        }

        char[] password = console.readPassword("Master password: ");
        assert password != null; // just in case
        return password;
    }


}
