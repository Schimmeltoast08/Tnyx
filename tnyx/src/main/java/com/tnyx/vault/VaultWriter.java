package com.tnyx.vault;

import com.tnyx.crypto.EncryptedVault;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.file.AtomicMoveNotSupportedException;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.nio.file.StandardOpenOption;
import java.nio.file.attribute.PosixFilePermission;
import java.util.Set;

public final class VaultWriter {
    private VaultWriter() {}

    public static void writeEncryptedVault(String filepath, EncryptedVault encryptedVault) throws IOException {
        if (filepath == null || filepath.isBlank()) throw new IOException("Vault path is empty");
        byte[] data = EncryptedVaultSerializer.serializeEncryptedVault(encryptedVault);
        writeBytes(Path.of(filepath).toAbsolutePath().normalize(), data, true);
    }

    public static void createNewVault(String filepath, EncryptedVault encryptedVault) throws IOException {
        if (filepath == null || filepath.isBlank()) throw new IOException("Vault path is empty");
        Path target = Path.of(filepath).toAbsolutePath().normalize();
        if (Files.exists(target, LinkOption.NOFOLLOW_LINKS) || Files.isSymbolicLink(target)) {
            throw new IOException("Vault already exists; refusing to overwrite it");
        }
        byte[] data = EncryptedVaultSerializer.serializeEncryptedVault(encryptedVault);
        writeBytes(target, data, false);
    }

    private static void writeBytes(Path target, byte[] data, boolean replace) throws IOException {
        if (Files.isSymbolicLink(target)) throw new IOException("Refusing to replace a symbolic-link vault path");
        Path parent = target.getParent();
        if (parent == null) throw new IOException("Vault path has no parent directory");
        Files.createDirectories(parent);
        // no restrictPermissions(parent): never modify a directory we don't own

        Path temp = Files.createTempFile(parent, target.getFileName().toString() + ".", ".tmp"); // 0600 on POSIX
        boolean moved = false;
        try {
            try (FileChannel channel = FileChannel.open(temp, StandardOpenOption.WRITE, StandardOpenOption.TRUNCATE_EXISTING)) {
                ByteBuffer buffer = ByteBuffer.wrap(data);
                while (buffer.hasRemaining()) channel.write(buffer);
                channel.force(true);
            }
            // ... move as before ...
            forceDirectory(parent);
            moved = true;
        } finally {
            if (!moved) Files.deleteIfExists(temp);
        }
    }

    private static void forceDirectory(Path parent) {
        try (FileChannel dir = FileChannel.open(parent, StandardOpenOption.READ)) { dir.force(true); }
        catch (Exception ignored) { }
    }

    private static void restrictPermissions(Path path) {
        try { Files.setPosixFilePermissions(path, Set.of(PosixFilePermission.OWNER_READ, PosixFilePermission.OWNER_WRITE)); }
        catch (UnsupportedOperationException | IOException ignored) { }
    }
}
