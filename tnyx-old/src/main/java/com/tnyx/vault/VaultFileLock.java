package com.tnyx.vault;

import java.io.IOException;
import java.nio.channels.FileChannel;
import java.nio.channels.FileLock;
import java.nio.channels.OverlappingFileLockException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.nio.file.LinkOption;
import java.util.Set;
import java.nio.file.attribute.PosixFilePermission;

/** Cooperative per-vault process lock held for the lifetime of an opened vault. */
public final class VaultFileLock implements AutoCloseable {
    private final FileChannel channel;
    private final FileLock lock;

    private VaultFileLock(FileChannel channel, FileLock lock) {
        this.channel = channel;
        this.lock = lock;
    }

    public static VaultFileLock acquire(Path vaultPath) throws IOException {
        Path absolute = vaultPath.toAbsolutePath().normalize();
        if (Files.isSymbolicLink(absolute)) throw new IOException("Refusing symbolic-link vault path");
        Path parent = absolute.getParent();
        if (parent == null) throw new IOException("Vault path has no parent");
        Files.createDirectories(parent);
        Path lockPath = parent.resolve(absolute.getFileName() + ".lock");
        if (Files.isSymbolicLink(lockPath)) throw new IOException("Refusing symbolic-link lock path");
        FileChannel channel = FileChannel.open(lockPath, Set.of(StandardOpenOption.CREATE, StandardOpenOption.WRITE, LinkOption.NOFOLLOW_LINKS));
        try {
            restrictPermissions(lockPath);
            FileLock lock = channel.tryLock();
            if (lock == null) throw new IOException("Vault is already open by another Tnyx process");
            return new VaultFileLock(channel, lock);
        } catch (OverlappingFileLockException e) {
            channel.close();
            throw new IOException("Vault is already open by another Tnyx process", e);
        } catch (IOException | RuntimeException e) {
            channel.close();
            throw e;
        }
    }

    @Override public void close() {
        try { lock.release(); } catch (IOException ignored) { }
        try { channel.close(); } catch (IOException ignored) { }
    }

    private static void restrictPermissions(Path path) {
        try { Files.setPosixFilePermissions(path, Set.of(PosixFilePermission.OWNER_READ, PosixFilePermission.OWNER_WRITE)); }
        catch (UnsupportedOperationException | IOException ignored) { }
    }
}
