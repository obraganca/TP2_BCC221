package com.termo.gui;

import java.awt.*;
import java.awt.event.*;
import java.util.HashMap;
import java.util.Map;
import javax.swing.*;

import com.termo.controller.*;
import com.termo.gui.components.LetterBox;
import com.termo.gui.components.RoundedBorder;

public class GameWindow {
    private Login sistemaLogin;
    private JFrame mainFrame;
    private JLabel headerLabel;
    private JLabel warnLabel;
    private JPanel headerPanel;
    private JLabel statusLabel;
    private JPanel southPanel;

    private JPanel controlPanel;
    private JScrollPane gameScrollPane; // Para telas pequenas

    private VirtualKeyboard virtualKeyboard;

    public static final int COLUMN = 5;
    public static final int ROW = 6;
    private LetterBox[][] letterBoxes;
    private int currentRow = 0;
    private int currentCol = 0;
    private Game jogo;
    private Usuario usuario;

    private final Map<Character, JButton> keyButtons = new HashMap<>();

    // Configurações responsivas
    private boolean isSmallScreen = false;
    private boolean isVerySmallScreen = false;

    public GameWindow() {
        this.sistemaLogin = new Login();
        sistemaLogin.debugUsuarios();
        jogo = new Game();
        showLoginDialog();
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
    private void showLoginDialog() {
        JTextField usuarioField = new JTextField();
        JPasswordField senhaField = new JPasswordField();

        Object[] message = {
                "Usuário:", usuarioField,
                "Senha:", senhaField
        };

        int option = JOptionPane.showConfirmDialog(
                null, message, "Login ou Cadastro", JOptionPane.OK_CANCEL_OPTION
        );

        if (option == JOptionPane.OK_OPTION) {
            String nome = usuarioField.getText();
            String senha = new String(senhaField.getPassword());

            boolean autenticado = sistemaLogin.loginOuCadastrar(nome, senha);

            if (autenticado) {
                usuario = sistemaLogin.getUsuario(nome);
                System.out.println("Usuário logado Hash: " + System.identityHashCode(usuario));
                JOptionPane.showMessageDialog(null, "Bem-vindo, " + nome + "!");
                prepareGUI();
                showEventDemo();
            } else {
                JOptionPane.showMessageDialog(null, "Senha incorreta!", "Erro", JOptionPane.ERROR_MESSAGE);
                showLoginDialog(); // Tenta novamente
            }
        } else {
            System.exit(0); // Sai do jogo se cancelar
        }
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
            usuario.getPerfil().show(false, mainFrame);
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

        // Menu de configurações (Resetar / Sair)
        JPopupMenu settingsMenu = new JPopupMenu();
        JMenuItem resetItem = new JMenuItem("Resetar jogo");
        JMenuItem exitItem = new JMenuItem("Sair");

        resetItem.addActionListener(ev -> {
            int confirm = JOptionPane.showConfirmDialog(mainFrame,
                    "Deseja realmente resetar o jogo? A palavra e o estado serão reiniciados.",
                    "Confirmar reset", JOptionPane.YES_NO_OPTION);
            if (confirm == JOptionPane.YES_OPTION) {
                resetGame();
            }
        });

        exitItem.addActionListener(ev -> {
            int confirm = JOptionPane.showConfirmDialog(mainFrame,
                    "Deseja sair para a tela de login? O jogo atual será perdido.",
                    "Confirmar saída", JOptionPane.YES_NO_OPTION);
            if (confirm == JOptionPane.YES_OPTION) {
                exitToLogin();
            }
        });

        settingsMenu.add(resetItem);
        settingsMenu.add(exitItem);

        rightBtn.addActionListener(e -> {
            // mostra o menu logo abaixo do botão
            settingsMenu.show(rightBtn, 0, rightBtn.getHeight());
        });

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

    // Substitua o método createKeyboardPanel por:
    private void createKeyboardPanel() {
        virtualKeyboard = new VirtualKeyboard(this::handleVirtualKey, isVerySmallScreen, isSmallScreen, this.hasWon());
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

        // South panel com teclado e status (agora é campo para permitir update)
        southPanel = new JPanel(new BorderLayout());
        southPanel.setOpaque(false);
        southPanel.add(virtualKeyboard, BorderLayout.CENTER);
        southPanel.add(statusLabel, BorderLayout.SOUTH);
        mainFrame.add(southPanel, BorderLayout.SOUTH);
    }

    private void resetGame() {
        // cria novo jogo (reseta palavra/estado)
        jogo = new Game();

        // reseta índices
        currentRow = 0;
        currentCol = 0;

        // limpa warn/status
        setWarnMessage("");
        if (statusLabel != null) statusLabel.setText("");

        // recria o teclado virtual (caso acumule cores)
        virtualKeyboard = new VirtualKeyboard(this::handleVirtualKey, isVerySmallScreen, isSmallScreen, this.hasWon());

        // substitui o teclado no painel sul
        if (southPanel != null) {
            southPanel.removeAll();
            southPanel.add(virtualKeyboard, BorderLayout.CENTER);
            southPanel.add(statusLabel, BorderLayout.SOUTH);
            southPanel.revalidate();
            southPanel.repaint();
        }

        // limpa e reconfigura as letterBoxes para o estado inicial
        if (letterBoxes != null) {
            for (int r = 0; r < ROW; r++) {
                for (int c = 0; c < COLUMN; c++) {
                    LetterBox box = letterBoxes[r][c];
                    if (box == null) continue;
                    box.setText("");
                    box.setOpaque(r != currentRow); // linha atual fica transparente como antes
                    box.setBackground(r == currentRow ? null : Color.decode("#615458"));
                    box.setBorder(new RoundedBorder(15, "#4c4347", 6));
                    box.setEnabled(true);
                    box.setEditable(true);
                }
            }
        }

        SwingUtilities.invokeLater(() -> {
            if (letterBoxes != null && letterBoxes[0][0] != null) {
                letterBoxes[0][0].requestFocusInWindow();
            }
        });

        controlPanel.revalidate();
        controlPanel.repaint();
    }

    private void exitToLogin() {
        // Esconde/fecha a janela atual
        if (mainFrame != null) {
            mainFrame.dispose();
        }
        usuario = null;
        jogo = new Game();
        currentRow = 0;
        currentCol = 0;

        SwingUtilities.invokeLater(() -> showLoginDialog());
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

    // --- Adicione este método na classe GameWindow ---
    private KeyAdapter createLetterBoxKeyListener(final int row, final int col) {
        return new KeyAdapter() {
            @Override
            public void keyTyped(KeyEvent e) {
                // keyTyped entrega o caractere composto (acentos, ç, etc.)
                if (row != currentRow) return; // só permite digitar na linha atual

                char ch = e.getKeyChar();
                if (Character.isLetter(ch)) {
                    // Insere a letra (maiuscula) na caixa e avança
                    String s = String.valueOf(ch).toUpperCase();
                    LetterBox box = letterBoxes[row][col];
                    box.setText(s);

                    // atualiza currentCol se estivermos na mesma coluna
                    if (currentRow == row) {
                        // se digitou na última coluna, mantém lá; caso contrário, avança
                        if (currentCol == col && currentCol < COLUMN - 1) {
                            currentCol++;
                        } else {
                            currentCol = Math.min(COLUMN - 1, col + 1);
                        }
                        final int nextCol = currentCol;
                        // pede foco no próximo componente (no EDT)
                        SwingUtilities.invokeLater(() -> {
                            if (letterBoxes[row][nextCol] != null) {
                                letterBoxes[row][nextCol].requestFocusInWindow();
                            }
                        });
                    }
                    // consome o evento pra evitar duplicação
                    e.consume();
                }
            }

            @Override
            public void keyPressed(KeyEvent e) {
                if (row != currentRow) return;

                int keyCode = e.getKeyCode();
                LetterBox currentBox = letterBoxes[currentRow][currentCol];

                if (keyCode == KeyEvent.VK_BACK_SPACE) {
                    // comportamento: se a caixa atual está vazia, volta e apaga a anterior
                    if (currentBox.getText().isEmpty() && currentCol > 0) {
                        moveToPreviousColumn();
                        letterBoxes[currentRow][currentCol].setText("");
                        letterBoxes[currentRow][currentCol].requestFocusInWindow();
                    } else {
                        currentBox.setText("");
                        setWarnMessage("");
                    }
                    e.consume();
                } else if (keyCode == KeyEvent.VK_ENTER && currentCol == COLUMN - 1) {
                    submitGuess();
                    e.consume();
                } else if (keyCode == KeyEvent.VK_RIGHT && currentCol < COLUMN - 1) {
                    moveToNextColumn();
                    e.consume();
                } else if (keyCode == KeyEvent.VK_LEFT && currentCol > 0) {
                    moveToPreviousColumn();
                    e.consume();
                }
            }
        };
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
                newBox.addKeyListener(createLetterBoxKeyListener(r, c));



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

    // Atualize o método updateKeyboardSizes para:
    private void updateKeyboardSizes() {
        if (virtualKeyboard != null) {
            virtualKeyboard.updateSizes(isVerySmallScreen, isSmallScreen);
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

                box.addKeyListener(createLetterBoxKeyListener(r, c));

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
        char keyChar = e.getKeyChar();
        LetterBox currentBox = letterBoxes[currentRow][currentCol];

        // aceita letras incluindo acentuadas e ç
        if (Character.isLetter(keyChar)) {
            currentBox.setText(String.valueOf(keyChar).toUpperCase());
            moveToNextColumn();
        } else if (keyCode == KeyEvent.VK_BACK_SPACE) {
            if (currentBox.getText().isEmpty() && currentCol > 0) {
                moveToPreviousColumn();
            }
            letterBoxes[currentRow][currentCol].setText("");
            setWarnMessage("");
        } else if (keyCode == KeyEvent.VK_ENTER && currentCol == COLUMN - 1) {
            submitGuess();
        } else if (keyCode == KeyEvent.VK_RIGHT && currentCol < COLUMN - 1) {
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

                    if (this.hasWon()){
                        usuario.getPerfil().registrarVitoria(currentRow+1);
                        virtualKeyboard.setWon(this.hasWon());
                        for (int c = 0; c < COLUMN; c++) {
                            for(int r = 0; r < ROW; r++){
                                letterBoxes[r][c].setEnabled(false);
                                letterBoxes[r][c].setEditable(false);
                            }
                        }
                    }
                    if (!this.hasWon()) usuario.getPerfil().registrarDerrota(currentRow+1);
                    System.out.println("=== ANTES DE MOSTRAR ESTATÍSTICAS ===");
                    System.out.println("HashCode do usuário: " + System.identityHashCode(usuario));
                    System.out.println("HashCode do perfil: " + System.identityHashCode(usuario.getPerfil()));
                    System.out.println("Dados: " + usuario.getPerfil().toString());

                    usuario.getPerfil().show(this.hasWon(), mainFrame);

                }
            } else {
                setWarnMessage("essa palavra não é aceita");
            }
        }
        controlPanel.repaint();
    }
    public boolean hasWon(){
        return jogo.getRightQuantityWord() == jogo.getWordLength();
    }

    public void guessProcessing() {
        char resultado[] = jogo.getResultado();

        // Usa a palavra tentativa canonical (pode ter acentos) que foi guardada pelo Game
        String guess = jogo.getpalavratentativa();
        if (guess == null || guess.length() != COLUMN) {
            // fallback para ler das caixas (comportamento original)
            StringBuilder guessSb = new StringBuilder();
            for (int c = 0; c < COLUMN; c++) {
                guessSb.append(letterBoxes[currentRow][c].getText());
            }
            guess = guessSb.toString().toUpperCase();
        }

        // Substitui o texto das LetterBox pela forma canonical (assim mostra acentos/ç)
        for (int i = 0; i < COLUMN; i++) {
            String ch = String.valueOf(guess.charAt(i));
            letterBoxes[currentRow][i].setText(ch);
        }

        // Agora aplica cores e desabilita edição, como antes
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


    // Atualize o método updateKeyboardColors para delegar para o VirtualKeyboard:
    private void updateKeyboardColors(String guess, char[] resultado) {
        virtualKeyboard.updateKeyboardColors(guess, resultado);
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
