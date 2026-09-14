package com.tnyx.crypto;

public final class CryptoConstants { // just to keep everything in one place to avoid a mismatch in code

    public static final int SALT_LENGTH = 16;
    public static final int DEK_LENGTH = 32;
    public static final int NONCE_LENGTH = 12;
    public static final int GCM_TAG_LENGTH = 128;

    public static final int ARGON2_MEMORY_KIB = 65536; //64MiB
    public static final int ARGON2_ITERATIONS = 3;
    public static final int ARGON2_PARALLELISM = 1;

    private CryptoConstants() {
    }
}
