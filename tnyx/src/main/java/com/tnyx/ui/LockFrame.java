package com.tnyx.ui;

import com.tnyx.Main;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.io.File;

import static com.tnyx.util.Log.log;

public class LockFrame extends JFrame implements ActionListener {

    JButton fileChooserButton;
    JButton exitButton;
    JButton openButton;

    JLabel entryLabel;
    JLabel selectedVaultLabel;
    JLabel selectedVaultPathLabel;

    JPanel mainPanel;
    JPanel selectedVaultPanel;

    JPasswordField pwField;

    File file;

    boolean placeholder = true;
    boolean mayProceed = false;

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


        openButton = new JButton("open");
        openButton.setFont(font);
        openButton.setBounds(5, 295, 200, 50);
        openButton.addActionListener(this);

        exitButton = new JButton("Exit");
        //exitButton.setAlignmentX(Component.RIGHT_ALIGNMENT);
        //exitButton.setAlignmentY(Component.BOTTOM_ALIGNMENT);
        exitButton.setBounds(495, 295, 200, 50);
        exitButton.addActionListener(this);

        mainPanel = new JPanel(null);
        //mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.Y_AXIS));
        mainPanel.setPreferredSize(new Dimension(700, 350));
        //mainPanel.setBackground(Color.CYAN);

        mainPanel.add(entryLabel);
        mainPanel.add(Box.createVerticalStrut(20));
        mainPanel.add(fileChooserButton);
        mainPanel.add(Box.createVerticalStrut(20));
        //mainPanel.add(selectedVaultLabel);
        mainPanel.add(selectedVaultPanel);

        mainPanel.add(pwField);

        mainPanel.add(openButton);

//        mainPanel.add(Box.createVerticalGlue());
//        mainPanel.add(Box.createVerticalStrut(10));
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
            if (!mayProceed){
                log("User did not select a Vault file", 3);
                JOptionPane.showMessageDialog(this, "Please select a valid TVault (.tvlt) file before submitting");
            }

            if (placeholder || pwField.getPassword().length == 0){
                log("User did not enter Password", 3);
                JOptionPane.showMessageDialog(this, "Please enter a password before submitting");
            }
        }





    }

}