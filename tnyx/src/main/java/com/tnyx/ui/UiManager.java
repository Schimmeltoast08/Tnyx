package com.tnyx.ui;


    public class UiManager {

        private final MainFrame mainFrame;
        private final LockFrame lockFrame;

        public UiManager() {
            mainFrame = new MainFrame();
            lockFrame = new LockFrame();
            showLockScreen();
        }

        public void showLockScreen() {
            mainFrame.setVisible(false);
            lockFrame.setVisible(true);
        }

        public void showMainScreen() {
            lockFrame.setVisible(false);
            mainFrame.setVisible(true);
        }
    }

