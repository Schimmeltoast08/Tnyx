package com.tnyx.ui;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;

public class LockFrame extends JFrame implements ActionListener {

    JButton fileChooserButton;
    JLabel entryLabel;

    File file;

    public LockFrame() {
        this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        this.setTitle("Tnyx Password manager");
        Font font = new Font("Arial", Font.PLAIN, 20);

        Container contentPane = this.getContentPane();
        FlowLayout layout = new FlowLayout();
        contentPane.setLayout(layout);


        entryLabel = new JLabel("Select vault to open");
        entryLabel.setFont(font);


        fileChooserButton = new JButton("Choose a File");
        fileChooserButton.setFont(font);
        fileChooserButton.addActionListener(this);


// add stack
        contentPane.add(entryLabel);
        contentPane.add(fileChooserButton);

        //

        this.pack();
    }

    @Override
    public void actionPerformed(ActionEvent e) {

        if (e.getSource() == fileChooserButton){
            JFileChooser fileChooser = new JFileChooser();
            int fileChooserExitCode = fileChooser.showOpenDialog(null);

            if (fileChooserExitCode == JFileChooser.APPROVE_OPTION){
                file = new File(fileChooser.getSelectedFile().getAbsolutePath());
                System.out.println(file.getAbsolutePath()); //TODO: Temporary for debug
            }

        }
    }
}