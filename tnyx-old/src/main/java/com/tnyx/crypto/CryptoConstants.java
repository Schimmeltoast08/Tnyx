package com.tnyx.crypto;

public final class CryptoConstants {
    public static final int SALT_LENGTH = 16;
    public static final int DEK_LENGTH = 32;
    public static final int NONCE_LENGTH = 12;
    public static final int GCM_TAG_LENGTH = 128;
    public static final int GCM_TAG_BYTES = GCM_TAG_LENGTH / 8;
    public static final int ENCRYPTED_DEK_LENGTH = DEK_LENGTH + GCM_TAG_BYTES;

    // Fixed, reviewed Argon2id profile. The file format never accepts attacker-selected work factors.
    public static final int ARGON2_MEMORY_KIB = 65536;
    public static final int ARGON2_ITERATIONS = 3;
    public static final int ARGON2_PARALLELISM = 1;

    public static final int MAX_VAULT_PLAINTEXT_SIZE = 64 * 1024 * 1024;
    public static final int MAX_VAULT_FILE_SIZE = MAX_VAULT_PLAINTEXT_SIZE + 4096;
    public static final int MAX_ENTRIES = 10_000;
    public static final int MAX_NAME_BYTES = 16 * 1024;
    public static final int MAX_USERNAME_BYTES = 16 * 1024;
    public static final int MAX_PASSWORD_BYTES = 64 * 1024;
    public static final int MAX_URL_BYTES = 16 * 1024;
    public static final int MAX_ENTRY_BYTES =
            16 + 4 * 4 + MAX_NAME_BYTES + MAX_USERNAME_BYTES + MAX_PASSWORD_BYTES + MAX_URL_BYTES;
    public static final int MAX_MASTER_PASSWORD_UTF8_BYTES = 1024 * 1024;

    private CryptoConstants() {}
}
