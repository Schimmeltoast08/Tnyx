package com.tnyx.ui;

import com.tnyx.crypto.OpenedVault;
import com.tnyx.vault.PasswordEntry;
import com.tnyx.vault.Vault;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import static com.tnyx.util.Log.log;

public class MainFrame extends JFrame implements ActionListener {

    private static OpenedVault openedVault;
    private static Vault vault;

    JButton newEntryButton;
    JButton editEntryButton;
    JButton deleteEntryButton;
    static Container contentPane;
    private static JPanel entryList;

    public MainFrame(){
        log("Created new MainFrame", 2);

        this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        this.setTitle("Tnyx Password manager");
        this.setLocationRelativeTo(null);

        contentPane = this.getContentPane();
        contentPane.setLayout(null);
        contentPane.setPreferredSize(new Dimension(1000, 650));

        entryList = new JPanel();
        entryList.setLayout(new BoxLayout(entryList, BoxLayout.Y_AXIS));
        entryList.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));

        JScrollPane scrollPane = new JScrollPane(entryList);
        scrollPane.setBounds(20, 20, 960, 570);
        scrollPane.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        scrollPane.getVerticalScrollBar().setUnitIncrement(16);
        contentPane.add(scrollPane);

        this.pack();
        this.setLocationRelativeTo(null);
    }

    @Override
    public void actionPerformed(ActionEvent actionEvent) {

    }

    public void setOpenedVault(OpenedVault opVault) {
        openedVault = opVault;
    }

    public static void updateEntries(){
        if (openedVault == null || entryList == null) {
            return;
        }

        vault = openedVault.getVault();
        entryList.removeAll();

        for (PasswordEntry entry : vault.getEntries()) {
            EntryPanel entryPanel = new EntryPanel(entry);
            JPanel panel = entryPanel.getJpanel();
            panel.setAlignmentX(Component.LEFT_ALIGNMENT);
            panel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 78));

            entryList.add(panel);
            entryList.add(Box.createVerticalStrut(6));
        }

        entryList.revalidate();
        entryList.repaint();
    }
}
