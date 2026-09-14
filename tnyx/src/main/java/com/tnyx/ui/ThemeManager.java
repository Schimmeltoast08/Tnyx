package com.tnyx.ui;

import com.formdev.flatlaf.FlatDarkLaf;
import com.formdev.flatlaf.FlatLaf;
import com.formdev.flatlaf.FlatLightLaf;


import javax.swing.UIManager;

public class ThemeManager {

    private ThemeManager() {}

    public static void setDark() throws Exception {
        UIManager.setLookAndFeel(new FlatDarkLaf());
        FlatLaf.updateUI();
    }

    public static void setLight() throws Exception {
        UIManager.setLookAndFeel(new FlatLightLaf());
        FlatLaf.updateUI();
    }
}
