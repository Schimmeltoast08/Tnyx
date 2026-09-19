package com.tnyx.ui;

import com.tnyx.Main;
import com.tnyx.crypto.OpenedVault;
import com.tnyx.vault.PasswordEntry;
import com.tnyx.vault.VaultHandler;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.net.URL;

import static com.tnyx.util.Log.log;

public class LockFrame extends JFrame implements ActionListener {

    JButton fileChooserButton;
    JButton exitButton;
    JButton openButton;
    JButton newVaultButton;

    JLabel entryLabel;
    JLabel selectedVaultLabel;
    JLabel selectedVaultPathLabel;
    JLabel piclabel;

    JPanel mainPanel;
    JPanel selectedVaultPanel;

    JPasswordField pwField;

    File file;

    boolean placeholder = true;
    boolean mayProceed = false;

    public LockFrame() {
        super("Tnyx Password manager"); //this.setTitle("Tnyx Password manager");
        this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        getRootPane().setBorder(BorderFactory.createMatteBorder(4, 4, 4, 4, new Color(53, 132, 228)));


        //this.setSize(500, 300);
        this.setLocationRelativeTo(null);
        Font font = new Font("Arial", Font.PLAIN, 20);

        Container contentPane = this.getContentPane();
        contentPane.setLayout(new FlowLayout(FlowLayout.LEADING)); // ik it's bad, but i tested 5 layouts and they all suck ass


        entryLabel = new JLabel("Select vault or Path");
        entryLabel.setFont(font);
        entryLabel.setBounds(5, 20, 300, 30);


        fileChooserButton = new JButton("Choose a File");
        fileChooserButton.setFont(font);
        fileChooserButton.setBounds(5, 70, 200, 40);
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
        selectedVaultPanel.setBounds(5, 130, 660, 40);

        selectedVaultPanel.add(selectedVaultLabel);
        selectedVaultPanel.add(selectedVaultPathLabel);

        pwField = new JPasswordField("Password");
        pwField.setEchoChar((char) 0);
        pwField.setBounds(5, 175, 200, 40);


        pwField.addFocusListener(new FocusAdapter() {
            @Override
            public void focusGained(FocusEvent e) {
                if (placeholder) {
                    pwField.setText("");
                    pwField.setEchoChar('•');
                    placeholder = false;
                }
            }

            @Override
            public void focusLost(FocusEvent e) {
                if (pwField.getPassword().length == 0) {
                    pwField.setText("Password");
                    pwField.setEchoChar((char) 0);
                    placeholder = true;
                }
            }
        });
        // so much code for this tiny bs :(


        openButton = new JButton("Open");
        openButton.setFont(font);
        openButton.setBounds(5, 295, 200, 50);
        openButton.addActionListener(this);

        newVaultButton = new JButton("New");
        newVaultButton.setFont(font);
        newVaultButton.setBounds(495, 230, 200, 50);
        newVaultButton.addActionListener(this);

        exitButton = new JButton("Exit");
        //exitButton.setAlignmentX(Component.RIGHT_ALIGNMENT);
        //exitButton.setAlignmentY(Component.BOTTOM_ALIGNMENT);
        exitButton.setBounds(495, 295, 200, 50);
        exitButton.addActionListener(this);

        URL url = LockFrame.class.getResource("/Tnyx-logo-no-background.png");

        if (url != null) {
            ImageIcon original = new ImageIcon(url);

            int maxWidth = 320;
            int maxHeight = 80;

            int originalWidth = original.getIconWidth();
            int originalHeight = original.getIconHeight();

            double scale = Math.min(
                    (double) maxWidth / originalWidth,
                    (double) maxHeight / originalHeight
            );

            int newWidth = (int) (originalWidth * scale);
            int newHeight = (int) (originalHeight * scale);

            Image scaledImage = original.getImage().getScaledInstance(
                    newWidth,
                    newHeight,
                    Image.SCALE_SMOOTH
            );

            piclabel = new JLabel(new ImageIcon(scaledImage));
            piclabel.setBounds(400, 5, maxWidth, maxHeight);
        } else {
            log("Could not find Tnyx logo", 3);
        }


        mainPanel = new JPanel(null);
        //mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.Y_AXIS));
        mainPanel.setPreferredSize(new Dimension(700, 350));
        //mainPanel.setBackground(Color.CYAN);


        mainPanel.add(entryLabel);

        mainPanel.add(Box.createVerticalStrut(20));
        mainPanel.add(fileChooserButton);

        mainPanel.add(Box.createVerticalStrut(20));
        mainPanel.add(selectedVaultPanel);
        mainPanel.add(pwField);
        mainPanel.add(openButton);
        mainPanel.add(newVaultButton);
        mainPanel.add(piclabel);


        mainPanel.add(exitButton);

// add stack

        contentPane.add(mainPanel);
        //

        //
        this.pack();
    }

    @Override
    public void actionPerformed(ActionEvent e) {

        if (e.getSource() == exitButton){
            log("Exiting GUI", 2);
            dispose();
            Main.exitApplication(0);
        }

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
                    mayProceed = true;
                }

                selectedVaultPathLabel.setVisible(true);
                SwingUtilities.invokeLater(() -> {
                    mainPanel.revalidate();
                    mainPanel.repaint();
                });


            }

        }



        if (e.getSource() == openButton){
            openVaultGui();


        }

        if (e.getSource() == newVaultButton) {
            try {
                log("Creating new vault at " + file.getAbsolutePath(), 2);
                VaultHandler.createEncryptedVault(file.getAbsolutePath(), pwField.getPassword());
                JOptionPane.showMessageDialog(this, "Successfully created new vault at:\n" + file.getAbsolutePath());
                openVaultGui();
            } catch (IOException ex) {
                log("Could not create new Vault from Gui", 3);

                JOptionPane.showMessageDialog(
                        this,
                        "Could not create vault. Is the path correct?",
                        "Could not create vault",
                        JOptionPane.ERROR_MESSAGE
                );
            }
        }




    }

    private void openVaultGui() {
        if (!mayProceed){
            log("User did not select a Vault file", 3);
            JOptionPane.showMessageDialog(this, "Please select a valid TVault (.tvlt) file before submitting");
        }

        if (placeholder || pwField.getPassword().length == 0){
            log("User did not enter Password", 3);
            JOptionPane.showMessageDialog(this, "Please enter a password before submitting");
        }

        char[] password = pwField.getPassword();

        log("Attempting to open vault: " + file.getAbsolutePath(), 1);

        // Pass these to your vault/decryption code

        try {
            OpenedVault vault = VaultHandler.openVault(file.getAbsolutePath(), password);
            UiManager.setOpenedVault(vault);
            UiManager.setMainFrameVault(file.getAbsolutePath());
            UiManager.showMainScreen();
            MainFrame.updateEntries();
        } catch (IOException ex) {
            log("could not pass values to VaultHandler.openVault", 4);
            JOptionPane.showMessageDialog(
                    this,
                    "Incorrect password or corrupted vault.",
                    "Could not open vault",
                    JOptionPane.ERROR_MESSAGE
            );

        }
    }

    public void setPWFieldText(String str){
        this.pwField.setText(str);
    }



}