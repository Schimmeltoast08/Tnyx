package com.tnyx.util.Config;

public class Config {
    int configVersion = 1;
    boolean flatlafInstalled = false;
    boolean darkMode = true;
    boolean rememberLastVault = false; // i want it to be true, but security for when readVault fails ig :/
    String pathToLastOpenedVault = "";
    int windowX;
    int windowY;
    boolean autoLockEnabeled = true;
    int autoLockTime = 5; // lock after 5 minutes
    boolean lockOnSystemSleep;
    boolean lockOnScreenLock; // idk if that is even possible
    String backupDirectory;
    int backupInterval = 7; // if on vault opened vault last backup date greater then 7 days old compared to today, make backup



    Config(){} // no direct construction


    public int getConfigVersion(){
        return this.configVersion;
    }

        public boolean getFlatlafInstallStatus(){
        return this.flatlafInstalled;
    }

    public boolean getDarkModeStatus(){
        return this.darkMode;
    }

    public boolean getRememberLastVaultStatus(){
        return this.rememberLastVault;
    }

    public String getPathToLastOpenedVault(){
        return this.pathToLastOpenedVault;
    }

    public int getWindowXFromConfig(){
        return this.windowX;
    }
    
    public int getWindowyFromConfig(){
        return this.windowY;
    }

    public boolean getAutoLockEnabeledStatus(){
        return this.autoLockEnabeled;
    }

    public int getAutoLockTime(){
        return this.autoLockTime;
    }

    public boolean getLockOnSystemSleepStatus(){
        return this.lockOnSystemSleep;
    }

    public boolean getLockOnScreenLockStatus(){
        return this.lockOnScreenLock;
    }

    public String getBackupDirectory(){
        return this.backupDirectory;
    }

    public int getBackupInterval(){
        return this.backupInterval;
    }

}
