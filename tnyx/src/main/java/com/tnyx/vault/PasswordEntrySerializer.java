package com.tnyx.vault;

import com.tnyx.crypto.CryptoConstants;

import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.UUID;

public final class PasswordEntrySerializer {
    private static final int HEADER_LENGTH = 16 + 4 * 4;

    private PasswordEntrySerializer() {}

    public static byte[] serializePasswordEntry(PasswordEntry entry) {
        if (entry == null || entry.getId() == null) {
            throw new IllegalArgumentException("Invalid password entry");
        }

        byte[] name = utf8(entry.getName(), CryptoConstants.MAX_NAME_BYTES, "name");
        byte[] username = utf8(entry.getUsername(), CryptoConstants.MAX_USERNAME_BYTES, "username");
        byte[] password = utf8(entry.getPassword(), CryptoConstants.MAX_PASSWORD_BYTES, "password");
        byte[] url = utf8(entry.getUrl(), CryptoConstants.MAX_URL_BYTES, "url");

        int total = HEADER_LENGTH + name.length + username.length + password.length + url.length;
        if (total > CryptoConstants.MAX_ENTRY_BYTES) {
            throw new IllegalArgumentException("Password entry is too large");
        }

        ByteBuffer buffer = ByteBuffer.allocate(total);
        UUID id = entry.getId();
        buffer.putLong(id.getMostSignificantBits());
        buffer.putLong(id.getLeastSignificantBits());
        buffer.putInt(name.length);
        buffer.putInt(username.length);
        buffer.putInt(password.length);
        buffer.putInt(url.length);
        buffer.put(name).put(username).put(password).put(url);
        return buffer.array();
    }

    public static PasswordEntry deserializePasswordEntry(byte[] entryBuffer) {
        if (entryBuffer == null
                || entryBuffer.length < HEADER_LENGTH
                || entryBuffer.length > CryptoConstants.MAX_ENTRY_BYTES) {
            throw new IllegalArgumentException("Invalid password entry length");
        }

        ByteBuffer buffer = ByteBuffer.wrap(entryBuffer);
        UUID id = new UUID(buffer.getLong(), buffer.getLong());
        if (id == null) {
            throw new IllegalArgumentException("Invalid entry UUID");
        }

        int nameLength = readLength(buffer, CryptoConstants.MAX_NAME_BYTES, "name");
        int usernameLength = readLength(buffer, CryptoConstants.MAX_USERNAME_BYTES, "username");
        int passwordLength = readLength(buffer, CryptoConstants.MAX_PASSWORD_BYTES, "password");
        int urlLength = readLength(buffer, CryptoConstants.MAX_URL_BYTES, "url");

        int total = nameLength + usernameLength + passwordLength + urlLength;
        if (total != buffer.remaining()) {
            throw new IllegalArgumentException("Password entry length fields do not match payload");
        }

        String name = readUtf8(buffer, nameLength, "name");
        String username = readUtf8(buffer, usernameLength, "username");
        String password = readUtf8(buffer, passwordLength, "password");
        String url = readUtf8(buffer, urlLength, "url");

        PasswordEntry entry = new PasswordEntry(id);
        entry.setName(name);
        entry.setUsername(username);
        entry.setPassword(password);
        entry.setUrl(url);
        return entry;
    }

    private static int readLength(ByteBuffer b, int max, String field) {
        if (b.remaining() < 4) throw new IllegalArgumentException("Missing " + field + " length");
        int length = b.getInt();
        if (length < 0 || length > max || length > b.remaining()) {
            throw new IllegalArgumentException("Invalid " + field + " length");
        }
        return length;
    }

    private static String readUtf8(ByteBuffer b, int length, String field) {
        byte[] bytes = new byte[length];
        b.get(bytes);
        return new String(bytes, StandardCharsets.UTF_8);
    }

    private static byte[] utf8(String value, int max, String field) {
        if (value == null) throw new IllegalArgumentException(field + " must not be null");
        byte[] bytes = value.getBytes(StandardCharsets.UTF_8);
        if (bytes.length > max) throw new IllegalArgumentException(field + " is too long");
        return bytes;
    }
}
