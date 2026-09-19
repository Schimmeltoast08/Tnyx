package com.tnyx.crypto;

import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.util.Arrays;

public final class VaultSession implements AutoCloseable {
    private byte[] dekBytes;
    private boolean closed;

    public VaultSession(SecretKey dek) {
        if (dek == null || !"AES".equalsIgnoreCase(dek.getAlgorithm())) {
            throw new IllegalArgumentException("Invalid DEK");
        }
        byte[] encoded = dek.getEncoded();
        if (encoded == null || encoded.length != CryptoConstants.DEK_LENGTH) {
            if (encoded != null) Arrays.fill(encoded, (byte) 0);
            throw new IllegalArgumentException("Invalid DEK length");
        }
        this.dekBytes = encoded.clone();
        Arrays.fill(encoded, (byte) 0);
    }

    public synchronized SecretKey getDek() {
        if (closed) {
            throw new IllegalStateException("Vault session is closed");
        }
        return new SecretKeySpec(dekBytes.clone(), "AES");
    }

    public synchronized boolean isClosed() {
        return closed;
    }

    @Override
    public synchronized void close() {
        if (!closed) {
            Arrays.fill(dekBytes, (byte) 0);
            closed = true;
        }
    }
}
