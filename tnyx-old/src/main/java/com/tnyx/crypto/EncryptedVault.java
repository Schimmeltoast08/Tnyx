package com.tnyx.crypto;

import java.util.Arrays;

public final class EncryptedVault {
    private final byte[] salt;
    private final int argon2MemoryKib;
    private final int argon2Iterations;
    private final int argon2Parallelism;
    private final int argon2OutputLength;
    private final byte[] dekNonce;
    private final byte[] encryptedDek;
    private final byte[] dataNonce;
    private final byte[] encryptedData;

    public EncryptedVault(
            byte[] salt,
            int argon2MemoryKib,
            int argon2Iterations,
            int argon2Parallelism,
            int argon2OutputLength,
            byte[] dekNonce,
            byte[] encryptedDek,
            byte[] dataNonce,
            byte[] encryptedData) {

        this.salt = copy(salt, "salt");
        this.dekNonce = copy(dekNonce, "DEK nonce");
        this.encryptedDek = copy(encryptedDek, "encrypted DEK");
        this.dataNonce = copy(dataNonce, "data nonce");
        this.encryptedData = copy(encryptedData, "encrypted data");
        this.argon2MemoryKib = argon2MemoryKib;
        this.argon2Iterations = argon2Iterations;
        this.argon2Parallelism = argon2Parallelism;
        this.argon2OutputLength = argon2OutputLength;
    }

    public byte[] getSalt() { return salt.clone(); }
    public int getArgon2MemoryKib() { return argon2MemoryKib; }
    public int getArgon2Iterations() { return argon2Iterations; }
    public int getArgon2Parallelism() { return argon2Parallelism; }
    public int getArgon2OutputLength() { return argon2OutputLength; }
    public byte[] getDekNonce() { return dekNonce.clone(); }
    public byte[] getEncryptedDek() { return encryptedDek.clone(); }
    public byte[] getDataNonce() { return dataNonce.clone(); }
    public byte[] getEncryptedData() { return encryptedData.clone(); }

    private static byte[] copy(byte[] value, String name) {
        if (value == null) {
            throw new IllegalArgumentException(name + " must not be null");
        }
        return Arrays.copyOf(value, value.length);
    }
}
