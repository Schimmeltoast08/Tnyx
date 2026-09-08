package com.tnyx.crypto;

public class EncryptedVault {

    private final byte[] salt;

    private final int argon2MemoryKib;
    private final int argon2Iterations;
    private final int argon2Parallelism;
    private final int argon2OutputLength;

    private final byte[] dekNonce;
    private final byte[] encryptedDek;

    private final byte[] dataNonce;
    private final byte[] encryptedData;

    public EncryptedVault(byte[] salt, int argon2MemoryKib, int argon2Iterations, int argon2Parallelism, int argon2OutputLength, byte[] dekNonce, byte[] encryptedDek, byte[] dataNonce, byte[] encryptedData) {
        this.salt = salt;
        this.argon2MemoryKib = argon2MemoryKib;
        this.argon2Iterations = argon2Iterations;
        this.argon2Parallelism = argon2Parallelism;
        this.argon2OutputLength = argon2OutputLength;
        this.dekNonce = dekNonce;
        this.encryptedDek = encryptedDek;
        this.dataNonce = dataNonce;
        this.encryptedData = encryptedData;
    }

    public byte[] getSalt() {
        return salt;
    }

    public int getArgon2MemoryKib() {
        return argon2MemoryKib;
    }

    public int getArgon2Iterations() {
        return argon2Iterations;
    }

    public int getArgon2Parallelism() {
        return argon2Parallelism;
    }

    public int getArgon2OutputLength() {
        return argon2OutputLength;
    }

    public byte[] getDekNonce() {
        return dekNonce;
    }

    public byte[] getEncryptedDek() {
        return encryptedDek;
    }

    public byte[] getDataNonce() {
        return dataNonce;
    }

    public byte[] getEncryptedData() {
        return encryptedData;
    }
}
