package com.tnyx.vault;

import com.tnyx.crypto.CryptoConstants;

import java.util.Arrays;
import java.util.UUID;

public final class PasswordEntry implements AutoCloseable {
    private String name = "";
    private String username = "";
    private char[] password = new char[0];
    private String url = "";
    private final UUID id;

    public PasswordEntry() {
        this.id = UUID.randomUUID();
    }

    public PasswordEntry(UUID id) {
        if (id == null) throw new IllegalArgumentException("id must not be null");
        this.id = id;
    }

    public String getUrl() { return url; }
    public void setUrl(String url) { this.url = requireText(url, "url"); }
    public char[] getPassword() { return password.clone(); }
    public void setPassword(char[] password) {
        if (password == null) throw new IllegalArgumentException("password must not be null");
        try {
            var encoder = java.nio.charset.StandardCharsets.UTF_8.newEncoder();
            var encoded = encoder.encode(java.nio.CharBuffer.wrap(password));
            if (encoded.remaining() > CryptoConstants.MAX_PASSWORD_BYTES) {
                throw new IllegalArgumentException("password is too long");
            }
        } catch (java.nio.charset.CharacterCodingException e) {
            throw new IllegalArgumentException("password contains invalid UTF-16", e);
        }
        Arrays.fill(this.password, '\0');
        this.password = password.clone();
    }
    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = requireText(username, "username"); }
    public String getName() { return name; }
    public void setName(String name) { this.name = requireText(name, "name"); }
    public UUID getId() { return id; }

    public void editEntry(String name, String username, String url, char[] password) {
        if (!name.isEmpty()) setName(name);
        if (!username.isEmpty()) setUsername(username);
        if (!url.isEmpty()) setUrl(url);
        if (password.length != 0) setPassword(password);
    }

    public int passwordLength() { return password.length; }

    @Override
    public void close() {
        Arrays.fill(password, '\0');
        password = new char[0];
    }

    private static String requireText(String value, String field) {
        if (value == null) throw new IllegalArgumentException(field + " must not be null");
        return value;
    }


}
