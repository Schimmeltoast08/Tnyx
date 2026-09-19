package com.tnyx.crypto;

import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;
import java.util.Arrays;

import org.bouncycastle.crypto.generators.Argon2BytesGenerator;
import org.bouncycastle.crypto.params.Argon2Parameters;

public final class KeyDerivation {
    private static final SecureRandom RANDOM = new SecureRandom();

    private KeyDerivation() {}

    public static byte[] generateSalt() {
        byte[] salt = new byte[CryptoConstants.SALT_LENGTH];
        RANDOM.nextBytes(salt);
        return salt;
    }

    public static byte[] deriveKEK(
            char[] password,
            byte[] salt,
            int memoryKib,
            int iterations,
            int parallelism,
            int outputLength) {

        if (password == null || password.length == 0) {
            throw new IllegalArgumentException("Password must not be empty");
        }
        if (salt == null || salt.length != CryptoConstants.SALT_LENGTH) {
            throw new IllegalArgumentException("Invalid salt length");
        }
        if (memoryKib != CryptoConstants.ARGON2_MEMORY_KIB
                || iterations != CryptoConstants.ARGON2_ITERATIONS
                || parallelism != CryptoConstants.ARGON2_PARALLELISM
                || outputLength != CryptoConstants.DEK_LENGTH) {
            throw new IllegalArgumentException("Unsupported Argon2 parameters");
        }

        ByteBuffer encoded = StandardCharsets.UTF_8.encode(CharBuffer.wrap(password));
        byte[] passwordBytes = new byte[encoded.remaining()];
        encoded.get(passwordBytes);

        try {
            Argon2Parameters parameters = new Argon2Parameters.Builder(Argon2Parameters.ARGON2_id)
                    .withSalt(Arrays.copyOf(salt, salt.length))
                    .withMemoryAsKB(memoryKib)
                    .withIterations(iterations)
                    .withParallelism(parallelism)
                    .build();

            Argon2BytesGenerator generator = new Argon2BytesGenerator();
            generator.init(parameters);

            byte[] kek = new byte[outputLength];
            generator.generateBytes(passwordBytes, kek);
            return kek;
        } finally {
            Arrays.fill(passwordBytes, (byte) 0);
            if (encoded.hasArray()) {
                java.util.Arrays.fill(
                        encoded.array(), encoded.arrayOffset(),
                        encoded.arrayOffset() + encoded.capacity(), (byte) 0);
            }
        }
    }
}
