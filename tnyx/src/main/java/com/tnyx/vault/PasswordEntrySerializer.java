package com.tnyx.vault;

import com.tnyx.util.Log;

import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.UUID;

public class PasswordEntrySerializer {

    public static byte[] serializePasswordEntry(PasswordEntry entry) {

        byte[] name = entry.getName().getBytes(StandardCharsets.UTF_8);
        byte[] username = entry.getUsername().getBytes(StandardCharsets.UTF_8);
        byte[] password = entry.getPassword().getBytes(StandardCharsets.UTF_8);
        byte[] url = entry.getUrl().getBytes(StandardCharsets.UTF_8);

        UUID id = entry.getId();

        int totalLength = 32 // 4x int a 4 byte + 16 bytes uuid
                + name.length
                + username.length
                + password.length
                + url.length;

        ByteBuffer buffer = ByteBuffer.allocate(totalLength);

        buffer.putLong(id.getMostSignificantBits()); // get UUID bits
        buffer.putLong(id.getLeastSignificantBits());

        buffer.putInt(name.length);
        buffer.putInt(username.length);
        buffer.putInt(password.length);
        buffer.putInt(url.length);

        buffer.put(name);
        buffer.put(username);
        buffer.put(password);
        buffer.put(url);

        return buffer.array();
    }

    public static PasswordEntry deserializePasswordEntry(byte[] entryBuffer) {
        ByteBuffer buffer = ByteBuffer.wrap(entryBuffer);

        long mostSignificantBits = buffer.getLong();
        long leastSignificantBits = buffer.getLong();

        UUID id = new UUID(mostSignificantBits, leastSignificantBits);

        PasswordEntry entry = new PasswordEntry(id);

        int nameLength = buffer.getInt();
        int usernameLength = buffer.getInt();
        int passwordLength = buffer.getInt();
        int urlLength = buffer.getInt();

        if (nameLength < 0
                || usernameLength < 0
                || passwordLength < 0
                || urlLength < 0) {
            Log.log("Corrupted file! Could not deserialize file, field length smaller then zero", 4);
            throw new IllegalArgumentException("Negative field length");
        }

        byte[] nameBytes = new byte[nameLength];

        byte[] usernameBytes = new byte[usernameLength];

        byte[] passwordBytes = new byte[passwordLength];

        byte[] urlBytes = new byte[urlLength];

        buffer.get(nameBytes);
        buffer.get(usernameBytes);
        buffer.get(passwordBytes);
        buffer.get(urlBytes);

        String name = new String(nameBytes, StandardCharsets.UTF_8);
        String username = new String(usernameBytes, StandardCharsets.UTF_8);
        String password = new String(passwordBytes, StandardCharsets.UTF_8);
        String url = new String(urlBytes, StandardCharsets.UTF_8);

        entry.setName(name);
        entry.setUsername(username);
        entry.setPassword(password);
        entry.setUrl(url);

        return entry;
    }

}
