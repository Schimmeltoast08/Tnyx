package com.tnyx.ui;

import javax.swing.Timer;
import java.awt.Toolkit;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.StringSelection;
import java.util.Arrays;

/** Time-limited password clipboard with ownership-safe clearing. */
public final class ClipboardManager {
    private static final int CLEAR_DELAY_MS = 30_000;
    private static final Clipboard CLIPBOARD = Toolkit.getDefaultToolkit().getSystemClipboard();
    private static Timer clearTimer;
    private static String clipboardValue;

    private ClipboardManager() {}

    public static synchronized void copy(char[] password) {
        if (password == null) return;
        String value = new String(password);
        Arrays.fill(password, '\0');
        clearNow();
        clipboardValue = value;
        CLIPBOARD.setContents(new StringSelection(value), null);
        clearTimer = new Timer(CLEAR_DELAY_MS, e -> clearNow());
        clearTimer.setRepeats(false);
        clearTimer.start();
    }

    public static synchronized void clearNow() {
        if (clearTimer != null) { clearTimer.stop(); clearTimer = null; }
        if (clipboardValue == null) return;
        try {
            var contents = CLIPBOARD.getContents(null);
            if (contents != null && contents.isDataFlavorSupported(DataFlavor.stringFlavor)) {
                Object current = contents.getTransferData(DataFlavor.stringFlavor);
                if (clipboardValue.equals(current)) CLIPBOARD.setContents(new StringSelection(""), null);
            }
        } catch (Exception ignored) { }
        clipboardValue = null;
    }
}
