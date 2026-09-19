package com.tnyx.crypto;

import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.charset.CharacterCodingException;
import java.nio.charset.CodingErrorAction;
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

        byte[] passwordBytes = encodePassword(password);
        try {
            Argon2Parameters parameters = new Argon2Parameters.Builder(Argon2Parameters.ARGON2_id)
                    .withSalt(salt.clone())
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
        }
    }

    private static byte[] encodePassword(char[] password) {
        try {
            var encoder = StandardCharsets.UTF_8.newEncoder()
                    .onMalformedInput(CodingErrorAction.REPORT)
                    .onUnmappableCharacter(CodingErrorAction.REPORT);
            ByteBuffer encoded = encoder.encode(CharBuffer.wrap(password));
            if (encoded.remaining() > CryptoConstants.MAX_MASTER_PASSWORD_UTF8_BYTES) {
                throw new IllegalArgumentException("Master password is too large");
            }
            byte[] bytes = new byte[encoded.remaining()];
            encoded.get(bytes);
            return bytes;
        } catch (CharacterCodingException e) {
            throw new IllegalArgumentException("Master password contains invalid UTF-16", e);
        }
    }
}
