package com.tnyx.vault;

import com.tnyx.crypto.CryptoConstants;

import java.nio.ByteBuffer;
import java.nio.charset.CharacterCodingException;
import java.nio.charset.CodingErrorAction;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.UUID;

public final class PasswordEntrySerializer {
    private static final int HEADER_LENGTH = 16 + 4 * 4;
    private PasswordEntrySerializer() {}

    public static byte[] serializePasswordEntry(PasswordEntry entry) {
        if (entry == null || entry.getId() == null) throw new IllegalArgumentException("Invalid password entry");
        byte[] name = utf8(entry.getName(), CryptoConstants.MAX_NAME_BYTES, "name");
        byte[] username = utf8(entry.getUsername(), CryptoConstants.MAX_USERNAME_BYTES, "username");
        char[] passwordChars = entry.getPassword();
        byte[] password;
        try { password = utf8(passwordChars, CryptoConstants.MAX_PASSWORD_BYTES, "password"); }
        finally { Arrays.fill(passwordChars, '\0'); }
        byte[] url = utf8(entry.getUrl(), CryptoConstants.MAX_URL_BYTES, "url");
        int total = HEADER_LENGTH + name.length + username.length + password.length + url.length;
        if (total > CryptoConstants.MAX_ENTRY_BYTES) throw new IllegalArgumentException("Password entry is too large");
        ByteBuffer buffer = ByteBuffer.allocate(total);
        UUID id = entry.getId();
        buffer.putLong(id.getMostSignificantBits()).putLong(id.getLeastSignificantBits());
        buffer.putInt(name.length).putInt(username.length).putInt(password.length).putInt(url.length);
        buffer.put(name).put(username).put(password).put(url);
        Arrays.fill(password, (byte) 0);
        return buffer.array();
    }

    public static PasswordEntry deserializePasswordEntry(byte[] entryBuffer) {
        if (entryBuffer == null || entryBuffer.length < HEADER_LENGTH || entryBuffer.length > CryptoConstants.MAX_ENTRY_BYTES) {
            throw new IllegalArgumentException("Invalid password entry length");
        }
        ByteBuffer buffer = ByteBuffer.wrap(entryBuffer);
        UUID id = new UUID(buffer.getLong(), buffer.getLong());
        int nameLength = readLength(buffer, CryptoConstants.MAX_NAME_BYTES, "name");
        int usernameLength = readLength(buffer, CryptoConstants.MAX_USERNAME_BYTES, "username");
        int passwordLength = readLength(buffer, CryptoConstants.MAX_PASSWORD_BYTES, "password");
        int urlLength = readLength(buffer, CryptoConstants.MAX_URL_BYTES, "url");
        int total = nameLength + usernameLength + passwordLength + urlLength;
        if (total != buffer.remaining()) throw new IllegalArgumentException("Password entry length fields do not match payload");
        String name = readUtf8(buffer, nameLength, "name");
        String username = readUtf8(buffer, usernameLength, "username");
        char[] password = readUtf8Chars(buffer, passwordLength, "password");
        String url = readUtf8(buffer, urlLength, "url");
        PasswordEntry entry = new PasswordEntry(id);
        try {
            entry.setName(name); entry.setUsername(username); entry.setPassword(password); entry.setUrl(url);
            return entry;
        } finally { Arrays.fill(password, '\0'); }
    }

    private static int readLength(ByteBuffer b, int max, String field) {
        if (b.remaining() < 4) throw new IllegalArgumentException("Missing " + field + " length");
        int length = b.getInt();
        if (length < 0 || length > max || length > b.remaining()) throw new IllegalArgumentException("Invalid " + field + " length");
        return length;
    }

    private static String readUtf8(ByteBuffer b, int length, String field) {
        byte[] bytes = new byte[length]; b.get(bytes);
        try {
            var decoder = StandardCharsets.UTF_8.newDecoder().onMalformedInput(CodingErrorAction.REPORT).onUnmappableCharacter(CodingErrorAction.REPORT);
            return decoder.decode(ByteBuffer.wrap(bytes)).toString();
        } catch (CharacterCodingException e) { throw new IllegalArgumentException("Invalid UTF-8 in " + field, e); }
        finally { Arrays.fill(bytes, (byte) 0); }
    }

    private static char[] readUtf8Chars(ByteBuffer b, int length, String field) {
        byte[] bytes = new byte[length]; b.get(bytes);
        try {
            var decoder = StandardCharsets.UTF_8.newDecoder().onMalformedInput(CodingErrorAction.REPORT).onUnmappableCharacter(CodingErrorAction.REPORT);
            java.nio.CharBuffer decoded = decoder.decode(ByteBuffer.wrap(bytes));
            char[] chars = new char[decoded.remaining()];
            decoded.get(chars);
            return chars;
        } catch (CharacterCodingException e) { throw new IllegalArgumentException("Invalid UTF-8 in " + field, e); }
        finally { Arrays.fill(bytes, (byte) 0); }
    }

    private static byte[] utf8(String value, int max, String field) {
        if (value == null) throw new IllegalArgumentException(field + " must not be null");
        byte[] bytes = value.getBytes(StandardCharsets.UTF_8);
        if (bytes.length > max) throw new IllegalArgumentException(field + " is too long");
        return bytes;
    }

    private static byte[] utf8(char[] value, int max, String field) {
        if (value == null) throw new IllegalArgumentException(field + " must not be null");
        try {
            var encoder = StandardCharsets.UTF_8.newEncoder().onMalformedInput(CodingErrorAction.REPORT).onUnmappableCharacter(CodingErrorAction.REPORT);
            ByteBuffer encoded = encoder.encode(java.nio.CharBuffer.wrap(value));
            if (encoded.remaining() > max) throw new IllegalArgumentException(field + " is too long");
            byte[] bytes = new byte[encoded.remaining()]; encoded.get(bytes); return bytes;
        } catch (CharacterCodingException e) { throw new IllegalArgumentException("Invalid UTF-16 in " + field, e); }
    }
}
