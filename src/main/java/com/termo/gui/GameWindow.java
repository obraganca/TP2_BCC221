package com.termo.gui;

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;

import com.termo.core.Game;
import com.termo.gui.components.LetterBox;
import com.termo.gui.components.RoundedBorder;
import com.termo.model.DataSourceModel;

public class GameWindow {
    private JFrame mainFrame;
    private JLabel headerLabel;
    private JPanel headerPanel;
    private JLabel statusLabel;
    private JPanel controlPanel;
    public static final int COLUMN = 5;
    public static final int ROW = 6;
    private LetterBox[][] letterBoxes;
    private int currentRow = 0;
    private int currentCol = 0;
    private Game jogo;

    public GameWindow() {
        jogo = new Game();
        prepareGUI();
        showEventDemo();
    }

    private void prepareGUI() {
        letterBoxes = new LetterBox[ROW][COLUMN];
        mainFrame = new JFrame("TERMO");
        mainFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        ImageIcon termoIcon = null;
        java.net.URL imgURL = GameWindow.class.getResource("/public/icon.png");
        if (imgURL != null) {
            termoIcon = new ImageIcon(imgURL);
            mainFrame.setIconImage(termoIcon.getImage());
        }

        mainFrame.getContentPane().setBackground(Color.decode("#6e5c62"));


        Toolkit toolkit =  Toolkit.getDefaultToolkit ();
        Dimension dim = toolkit.getScreenSize();
        mainFrame.setSize(dim.width,dim.height);

        mainFrame.setLayout(new BorderLayout(10, 10));

        headerLabel = new JLabel("TERMO", JLabel.CENTER);
        headerLabel.setForeground(Color.WHITE);
        headerLabel.setFont(new Font("Arial", Font.BOLD, 24));


        statusLabel = new JLabel("", JLabel.CENTER);
        statusLabel.setSize(350, 100);
        statusLabel.setForeground(Color.WHITE);
        controlPanel = new JPanel(new GridLayout(GameWindow.ROW, GameWindow.COLUMN, 5, 5));

        controlPanel.setPreferredSize(new Dimension(440, 500));  // Aumentei a altura de 300 para 360
        controlPanel.setOpaque(false);

        headerPanel = new JPanel(new GridLayout(2, 1, 5, 5));
        headerPanel.setOpaque(false);
        headerLabel.setPreferredSize(new Dimension(50, 50));  // Aumentei a altura de 300 para 360
        headerPanel.add(headerLabel, BorderLayout.NORTH);

        JLabel headerLabel1 = new JLabel("TERMO", JLabel.CENTER);
        headerLabel1.setForeground(Color.WHITE);
        headerLabel1.setFont(new Font("Arial", Font.BOLD, 24));

        headerPanel.add(headerLabel1, BorderLayout.SOUTH);

        JPanel centerWrapper = new JPanel();
        centerWrapper.setOpaque(false);
        centerWrapper.setLayout(new GridBagLayout());
        centerWrapper.add(controlPanel);


        mainFrame.add(headerPanel, BorderLayout.NORTH);
        mainFrame.add(centerWrapper, BorderLayout.CENTER);
        mainFrame.add(statusLabel, BorderLayout.SOUTH);

        mainFrame.setLocationRelativeTo(null);
    }

    public void showEventDemo() {
        controlPanel.removeAll();

        for (int row = 0; row < ROW; row++) {
            for (int col = 0; col < COLUMN; col++) {
                LetterBox box = new LetterBox(1, 1);
                box.setBorder(new RoundedBorder(15, "#4c4347", 6)); // branca fina
                box.setOpaque(false); // fundo transparente
                box.setHorizontalAlignment(JTextField.CENTER);
                box.setForeground(Color.WHITE);
                box.setFont(new Font("Arial", Font.BOLD, 28));
                box.setDisabledTextColor(Color.WHITE);

                box.setOpaque(false); // necessário para mostrar o background color
                if(row != currentRow){
                    //box.setEnabled(false);
                    box.setOpaque(true); // necessário para mostrar o background color
                    box.setBackground(Color.decode("#615458"));
                    box.setBorder(new RoundedBorder(15, "#615458", 6)); // branca fina
                }
                final int r = row, c = col;
                box.addKeyListener(new KeyAdapter() {
                    @Override
                    public void keyPressed(KeyEvent e) {
                        handleKeyPress(e, r, c);
                    }
                });

                letterBoxes[row][col] = box;
                controlPanel.add(box);
            }
        }

        letterBoxes[0][0].requestFocusInWindow();
        controlPanel.revalidate();
        controlPanel.repaint();
        mainFrame.setVisible(true);
    }

    private void handleKeyPress(KeyEvent e, int row, int col) {
        if (row != currentRow) return;

        int keyCode = e.getKeyCode();
        LetterBox currentBox = letterBoxes[currentRow][currentCol];

        if (keyCode >= KeyEvent.VK_A && keyCode <= KeyEvent.VK_Z) {
            char c = e.getKeyChar();
            currentBox.setText(String.valueOf(c).toUpperCase());
            moveToNextColumn();
        }
        else if (keyCode == KeyEvent.VK_BACK_SPACE) {
            if (currentBox.getText().isEmpty() && currentCol > 0) {
                moveToPreviousColumn();
            }
            letterBoxes[currentRow][currentCol].setText("");
        }
        else if (keyCode == KeyEvent.VK_ENTER && currentCol == 4) {
            submitGuess();
        }
        else if (keyCode == KeyEvent.VK_RIGHT && currentCol < 4) {
            moveToNextColumn();
        }
        else if (keyCode == KeyEvent.VK_LEFT && currentCol > 0) {
            moveToPreviousColumn();
        }
    }

    private void moveToNextColumn() {
        if (currentCol < 4) {
            currentCol++;
            letterBoxes[currentRow][currentCol].requestFocus();
        }
    }

    private void moveToPreviousColumn() {
        if (currentCol > 0) {
            currentCol--;
            letterBoxes[currentRow][currentCol].requestFocus();
        }
    }

    private void submitGuess() {
        StringBuilder guess = new StringBuilder();
        for (int col = 0; col < COLUMN; col++) {
            guess.append(letterBoxes[currentRow][col].getText());
        }

        if (guess.length() == COLUMN) {
            boolean isValid = jogo.validateGuess(guess.toString());
            guessProcessing();
            System.out.println(isValid);
            if (isValid) {

                if (currentRow < ROW-1 && jogo.getRightQuantityWord() != jogo.getWordLength()) {
                    currentRow++;
                    currentCol = 0;
                    for (int c = 0; c < COLUMN; c++) {
                        letterBoxes[currentRow][c].setOpaque(false);
                        letterBoxes[currentRow][c].setBackground(null);
                        letterBoxes[currentRow][c].setBorder(new RoundedBorder(15, "#4c4347", 6));
                        letterBoxes[currentRow][c].setEnabled(true);
                        letterBoxes[currentRow][c].setEditable(true);

                    }
                    letterBoxes[currentRow][currentCol].requestFocus();
                } else {
                    statusLabel.setText("Fim do jogo!");

                }
            } else {
                statusLabel.setText("Palavra inválida!");
            }
        }
        controlPanel.repaint();
    }

    public void guessProcessing(){
        char resultado[] = jogo.getResultado();
        for (int i = 0; i < COLUMN; i++) {
            LetterBox box = letterBoxes[currentRow][i];
            box.setEditable(false);
            box.setEnabled(false);
            box.setOpaque(false);

            switch (resultado[i]) {
                case 'G' :
                    box.setBackground(Color.decode("#3aa394"));
                    box.setBorder(new RoundedBorder(15, "#3aa394", 6)); // branca fina
                    break;
                case 'Y' :
                    box.setBackground(Color.decode("#d3ad69"));
                    box.setBorder(new RoundedBorder(15, "#d3ad69", 6)); // branca fina

                    break;
                case 'B' :
                    box.setBackground(Color.decode("#312a2c"));
                    box.setBorder(new RoundedBorder(15, "#312a2c", 6)); // branca fina
                    break;
            }
        }
        controlPanel.repaint();
    }
}