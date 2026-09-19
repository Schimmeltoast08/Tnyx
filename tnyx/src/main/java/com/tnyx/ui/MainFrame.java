package com.tnyx.ui;

import com.tnyx.Main;
import com.tnyx.crypto.OpenedVault;
import com.tnyx.vault.PasswordEntry;
import com.tnyx.vault.Vault;
import com.tnyx.vault.VaultHandler;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.util.UUID;

import static com.tnyx.util.Log.log;

public class MainFrame extends JFrame implements ActionListener {

    private static OpenedVault openedVault;
    private static Vault vault;
    private static EntryPanel selectedEntryPanel;
    private static MainFrame instance;

    private final JButton newEntryButton;
    private final JButton editEntryButton;
    private final JButton deleteEntryButton;
    private final JButton exitButton;
    private final JButton returnToLockFrameButton;

    private final JPanel entryList;
    private final JPanel contentPane;

    private final java.util.Map<UUID, EntryPanel> entryPanels = new java.util.HashMap<>();

    private String vaultPath;

    public MainFrame() {
        instance = this;
        getRootPane().setBorder(BorderFactory.createMatteBorder(4, 4, 4, 4, new Color(53, 132, 228)));
        log("Created new MainFrame", 2);

        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setTitle("Tnyx Password manager");

        contentPane = new JPanel(null);
        contentPane.setPreferredSize(new Dimension(1000, 650));
        setContentPane(contentPane);

        entryList = new JPanel();
        entryList.setLayout(new BoxLayout(entryList, BoxLayout.Y_AXIS));
        entryList.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));

        JScrollPane scrollPane = new JScrollPane(entryList);
        scrollPane.setBounds(20, 20, 960, 535);
        scrollPane.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        scrollPane.getVerticalScrollBar().setUnitIncrement(16);
        contentPane.add(scrollPane);

        exitButton = new JButton("Exit");
        exitButton.addActionListener(e -> {
            log("Exiting GUI", 2);
            dispose();
            Main.exitApplication(0);
        });

        returnToLockFrameButton = new JButton("Return to Lockscreen");
        returnToLockFrameButton.addActionListener(e -> {
            log("Returning to LockFrame", 2);
            UiManager.showLockScreen();
            UiManager.resetPWFieldOfLockscreen();
        });

        JPanel toolbar = new JPanel(new BorderLayout());
        toolbar.setBounds(20, 565, 960, 55);
        toolbar.setBorder(BorderFactory.createEmptyBorder(5, 0, 5, 0));

        JPanel entryActions = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        newEntryButton = new JButton("Add");
        editEntryButton = new JButton("Edit");
        deleteEntryButton = new JButton("Delete");

        newEntryButton.addActionListener(this);
        editEntryButton.addActionListener(this);
        deleteEntryButton.addActionListener(this);

        entryActions.add(newEntryButton);
        entryActions.add(editEntryButton);
        entryActions.add(deleteEntryButton);


        JPanel appActions = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));

        appActions.add(returnToLockFrameButton);
        appActions.add(exitButton);


        toolbar.add(entryActions, BorderLayout.WEST);
        toolbar.add(appActions, BorderLayout.EAST);


        contentPane.add(toolbar);

        updateActionState();

        pack();
        setLocationRelativeTo(null);
    }

    public static UUID getSelectedEntryUUID() {
        return selectedEntryPanel == null ? null : selectedEntryPanel.getId();
    }

    public static void setSelectedEntryPanel(EntryPanel entryPanel) {
        if (selectedEntryPanel == entryPanel) {
            return;
        }

        if (selectedEntryPanel != null) {
            selectedEntryPanel.setSelected(false);
        }

        selectedEntryPanel = entryPanel;

        if (selectedEntryPanel != null) {
            selectedEntryPanel.setSelected(true);
        }

        if (instance != null) {
            instance.updateActionState();
        }
    }

    private void updateActionState() {
        boolean hasSelection = selectedEntryPanel != null;
        editEntryButton.setEnabled(hasSelection);
        deleteEntryButton.setEnabled(hasSelection);
    }

    @Override
    public void actionPerformed(ActionEvent event) {
        if (event.getSource() == newEntryButton) {
            addEntry();
        } else if (event.getSource() == editEntryButton) {
            editEntry();
        } else if (event.getSource() == deleteEntryButton) {
            deleteEntry();
        }
    }

    public void setOpenedVault(OpenedVault opVault) {
        openedVault = opVault;
        vault = opVault == null ? null : opVault.getVault();
        clearSelection();
        updateActionState();
    }

    public void setVaultPath(String vaultPath) {
        this.vaultPath = vaultPath;
    }

    public static void updateEntries() {
        MainFrame frame = getMainFrame();
        if (frame == null || openedVault == null) {
            return;
        }

        frame.refreshEntries();
    }

    private void refreshEntries() {
        vault = openedVault.getVault();
        clearSelection();
        entryPanels.clear();
        entryList.removeAll();

        for (PasswordEntry entry : vault.getEntries()) {
            EntryPanel entryPanel = new EntryPanel(entry);
            JPanel panel = entryPanel.getJpanel();
            panel.setAlignmentX(Component.LEFT_ALIGNMENT);
            panel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 78));

            entryList.add(panel);
            entryList.add(Box.createVerticalStrut(6));
            entryPanels.put(entry.getId(), entryPanel);
        }

        entryList.revalidate();
        entryList.repaint();
        updateActionState();
    }

    private void addEntry() {
        PasswordEntry entry = new PasswordEntry();

        if (!EntryDialog.showAddDialog(this, entry)) {
            return;
        }

        vault.addEntry(entry);
        saveVault();
        refreshEntries();
        selectEntry(entry.getId());
    }

    private void editEntry() {
        UUID id = getSelectedEntryUUID();
        if (id == null) {
            return;
        }

        PasswordEntry entry = vault.getEntry(id);
        if (!EntryDialog.showEditDialog(this, entry)) {
            return;
        }

        saveVault();
        refreshEntries();
        selectEntry(id);
    }

    private void deleteEntry() {
        UUID id = getSelectedEntryUUID();
        if (id == null) {
            return;
        }

        PasswordEntry entry = vault.getEntry(id);
        int result = JOptionPane.showConfirmDialog(
                this,
                "Delete '" + entry.getName() + "'?\nThis cannot be undone.",
                "Delete Entry",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.WARNING_MESSAGE
        );

        if (result != JOptionPane.YES_OPTION) {
            return;
        }

        vault.removeEntry(id);
        saveVault();
        refreshEntries();
    }

    private void saveVault() {
        if (vaultPath == null || openedVault == null) {
            JOptionPane.showMessageDialog(
                    this,
                    "The vault path is not set, so the change could not be saved.",
                    "Could not save vault",
                    JOptionPane.ERROR_MESSAGE
            );
            return;
        }

        try {
            VaultHandler.saveVault(vaultPath, openedVault);
        } catch (IOException e) {
            log("Could not save vault from GUI: " + e.getMessage(), 4);
            JOptionPane.showMessageDialog(
                    this,
                    "The vault was changed in memory, but could not be saved.\n" + e.getMessage(),
                    "Could not save vault",
                    JOptionPane.ERROR_MESSAGE
            );
        }
    }

    private void selectEntry(UUID id) {
        EntryPanel entryPanel = entryPanels.get(id);
        if (entryPanel != null) {
            setSelectedEntryPanel(entryPanel);
        }
    }

    private void clearSelection() {
        if (selectedEntryPanel != null) {
            selectedEntryPanel.setSelected(false);
        }
        selectedEntryPanel = null;
    }

    private static MainFrame getMainFrame() {
        return instance;
    }
}
