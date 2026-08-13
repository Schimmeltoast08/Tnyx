package com.tnyx.vault;

import com.tnyx.util.Log;
import java.io.DataInputStream;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;
import java.time.Instant;

public class VaultHandler {

    @SuppressWarnings("unused")
    private static final int TNYX_MAIN_VERSION = 1;
    private static final int VAULT_FORMAT_VERSION = 1;
    private static final String KDF = "Argon2id"; // replace \w name later
    private static final String ENCRYPTION_ALGORITHM = "AES"; // same as KDF

    public static void createOpenVault(String filepath) throws IOException {
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
            byte[] kdfBytes = KDF.getBytes(StandardCharsets.UTF_8);
            vaultOut.write(kdfBytes.length);
            vaultOut.write(kdfBytes);

            vaultOut.write(salt.length);
            vaultOut.write(salt);
            //vaultOut.write(parameters);

            vaultOut.write("[Encryption]".getBytes(StandardCharsets.UTF_8));
            byte[] encryptionAlgorithmBytes = ENCRYPTION_ALGORITHM.getBytes(StandardCharsets.UTF_8);
            vaultOut.write(encryptionAlgorithmBytes.length);
            vaultOut.write(encryptionAlgorithmBytes);

            vaultOut.write(nonce.length);
            vaultOut.write(nonce);
            // DEK u KEK, more research
            //vaultOut.write(Data);

            vaultOut.write("[Time]".getBytes(StandardCharsets.UTF_8));
            long timestamp = Instant.now().getEpochSecond();
            vaultOut.write(ByteBuffer.allocate(Long.BYTES).putLong(timestamp).array()); // creation date // 8 bytes
            vaultOut.write(ByteBuffer.allocate(Long.BYTES).putLong(timestamp).array()); // last modified // both equal during creation

            vaultOut.write("[Data]".getBytes(StandardCharsets.UTF_8));
            vaultOut.write(nonce2.length);
            vaultOut.write(nonce2);

        }

    }

    public static Vault readVault(String filepath) throws IOException {
        Vault vault = new Vault();
        DataInputStream dataIn = new DataInputStream(new FileInputStream(filepath));

        //
        byte[] formatMagic = new byte[8];
        dataIn.readFully(formatMagic);
        String formatHeader = new String(formatMagic, StandardCharsets.UTF_8);

        if (!formatHeader.equals("[Format]")) {
            Log.log("Format Error! missing [Format] header", 4);
            throw new IOException("Invalid vault file: missing [Format] header");
            
        }

        int formatVersion = dataIn.readUnsignedByte();
//

        byte[] kdfMagic = new byte[5];
        dataIn.readFully(kdfMagic);
        String kdfHeader = new String(kdfMagic, StandardCharsets.UTF_8);

        if(!kdfHeader.equals("[KDF]")){
            Log.log("Format Error! Missing [KDF] section header", 4);
            throw new IOException("Invalid vault file: missing [KDF] section header");
        }

        int kdfLength = dataIn.readUnsignedByte();
        byte[] kdfBytes = new byte[kdfLength];
        dataIn.readFully(kdfBytes);

        String KDF = new String(kdfBytes, StandardCharsets.UTF_8);

        System.out.println(kdfHeader + " " + formatHeader + " " + KDF);









        return vault;
    }

}
