package com.tnyx.ui;

import com.tnyx.vault.PasswordEntry;

import javax.swing.*;
import javax.swing.border.Border;
import java.awt.*;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.Arrays;
import java.util.UUID;

public final class EntryPanel {
    private static final Border NORMAL_BORDER = BorderFactory.createLineBorder(new Color(85, 85, 85));
    private static final Border SELECTED_BORDER = BorderFactory.createLineBorder(new Color(50, 140, 255), 2);
    private final JPasswordField passwordField;
    private final JPanel panel;
    private final PasswordEntry entry;
    private final JLabel nameLabel;
    private final JLabel usernameLabel;
    private final JLabel urlLabel;
    private boolean selected;

    public EntryPanel(PasswordEntry entry) {
        this.entry = entry;
        panel = new JPanel(new GridLayout(2, 2, 20, 4));
        panel.setPreferredSize(new Dimension(940, 78));
        panel.setMinimumSize(new Dimension(100, 78));
        panel.setBorder(NORMAL_BORDER);
        panel.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        nameLabel = createInfoLabel("Name:", entry.getName());
        usernameLabel = createInfoLabel("Username:", entry.getUsername());
        urlLabel = createInfoLabel("URL:", entry.getUrl());
        panel.add(nameLabel); panel.add(usernameLabel); panel.add(urlLabel);

        passwordField = new JPasswordField();
        passwordField.setEditable(false);
        passwordField.setEchoChar('•');
        passwordField.setToolTipText("Focus to reveal temporarily");
        passwordField.addFocusListener(new FocusAdapter() {
            @Override public void focusGained(FocusEvent e) { select(); reveal(); }
            @Override public void focusLost(FocusEvent e) { hidePassword(); }
        });
        JButton copyButton = new JButton("Copy");
        copyButton.setToolTipText("Copy password; clipboard clears after 30 seconds");
        copyButton.addActionListener(e -> copyPassword());
        JPanel passwordPanel = new JPanel(new BorderLayout(8, 0));
        passwordPanel.add(new JLabel("Password:"), BorderLayout.WEST);
        passwordPanel.add(passwordField, BorderLayout.CENTER);
        passwordPanel.add(copyButton, BorderLayout.EAST);
        panel.add(passwordPanel);

        MouseAdapter selectionListener = new MouseAdapter() { @Override public void mousePressed(MouseEvent e) { select(); } };
        addSelectionListener(panel, selectionListener);
    }

    private void addSelectionListener(Component component, MouseAdapter listener) {
        component.addMouseListener(listener);
        if (component instanceof Container container) for (Component child : container.getComponents()) {
            if (child != passwordField && !(child instanceof JButton)) addSelectionListener(child, listener);
        }
    }

    private JLabel createInfoLabel(String name, String value) { return new JLabel("<html><b>" + escapeHtml(name) + "</b> " + escapeHtml(value) + "</html>"); }
    private String escapeHtml(String value) { return value == null ? "" : value.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;").replace("\"", "&quot;"); }

    private void reveal() {
        char[] password = entry.getPassword();
        try { passwordField.setText(new String(password)); passwordField.setEchoChar((char) 0); }
        finally { Arrays.fill(password, '\0'); }
    }

    private void hidePassword() {
        passwordField.setText("");
        passwordField.setEchoChar('•');
    }

    private void copyPassword() {
        select();
        ClipboardManager.copy(entry.getPassword());
        hidePassword();
    }

    private void select() { MainFrame.setSelectedEntryPanel(this); UiManager.touchActivity(); }
    public void setSelected(boolean selected) { this.selected = selected; if (!selected) hidePassword(); panel.setBorder(selected ? SELECTED_BORDER : NORMAL_BORDER); }
    public boolean isSelected() { return selected; }
    public UUID getId() { return entry.getId(); }
    public PasswordEntry getEntry() { return entry; }
    public void refresh() { nameLabel.setText("<html><b>Name:</b> " + escapeHtml(entry.getName()) + "</html>"); usernameLabel.setText("<html><b>Username:</b> " + escapeHtml(entry.getUsername()) + "</html>"); urlLabel.setText("<html><b>URL:</b> " + escapeHtml(entry.getUrl()) + "</html>"); hidePassword(); }
    public JPanel getJpanel() { return panel; }
}
