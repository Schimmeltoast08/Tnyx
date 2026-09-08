package com.tnyx.crypto;

import java.nio.charset.StandardCharsets;
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

    public static byte[] deriveKEK(char[] password, byte[] salt, int memoryKib, int iterations, int parallelism, int outputLength) {
        Log.log("Deriving KEK", 1);

        byte[] passwordBytes = new String(password).getBytes(StandardCharsets.UTF_8); //TODO fix away from immuteable string

        Argon2Parameters parameters = new Argon2Parameters.Builder(Argon2Parameters.ARGON2_id)
                .withSalt(salt)
                .withMemoryAsKB(memoryKib)
                .withIterations(iterations)
                .withParallelism(parallelism)
                .build();

        Argon2BytesGenerator generator = new Argon2BytesGenerator();
        generator.init(parameters);

        byte[] kek = new byte[outputLength];
        generator.generateBytes(passwordBytes, kek);

        Arrays.fill(passwordBytes, (byte) 0);
        Arrays.fill(password, '\0');

        Log.log("KEK Derived: OK", 1);

        return kek;
    }

    public static byte[] deriveKEK(char[] password, byte[] salt) {
        return deriveKEK(
                password,
                salt,
                CryptoConstants.ARGON2_MEMORY_KIB,
                CryptoConstants.ARGON2_ITERATIONS,
                CryptoConstants.ARGON2_PARALLELISM,
                CryptoConstants.DEK_LENGTH
        );
    }

}
