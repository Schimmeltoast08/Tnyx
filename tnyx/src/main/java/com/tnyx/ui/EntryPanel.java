package com.tnyx.ui;

import com.tnyx.vault.PasswordEntry;

import javax.swing.*;
import java.awt.*;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;

public class EntryPanel {
    private final JPasswordField passwordField;
    private final JPanel panel;

    EntryPanel(PasswordEntry entry){
        panel = new JPanel(new GridLayout(2, 2, 20, 4));
        panel.setPreferredSize(new Dimension(940, 78));
        panel.setMinimumSize(new Dimension(100, 78));
        panel.setBorder(BorderFactory.createEmptyBorder(8, 12, 8, 12));

        panel.add(createInfoLabel("Name:", entry.getName()));
        panel.add(createInfoLabel("Username:", entry.getUsername()));
        panel.add(createInfoLabel("URL:", entry.getUrl()));

        passwordField = new JPasswordField(entry.getPassword());
        passwordField.setEditable(false);
        passwordField.setEchoChar('•');
        passwordField.setToolTipText("Click to show password");

        passwordField.addFocusListener(new FocusAdapter() {
            @Override
            public void focusGained(FocusEvent e) {
                passwordField.setEchoChar((char) 0);
            }

            @Override
            public void focusLost(FocusEvent e) {
                passwordField.setEchoChar('•');
            }
        });

        JPanel passwordPanel = new JPanel(new BorderLayout(8, 0));
        passwordPanel.add(new JLabel("Password:"), BorderLayout.WEST);
        passwordPanel.add(passwordField, BorderLayout.CENTER);
        panel.add(passwordPanel);
    }

    private JLabel createInfoLabel(String name, String value) {
        JLabel label = new JLabel("<html><b>" + escapeHtml(name) + "</b> " + escapeHtml(value) + "</html>");
        label.setToolTipText(value);
        return label;
    }

    private String escapeHtml(String value) {
        if (value == null) return "";
        return value.replace("&", "&amp;")
                .replace("<", "&lt;")
                .replace(">", "&gt;")
                .replace("\"", "&quot;");
    }

    public void makeEditable(){ }
    public void makeUnEditable(){ }

    public JPanel getJpanel(){
        return panel;
    }
}
