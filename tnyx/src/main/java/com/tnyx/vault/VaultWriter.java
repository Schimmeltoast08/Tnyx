package com.tnyx.vault;

import com.tnyx.util.Log;

import java.io.IOException;
import java.nio.channels.FileChannel;
import java.nio.file.AtomicMoveNotSupportedException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.nio.file.StandardOpenOption;
import java.nio.file.attribute.PosixFilePermission;
import java.util.Set;

public final class VaultWriter {
    private VaultWriter() {}

    public static void writeEncryptedVault(String filepath,
                                           com.tnyx.crypto.EncryptedVault encryptedVault,
                                           boolean atomic) throws IOException {
        byte[] data = EncryptedVaultSerializer.serializeEncryptedVault(encryptedVault);
        writeBytes(filepath, data, atomic);
    }

    private static void writeBytes(String filepath, byte[] data, boolean atomic) throws IOException {
        if (filepath == null || filepath.isBlank()) {
            throw new IOException("Vault path is empty");
        }

        Path target = Path.of(filepath).toAbsolutePath().normalize();
        if (Files.isSymbolicLink(target)) {
            throw new IOException("Refusing to replace a symbolic-link vault path");
        }

        Path parent = target.getParent();
        if (parent == null) {
            throw new IOException("Vault path has no parent directory");
        }
        Files.createDirectories(parent);

        if (!atomic) {
            writeAndForce(target, data, false);
            restrictPermissions(target);
            return;
        }

        Path temp = Files.createTempFile(parent, target.getFileName().toString() + ".", ".tmp");
        boolean moved = false;
        try {
            restrictPermissions(temp);
            writeAndForce(temp, data, true);

            try {
                Files.move(temp, target,
                        StandardCopyOption.ATOMIC_MOVE,
                        StandardCopyOption.REPLACE_EXISTING);
            } catch (AtomicMoveNotSupportedException e) {
                Log.log("Atomic vault replacement is not supported; using a non-atomic replacement.", 3);
                Files.move(temp, target, StandardCopyOption.REPLACE_EXISTING);
            }
            restrictPermissions(target);
            moved = true;
        } finally {
            if (!moved) {
                Files.deleteIfExists(temp);
            }
        }
    }

    private static void writeAndForce(Path path, byte[] data, boolean sync) throws IOException {
        try (FileChannel channel = FileChannel.open(path,
                StandardOpenOption.CREATE,
                StandardOpenOption.WRITE,
                StandardOpenOption.TRUNCATE_EXISTING)) {
            channel.write(java.nio.ByteBuffer.wrap(data));
            if (sync) {
                channel.force(true);
            }
        }
    }

    private static void restrictPermissions(Path path) {
        try {
            Set<PosixFilePermission> ownerOnly = Set.of(
                    PosixFilePermission.OWNER_READ,
                    PosixFilePermission.OWNER_WRITE);
            Files.setPosixFilePermissions(path, ownerOnly);
        } catch (UnsupportedOperationException | IOException ignored) {
            // Windows and non-POSIX file systems use their own ACL model.
            // The vault must still be placed in a user-private directory.
        }
    }
}
