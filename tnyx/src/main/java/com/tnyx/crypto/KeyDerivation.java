package com.tnyx.crypto;

import java.security.SecureRandom;
import java.util.Arrays;

import org.bouncycastle.crypto.generators.Argon2BytesGenerator;
import org.bouncycastle.crypto.params.Argon2Parameters;

import com.tnyx.util.Log;

public class KeyDerivation {

    public static byte[] generateSalt() {
        byte[] salt = new byte[CryptoConstants.SALT_LENGTH];
        SecureRandom random = new SecureRandom();
        random.nextBytes(salt);
        return salt;

    }

    //TODO: Step 6 in developement plan // plan is NOT public, sorry :(


    public static byte[] deriveKEK(char[] password, byte[] salt) {
        Log.log("Deriving KEK", 1);
        //byte[] passwordBytes = new String(password).getBytes(StandardCharsets.UTF_8);

        byte[] passwordBytes = new byte[password.length];
        for (int i = 0; i < password.length; i++){
            passwordBytes[i] = (byte) password[i]; // avoid strings to stop creation of immutable objects
                   }
        Argon2Parameters parameters = new Argon2Parameters.Builder(Argon2Parameters.ARGON2_id)
                .withSalt(salt)
                .withMemoryAsKB(CryptoConstants.ARGON2_MEMORY_KIB)
                .withIterations(CryptoConstants.ARGON2_ITERATIONS)
                .withParallelism(CryptoConstants.ARGON2_PARALLELISM)
                .build();

        Argon2BytesGenerator generator = new Argon2BytesGenerator();
        generator.init(parameters);

        byte[] kek = new byte[CryptoConstants.DEK_LENGTH];
        generator.generateBytes(passwordBytes, kek);

        Arrays.fill(passwordBytes, (byte) 0);
        Arrays.fill(password, '\0'); // wipe memmory

        Log.log("KEK Derived: OK", 1);
        return kek;
    }

}
