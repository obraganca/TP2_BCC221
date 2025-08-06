package com.termo.gui;

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;

import com.termo.core.Game;
import com.termo.gui.components.LetterBox;
import com.termo.gui.components.RoundedBorder;

public class GameWindow {
    private JFrame mainFrame;
    private JLabel headerLabel;
    private JLabel statusLabel;
    private JPanel controlPanel;
    private static LetterBox[] letterBoxes;


    private int currentRow = 0;
    private int currentCol = 0;

    Game jogo;

    public GameWindow(String resposta){
        jogo = new Game(resposta);
        prepareGUI();
    }

    private void prepareGUI(){
        letterBoxes = new LetterBox[5];
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

                // Só tornamos editáveis as caixas da linha atual
                box.setEditable(row == currentRow);

                // Armazenamos apenas as caixas da linha atual
                if (row == currentRow) {
                    letterBoxes[col] = box;

                    // Adicionamos o KeyListener apenas às caixas da linha atual
                    box.addKeyListener(new KeyAdapter() {
                        @Override
                        public void keyPressed(KeyEvent e) {
                            char keyChar = Character.toUpperCase(e.getKeyChar());

                            // Letras de A-Z
                            if (Character.isLetter(keyChar) && currentCol < 5) {
                                letterBoxes[currentCol].setText(String.valueOf(keyChar));
                                if (currentCol < 4) {
                                    currentCol++;
                                    letterBoxes[currentCol].requestFocus();
                                }
                            }

                            // Backspace
                            if (e.getKeyCode() == KeyEvent.VK_BACK_SPACE && currentCol >= 0) {
                                if (letterBoxes[currentCol].getText().isEmpty() && currentCol > 0) {
                                    currentCol--;
                                }
                                letterBoxes[currentCol].setText("");
                                letterBoxes[currentCol].requestFocus();
                            }

                            // Enter - Aqui corrigimos o fluxo para o backend
                            if (e.getKeyCode() == KeyEvent.VK_ENTER && currentCol == 4) {
                                String tentativa = "";
                                for (int i = 0; i < 5; i++) {
                                    tentativa += letterBoxes[i].getText();
                                }

                                jogo.tentativa(tentativa);
                                char[] resultado = jogo.getResultado();

                                // Atualiza cores
                                for (int i = 0; i < 5; i++) {
                                    switch (resultado[i]) {
                                        case 'G' -> letterBoxes[i].setBackground(Color.GREEN);
                                        case 'Y' -> letterBoxes[i].setBackground(Color.YELLOW);
                                        case 'B' -> letterBoxes[i].setBackground(Color.GRAY);
                                    }
                                    letterBoxes[i].setEditable(false);
                                }

                                // Prepara próxima linha
                                if (currentRow < 4) {
                                    currentRow++;
                                    currentCol = 0;
                                    showEventDemo(); // Recria a UI para a nova linha
                                } else {
                                    statusLabel.setText("Fim de jogo! A palavra era: " + jogo.getPalavra());
                                }
                            }
                        }
                    });
                }
            }
        }
        controlPanel.revalidate();
        controlPanel.repaint();
    }
    private void handleKeyPress(KeyEvent e, LetterBox box) {
        char keyChar = Character.toUpperCase(e.getKeyChar());

        if (Character.isLetter(keyChar) && currentCol < 5) {
            letterBoxes[currentCol].setText(String.valueOf(keyChar));
            if (currentCol < 4) { // move para a proxima letterbox se nao for a ultima
                currentCol++;
                letterBoxes[currentCol].requestFocus();
            }
        }

        // apagar caracteres
        if (e.getKeyCode() == KeyEvent.VK_BACK_SPACE && currentCol >= 0) {
            if (letterBoxes[currentCol].getText().isEmpty() && currentCol > 0) {
                currentCol--;
            }
            letterBoxes[currentCol].setText("");
            letterBoxes[currentCol].requestFocus();
        }

        // processar o enter
        if (e.getKeyCode() == KeyEvent.VK_ENTER && currentCol == 4) {
            processaTentativa();
        }
    }

    public void processaTentativa(){
        String chute = "";
        for (int i = 0; i<5; i++){
            chute += getLetterBoxes()[i].getText();
        }
        jogo.tentativa(chute);
        char resultado[] = jogo.getResultado();
        for (int i = 0; i < 5; i++) {
            LetterBox box = letterBoxes[currentRow * 5 + i];
            box.setEditable(false);
            switch (resultado[i]) {
                case 'G' -> box.setBackground(Color.GREEN);
                case 'Y' -> box.setBackground(Color.YELLOW);
                case 'B' -> box.setBackground(Color.GRAY);
            }
        }
    }

    public static LetterBox[] getLetterBoxes() {
        return letterBoxes;
    }
}