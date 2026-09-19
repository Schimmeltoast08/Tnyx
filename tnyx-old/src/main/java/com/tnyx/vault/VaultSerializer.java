package com.tnyx.vault;

import com.tnyx.crypto.CryptoConstants;

import java.io.ByteArrayOutputStream;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.nio.charset.CodingErrorAction;
import java.nio.charset.CharacterCodingException;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

/** Strict plaintext vault format. Version 3 is accepted only for migration; version 4 is emitted. */
public final class VaultSerializer {
    private static final int CURRENT_VERSION = Vault.CURRENT_FORMAT_VERSION;
    private static final int LEGACY_VERSION = 3;
    private static final byte[] FORMAT = "[Format]".getBytes(StandardCharsets.US_ASCII);
    private static final byte[] TIME = "[Time]".getBytes(StandardCharsets.US_ASCII);
    private static final byte[] ENTRIES = "[Entries]".getBytes(StandardCharsets.US_ASCII);

    private VaultSerializer() {}

    public static byte[] serializeVault(Vault vault) {
        if (vault == null) throw new IllegalArgumentException("vault must not be null");
        if (vault.getEntries().size() > CryptoConstants.MAX_ENTRIES) throw new IllegalArgumentException("Too many entries");
        int size = FORMAT.length + 4 + TIME.length + 16 + ENTRIES.length + 4;
        byte[][] serializedEntries = new byte[vault.getEntries().size()][];
        for (int i = 0; i < serializedEntries.length; i++) {
            serializedEntries[i] = PasswordEntrySerializer.serializePasswordEntry(vault.getEntries().get(i));
            size = checkedAdd(size, 4 + serializedEntries[i].length);
            if (size > CryptoConstants.MAX_VAULT_PLAINTEXT_SIZE) throw new IllegalArgumentException("Vault is too large");
        }
        ByteArrayOutputStream out = new ByteArrayOutputStream(size);
        out.writeBytes(FORMAT); writeInt(out, CURRENT_VERSION);
        out.writeBytes(TIME); writeLong(out, vault.getCreationTime()); writeLong(out, vault.getLastEditedTime());
        out.writeBytes(ENTRIES); writeInt(out, serializedEntries.length);
        for (byte[] entry : serializedEntries) { writeInt(out, entry.length); out.writeBytes(entry); }
        return out.toByteArray();
    }

    public static Vault deserializeVault(byte[] data) {
        if (data == null || data.length > CryptoConstants.MAX_VAULT_PLAINTEXT_SIZE) throw new IllegalArgumentException("Vault is missing or too large");
        ByteBuffer b = ByteBuffer.wrap(data);
        readMarker(b, FORMAT);
        int version = readInt(b, "format version");
        if (version == CURRENT_VERSION) return parseV4(b);
        if (version == LEGACY_VERSION) return parseLegacyV3(b);
        throw new IllegalArgumentException("Unsupported vault plaintext format version: " + version);
    }

    private static Vault parseV4(ByteBuffer b) {
        readMarker(b, TIME);
        long creation = readLong(b, "creation time");
        long edited = readLong(b, "last edited time");
        readMarker(b, ENTRIES);
        int count = readInt(b, "entry count");
        if (count < 0 || count > CryptoConstants.MAX_ENTRIES) throw new IllegalArgumentException("Invalid entry count");
        Vault vault = new Vault();
        vault.setVaultFormatVersion(CURRENT_VERSION);
        vault.setCreationTime(creation); vault.setLastEditedTime(edited);
        parseEntries(b, count, vault);
        ensureEnd(b);
        return vault;
    }

    private static Vault parseLegacyV3(ByteBuffer b) {
        readMarker(b, "[KDF]".getBytes(StandardCharsets.US_ASCII));
        String kdf = readUtf8Field(b, "KDF", 64);
        byte[] salt = readBytesField(b, "salt", CryptoConstants.SALT_LENGTH);
        readMarker(b, "[Encryption]".getBytes(StandardCharsets.US_ASCII));
        String algorithm = readUtf8Field(b, "encryption algorithm", 64);
        byte[] nonce = readBytesField(b, "nonce", CryptoConstants.NONCE_LENGTH);
        readMarker(b, TIME);
        long creation = readLong(b, "creation time");
        long edited = readLong(b, "last edited time");
        readMarker(b, "[Data]".getBytes(StandardCharsets.US_ASCII));
        byte[] nonce2 = readBytesField(b, "nonce2", CryptoConstants.NONCE_LENGTH);
        int count = readInt(b, "entry count");
        if (count < 0 || count > CryptoConstants.MAX_ENTRIES) throw new IllegalArgumentException("Invalid entry count");
        Vault vault = new Vault();
        vault.setVaultFormatVersion(LEGACY_VERSION);
        vault.setCreationTime(creation); vault.setLastEditedTime(edited);
        parseEntries(b, count, vault);
        ensureEnd(b);
        if (!"Argon2id".equals(kdf) || !"AES".equals(algorithm)) throw new IllegalArgumentException("Invalid legacy vault cryptographic metadata");
        return vault;
    }

    private static void parseEntries(ByteBuffer b, int count, Vault vault) {
        Set<UUID> ids = new HashSet<>();
        for (int i = 0; i < count; i++) {
            int length = readInt(b, "entry length");
            if (length < 0 || length > CryptoConstants.MAX_ENTRY_BYTES || length > b.remaining()) throw new IllegalArgumentException("Invalid entry length");
            byte[] entryBytes = new byte[length]; b.get(entryBytes);
            PasswordEntry entry = PasswordEntrySerializer.deserializePasswordEntry(entryBytes);
            if (!ids.add(entry.getId())) throw new IllegalArgumentException("Duplicate entry UUID");
            vault.addEntry(entry);
        }
    }

    private static byte[] readBytesField(ByteBuffer b, String field, int exact) {
        int length = readInt(b, field + " length");
        if (length != exact || length > b.remaining()) throw new IllegalArgumentException("Invalid " + field + " length");
        byte[] result = new byte[length]; b.get(result); return result;
    }

    private static String readUtf8Field(ByteBuffer b, String field, int max) {
        int length = readInt(b, field + " length");
        if (length < 0 || length > max || length > b.remaining()) throw new IllegalArgumentException("Invalid " + field + " length");
        byte[] bytes = new byte[length]; b.get(bytes);
        try {
            return StandardCharsets.UTF_8.newDecoder().onMalformedInput(CodingErrorAction.REPORT).onUnmappableCharacter(CodingErrorAction.REPORT).decode(ByteBuffer.wrap(bytes)).toString();
        } catch (CharacterCodingException e) {
            throw new IllegalArgumentException("Invalid UTF-8 in " + field, e);
        } finally {
            java.util.Arrays.fill(bytes, (byte) 0);
        }
    }

    private static void readMarker(ByteBuffer b, byte[] expected) {
        if (b.remaining() < expected.length) throw new IllegalArgumentException("Missing marker");
        for (byte value : expected) if (b.get() != value) throw new IllegalArgumentException("Unexpected vault marker");
    }

    private static int readInt(ByteBuffer b, String field) { if (b.remaining() < 4) throw new IllegalArgumentException("Missing " + field); return b.getInt(); }
    private static long readLong(ByteBuffer b, String field) { if (b.remaining() < 8) throw new IllegalArgumentException("Missing " + field); return b.getLong(); }
    private static void ensureEnd(ByteBuffer b) { if (b.hasRemaining()) throw new IllegalArgumentException("Trailing data in vault"); }
    private static void writeInt(ByteArrayOutputStream out, int value) { out.writeBytes(ByteBuffer.allocate(4).putInt(value).array()); }
    private static void writeLong(ByteArrayOutputStream out, long value) { out.writeBytes(ByteBuffer.allocate(8).putLong(value).array()); }
    private static int checkedAdd(int a, int b) { long result = (long) a + b; if (result > Integer.MAX_VALUE) throw new IllegalArgumentException("Vault is too large"); return (int) result; }
}
