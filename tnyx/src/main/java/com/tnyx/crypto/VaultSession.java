package com.tnyx.crypto;

import javax.crypto.SecretKey;

public class VaultSession {

    private final SecretKey dek;

    public VaultSession(SecretKey dek) {
        this.dek = dek;
    }

    public SecretKey getDek() {
        return dek;
    }
}