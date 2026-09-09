package com.tnyx.util;

import java.io.File;
import java.io.IOException;
import java.util.logging.FileHandler;
import java.util.logging.Formatter;
import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.logging.Logger;

public class Log {

    @SuppressWarnings("unused")
    private Log() {
    } // so no instanciation

    private static final String APP_NAME = "tnyx";

    public static final Logger LOGGER = Logger.getLogger("TnyxLogger");
    private static final String LOG_FILE_NAME = "tnyx.log";
    private static final int MAX_LOG_SIZE = 5 * 1024 * 1024; // 5 MiB

    static {
        try {
            initLogger();
        } catch (Exception e) {
            System.out.print("ERROR at initializing logger: " + e);
        }
    }

    public static void log(String msg, int severity) {

        Level severityLevel = switch (severity) {
            case 1 -> Level.FINE;
            case 2 -> Level.INFO;
            case 3 -> Level.WARNING;
            case 4 -> Level.SEVERE;
            default -> Level.INFO;
        };

        LOGGER.log(severityLevel, msg);

    }

    private static String getLogPath() {
        String userHome = System.getProperty("user.home");
        String userOS = System.getProperty("os.name").toLowerCase().strip(); // strip just to be sure

        File logDirectory;

        if (userOS.contains("win")) { // Windows

            String appData = System.getenv("APPDATA");

            if (appData != null && !appData.isBlank()) {
                logDirectory = new File(appData, APP_NAME);
            } else {
                logDirectory = new File(userHome, "AppData" + File.separator + "Roaming" + File.separator + APP_NAME);
            }

        } else if (userOS.contains("mac")) { // Mac
            logDirectory = new File(userHome, "Library" + File.separator + "Logs" + File.separator + APP_NAME);
        } else { // Linux
            logDirectory = new File(userHome, ".local" + File.separator + "share" + File.separator + APP_NAME);
        }

        return new File(logDirectory, LOG_FILE_NAME).getAbsolutePath();
    }

    static private void initLogger() throws IOException {
        String logPath = getLogPath();
        File logFile = new File(logPath);

        File logFileParent = logFile.getParentFile();

        if (logFileParent != null && !logFileParent.exists()) {
            if (!logFileParent.mkdirs() && !logFileParent.exists()) {
                throw new IOException("Could not create log directory: " + logFileParent);
            }
        }

        FileHandler fileHandler = new FileHandler(logPath, MAX_LOG_SIZE, 3, true); // 5 mb file, then log rotation, keep 3 max files, append

        fileHandler.setFormatter(new Formatter() {
            @Override
            public String format(LogRecord record) {
                return String.format("%1$tF %1$tT %2$s: %3$s%n", new java.util.Date(record.getMillis()),
                        record.getLevel(),
                        record.getMessage()
                );
            }
        });

        LOGGER.addHandler(fileHandler);
        LOGGER.setLevel(Level.ALL);
        LOGGER.setUseParentHandlers(false);
    }

    public static void log(String message, Level severity) {
        LOGGER.log(severity, message);

    }

}
