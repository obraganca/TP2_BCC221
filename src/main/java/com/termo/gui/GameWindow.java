package com.termo.gui;

import java.awt.*;
import java.awt.event.*;
import java.util.HashMap;
import java.util.Map;
import javax.swing.*;

import com.termo.controller.Game;
import com.termo.gui.components.LetterBox;
import com.termo.gui.components.RoundedBorder;

public class GameWindow {
    private JFrame mainFrame;
    private JLabel headerLabel;
    private JLabel warnLabel;
    private JPanel headerPanel;
    private JLabel statusLabel;
    private JPanel controlPanel;
    private JPanel keyboardPanel;
    private JScrollPane gameScrollPane; // Para telas pequenas

    public static final int COLUMN = 5;
    public static final int ROW = 6;
    private LetterBox[][] letterBoxes;
    private int currentRow = 0;
    private int currentCol = 0;
    private Game jogo;

    private final Map<Character, JButton> keyButtons = new HashMap<>();
    private StatsOverlay statsOverlay;

    // Configurações responsivas
    private boolean isSmallScreen = false;
    private boolean isVerySmallScreen = false;

    public GameWindow() {
        jogo = new Game();
        prepareGUI();
        showEventDemo();
    }

    private void prepareGUI() {
        letterBoxes = new LetterBox[ROW][COLUMN];
        mainFrame = new JFrame("TERMO");
        mainFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        // Ícone
        ImageIcon termoIcon = null;
        java.net.URL imgURL = GameWindow.class.getResource("/public/icon.png");
        if (imgURL != null) {
            termoIcon = new ImageIcon(imgURL);
            mainFrame.setIconImage(termoIcon.getImage());
        }

        mainFrame.getContentPane().setBackground(Color.decode("#6e5c62"));

        // Configuração inicial da janela
        setupInitialWindowSize();

        mainFrame.setLayout(new BorderLayout(10, 10));

        // Criar componentes responsivos
        createHeaderPanel();
        createGamePanel();
        createKeyboardPanel();

        // Layout principal
        layoutComponents();

        // Listener para redimensionamento
        mainFrame.addComponentListener(new ComponentAdapter() {
            @Override
            public void componentResized(ComponentEvent e) {
                updateResponsiveLayout();
            }
        });

        mainFrame.setLocationRelativeTo(null);
    }

    private void setupInitialWindowSize() {
        Toolkit toolkit = Toolkit.getDefaultToolkit();
        Dimension screenSize = toolkit.getScreenSize();

        // Detecta se é dispositivo pequeno
        isVerySmallScreen = screenSize.width < 600 || screenSize.height < 500;
        isSmallScreen = screenSize.width < 900 || screenSize.height < 700;

        if (isVerySmallScreen) {
            mainFrame.setSize((int)(screenSize.width * 0.95), (int)(screenSize.height * 0.95));
            mainFrame.setExtendedState(JFrame.MAXIMIZED_BOTH);
        } else if (isSmallScreen) {
            mainFrame.setSize((int)(screenSize.width * 0.85), (int)(screenSize.height * 0.85));
        } else {
            mainFrame.setSize(Math.min(1200, screenSize.width), Math.min(900, screenSize.height));
        }
    }

    private void createHeaderPanel() {
        headerPanel = new JPanel(new BorderLayout());
        headerPanel.setOpaque(false);

        // Top row com botões e título
        JPanel topRow = new JPanel(new FlowLayout(FlowLayout.CENTER, 6, 6));
        topRow.setOpaque(false);
        topRow.setBorder(BorderFactory.createEmptyBorder(4, 0, 4, 0));

        // Botão estatísticas
        JButton leftBtn = makeHeaderButton("📊");
        leftBtn.setToolTipText("Estatísticas");
        updateButtonSize(leftBtn);
        leftBtn.addActionListener(e -> {
            if (statsOverlay == null) statsOverlay = new StatsOverlay(mainFrame);
            statsOverlay.show(false);
        });

        // Título responsivo
        headerLabel = new JLabel("TERMO", JLabel.CENTER);
        headerLabel.setForeground(Color.WHITE);
        updateHeaderFont();
        headerLabel.setBorder(BorderFactory.createEmptyBorder(0, 6, 0, 6));

        // Botão configurações
        JButton rightBtn = makeHeaderButton("⚙");
        rightBtn.setToolTipText("Configurações");
        updateButtonSize(rightBtn);

        topRow.add(leftBtn);
        topRow.add(headerLabel);
        topRow.add(rightBtn);

        headerPanel.add(topRow, BorderLayout.NORTH);

        // Warning panel
        JPanel warnPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 0, 0));
        warnPanel.setOpaque(false);

        warnLabel = new JLabel("", JLabel.CENTER);
        warnLabel.setForeground(Color.WHITE);
        updateWarnLabelFont();
        warnLabel.setOpaque(false);
        warnLabel.setBackground(Color.decode("#009AFE"));
        warnLabel.setBorder(BorderFactory.createEmptyBorder(5, 15, 5, 15));

        warnPanel.add(warnLabel);
        headerPanel.add(warnPanel, BorderLayout.SOUTH);
    }

    private void createGamePanel() {
        // Grid do jogo
        controlPanel = new JPanel(new GridLayout(ROW, COLUMN, 5, 5));
        controlPanel.setOpaque(false);

        // Wrapper para centralizar o grid
        JPanel centerWrapper = new JPanel(new GridBagLayout());
        centerWrapper.setOpaque(false);
        centerWrapper.add(controlPanel);

        // ScrollPane para telas muito pequenas
        if (isVerySmallScreen) {
            gameScrollPane = new JScrollPane(centerWrapper);
            gameScrollPane.setOpaque(false);
            gameScrollPane.getViewport().setOpaque(false);
            gameScrollPane.setBorder(null);
            gameScrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
            gameScrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        }

        updateGamePanelSize();
    }

    private void createKeyboardPanel() {
        keyboardPanel = createVirtualKeyboard();

        // Status label
        statusLabel = new JLabel("", JLabel.CENTER);
        statusLabel.setSize(350, 100);
        statusLabel.setForeground(Color.WHITE);
    }

    private void layoutComponents() {
        mainFrame.add(headerPanel, BorderLayout.NORTH);

        if (isVerySmallScreen && gameScrollPane != null) {
            mainFrame.add(gameScrollPane, BorderLayout.CENTER);
        } else {
            JPanel centerWrapper = new JPanel(new GridBagLayout());
            centerWrapper.setOpaque(false);
            centerWrapper.add(controlPanel);
            mainFrame.add(centerWrapper, BorderLayout.CENTER);
        }

        // South panel com teclado e status
        JPanel southPanel = new JPanel(new BorderLayout());
        southPanel.setOpaque(false);
        southPanel.add(keyboardPanel, BorderLayout.CENTER);
        southPanel.add(statusLabel, BorderLayout.SOUTH);
        mainFrame.add(southPanel, BorderLayout.SOUTH);
    }

    private JPanel createVirtualKeyboard() {
        keyButtons.clear();

        JPanel panel = new JPanel();
        panel.setLayout(new GridBagLayout());
        panel.setOpaque(false);

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(4, 4, 4, 4); // Espaçamento menor em telas pequenas
        gbc.fill = GridBagConstraints.HORIZONTAL;

        String row1 = "QWERTYUIOP";
        String row2 = "ASDFGHJKL";
        String row3[] = {"ENTER", "ZXCVBNM", "BACK"};

        // Row 1
        JPanel r1 = new JPanel(new FlowLayout(FlowLayout.CENTER, 4, 0));
        r1.setOpaque(false);
        for (char ch : row1.toCharArray()) {
            JButton b = makeKeyButton(String.valueOf(ch));
            r1.add(b);
            keyButtons.put(ch, b);
        }
        gbc.gridy = 0;
        panel.add(r1, gbc);

        // Row 2
        JPanel r2 = new JPanel(new FlowLayout(FlowLayout.CENTER, 4, 0));
        r2.setOpaque(false);
        for (char ch : row2.toCharArray()) {
            JButton b = makeKeyButton(String.valueOf(ch));
            r2.add(b);
            keyButtons.put(ch, b);
        }
        gbc.gridy = 1;
        panel.add(r2, gbc);

        // Row 3
        JPanel r3 = new JPanel(new FlowLayout(FlowLayout.CENTER, 4, 0));
        r3.setOpaque(false);

        JButton enterBtn = makeKeyButton("ENTER");
        updateSpecialKeySize(enterBtn, true);
        r3.add(enterBtn);

        for (char ch : row3[1].toCharArray()) {
            JButton b = makeKeyButton(String.valueOf(ch));
            r3.add(b);
            keyButtons.put(ch, b);
        }

        JButton backBtn = makeKeyButton("BACK");
        updateSpecialKeySize(backBtn, false);
        backBtn.setText("←");
        backBtn.setToolTipText("Backspace");
        r3.add(backBtn);

        gbc.gridy = 2;
        panel.add(r3, gbc);

        return panel;
    }

    private JButton makeKeyButton(String label) {
        JButton btn = new JButton(label);
        btn.setFocusable(false);
        btn.setFont(new Font("Arial", Font.BOLD, getKeyboardFontSize()));
        updateKeySize(btn);
        btn.setBackground(Color.decode("#4c4347"));
        btn.setForeground(Color.WHITE);
        btn.setBorder(new RoundedBorder(8, "#4c4347", 2));
        btn.setOpaque(true);

        btn.addActionListener(e -> {
            String key = label;
            if ("←".equals(label)) key = "BACK";
            handleVirtualKey(key);
        });

        return btn;
    }

    private JButton makeHeaderButton(String label) {
        JButton b = new JButton(label);
        b.setFocusable(false);
        b.setFont(new Font("Arial", Font.BOLD, 16));
        b.setBackground(Color.decode("#3d3a3b"));
        b.setForeground(Color.WHITE);
        b.setBorder(new RoundedBorder(8, "#3d3a3b", 2));
        b.setOpaque(true);
        return b;
    }

    // Métodos para atualização responsiva
    private void updateResponsiveLayout() {
        Dimension currentSize = mainFrame.getSize();

        boolean wasSmallScreen = isSmallScreen;
        boolean wasVerySmallScreen = isVerySmallScreen;

        isVerySmallScreen = currentSize.width < 600 || currentSize.height < 500;
        isSmallScreen = currentSize.width < 900 || currentSize.height < 700;

        // Se mudou de categoria de tela, recria layout
        if (wasSmallScreen != isSmallScreen || wasVerySmallScreen != isVerySmallScreen) {
            SwingUtilities.invokeLater(() -> recreateLayout());
        } else {
            // Apenas atualiza tamanhos
            updateComponentSizes();
        }
    }


    private void recreateLayout() {
        // Store current letter boxes state before recreating
        LetterBox[][] oldLetterBoxes = new LetterBox[ROW][COLUMN];
        for (int i = 0; i < ROW; i++) {
            System.arraycopy(letterBoxes[i], 0, oldLetterBoxes[i], 0, COLUMN);
        }

        mainFrame.getContentPane().removeAll();
        createGamePanel();
        createKeyboardPanel();

        // Restore the letter boxes with their current state
        restoreLetterBoxes(oldLetterBoxes);

        layoutComponents();
        updateComponentSizes();
        mainFrame.revalidate();
        mainFrame.repaint();
    }

    private void restoreLetterBoxes(LetterBox[][] oldBoxes) {
        controlPanel.removeAll();

        int boxSize = isVerySmallScreen ? 40 : (isSmallScreen ? 55 : 70);
        int fontSize = isVerySmallScreen ? 18 : (isSmallScreen ? 24 : 28);

        for (int row = 0; row < ROW; row++) {
            for (int col = 0; col < COLUMN; col++) {
                LetterBox oldBox = oldBoxes[row][col];

                // Create new box with same properties as old one
                LetterBox newBox = new LetterBox(1, 1);

                // Copy all properties from old box
                newBox.setText(oldBox.getText());
                newBox.setBackground(oldBox.getBackground());
                newBox.setOpaque(oldBox.isOpaque());
                newBox.setEnabled(oldBox.isEnabled());
                newBox.setEditable(oldBox.isEditable());

                // Apply current responsive sizing
                newBox.setPreferredSize(new Dimension(boxSize, boxSize));
                newBox.setMaximumSize(new Dimension(boxSize, boxSize));
                newBox.setMinimumSize(new Dimension(boxSize, boxSize));

                // Apply styling
                newBox.setHorizontalAlignment(JTextField.CENTER);
                newBox.setForeground(Color.WHITE);
                newBox.setFont(new Font("Arial", Font.BOLD, fontSize));
                newBox.setDisabledTextColor(Color.WHITE);

                // Copy border (preserve colors for completed rows)
                newBox.setBorder(oldBox.getBorder());

                // Add key listener
                final int r = row, c = col;
                newBox.addKeyListener(new KeyAdapter() {
                    @Override
                    public void keyPressed(KeyEvent e) {
                        handleKeyPress(e, r, c);
                    }
                });

                letterBoxes[row][col] = newBox;
                controlPanel.add(newBox);
            }
        }

        // Restore focus if needed
        if (currentRow < ROW && currentCol < COLUMN && letterBoxes[currentRow][currentCol].isEnabled()) {
            letterBoxes[currentRow][currentCol].requestFocusInWindow();
        }

        controlPanel.revalidate();
        controlPanel.repaint();
    }


    private void updateComponentSizes() {
        updateHeaderFont();
        updateWarnLabelFont();
        updateGamePanelSize();
        updateKeyboardSizes();

        // Atualiza botões do header
        Component[] headerComponents = headerPanel.getComponents();
        for (Component comp : headerComponents) {
            if (comp instanceof JPanel) {
                updateHeaderButtonsInPanel((JPanel) comp);
            }
        }
    }

    private void updateHeaderButtonsInPanel(JPanel panel) {
        Component[] components = panel.getComponents();
        for (Component comp : components) {
            if (comp instanceof JButton) {
                updateButtonSize((JButton) comp);
            } else if (comp instanceof JPanel) {
                updateHeaderButtonsInPanel((JPanel) comp);
            }
        }
    }

    private void updateHeaderFont() {
        if (headerLabel != null) {
            int fontSize = isVerySmallScreen ? 20 : (isSmallScreen ? 26 : 32);
            headerLabel.setFont(new Font("Arial", Font.BOLD, fontSize));
        }
    }

    private void updateWarnLabelFont() {
        if (warnLabel != null) {
            int fontSize = isVerySmallScreen ? 12 : (isSmallScreen ? 15 : 18);
            warnLabel.setFont(new Font("Arial", Font.BOLD, fontSize));
        }
    }

    private void updateButtonSize(JButton btn) {
        int size = isVerySmallScreen ? 28 : (isSmallScreen ? 32 : 36);
        btn.setPreferredSize(new Dimension(size, size));
    }

    private void updateGamePanelSize() {
        if (controlPanel != null) {
            int boxSize = isVerySmallScreen ? 40 : (isSmallScreen ? 55 : 70);
            int gap = isVerySmallScreen ? 3 : 5;

            controlPanel.setLayout(new GridLayout(ROW, COLUMN, gap, gap));

            int totalWidth = (boxSize * COLUMN) + (gap * (COLUMN - 1));
            int totalHeight = (boxSize * ROW) + (gap * (ROW - 1));

            controlPanel.setPreferredSize(new Dimension(totalWidth, totalHeight));
            controlPanel.setMaximumSize(new Dimension(totalWidth, totalHeight));
            controlPanel.setMinimumSize(new Dimension(totalWidth, totalHeight));
        }
    }

    private void updateKeyboardSizes() {
        // Atualiza tamanho das teclas
        for (JButton btn : keyButtons.values()) {
            updateKeySize(btn);
            btn.setFont(new Font("Arial", Font.BOLD, getKeyboardFontSize()));
        }

        // Atualiza teclas especiais se existir o painel
        if (keyboardPanel != null) {
            updateSpecialKeysInPanel(keyboardPanel);
        }
    }

    private void updateSpecialKeysInPanel(Container container) {
        Component[] components = container.getComponents();
        for (Component comp : components) {
            if (comp instanceof JButton) {
                JButton btn = (JButton) comp;
                String text = btn.getText();
                if ("ENTER".equals(text)) {
                    updateSpecialKeySize(btn, true);
                } else if ("←".equals(text)) {
                    updateSpecialKeySize(btn, false);
                }
            } else if (comp instanceof Container) {
                updateSpecialKeysInPanel((Container) comp);
            }
        }
    }

    private void updateKeySize(JButton btn) {
        int size = isVerySmallScreen ? 35 : (isSmallScreen ? 50 : 70);
        btn.setPreferredSize(new Dimension(size, size));
    }

    private void updateSpecialKeySize(JButton btn, boolean isEnter) {
        int height = isVerySmallScreen ? 35 : (isSmallScreen ? 40 : 48);
        int width = isVerySmallScreen ? 60 : (isSmallScreen ? 75 : 90);
        btn.setPreferredSize(new Dimension(width, height));
    }

    private int getKeyboardFontSize() {
        return isVerySmallScreen ? 12 : (isSmallScreen ? 15 : 18);
    }

    private void handleVirtualKey(String key) {
        if (key.length() == 1 && Character.isLetter(key.charAt(0))) {
            String c = key.toUpperCase();
            LetterBox currentBox = letterBoxes[currentRow][currentCol];
            currentBox.setText(c);
            if (currentCol < COLUMN - 1) {
                currentCol++;
                letterBoxes[currentRow][currentCol].requestFocus();
            } else {
                letterBoxes[currentRow][currentCol].requestFocus();
            }
        } else if ("ENTER".equalsIgnoreCase(key)) {
            submitGuess();
        } else if ("BACK".equalsIgnoreCase(key) || "DEL".equalsIgnoreCase(key)) {
            LetterBox currentBox = letterBoxes[currentRow][currentCol];
            if (currentBox.getText().isEmpty() && currentCol > 0) {
                currentCol--;
                letterBoxes[currentRow][currentCol].setText("");
                letterBoxes[currentRow][currentCol].requestFocus();
            } else {
                currentBox.setText("");
                letterBoxes[currentRow][currentCol].requestFocus();
            }
            setWarnMessage("");
        }
    }

    public void showEventDemo() {
        controlPanel.removeAll();

        int boxSize = isVerySmallScreen ? 40 : (isSmallScreen ? 55 : 70);
        int fontSize = isVerySmallScreen ? 18 : (isSmallScreen ? 24 : 28);

        for (int row = 0; row < ROW; row++) {
            for (int col = 0; col < COLUMN; col++) {
                LetterBox box = new LetterBox(1, 1);
                box.setBorder(new RoundedBorder(15, "#4c4347", 6));
                box.setOpaque(false);
                box.setHorizontalAlignment(JTextField.CENTER);
                box.setForeground(Color.WHITE);
                box.setFont(new Font("Arial", Font.BOLD, fontSize));
                box.setDisabledTextColor(Color.WHITE);

                // Tamanho responsivo
                box.setPreferredSize(new Dimension(boxSize, boxSize));
                box.setMaximumSize(new Dimension(boxSize, boxSize));
                box.setMinimumSize(new Dimension(boxSize, boxSize));

                if (row != currentRow) {
                    box.setOpaque(true);
                    box.setBackground(Color.decode("#615458"));
                    box.setBorder(new RoundedBorder(15, "#615458", 6));
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
        statsOverlay = new StatsOverlay(mainFrame);
    }

    private void handleKeyPress(KeyEvent e, int row, int col) {
        if (row != currentRow) return;

        int keyCode = e.getKeyCode();
        LetterBox currentBox = letterBoxes[currentRow][currentCol];

        if (keyCode >= KeyEvent.VK_A && keyCode <= KeyEvent.VK_Z) {
            char c = e.getKeyChar();
            currentBox.setText(String.valueOf(c).toUpperCase());
            moveToNextColumn();
        } else if (keyCode == KeyEvent.VK_BACK_SPACE) {
            if (currentBox.getText().isEmpty() && currentCol > 0) {
                moveToPreviousColumn();
            }
            letterBoxes[currentRow][currentCol].setText("");
            setWarnMessage("");
        } else if (keyCode == KeyEvent.VK_ENTER && currentCol == 4) {
            submitGuess();
        } else if (keyCode == KeyEvent.VK_RIGHT && currentCol < 4) {
            moveToNextColumn();
        } else if (keyCode == KeyEvent.VK_LEFT && currentCol > 0) {
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
            if (isValid) {
                guessProcessing();
                if (currentRow < ROW - 1 && jogo.getRightQuantityWord() != jogo.getWordLength()) {
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
                    controlPanel.requestFocus();
                    setWarnMessage("Fim do jogo !");

                    boolean won = jogo.getRightQuantityWord() == jogo.getWordLength();
                    statsOverlay.recordGame(won, won ? currentRow + 1 : 0);
                    statsOverlay.show(won);
                }
            } else {
                setWarnMessage("essa palavra não é aceita");
            }
        }
        controlPanel.repaint();
    }

    public void guessProcessing() {
        char resultado[] = jogo.getResultado();

        StringBuilder guessSb = new StringBuilder();
        for (int c = 0; c < COLUMN; c++) {
            guessSb.append(letterBoxes[currentRow][c].getText());
        }
        String guess = guessSb.toString().toUpperCase();

        for (int i = 0; i < COLUMN; i++) {
            LetterBox box = letterBoxes[currentRow][i];
            box.setEditable(false);
            box.setEnabled(false);
            box.setOpaque(true);

            switch (resultado[i]) {
                case 'G' :
                    box.setBackground(Color.decode("#3aa394"));
                    box.setBorder(new RoundedBorder(15, "#3aa394", 6));
                    break;
                case 'Y' :
                    box.setBackground(Color.decode("#d3ad69"));
                    box.setBorder(new RoundedBorder(15, "#d3ad69", 6));
                    break;
                case 'B' :
                    box.setBackground(Color.decode("#312a2c"));
                    box.setBorder(new RoundedBorder(15, "#312a2c", 6));
                    break;
            }
        }

        updateKeyboardColors(guess, resultado);
        controlPanel.repaint();
    }

    private void updateKeyboardColors(String guess, char[] resultado) {
        if (guess == null || resultado == null) return;

        Color green = Color.decode("#3aa394");
        Color yellow = Color.decode("#d3ad69");
        Color disabledBg = Color.decode("#2f2a2c");
        Color defaultBg = Color.decode("#4c4347");
        Color white = Color.WHITE;

        for (int i = 0; i < Math.min(guess.length(), resultado.length); i++) {
            char ch = guess.charAt(i);
            if (!Character.isLetter(ch)) continue;
            ch = Character.toUpperCase(ch);
            JButton keyBtn = keyButtons.get(ch);
            if (keyBtn == null) continue;

            Color currentBg = keyBtn.getBackground();

            switch (resultado[i]) {
                case 'G':
                    keyBtn.setBackground(green);
                    keyBtn.setForeground(white);
                    keyBtn.setBorder(new RoundedBorder(8, "#3aa394", 2));
                    break;
                case 'Y':
                    if (!colorsEqual(currentBg, green)) {
                        keyBtn.setBackground(yellow);
                        keyBtn.setForeground(white);
                        keyBtn.setBorder(new RoundedBorder(8, "#d3ad69", 2));
                    }
                    break;
                case 'B':
                    if (!colorsEqual(currentBg, green) && !colorsEqual(currentBg, yellow)) {
                        keyBtn.setBackground(disabledBg);
                        keyBtn.setForeground(Color.decode("#bdb6b6"));
                        keyBtn.setBorder(new RoundedBorder(8, "#2f2a2c", 2));
                    }
                    break;
            }
        }
    }

    private boolean colorsEqual(Color a, Color b) {
        if (a == null && b == null) return true;
        if (a == null || b == null) return false;
        return a.getRGB() == b.getRGB();
    }

    private void setWarnMessage(String message) {
        warnLabel.setText(message);
        boolean hasText = message != null && !message.isEmpty();
        warnLabel.setOpaque(hasText);
        warnLabel.revalidate();
        warnLabel.repaint();
    }
}