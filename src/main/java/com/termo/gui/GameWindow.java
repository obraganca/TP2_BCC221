package com.termo.gui;

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import com.termo.gui.components.LetterBox;
import com.termo.gui.components.RoundedBorder;

public class GameWindow {
    private JFrame mainFrame;
    private JLabel headerLabel;
    private JLabel statusLabel;
    private JPanel controlPanel;

    public GameWindow(){
        prepareGUI();
    }

    private void prepareGUI(){
        mainFrame = new JFrame("TERMO");
        ImageIcon termoIcon = null;

        java.net.URL imgURL = GameWindow.class.getResource("/public/icon.png");
        if (imgURL != null) {
            termoIcon = new ImageIcon(imgURL);
            mainFrame.setIconImage(termoIcon.getImage());
        } else {
            JOptionPane.showMessageDialog(mainFrame, "Icon image not found.");
        }

        mainFrame.getContentPane().setBackground(Color.decode("#6e5c62"));
        mainFrame.setSize(500, 600);
        mainFrame.setLayout(new BorderLayout(10, 10));

        headerLabel = new JLabel("TERMO", JLabel.CENTER);
        headerLabel.setForeground(Color.WHITE);
        headerLabel.setFont(new Font("Arial", Font.BOLD, 24));

        statusLabel = new JLabel("", JLabel.CENTER);
        statusLabel.setSize(350, 100);
        statusLabel.setForeground(Color.WHITE);

        controlPanel = new JPanel(new GridLayout(5, 5, 5, 5));
        controlPanel.setPreferredSize(new Dimension(300, 300));
        controlPanel.setBackground(Color.decode("#6e5c62"));

        JPanel centerWrapper = new JPanel();
        centerWrapper.setOpaque(false);
        centerWrapper.setLayout(new GridBagLayout());
        centerWrapper.add(controlPanel);

        mainFrame.add(headerLabel, BorderLayout.NORTH);
        mainFrame.add(centerWrapper, BorderLayout.CENTER);
        mainFrame.add(statusLabel, BorderLayout.SOUTH);

        mainFrame.setLocationRelativeTo(null);
        mainFrame.setVisible(true);
    }

    public void showEventDemo() {
        controlPanel.removeAll();

        for (int row = 0; row < 5; row++) {
            for (int col = 0; col < 5; col++) {
                LetterBox box = new LetterBox(1, 1);
                box.setBorder(new RoundedBorder(15, "#4c4347", 6));
                box.setBackground(new Color(0,0,0,0));
                controlPanel.add(box);
            }
        }

        controlPanel.revalidate();
        controlPanel.repaint();
    }
}