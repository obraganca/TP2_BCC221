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
    private JPanel keyboardPanel; // painel do teclado virtual
    public static final int COLUMN = 5;
    public static final int ROW = 6;
    private LetterBox[][] letterBoxes;
    private int currentRow = 0;
    private int currentCol = 0;
    private Game jogo;

    // mapa letra -> botão do teclado virtual
    private final Map<Character, JButton> keyButtons = new HashMap<>();
    private StatsOverlay statsOverlay;


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

        Toolkit toolkit = Toolkit.getDefaultToolkit();
        Dimension dim = toolkit.getScreenSize();
        mainFrame.setSize(dim.width, dim.height);

        mainFrame.setLayout(new BorderLayout(10, 10));

        headerLabel = new JLabel("TERMO", JLabel.CENTER);
        headerLabel.setForeground(Color.WHITE);
        headerLabel.setFont(new Font("Arial", Font.BOLD, 32));

        statusLabel = new JLabel("", JLabel.CENTER);
        statusLabel.setSize(350, 100);
        statusLabel.setForeground(Color.WHITE);

        controlPanel = new JPanel(new GridLayout(GameWindow.ROW, GameWindow.COLUMN, 5, 5));
        controlPanel.setPreferredSize(new Dimension(440, 500));
        controlPanel.setOpaque(false);


        headerPanel = new JPanel(new BorderLayout());
        headerPanel.setOpaque(false);

        // cria a linha superior que contém um botão à esquerda, o título (centro) e um botão à direita
        // linha superior: botão-esquerdo, título e botão-direito todos na mesma linha,
        // com gaps muito pequenos para ficarem próximos ao título
        JPanel topRow = new JPanel(new FlowLayout(FlowLayout.CENTER, 6, 6));
        topRow.setOpaque(false);
        topRow.setBorder(BorderFactory.createEmptyBorder(4, 0, 4, 0));

        // botão esquerdo (muito próximo ao título)
        JButton leftBtn = makeHeaderButton("📊");
        leftBtn.setToolTipText("Estatísticas");
        leftBtn.setPreferredSize(new Dimension(34, 34)); // ainda menor
        leftBtn.addActionListener(e -> {
            if (statsOverlay == null) statsOverlay = new StatsOverlay(mainFrame);
            statsOverlay.show(false);
        });

        // título
        headerLabel.setHorizontalAlignment(JLabel.CENTER);
        headerLabel.setFont(new Font("Arial", Font.BOLD, 32));
        headerLabel.setBorder(BorderFactory.createEmptyBorder(0, 6, 0, 6)); // pequeno padding interno

        // botão direito (muito próximo ao título)
        JButton rightBtn = makeHeaderButton("⚙");
        rightBtn.setToolTipText("Configurações");
        rightBtn.setPreferredSize(new Dimension(34, 34));

        // adiciona na ordem: left, title, right — tudo centralizado
        topRow.add(leftBtn);
        topRow.add(headerLabel);
        topRow.add(rightBtn);

        headerPanel.add(topRow, BorderLayout.NORTH);


        // warnLabel
        JPanel warnPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 0, 0));
        warnPanel.setOpaque(false);

        warnLabel = new JLabel("", JLabel.CENTER);
        warnLabel.setForeground(Color.WHITE);
        warnLabel.setFont(new Font("Arial", Font.BOLD, 18));
        warnLabel.setOpaque(false); // inicialmente sem cor
        warnLabel.setBackground(Color.decode("#009AFE"));
        // padding interno
        warnLabel.setBorder(BorderFactory.createEmptyBorder(5, 15, 5, 15));

        warnPanel.add(warnLabel);
        headerPanel.add(warnPanel, BorderLayout.SOUTH);

        // painel central que centraliza o grid
        JPanel centerWrapper = new JPanel();
        centerWrapper.setOpaque(false);
        centerWrapper.setLayout(new GridBagLayout());
        centerWrapper.add(controlPanel);

        // Cria teclado virtual
        keyboardPanel = createKeyboardPanel();
        keyboardPanel.setOpaque(false);

        // Organiza status + teclado em um painel ao sul
        JPanel southPanel = new JPanel(new BorderLayout());
        southPanel.setOpaque(false);
        southPanel.add(keyboardPanel, BorderLayout.CENTER);
        southPanel.add(statusLabel, BorderLayout.SOUTH);

        mainFrame.add(headerPanel, BorderLayout.NORTH);
        mainFrame.add(centerWrapper, BorderLayout.CENTER);
        mainFrame.add(southPanel, BorderLayout.SOUTH);

        mainFrame.setLocationRelativeTo(null);
    }

    /**
     * Cria o painel com o teclado virtual (3 linhas).
     */
    private JPanel createKeyboardPanel() {
        // limpa mapa (caso seja recriado)
        keyButtons.clear();

        JPanel panel = new JPanel();
        panel.setLayout(new GridBagLayout());
        panel.setOpaque(false);

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(6, 6, 6, 6);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.gridx = 0;
        gbc.gridy = 0;

        String row1 = "QWERTYUIOP";
        String row2 = "ASDFGHJKL";
        String row3[] = {"ENTER", "ZXCVBNM", "BACK"};

        // linha 1
        JPanel r1 = new JPanel(new FlowLayout(FlowLayout.CENTER, 6, 0));
        r1.setOpaque(false);
        for (char ch : row1.toCharArray()) {
            JButton b = makeKeyButton(String.valueOf(ch));
            r1.add(b);
            keyButtons.put(ch, b);
        }
        gbc.gridy = 0;
        panel.add(r1, gbc);

        // linha 2
        JPanel r2 = new JPanel(new FlowLayout(FlowLayout.CENTER, 6, 0));
        r2.setOpaque(false);
        for (char ch : row2.toCharArray()) {
            JButton b = makeKeyButton(String.valueOf(ch));
            r2.add(b);
            keyButtons.put(ch, b);
        }
        gbc.gridy = 1;
        panel.add(r2, gbc);

        // linha 3 (ENTER + letras + BACK)
        JPanel r3 = new JPanel(new FlowLayout(FlowLayout.CENTER, 6, 0));
        r3.setOpaque(false);

        JButton enterBtn = makeKeyButton("ENTER");
        enterBtn.setPreferredSize(new Dimension(90, 48));
        r3.add(enterBtn);

        for (char ch : row3[1].toCharArray()) {
            JButton b = makeKeyButton(String.valueOf(ch));
            r3.add(b);
            keyButtons.put(ch, b);
        }

        JButton backBtn = makeKeyButton("BACK");
        backBtn.setPreferredSize(new Dimension(90, 48));
        backBtn.setText("←"); // ícone simples para backspace
        backBtn.setToolTipText("Backspace");
        r3.add(backBtn);

        gbc.gridy = 2;
        panel.add(r3, gbc);

        return panel;
    }

    /**
     * Cria um botão estilizado para o teclado virtual.
     * Os botões não recebem foco para não atrapalhar a captura de teclado físico.
     */
    private JButton makeKeyButton(String label) {
        JButton btn = new JButton(label);
        btn.setFocusable(false);
        btn.setFont(new Font("Arial", Font.BOLD, 18));
        btn.setPreferredSize(new Dimension(70, 70));
        btn.setBackground(Color.decode("#4c4347")); // cor padrão do teclado
        btn.setForeground(Color.WHITE);
        btn.setBorder(new RoundedBorder(8, "#4c4347", 2));
        btn.setOpaque(true);

        // ação do clique -> encaminha para lógica de input
        btn.addActionListener(e -> {
            String key = label;
            // se o botão é o back visual que troquei o texto para "←", recupere a intenção
            if ("←".equals(label)) key = "BACK";
            handleVirtualKey(key);
        });

        return btn;
    }

    /**
     * Lógica chamada pelos botões virtuais.
     */
    private void handleVirtualKey(String key) {
        // letras
        if (key.length() == 1 && Character.isLetter(key.charAt(0))) {
            String c = key.toUpperCase();
            LetterBox currentBox = letterBoxes[currentRow][currentCol];
            currentBox.setText(c);
            // se não estiver na última coluna, avança
            if (currentCol < COLUMN - 1) {
                currentCol++;
                letterBoxes[currentRow][currentCol].requestFocus();
            } else {
                // deixa o foco no último
                letterBoxes[currentRow][currentCol].requestFocus();
            }
        }
        // enter
        else if ("ENTER".equalsIgnoreCase(key)) {
            submitGuess();
        }
        // backspace
        else if ("BACK".equalsIgnoreCase(key) || "DEL".equalsIgnoreCase(key)) {
            LetterBox currentBox = letterBoxes[currentRow][currentCol];
            if (currentBox.getText().isEmpty() && currentCol > 0) {
                // volta uma coluna e limpa
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
                if (row != currentRow) {
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
        statsOverlay = new StatsOverlay(mainFrame);

    }

    /**
     * Cria um botão pequeno para o header (ícone/texto).
     */
    private JButton makeHeaderButton(String label) {
        JButton b = new JButton(label);
        b.setFocusable(false);
        b.setFont(new Font("Arial", Font.BOLD, 16));
        // menor preferred size para ficar próximo ao título
        b.setPreferredSize(new Dimension(36, 36));
        b.setMargin(new Insets(2, 2, 2, 2));
        b.setBackground(Color.decode("#3d3a3b"));
        b.setForeground(Color.WHITE);
        b.setBorder(new RoundedBorder(8, "#3d3a3b", 2));
        b.setOpaque(true);
        return b;
    }



    /**
     * Cria (e retorna) o painel com os botões do header (estatísticas, config).
     * Agora não adiciona diretamente ao headerPanel — quem chama decide onde posicionar.
     */
    private JPanel createHeaderButtonsPanel() {
        JPanel headerButtons = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        headerButtons.setOpaque(false);

        // botão estatísticas (use um emoji/ícone ou texto)
        JButton statsBtn = makeHeaderButton("📊");
        statsBtn.setToolTipText("Estatísticas");
        statsBtn.addActionListener(e -> {
            if (statsOverlay == null) {
                statsOverlay = new StatsOverlay(mainFrame);
            }
            statsOverlay.show(false);
        });
        headerButtons.add(statsBtn);

        // botão de configurações
        JButton settingsBtn = makeHeaderButton("⚙");
        settingsBtn.setToolTipText("Configurações");
        headerButtons.add(settingsBtn);

        return headerButtons;
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

        // recolhe a guess atual
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

        // Atualiza o teclado visual com base no resultado da linha atual
        updateKeyboardColors(guess, resultado);

        controlPanel.repaint();
    }

    /**
     * Atualiza as cores do teclado virtual de acordo com o resultado.
     * Prioridade: G (verde) > Y (amarelo) > B (desativado visual).
     */
    private void updateKeyboardColors(String guess, char[] resultado) {
        if (guess == null || resultado == null) return;
        // cores usadas
        Color green = Color.decode("#3aa394");
        Color yellow = Color.decode("#d3ad69");
        Color disabledBg = Color.decode("#2f2a2c"); // tom "sem cor" / desativado
        Color defaultBg = Color.decode("#4c4347");
        Color white = Color.WHITE;

        for (int i = 0; i < Math.min(guess.length(), resultado.length); i++) {
            char ch = guess.charAt(i);
            if (!Character.isLetter(ch)) continue;
            ch = Character.toUpperCase(ch);
            JButton keyBtn = keyButtons.get(ch);
            if (keyBtn == null) continue;

            // leia a cor atual para decidir prioridade
            Color currentBg = keyBtn.getBackground();

            switch (resultado[i]) {
                case 'G':
                    // verde sempre tem prioridade: define diretamente
                    keyBtn.setBackground(green);
                    keyBtn.setForeground(white);
                    keyBtn.setBorder(new RoundedBorder(8, "#3aa394", 2));
                    break;
                case 'Y':
                    // amarelo se não for verde já
                    if (!colorsEqual(currentBg, green)) {
                        keyBtn.setBackground(yellow);
                        keyBtn.setForeground(white);
                        keyBtn.setBorder(new RoundedBorder(8, "#d3ad69", 2));
                    }
                    break;
                case 'B':
                    // "desativado visual" apenas se não for verde/nem amarelo
                    if (!colorsEqual(currentBg, green) && !colorsEqual(currentBg, yellow)) {
                        keyBtn.setBackground(disabledBg);
                        keyBtn.setForeground(Color.decode("#bdb6b6")); // tom mais apagado
                        keyBtn.setBorder(new RoundedBorder(8, "#2f2a2c", 2));
                    }
                    break;
            }
        }
    }

    // utilitário simples pra comparar cores (null safe)
    private boolean colorsEqual(Color a, Color b) {
        if (a == null && b == null) return true;
        if (a == null || b == null) return false;
        return a.getRGB() == b.getRGB();
    }

    // mostra/esconde warnLabel com padding (já definido)
    private void setWarnMessage(String message) {
        warnLabel.setText(message);
        boolean hasText = message != null && !message.isEmpty();
        warnLabel.setOpaque(hasText); // só deixa com fundo se tiver texto
        warnLabel.revalidate();
        warnLabel.repaint();
    }
}
