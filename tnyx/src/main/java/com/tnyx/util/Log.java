package com.tnyx.util;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Path;
import java.nio.file.attribute.PosixFilePermission;
import java.util.Set;
import java.util.logging.FileHandler;
import java.util.logging.Formatter;
import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.logging.Logger;

public final class Log {
    private static final String APP_NAME = "tnyx";
    public static final Logger LOGGER = Logger.getLogger("TnyxLogger");
    private static final String LOG_FILE_NAME = "tnyx.log";
    private static final int MAX_LOG_SIZE = 5 * 1024 * 1024;

    static {
        try { initLogger(); }
        catch (Exception e) { System.err.print("ERROR at initializing logger: " + e); }
    }

    private Log() {}

    public static void log(String msg, int severity) {
        Level level = switch (severity) { case 1 -> Level.FINE; case 2 -> Level.INFO; case 3 -> Level.WARNING; case 4 -> Level.SEVERE; default -> Level.INFO; };
        LOGGER.log(level, msg);
    }
    public static void log(String message, Level severity) { LOGGER.log(severity, message); }

    private static Path getLogPath() {
        String home = System.getProperty("user.home");
        String os = System.getProperty("os.name", "").toLowerCase().strip();
        Path dir;
        if (os.contains("win")) {
            String appData = System.getenv("APPDATA");
            dir = Path.of(appData != null && !appData.isBlank() ? appData : home + "/AppData/Roaming", APP_NAME);
        } else if (os.contains("mac")) dir = Path.of(home, "Library", "Logs", APP_NAME);
        else dir = Path.of(home, ".local", "share", APP_NAME);
        return dir.resolve(LOG_FILE_NAME);
    }

    private static void initLogger() throws IOException {
        Path logPath = getLogPath();
        Path parent = logPath.getParent();
        if (parent == null) throw new IOException("Log path has no parent");
        Files.createDirectories(parent);
        restrict(parent, true);
        if (Files.isSymbolicLink(logPath)) throw new IOException("Refusing symbolic-link log file");
        FileHandler handler = new FileHandler(logPath.toString(), MAX_LOG_SIZE, 3, true);
        restrict(logPath, false);
        handler.setFormatter(new Formatter() {
            @Override public String format(LogRecord record) { return String.format("%1$tF %1$tT %2$s: %3$s%n", new java.util.Date(record.getMillis()), record.getLevel(), record.getMessage()); }
        });
        LOGGER.addHandler(handler); LOGGER.setLevel(Level.ALL); LOGGER.setUseParentHandlers(false);
    }

    private static void restrict(Path path, boolean directory) {
        try {
            if (directory) Files.setPosixFilePermissions(path, Set.of(PosixFilePermission.OWNER_READ, PosixFilePermission.OWNER_WRITE, PosixFilePermission.OWNER_EXECUTE));
            else Files.setPosixFilePermissions(path, Set.of(PosixFilePermission.OWNER_READ, PosixFilePermission.OWNER_WRITE));
        } catch (UnsupportedOperationException | IOException ignored) { }
    }
}
