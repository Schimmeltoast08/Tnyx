package com.tnyx.ui;

import com.tnyx.vault.PasswordEntry;

import javax.swing.*;
import java.awt.*;

/** Small reusable dialog for creating and editing PasswordEntry objects. */
public final class EntryDialog {
    private EntryDialog() {
    }

    public static boolean showAddDialog(Component parent, PasswordEntry target) {
        return showDialog(parent, "Add Password Entry", target, false);
    }

    public static boolean showEditDialog(Component parent, PasswordEntry target) {
        return showDialog(parent, "Edit Password Entry", target, true);
    }

    private static boolean showDialog(Component parent, String title, PasswordEntry target, boolean editing) {
        JTextField nameField = new JTextField(editing ? target.getName() : "", 25);
        JTextField usernameField = new JTextField(editing ? target.getUsername() : "", 25);
        JTextField urlField = new JTextField(editing ? target.getUrl() : "", 25);
        JPasswordField passwordField = new JPasswordField(editing ? target.getPassword() : "", 25);

        JPanel fields = new JPanel(new GridBagLayout());
        GridBagConstraints c = new GridBagConstraints();
        c.insets = new Insets(5, 5, 5, 5);
        c.anchor = GridBagConstraints.WEST;
        c.fill = GridBagConstraints.HORIZONTAL;

        addRow(fields, c, 0, "Name:", nameField);
        addRow(fields, c, 1, "Username:", usernameField);
        addRow(fields, c, 2, "URL:", urlField);
        addRow(fields, c, 3, "Password:", passwordField);

        int result = JOptionPane.showConfirmDialog(
                parent,
                fields,
                title,
                JOptionPane.OK_CANCEL_OPTION,
                JOptionPane.PLAIN_MESSAGE
        );

        if (result != JOptionPane.OK_OPTION) {
            return false;
        }

        if (nameField.getText().isBlank()) {
            JOptionPane.showMessageDialog(parent, "Name cannot be empty.", "Invalid Entry", JOptionPane.WARNING_MESSAGE);
            return false;
        }

        target.setName(nameField.getText());
        target.setUsername(usernameField.getText());
        target.setUrl(urlField.getText());
        target.setPassword(new String(passwordField.getPassword()));
        return true;
    }

    private static void addRow(JPanel panel, GridBagConstraints c, int row, String label, JComponent field) {
        c.gridx = 0;
        c.gridy = row;
        c.weightx = 0;
        panel.add(new JLabel(label), c);

        c.gridx = 1;
        c.weightx = 1;
        panel.add(field, c);
    }
}
