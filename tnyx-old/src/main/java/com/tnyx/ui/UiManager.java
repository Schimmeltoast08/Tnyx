package com.tnyx.ui;

import com.tnyx.crypto.OpenedVault;

import java.awt.AWTEvent;
import java.awt.Toolkit;
import java.awt.event.AWTEventListener;
import java.awt.event.InputEvent;
import javax.swing.Timer;

public final class UiManager {
    private static final long AUTO_LOCK_NANOS = java.util.concurrent.TimeUnit.MINUTES.toNanos(5);
    private static final MainFrame mainFrame = new MainFrame();
    private static final LockFrame lockFrame = new LockFrame();
    private static OpenedVault openedVault;
    private static long lastActivityNanos = System.nanoTime();
    private static Timer autoLockTimer;
    private static AWTEventListener activityListener;

    public UiManager() {
        startSecurityTimers();
        showLockScreen();
    }

    public static void showLockScreen() {
        mainFrame.setVisible(false);
        lockFrame.setVisible(true);
        touchActivity();
    }

    public static void lockAndClear() { lockAndClear(false); }

    private static void lockAndClear(boolean automatic) {
        if (!mainFrame.prepareForLock(automatic)) return;
        OpenedVault current = openedVault;
        openedVault = null;
        mainFrame.setOpenedVault(null);
        mainFrame.setVaultPath(null);
        ClipboardManager.clearNow();
        if (current != null) current.close();
        resetPWFieldOfLockscreen();
        showLockScreen();
    }

    public static void shutdown() {
        if (autoLockTimer != null) autoLockTimer.stop();
        if (activityListener != null) Toolkit.getDefaultToolkit().removeAWTEventListener(activityListener);
        autoLockTimer = null; activityListener = null;
        OpenedVault current = openedVault;
        openedVault = null;
        if (current != null) current.close();
        ClipboardManager.clearNow();
        mainFrame.dispose(); lockFrame.dispose();
    }

    public static void showMainScreen() { lockFrame.setVisible(false); mainFrame.setVisible(true); touchActivity(); }
    public static OpenedVault getOpenedVault() { return openedVault; }

    public static void setOpenedVault(OpenedVault opVault) {
        if (openedVault != null && openedVault != opVault) openedVault.close();
        openedVault = opVault;
        touchActivity();
    }

    public static void setMainFrameVault(String vaultPath) { mainFrame.setOpenedVault(openedVault); mainFrame.setVaultPath(vaultPath); }
    public static void resetPWFieldOfLockscreen() { lockFrame.clearPassword(); }
    public static void touchActivity() { lastActivityNanos = System.nanoTime(); }

    private static void startSecurityTimers() {
        if (activityListener != null) return;
        activityListener = event -> { if (event instanceof InputEvent) touchActivity(); };
        Toolkit.getDefaultToolkit().addAWTEventListener(activityListener, AWTEvent.KEY_EVENT_MASK | AWTEvent.MOUSE_EVENT_MASK | AWTEvent.MOUSE_MOTION_EVENT_MASK | AWTEvent.WINDOW_EVENT_MASK);
        autoLockTimer = new Timer(5_000, e -> {
            if (openedVault != null && System.nanoTime() - lastActivityNanos >= AUTO_LOCK_NANOS) lockAndClear(true);
        });
        autoLockTimer.start();
    }
}
