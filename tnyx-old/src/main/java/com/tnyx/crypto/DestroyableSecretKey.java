package com.tnyx.crypto;

import javax.crypto.SecretKey;
import java.util.Arrays;
import javax.security.auth.DestroyFailedException;

/** A small in-process AES key holder whose backing bytes can be explicitly destroyed. */
public final class DestroyableSecretKey implements SecretKey {
    private final String algorithm;
    private byte[] keyBytes;
    private boolean destroyed;

    public DestroyableSecretKey(byte[] keyBytes, String algorithm) {
        if (keyBytes == null || keyBytes.length != CryptoConstants.DEK_LENGTH) {
            throw new IllegalArgumentException("AES-256 key required");
        }
        if (algorithm == null || algorithm.isBlank()) {
            throw new IllegalArgumentException("Key algorithm must not be blank");
        }
        this.algorithm = algorithm;
        this.keyBytes = keyBytes.clone();
    }

    @Override
    public synchronized String getAlgorithm() {
        return algorithm;
    }

    @Override
    public synchronized String getFormat() {
        return "RAW";
    }

    @Override
    public synchronized byte[] getEncoded() {
        ensureUsable();
        return keyBytes.clone();
    }

    public synchronized boolean isDestroyed() {
        return destroyed;
    }

    public synchronized void destroy() throws DestroyFailedException {
        if (!destroyed) {
            Arrays.fill(keyBytes, (byte) 0);
            destroyed = true;
        }
    }

    public synchronized void close() {
        try { destroy(); } catch (DestroyFailedException ignored) { }
    }

    private void ensureUsable() {
        if (destroyed) {
            throw new IllegalStateException("Key has been destroyed");
        }
    }
}
