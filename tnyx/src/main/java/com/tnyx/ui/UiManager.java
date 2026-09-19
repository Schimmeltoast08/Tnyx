package com.tnyx.ui;


import com.tnyx.Main;
import com.tnyx.crypto.OpenedVault;
import com.tnyx.vault.Vault;

public class UiManager {

        private static final MainFrame mainFrame = new MainFrame();
        private static final LockFrame lockFrame = new LockFrame();

        private static OpenedVault openedVault;

        public UiManager() {
            showLockScreen();
        }

        public static void showLockScreen() {
            mainFrame.setVisible(false);
            lockFrame.setVisible(true);
        }

        public static void showMainScreen() {
            lockFrame.setVisible(false);
            mainFrame.setVisible(true);
        }

    public static OpenedVault getOpenedVault() {
        return openedVault;
    }

    public static void setOpenedVault(OpenedVault opVault) {
        openedVault = opVault;
    }

    public static void setMainFrameVault(String vaultPath){
            mainFrame.setOpenedVault(openedVault);
            mainFrame.setVaultPath(vaultPath);
    }

    public static void resetPWFieldOfLockscreen(){
            lockFrame.setPWFieldText("");
    }

}

