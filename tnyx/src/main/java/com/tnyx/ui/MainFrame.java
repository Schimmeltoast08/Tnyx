package com.tnyx.ui;

import com.tnyx.crypto.OpenedVault;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import static com.tnyx.util.Log.log;

public class MainFrame extends JFrame implements ActionListener {

    private OpenedVault openedVault;

    public MainFrame(){
        log("Created new MainFrame", 2);

        this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        this.setTitle("Tnyx Password manager");

        Container contentPane = this.getContentPane();
        FlowLayout layout = new FlowLayout();
        contentPane.setLayout(layout);
        contentPane.add(new JLabel("Label"));
        contentPane.add(new JTextField("Text field", 15));
        this.pack();


        //this.setLayout(new SpringLayout());

    }

    @Override
    public void actionPerformed(ActionEvent actionEvent) {

    }

    public void setOpenedVault(OpenedVault openedVault) {
        this.openedVault = openedVault;
    }
}