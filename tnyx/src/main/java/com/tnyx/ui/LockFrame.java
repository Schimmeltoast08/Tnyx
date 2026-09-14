package com.tnyx.ui;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;

import static com.tnyx.util.Log.log;

public class LockFrame extends JFrame implements ActionListener {

    JButton fileChooserButton;

    JLabel entryLabel;
    JLabel selectedVaultLabel;
    JLabel selectedVaultPathLabel;

    JPanel mainPanel;
    JPanel selectedVaultPanel;

    File file;

    boolean mayProceed = true;
    public LockFrame() {
        super("Tnyx Password manager"); //this.setTitle("Tnyx Password manager");
        this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);


        //this.setSize(500, 300);
        this.setLocationRelativeTo(null);
        Font font = new Font("Arial", Font.PLAIN, 20);

        Container contentPane = this.getContentPane();
        contentPane.setLayout(new FlowLayout(FlowLayout.LEADING)); // ik it's bad, but i tested 5 layouts and they all suck ass


        entryLabel = new JLabel("Select vault to open");
        entryLabel.setFont(font);


        fileChooserButton = new JButton("Choose a File");
        fileChooserButton.setFont(font);
        fileChooserButton.addActionListener(this);


        selectedVaultLabel = new JLabel("Selected Vault: ");
        selectedVaultLabel.setFont(font);
        selectedVaultLabel.setVisible(true);

        selectedVaultPathLabel = new JLabel("LABEL");
        selectedVaultPathLabel.setFont(font);
        selectedVaultPathLabel.setVisible(false);

        selectedVaultPanel = new JPanel(new FlowLayout(FlowLayout.LEADING, 0, 0));
        selectedVaultPanel.setAlignmentX(Component.LEFT_ALIGNMENT);
        selectedVaultPanel.setPreferredSize(new Dimension(700, 50));
        selectedVaultPanel.setMaximumSize(new Dimension(700, 50));

        selectedVaultPanel.add(selectedVaultLabel);
        selectedVaultPanel.add(selectedVaultPathLabel);


        mainPanel = new JPanel();
        mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.Y_AXIS));
        mainPanel.setPreferredSize(new Dimension(700, 250));
        mainPanel.setBackground(Color.CYAN);

        mainPanel.add(entryLabel);
        mainPanel.add(Box.createVerticalStrut(20));
        mainPanel.add(fileChooserButton);
        mainPanel.add(Box.createVerticalStrut(20));
        //mainPanel.add(selectedVaultLabel);
        mainPanel.add(selectedVaultPanel);

// add stack

        contentPane.add(mainPanel);
        //

        //
        this.pack();
    }

    @Override
    public void actionPerformed(ActionEvent e) {

        if (e.getSource() == fileChooserButton){
            selectedVaultPathLabel.setVisible(false);
            JFileChooser fileChooser = new JFileChooser();
            int fileChooserExitCode = fileChooser.showOpenDialog(this);

            if (fileChooserExitCode == JFileChooser.APPROVE_OPTION){
                file = new File(fileChooser.getSelectedFile().getAbsolutePath());

                if (!(file.getAbsolutePath().endsWith(".tvlt"))){
                    log("User submitted a file that is not a tvlt file", 3);
                    selectedVaultPathLabel.setText("Selected file is not a Vault file");
                    mayProceed = false; // allow continuation anyway. Only block on submit
                } else {
                    //selectedVaultPathLabel.setText(file.getAbsolutePath());
                    String fileName = file.toPath().getFileName().toString(); // splitting using File.separator does not work on win bcs win FS is \ and \ is a regex keyword
                    selectedVaultPathLabel.setText(fileName);
                }

                selectedVaultPathLabel.setVisible(true);
                SwingUtilities.invokeLater(() -> {
                    mainPanel.revalidate();
                    mainPanel.repaint();
                });


               
            }

        }
    }

}