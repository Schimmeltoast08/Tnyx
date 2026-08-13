package com.tnyx;

import com.tnyx.util.Log;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;
import java.time.Instant;

public class Main {

    @SuppressWarnings("unused")
    private static final int TNYX_MAIN_VERSION = 1;
    private static final int VAULT_FORMAT_VERSION = 1;
    private static final String KDF = "Argon2id"; // replace \w name later
    private static final String ENCRYPTION_ALGORITHM = "AES"; // same as KDF
    

    public static void main(String[] args) {
        Log.log("Starting application", 2);
        System.out.println(Instant.now().getEpochSecond());

        if (args.length > 0 && args[0].equals("--new")) {
            try {
                Log.log("Creating new Vault", 2);
                createOpenVault(args[1]);
            } catch (IOException e) {
                Log.log("Failed to create Vault", 4);
            }
        }

    }

    private static void createOpenVault(String filepath) throws IOException {
        try (FileOutputStream vaultOut = new FileOutputStream(filepath)) {

            SecureRandom random = new SecureRandom();
            //temporary
            byte[] salt = new byte[16];
            random.nextBytes(salt);
            byte[] nonce = new byte[12];
            random.nextBytes(nonce);
            byte[] dek = new byte[32];
            random.nextBytes(dek);
            byte[] nonce2 = new byte[12];
            random.nextBytes(nonce2); 

            //   Write Block   \\
            vaultOut.write("[Format]".getBytes(StandardCharsets.UTF_8));
            vaultOut.write(VAULT_FORMAT_VERSION);
            
            vaultOut.write("[KDF]".getBytes(StandardCharsets.UTF_8));
            vaultOut.write(KDF.getBytes(StandardCharsets.UTF_8).length);
            vaultOut.write(KDF.getBytes(StandardCharsets.UTF_8));
            vaultOut.write(salt.length);
            vaultOut.write(salt);
            //vaultOut.write(parameters);
            
            vaultOut.write("[Encryption]".getBytes(StandardCharsets.UTF_8));
            vaultOut.write(ENCRYPTION_ALGORITHM.getBytes(StandardCharsets.UTF_8).length);
            vaultOut.write(ENCRYPTION_ALGORITHM.getBytes(StandardCharsets.UTF_8));
            vaultOut.write(nonce.length);
            vaultOut.write(nonce);
            // DEK u KEK, more research
            //vaultOut.write(Data);
            
            vaultOut.write("[Time]".getBytes(StandardCharsets.UTF_8));
            //vaultOut.write(Instant.now()); // creation date
            vaultOut.write((byte) Instant.now().getEpochSecond()); // last modified. both equal during creation

            vaultOut.write("[Data]".getBytes(StandardCharsets.UTF_8));
            vaultOut.write(nonce2.length);
            vaultOut.write(nonce2);


            

        }

    }

}
