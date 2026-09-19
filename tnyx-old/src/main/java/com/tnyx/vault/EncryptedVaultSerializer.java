package com.tnyx.vault;

import com.tnyx.crypto.CryptoConstants;
import com.tnyx.crypto.EncryptedVault;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;

public final class EncryptedVaultSerializer {
    private static final int FORMAT_VERSION = 3;
    private static final byte[] KDF = "Argon2id".getBytes(StandardCharsets.UTF_8);
    private static final byte[] ALGORITHM = "AES-256-GCM".getBytes(StandardCharsets.UTF_8);

    private EncryptedVaultSerializer() {}

    public static byte[] serializeEncryptedVault(EncryptedVault v) throws IOException {
        validate(v);

        ByteArrayOutputStream out = new ByteArrayOutputStream();
        writeMarker(out, "[Format]");
        writeInt(out, FORMAT_VERSION);

        writeMarker(out, "[KDF]");
        writeBytes(out, KDF);
        writeBytes(out, v.getSalt());
        writeInt(out, v.getArgon2MemoryKib());
        writeInt(out, v.getArgon2Iterations());
        writeInt(out, v.getArgon2Parallelism());
        writeInt(out, v.getArgon2OutputLength());

        writeMarker(out, "[Encryption]");
        writeBytes(out, ALGORITHM);
        writeBytes(out, v.getDekNonce());
        writeBytes(out, v.getEncryptedDek());

        writeMarker(out, "[Data]");
        writeBytes(out, v.getDataNonce());
        writeBytes(out, v.getEncryptedData());

        byte[] result = out.toByteArray();
        if (result.length > CryptoConstants.MAX_VAULT_FILE_SIZE + 4096) {
            throw new IOException("Encrypted vault is too large");
        }
        return result;
    }

    public static EncryptedVault deserializeEncryptedVault(byte[] data) {
        if (data == null || data.length > CryptoConstants.MAX_VAULT_FILE_SIZE + 4096) {
            throw new IllegalArgumentException("Encrypted vault is missing or too large");
        }

        ByteBuffer buffer = ByteBuffer.wrap(data);
        readMarker(buffer, "[Format]");
        int version = readInt(buffer, "format version");
        if (version != FORMAT_VERSION) {
            throw new IllegalArgumentException("Unsupported encrypted vault format version");
        }

        readMarker(buffer, "[KDF]");
        byte[] kdf = readBytes(buffer, "KDF", KDF.length, KDF.length);
        if (!Arrays.equals(kdf, KDF)) {
            throw new IllegalArgumentException("Unsupported KDF");
        }

        byte[] salt = readBytes(buffer, "salt",
                CryptoConstants.SALT_LENGTH, CryptoConstants.SALT_LENGTH);

        int memory = readInt(buffer, "Argon2 memory");
        int iterations = readInt(buffer, "Argon2 iterations");
        int parallelism = readInt(buffer, "Argon2 parallelism");
        int outputLength = readInt(buffer, "Argon2 output length");

        if (memory != CryptoConstants.ARGON2_MEMORY_KIB
                || iterations != CryptoConstants.ARGON2_ITERATIONS
                || parallelism != CryptoConstants.ARGON2_PARALLELISM
                || outputLength != CryptoConstants.DEK_LENGTH) {
            throw new IllegalArgumentException("Unsupported Argon2 parameters");
        }

        readMarker(buffer, "[Encryption]");
        byte[] algorithm = readBytes(buffer, "encryption algorithm",
                ALGORITHM.length, ALGORITHM.length);
        if (!Arrays.equals(algorithm, ALGORITHM)) {
            throw new IllegalArgumentException("Unsupported encryption algorithm");
        }

        byte[] dekNonce = readBytes(buffer, "DEK nonce",
                CryptoConstants.NONCE_LENGTH, CryptoConstants.NONCE_LENGTH);
        byte[] encryptedDek = readBytes(buffer, "encrypted DEK",
                CryptoConstants.ENCRYPTED_DEK_LENGTH, CryptoConstants.ENCRYPTED_DEK_LENGTH);

        readMarker(buffer, "[Data]");
        byte[] dataNonce = readBytes(buffer, "data nonce",
                CryptoConstants.NONCE_LENGTH, CryptoConstants.NONCE_LENGTH);
        byte[] encryptedData = readBytes(buffer, "encrypted data",
                CryptoConstants.GCM_TAG_BYTES,
                CryptoConstants.MAX_VAULT_FILE_SIZE + CryptoConstants.GCM_TAG_BYTES);

        if (buffer.hasRemaining()) {
            throw new IllegalArgumentException("Trailing data in encrypted vault");
        }

        return new EncryptedVault(
                salt, memory, iterations, parallelism, outputLength,
                dekNonce, encryptedDek, dataNonce, encryptedData);
    }

    private static void validate(EncryptedVault v) throws IOException {
        if (v == null
                || v.getSalt().length != CryptoConstants.SALT_LENGTH
                || v.getDekNonce().length != CryptoConstants.NONCE_LENGTH
                || v.getEncryptedDek().length != CryptoConstants.ENCRYPTED_DEK_LENGTH
                || v.getDataNonce().length != CryptoConstants.NONCE_LENGTH
                || v.getEncryptedData().length < CryptoConstants.GCM_TAG_BYTES
                || v.getEncryptedData().length > CryptoConstants.MAX_VAULT_PLAINTEXT_SIZE + CryptoConstants.GCM_TAG_BYTES
                || v.getArgon2MemoryKib() != CryptoConstants.ARGON2_MEMORY_KIB
                || v.getArgon2Iterations() != CryptoConstants.ARGON2_ITERATIONS
                || v.getArgon2Parallelism() != CryptoConstants.ARGON2_PARALLELISM
                || v.getArgon2OutputLength() != CryptoConstants.DEK_LENGTH) {
            throw new IOException("Invalid encrypted vault");
        }
    }

    private static void writeMarker(ByteArrayOutputStream out, String marker) {
        out.writeBytes(marker.getBytes(StandardCharsets.UTF_8));
    }

    private static void writeInt(ByteArrayOutputStream out, int value) {
        out.writeBytes(ByteBuffer.allocate(4).putInt(value).array());
    }

    private static void writeBytes(ByteArrayOutputStream out, byte[] value) {
        writeInt(out, value.length);
        out.writeBytes(value);
    }

    private static int readInt(ByteBuffer b, String field) {
        require(b, 4, field);
        return b.getInt();
    }

    private static byte[] readBytes(ByteBuffer b, String field, int min, int max) {
        int length = readInt(b, field + " length");
        if (length < min || length > max) {
            throw new IllegalArgumentException("Invalid " + field + " length");
        }
        require(b, length, field);
        byte[] result = new byte[length];
        b.get(result);
        return result;
    }

    private static void require(ByteBuffer b, int count, String field) {
        if (count < 0 || count > b.remaining()) {
            throw new IllegalArgumentException("Truncated encrypted vault: " + field);
        }
    }

    private static void readMarker(ByteBuffer b, String expected) {
        byte[] expectedBytes = expected.getBytes(StandardCharsets.UTF_8);
        require(b, expectedBytes.length, expected);
        byte[] actual = new byte[expectedBytes.length];
        b.get(actual);
        if (!Arrays.equals(actual, expectedBytes)) {
            throw new IllegalArgumentException("Invalid encrypted vault marker");
        }
    }
}
