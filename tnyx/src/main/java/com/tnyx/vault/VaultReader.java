package com.tnyx.vault;

import com.tnyx.crypto.CryptoConstants;
import com.tnyx.crypto.EncryptedVault;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.file.LinkOption;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.Set;

public final class VaultReader {
    private VaultReader() {}

    public static byte[] readBytes(String filepath) throws IOException {
        if (filepath == null || filepath.isBlank()) throw new IOException("Vault path is empty");
        Path path = Path.of(filepath).toAbsolutePath().normalize();
        if (java.nio.file.Files.isSymbolicLink(path)) throw new IOException("Refusing to read a symbolic-link vault path");
        if (!java.nio.file.Files.isRegularFile(path, LinkOption.NOFOLLOW_LINKS)) throw new IOException("Vault is not a regular file");

        try (FileChannel channel = FileChannel.open(path, Set.of(StandardOpenOption.READ, LinkOption.NOFOLLOW_LINKS))) {
            long size = channel.size();
            if (size <= 0 || size > CryptoConstants.MAX_VAULT_FILE_SIZE + 4096L) {
                throw new IOException("Vault file is missing, empty, or too large");
            }
            if (size > Integer.MAX_VALUE) throw new IOException("Vault file is too large");
            byte[] data = new byte[(int) size];
            ByteBuffer buffer = ByteBuffer.wrap(data);
            while (buffer.hasRemaining()) {
                int read = channel.read(buffer);
                if (read < 0) throw new IOException("Unexpected end of vault file");
            }
            if (channel.size() != size) throw new IOException("Vault file changed while it was being read");
            return data;
        }
    }

    public static EncryptedVault readEncryptedVault(String filepath) throws IOException {
        try {
            return EncryptedVaultSerializer.deserializeEncryptedVault(readBytes(filepath));
        } catch (IllegalArgumentException e) {
            throw new IOException("Could not parse encrypted vault: " + e.getMessage(), e);
        }
    }
}
