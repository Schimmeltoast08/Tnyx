package com.tnyx;

import com.tnyx.util.Log;
import com.tnyx.vault.Vault;
import com.tnyx.vault.VaultHandler;
import java.io.IOException;



public class Main {

    @SuppressWarnings("unused")
    private static final int TNYX_MAIN_VERSION = 1;
    private static final int VAULT_FORMAT_VERSION = 1;
    private static final String KDF = "Argon2id"; // replace \w name later
    private static final String ENCRYPTION_ALGORITHM = "AES"; // same as KDF
    

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
                
            } catch (IOException e) {
                Log.log("General Error at reading vault", 4);
            }
        }

    }



}
