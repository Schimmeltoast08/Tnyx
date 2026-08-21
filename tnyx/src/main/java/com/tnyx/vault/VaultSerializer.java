package com.tnyx.vault;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import com.tnyx.vault.Password.PasswordEntrySerializer;

public class VaultSerializer {

    public static byte[] serializeVault(Vault vault) throws IOException{

        List<PasswordEntry> entries = vault.getEntries();

        ArrayList<byte[]> arr = new ArrayList<>();
        arr.add(new byte[] {(byte) vault.getVaultFormatVersion()});
        //TODO: FINISH THIS. ADD EVERYTHING ELSE MISSING




        for (PasswordEntry e : entries) {
            arr.add(PasswordEntrySerializer.serializePasswordEntry(e));
        }

        ByteArrayOutputStream out = new ByteArrayOutputStream();

        for (byte[] bytes : arr) {
            out.write(bytes);
        }

        byte[] serializedVault = out.toByteArray();

        return serializedVault;
    }

    public static ArrayList<PasswordEntry> deserializeVault(byte[] vaultData) {
    ArrayList<PasswordEntry> entries = new ArrayList<>();

    ByteBuffer buffer = ByteBuffer.wrap(vaultData);

    while (buffer.hasRemaining()) {
        // Every entry has at least:
        // UUID = 16 bytes
        // 4 string lengths = 16 bytes
        // Total minimum = 32 bytes
        if (buffer.remaining() < 32) {
            throw new IllegalArgumentException(
                    "Corrupted vault: incomplete entry header"
            );
        }

        // UUID
        long mostSignificantBits = buffer.getLong();
        long leastSignificantBits = buffer.getLong();

        UUID id = new UUID(mostSignificantBits, leastSignificantBits);

        // String lengths
        int nameLength = buffer.getInt();
        int usernameLength = buffer.getInt();
        int passwordLength = buffer.getInt();
        int urlLength = buffer.getInt();

        // Validate lengths
        if (nameLength < 0 ||
            usernameLength < 0 ||
            passwordLength < 0 ||
            urlLength < 0) {

            throw new IllegalArgumentException(
                    "Corrupted vault: negative field length"
            );
        }

        // Make sure the claimed lengths can actually fit
        long dataLength =
                (long) nameLength +
                usernameLength +
                passwordLength +
                urlLength;

        if (dataLength > buffer.remaining()) {
            throw new IllegalArgumentException(
                    "Corrupted vault: entry exceeds remaining data"
            );
        }

        // Read fields
        byte[] nameBytes = new byte[nameLength];
        byte[] usernameBytes = new byte[usernameLength];
        byte[] passwordBytes = new byte[passwordLength];
        byte[] urlBytes = new byte[urlLength];

        buffer.get(nameBytes);
        buffer.get(usernameBytes);
        buffer.get(passwordBytes);
        buffer.get(urlBytes);

        // Convert UTF-8 bytes back to Strings
        String name = new String(nameBytes, StandardCharsets.UTF_8);
        String username = new String(usernameBytes, StandardCharsets.UTF_8);
        String password = new String(passwordBytes, StandardCharsets.UTF_8);
        String url = new String(urlBytes, StandardCharsets.UTF_8);

        // Create entry
        PasswordEntry entry = new PasswordEntry(id);

        entry.setName(name);
        entry.setUsername(username);
        entry.setPassword(password);
        entry.setUrl(url);

        entries.add(entry);
    }

    return entries;
}
}
