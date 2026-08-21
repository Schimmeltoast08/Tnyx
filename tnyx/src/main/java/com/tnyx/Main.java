package com.tnyx;

import java.io.IOException;

import com.tnyx.util.Log;
import com.tnyx.vault.Vault;
import com.tnyx.vault.VaultHandler;



public class Main {

    @SuppressWarnings("unused")
    private static final int TNYX_MAIN_VERSION = 1;
    

    public static void main(String[] args) {
        Log.log("Starting application", 2);
        

        if (args.length > 0 && args[0].equals("--new")) {
            try {
                Log.log("Creating new Vault", 2);
                VaultHandler.createOpenVault(args[1]);
            } catch (IOException e) {
                Log.log("Failed to create Vault", 4);
            }
        }

        if (args.length > 0 && args[0].equals("--open")){
            try {
                Vault vault = VaultHandler.readVault(args[1]);
                vault.printVault(); // temporary for developement
                
            } catch (IOException e) {
                Log.log("General Error at reading vault", 4);
            }
        }

        if (args.length > 0 && args[0].equals("--add")){
            String filepath = "";
            String name = "";
            String username = "";
            String password = "";
            String url = "";

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



            VaultHandler.addVaultEntry(filepath, name, username, password, url);
        }

        if (args.length > 0 && args[0].equals("--remove")){
            try{
                VaultHandler.removeEntry(args[1]);
            } catch (IOException e){}
        }

        if (args.length > 0 && args[0].equals("--edit")){
            try{
            VaultHandler.editEntry(args[1]);
            } catch (Exception e){}
        }


        if (args.length > 0 && args[0].equals("--encrypt")){
            try{
            VaultHandler.encryptVault(args[1], new char[2]);
            } catch (Exception e){}
        }




    }



}
