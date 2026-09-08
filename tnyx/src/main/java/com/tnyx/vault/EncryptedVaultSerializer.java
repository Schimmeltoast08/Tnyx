package com.tnyx.vault;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;

import com.tnyx.crypto.EncryptedVault;
import com.tnyx.util.Log;

public class EncryptedVaultSerializer {

    public static byte[] serializeEncryptedVault(EncryptedVault encryptedVault) throws IOException {

        ByteArrayOutputStream out = new ByteArrayOutputStream();

        // [Format]
        out.write("[Format]".getBytes(StandardCharsets.UTF_8));

        ByteBuffer formatVersion = ByteBuffer.allocate(4);
        formatVersion.putInt(3);
        out.write(formatVersion.array());

        // [KDF]
        out.write("[KDF]".getBytes(StandardCharsets.UTF_8));

        byte[] kdfBytes = "Argon2id".getBytes(StandardCharsets.UTF_8);

        ByteBuffer kdfLength = ByteBuffer.allocate(4);
        kdfLength.putInt(kdfBytes.length);
        out.write(kdfLength.array());

        out.write(kdfBytes);

        ByteBuffer saltLength = ByteBuffer.allocate(4);
        saltLength.putInt(encryptedVault.getSalt().length);
        out.write(saltLength.array());

        out.write(encryptedVault.getSalt());

        ByteBuffer memory = ByteBuffer.allocate(4);
        memory.putInt(encryptedVault.getArgon2MemoryKib());
        out.write(memory.array());

        ByteBuffer iterations = ByteBuffer.allocate(4);
        iterations.putInt(encryptedVault.getArgon2Iterations());
        out.write(iterations.array());

        ByteBuffer parallelism = ByteBuffer.allocate(4);
        parallelism.putInt(encryptedVault.getArgon2Parallelism());
        out.write(parallelism.array());

        ByteBuffer outputLength = ByteBuffer.allocate(4);
        outputLength.putInt(encryptedVault.getArgon2OutputLength());
        out.write(outputLength.array());

        // [Encryption]
        out.write("[Encryption]".getBytes(StandardCharsets.UTF_8));

        byte[] encryptionAlgorithmBytes = "AES-256-GCM".getBytes(StandardCharsets.UTF_8);

        ByteBuffer encryptionAlgorithmLength = ByteBuffer.allocate(4);
        encryptionAlgorithmLength.putInt(encryptionAlgorithmBytes.length);
        out.write(encryptionAlgorithmLength.array());

        out.write(encryptionAlgorithmBytes);

        ByteBuffer dekNonceLength = ByteBuffer.allocate(4);
        dekNonceLength.putInt(encryptedVault.getDekNonce().length);
        out.write(dekNonceLength.array());

        out.write(encryptedVault.getDekNonce());

        ByteBuffer encryptedDekLength = ByteBuffer.allocate(4);
        encryptedDekLength.putInt(encryptedVault.getEncryptedDek().length);
        out.write(encryptedDekLength.array());

        out.write(encryptedVault.getEncryptedDek());

        // [Data]
        out.write("[Data]".getBytes(StandardCharsets.UTF_8));

        ByteBuffer dataNonceLength = ByteBuffer.allocate(4);
        dataNonceLength.putInt(encryptedVault.getDataNonce().length);
        out.write(dataNonceLength.array());

        out.write(encryptedVault.getDataNonce());

        ByteBuffer encryptedDataLength = ByteBuffer.allocate(4);
        encryptedDataLength.putInt(encryptedVault.getEncryptedData().length);
        out.write(encryptedDataLength.array());

        out.write(encryptedVault.getEncryptedData());

        return out.toByteArray();
    }

    public static EncryptedVault deserializeEncryptedVault(byte[] data) {

        ByteBuffer buffer = ByteBuffer.wrap(data);

        readMarker(buffer, "[Format]");

        requireBytes(buffer, 4, "format version");
        int formatVersion = buffer.getInt();

        if (formatVersion != 3) {
            String message = "Unsupported encrypted vault format version: " + formatVersion;
            Log.log(message, 4);
            throw new IllegalArgumentException(message);
        }

        readMarker(buffer, "[KDF]");

        int kdfLength = readLength(buffer, "KDF");
        requireBytes(buffer, kdfLength, "KDF");
        byte[] kdfBytes = new byte[kdfLength];
        buffer.get(kdfBytes);

        String kdf = new String(kdfBytes, StandardCharsets.UTF_8);

        if (!kdf.equals("Argon2id")) {
            String message = "Unsupported KDF: " + kdf;
            Log.log(message, 4);
            throw new IllegalArgumentException(message);
        }

        int saltLength = readLength(buffer, "salt");
        requireBytes(buffer, saltLength, "salt");
        byte[] salt = new byte[saltLength];
        buffer.get(salt);

        requireBytes(buffer, 16, "Argon2 parameters");

        int memoryKib = buffer.getInt();
        int iterations = buffer.getInt();
        int parallelism = buffer.getInt();
        int outputLength = buffer.getInt();

        readMarker(buffer, "[Encryption]");

        int encryptionAlgorithmLength = readLength(buffer, "encryption algorithm");
        requireBytes(buffer, encryptionAlgorithmLength, "encryption algorithm");

        byte[] encryptionAlgorithmBytes = new byte[encryptionAlgorithmLength];
        buffer.get(encryptionAlgorithmBytes);

        String encryptionAlgorithm = new String(encryptionAlgorithmBytes, StandardCharsets.UTF_8);

        if (!encryptionAlgorithm.equals("AES-256-GCM")) {
            String message = "Unsupported encryption algorithm: " + encryptionAlgorithm;
            Log.log(message, 4);
            throw new IllegalArgumentException(message);
        }

        int dekNonceLength = readLength(buffer, "DEK nonce");
        requireBytes(buffer, dekNonceLength, "DEK nonce");

        byte[] dekNonce = new byte[dekNonceLength];
        buffer.get(dekNonce);

        int encryptedDekLength = readLength(buffer, "encrypted DEK");
        requireBytes(buffer, encryptedDekLength, "encrypted DEK");

        byte[] encryptedDek = new byte[encryptedDekLength];
        buffer.get(encryptedDek);

        readMarker(buffer, "[Data]");

        int dataNonceLength = readLength(buffer, "data nonce");
        requireBytes(buffer, dataNonceLength, "data nonce");

        byte[] dataNonce = new byte[dataNonceLength];
        buffer.get(dataNonce);

        int encryptedDataLength = readLength(buffer, "encrypted data");
        requireBytes(buffer, encryptedDataLength, "encrypted data");

        byte[] encryptedData = new byte[encryptedDataLength];
        buffer.get(encryptedData);

        if (buffer.hasRemaining()) {
            String message = "Corrupted encrypted vault: unexpected data after encrypted data";
            Log.log(message, 4);
            throw new IllegalArgumentException(message);
        }

        return new EncryptedVault(
                salt,
                memoryKib,
                iterations,
                parallelism,
                outputLength,
                dekNonce,
                encryptedDek,
                dataNonce,
                encryptedData
        );
    }

    private static int readLength(ByteBuffer buffer, String field) {

        requireBytes(buffer, 4, field + " length");

        int length = buffer.getInt();

        if (length < 0) {
            String message = "Corrupted encrypted vault: negative " + field + " length";
            Log.log(message, 4);
            throw new IllegalArgumentException(message);
        }

        return length;
    }

    private static void requireBytes(ByteBuffer buffer, int required, String field) {

        if (required < 0 || required > buffer.remaining()) {
            String message = "Corrupted encrypted vault: incomplete " + field;
            Log.log(message, 4);
            throw new IllegalArgumentException(message);
        }
    }

    private static void readMarker(ByteBuffer buffer, String expected) {

        byte[] expectedBytes = expected.getBytes(StandardCharsets.UTF_8);

        requireBytes(buffer, expectedBytes.length, expected + " marker");

        byte[] actual = new byte[expectedBytes.length];
        buffer.get(actual);

        if (!java.util.Arrays.equals(actual, expectedBytes)) {
            String message = "Corrupted encrypted vault: expected " + expected + " marker";
            Log.log(message, 4);
            throw new IllegalArgumentException(message);
        }
    }
}
