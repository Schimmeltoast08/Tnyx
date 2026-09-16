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
        this.setLocationRelativeTo(null);

        Font font = new Font("Arial", Font.PLAIN, 20);

        Container contentPane = this.getContentPane();
        contentPane.setLayout(null); // i hate java swing layouts. They all suck
        contentPane.setPreferredSize(new Dimension(700, 350));










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