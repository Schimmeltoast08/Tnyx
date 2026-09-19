package com.tnyx.crypto;

import javax.crypto.SecretKey;
import java.security.GeneralSecurityException;
import java.util.Arrays;

/** Owns all live vault key material. No raw key is exposed to callers. */
public final class VaultSession implements AutoCloseable {
    private final DestroyableSecretKey kek;
    private final DestroyableSecretKey dek;
    private boolean closed;

    public VaultSession(SecretKey kek, SecretKey dek) {
        this.kek = copyKey(kek, "KEK");
        this.dek = copyKey(dek, "DEK");
    }

    public synchronized boolean isClosed() {
        return closed;
    }

    public synchronized byte[] decryptData(byte[] ciphertext, byte[] nonce)
            throws GeneralSecurityException {
        ensureOpen();
        return Encryption.decryptGcm(ciphertext, dek, nonce);
    }

    public synchronized byte[] encryptData(byte[] plaintext, byte[] nonce)
            throws GeneralSecurityException {
        ensureOpen();
        return Encryption.encryptGcm(plaintext, dek, nonce);
    }

    public synchronized byte[] rewrapDek(byte[] nonce) throws GeneralSecurityException {
        ensureOpen();
        return CryptoEngine.encryptDek(dek, kek, nonce);
    }

    public synchronized void close() {
        if (closed) return;
        try { dek.destroy(); } catch (Exception ignored) { }
        try { kek.destroy(); } catch (Exception ignored) { }
        closed = true;
    }

    private void ensureOpen() {
        if (closed) throw new IllegalStateException("Vault session is closed");
    }

    private static DestroyableSecretKey copyKey(SecretKey key, String name) {
        if (key == null || !"AES".equalsIgnoreCase(key.getAlgorithm())) {
            throw new IllegalArgumentException("Invalid " + name);
        }
        byte[] encoded = key.getEncoded();
        try {
            if (encoded == null || encoded.length != CryptoConstants.DEK_LENGTH) {
                throw new IllegalArgumentException("Invalid " + name + " length");
            }
            return new DestroyableSecretKey(encoded, "AES");
        } finally {
            if (encoded != null) Arrays.fill(encoded, (byte) 0);
        }
    }
}
