package com.tnyx.crypto;

public final class CryptoConstants {
    public static final int SALT_LENGTH = 16;
    public static final int DEK_LENGTH = 32;
    public static final int NONCE_LENGTH = 12;
    public static final int GCM_TAG_LENGTH = 128;
    public static final int GCM_TAG_BYTES = GCM_TAG_LENGTH / 8;
    public static final int ENCRYPTED_DEK_LENGTH = DEK_LENGTH + GCM_TAG_BYTES;

    // Format v3 uses one fixed, reviewed Argon2id profile. Do not accept
    // attacker-selected work factors; that would make opening a vault a DoS primitive.
    public static final int ARGON2_MEMORY_KIB = 65536;
    public static final int ARGON2_ITERATIONS = 3;
    public static final int ARGON2_PARALLELISM = 1;

    public static final int MAX_VAULT_FILE_SIZE = 64 * 1024 * 1024;
    public static final int MAX_ENTRIES = 10_000;
    public static final int MAX_NAME_BYTES = 16 * 1024;
    public static final int MAX_USERNAME_BYTES = 16 * 1024;
    public static final int MAX_PASSWORD_BYTES = 64 * 1024;
    public static final int MAX_URL_BYTES = 16 * 1024;
    public static final int MAX_ENTRY_BYTES =
            16 + 4 * 4 + MAX_NAME_BYTES + MAX_USERNAME_BYTES + MAX_PASSWORD_BYTES + MAX_URL_BYTES;

    private CryptoConstants() {}
}
