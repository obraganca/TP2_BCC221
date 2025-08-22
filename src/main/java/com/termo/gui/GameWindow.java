package com.termo.gui;

import java.awt.*;
import java.awt.event.*;
import java.util.HashMap;
import java.util.Map;
import javax.swing.*;

import com.termo.controller.*;
import com.termo.gui.components.LetterBox;
import com.termo.gui.components.RoundedBorder;

/**
 * A classe principal da interface gráfica do jogo.
 * Gerencia a janela principal, o grid de letras, o teclado virtual,
 * o fluxo do jogo e a interação com os controllers.
 */
public class GameWindow {
    String file; // Caminho do arquivo de palavras.
    private StatsOverlay statsOverlay; // Instância da tela de estatísticas.
    private Login sistemaLogin; // Controller para login e cadastro.
    // Componentes principais da UI do Swing.
    private JFrame mainFrame;
    private JLabel headerLabel;
    private JLabel warnLabel;
    private JPanel headerPanel;
    private JLabel statusLabel;
    private JPanel southPanel;

    private JPanel controlPanel; // Painel que contém o grid de letras.
    private JScrollPane gameScrollPane; // Usado para rolagem em telas pequenas.

    private VirtualKeyboard virtualKeyboard; // Instância do teclado virtual.

    // Constantes para o grid do jogo.
    public static final int COLUMN = 5;
    public static final int ROW = 6;
    private LetterBox[][] letterBoxes; // Matriz 2D para as caixas de letras.
    private int currentRow = 0; // Linha atual da tentativa.
    private int currentCol = 0; // Coluna atual do cursor.
    private Game jogo; // Controller principal do jogo.
    private Usuario usuario; // O usuário atualmente logado.

    // Flags para o design responsivo.
    private boolean isSmallScreen = false;
    private boolean isVerySmallScreen = false;

    /**
     * Construtor da janela do jogo.
     * @param file O caminho para o arquivo de palavras.
     */
    public GameWindow(String file) {
        this.file = file;
        this.sistemaLogin = new Login();
        sistemaLogin.debugUsuarios(); // Método para popular usuários de teste.
        jogo = new Game(file);
        showLoginDialog(); // Inicia o fluxo pela tela de login.
    }

    /**
     * Prepara e inicializa a interface gráfica principal do jogo.
     * Este método é chamado após o login bem-sucedido.
     */
    private void prepareGUI() {
        letterBoxes = new LetterBox[ROW][COLUMN];
        mainFrame = new JFrame("TERMO");
        mainFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        // Define o ícone da aplicação.
        ImageIcon termoIcon = null;
        java.net.URL imgURL = GameWindow.class.getResource("/public/icon.png");
        if (imgURL != null) {
            termoIcon = new ImageIcon(imgURL);
            mainFrame.setIconImage(termoIcon.getImage());
        }

        mainFrame.getContentPane().setBackground(Color.decode("#6e5c62"));

        // Define o tamanho inicial da janela com base no tamanho da tela.
        setupInitialWindowSize();

        mainFrame.setLayout(new BorderLayout(10, 10));

        // Cria os componentes principais da UI (cabeçalho, grid, teclado).
        createHeaderPanel();
        createGamePanel();
        createKeyboardPanel();

        // Organiza os componentes criados na janela.
        layoutComponents();

        // Adiciona um listener para detectar redimensionamento da janela e ajustar o layout.
        mainFrame.addComponentListener(new ComponentAdapter() {
            @Override
            public void componentResized(ComponentEvent e) {
                updateResponsiveLayout();
            }
        });

        mainFrame.setLocationRelativeTo(null); // Centraliza a janela na tela.
    }
    /**
     * Exibe uma caixa de diálogo JOptionPane para login ou cadastro do usuário.
     * Este é o ponto de entrada da aplicação para o usuário.
     */
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
                JOptionPane.showMessageDialog(null, "Bem-vindo, " + nome + "!");
                prepareGUI(); // Prepara a UI do jogo.
                showEventDemo(); // Exibe a UI e inicia o jogo.
            } else {
                JOptionPane.showMessageDialog(null, "Senha incorreta!", "Erro", JOptionPane.ERROR_MESSAGE);
                showLoginDialog(); // Mostra o diálogo novamente em caso de falha.
            }
        } else {
            System.exit(0); // Fecha a aplicação se o usuário cancelar o login.
        }
    }

    /**
     * Configura o tamanho inicial da janela com base nas dimensões da tela do usuário.
     */
    private void setupInitialWindowSize() {
        Toolkit toolkit = Toolkit.getDefaultToolkit();
        Dimension screenSize = toolkit.getScreenSize();

        // Detecta o "tamanho" da tela para ajustar o layout de forma responsiva.
        isVerySmallScreen = screenSize.width < 600 || screenSize.height < 500;
        isSmallScreen = screenSize.width < 900 || screenSize.height < 700;

        if (isVerySmallScreen) {
            mainFrame.setSize((int)(screenSize.width * 0.95), (int)(screenSize.height * 0.95));
            mainFrame.setExtendedState(JFrame.MAXIMIZED_BOTH); // Maximiza em telas muito pequenas.
        } else if (isSmallScreen) {
            mainFrame.setSize((int)(screenSize.width * 0.85), (int)(screenSize.height * 0.85));
        } else {
            mainFrame.setSize(Math.min(1200, screenSize.width), Math.min(900, screenSize.height));
        }
    }

    /**
     * Cria o painel do cabeçalho, que inclui o título e os botões de estatísticas e configurações.
     */
    private void createHeaderPanel() {
        headerPanel = new JPanel(new BorderLayout());
        headerPanel.setOpaque(false);

        JPanel topRow = new JPanel(new FlowLayout(FlowLayout.CENTER, 6, 6));
        topRow.setOpaque(false);
        topRow.setBorder(BorderFactory.createEmptyBorder(4, 0, 4, 0));

        // Botão de estatísticas.
        JButton leftBtn = makeHeaderButton("📊");
        leftBtn.setToolTipText("Estatísticas");
        updateButtonSize(leftBtn);
        leftBtn.addActionListener(e -> {
            statsOverlay = new StatsOverlay(usuario.getPerfil());
            statsOverlay.show(false, mainFrame);
        });

        // Título do jogo.
        headerLabel = new JLabel("TERMO", JLabel.CENTER);
        headerLabel.setForeground(Color.WHITE);
        updateHeaderFont();
        headerLabel.setBorder(BorderFactory.createEmptyBorder(0, 6, 0, 6));

        // Botão de configurações com um menu pop-up.
        JButton rightBtn = makeHeaderButton("⚙");
        rightBtn.setToolTipText("Configurações");
        updateButtonSize(rightBtn);

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
                // Mostra as estatísticas e, ao fechar, executa o método exitToLogin como callback.
                statsOverlay = new StatsOverlay(usuario.getPerfil());
                statsOverlay.show(this.hasWon(), mainFrame, this::exitToLogin);
            }
        });


        settingsMenu.add(resetItem);
        settingsMenu.add(exitItem);

        rightBtn.addActionListener(e -> {
            // Mostra o menu logo abaixo do botão de configurações.
            settingsMenu.show(rightBtn, 0, rightBtn.getHeight());
        });

        topRow.add(leftBtn);
        topRow.add(headerLabel);
        topRow.add(rightBtn);

        headerPanel.add(topRow, BorderLayout.NORTH);

        // Painel para mensagens de aviso ao usuário (ex: "palavra não existe").
        JPanel warnPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 0, 0));
        warnPanel.setOpaque(false);

        warnLabel = new JLabel("", JLabel.CENTER);
        warnLabel.setForeground(Color.WHITE);
        updateWarnLabelFont();
        warnLabel.setOpaque(false); // Fica transparente por padrão.
        warnLabel.setBackground(Color.decode("#009AFE"));
        warnLabel.setBorder(BorderFactory.createEmptyBorder(5, 15, 5, 15));

        warnPanel.add(warnLabel);
        headerPanel.add(warnPanel, BorderLayout.SOUTH);
    }


    /**
     * Cria o painel central que contém o grid de caixas de letras.
     */
    private void createGamePanel() {
        // O `controlPanel` é o container direto para as LetterBoxes.
        controlPanel = new JPanel(new GridLayout(ROW, COLUMN, 5, 5));
        controlPanel.setOpaque(false);

        // Um painel "wrapper" é usado para centralizar o grid na tela.
        JPanel centerWrapper = new JPanel(new GridBagLayout());
        centerWrapper.setOpaque(false);
        centerWrapper.add(controlPanel);

        // Se a tela for muito pequena, o grid é colocado dentro de um JScrollPane para permitir rolagem.
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

    /**
     * Cria o teclado virtual e a label de status.
     * A criação é delegada para a classe VirtualKeyboard.
     */
    private void createKeyboardPanel() {
        virtualKeyboard = new VirtualKeyboard(this::handleVirtualKey, isVerySmallScreen, isSmallScreen, this.hasWon());
        statusLabel = new JLabel("", JLabel.CENTER);
        statusLabel.setSize(350, 100);
        statusLabel.setForeground(Color.WHITE);
    }

    /**
     * Adiciona os painéis principais (cabeçalho, jogo, teclado) ao layout da janela principal.
     */
    private void layoutComponents() {
        mainFrame.add(headerPanel, BorderLayout.NORTH);

        // Adiciona o painel de jogo (com ou sem scroll) na área central.
        if (isVerySmallScreen && gameScrollPane != null) {
            mainFrame.add(gameScrollPane, BorderLayout.CENTER);
        } else {
            JPanel centerWrapper = new JPanel(new GridBagLayout());
            centerWrapper.setOpaque(false);
            centerWrapper.add(controlPanel);
            mainFrame.add(centerWrapper, BorderLayout.CENTER);
        }

        // O painel sul contém o teclado virtual e a label de status.
        southPanel = new JPanel(new BorderLayout());
        southPanel.setOpaque(false);
        southPanel.add(virtualKeyboard, BorderLayout.CENTER);
        southPanel.add(statusLabel, BorderLayout.SOUTH);
        mainFrame.add(southPanel, BorderLayout.SOUTH);
    }

    /**
     * Reseta o estado do jogo para uma nova partida.
     */
    private void resetGame() {
        // Cria uma nova instância de jogo, obtendo uma nova palavra.
        jogo = new Game(file);

        // Reseta os índices de posição.
        currentRow = 0;
        currentCol = 0;

        // Limpa as mensagens de aviso e status.
        setWarnMessage("");
        if (statusLabel != null) statusLabel.setText("");

        // Recria o teclado virtual para limpar as cores das teclas.
        virtualKeyboard = new VirtualKeyboard(this::handleVirtualKey, isVerySmallScreen, isSmallScreen, this.hasWon());

        // Substitui o teclado antigo pelo novo no painel sul.
        if (southPanel != null) {
            southPanel.removeAll();
            southPanel.add(virtualKeyboard, BorderLayout.CENTER);
            southPanel.add(statusLabel, BorderLayout.SOUTH);
            southPanel.revalidate();
            southPanel.repaint();
        }

        // Limpa e reconfigura todas as caixas de letras para o estado inicial.
        if (letterBoxes != null) {
            for (int r = 0; r < ROW; r++) {
                for (int c = 0; c < COLUMN; c++) {
                    LetterBox box = letterBoxes[r][c];
                    if (box == null) continue;
                    box.setText("");

                    // Linhas futuras são desabilitadas, linha atual é habilitada.
                    if (r != currentRow) {
                        box.setOpaque(true);
                        box.setBackground(Color.decode("#615458"));
                        box.setBorder(new RoundedBorder(15, "#615458", 6));
                        box.setEnabled(false);
                        box.setEditable(false);
                    } else {
                        box.setOpaque(false);
                        box.setBackground(null); // Remove o fundo colorido
                        box.setBorder(new RoundedBorder(15, "#4c4347", 6)); // Restaura a borda padrão da linha ativa
                        box.setEnabled(true);
                        box.setEditable(true);
                    }
                }
            }
        }

        // Devolve o foco para a primeira caixa da primeira linha.
        SwingUtilities.invokeLater(() -> {
            if (letterBoxes != null && letterBoxes[0][0] != null) {
                letterBoxes[0][0].requestFocusInWindow();
            }
        });

        controlPanel.revalidate();
        controlPanel.repaint();
    }

    /**
     * Fecha a janela do jogo e retorna para a tela de login.
     */
    private void exitToLogin() {
        // Descarta a janela atual.
        if (mainFrame != null) {
            mainFrame.dispose();
        }
        // Reseta o estado do jogo.
        usuario = null;
        jogo = new Game(file);
        currentRow = 0;
        currentCol = 0;

        // Chama a tela de login novamente.
        SwingUtilities.invokeLater(() -> showLoginDialog());
    }



    /**
     * Método auxiliar para criar um botão estilizado para o cabeçalho.
     * @param label O texto ou ícone do botão.
     * @return Um JButton estilizado.
     */
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

    // --- Métodos para Lógica Responsiva ---

    /**
     * Chamado quando a janela é redimensionada. Decide se o layout precisa ser recriado
     * ou se apenas os tamanhos dos componentes precisam ser atualizados.
     */
    private void updateResponsiveLayout() {
        Dimension currentSize = mainFrame.getSize();

        boolean wasSmallScreen = isSmallScreen;
        boolean wasVerySmallScreen = isVerySmallScreen;

        isVerySmallScreen = currentSize.width < 600 || currentSize.height < 500;
        isSmallScreen = currentSize.width < 900 || currentSize.height < 700;

        // Se mudou de uma "categoria" de tamanho para outra (ex: normal para pequena), recria o layout.
        if (wasSmallScreen != isSmallScreen || wasVerySmallScreen != isVerySmallScreen) {
            SwingUtilities.invokeLater(() -> recreateLayout());
        } else {
            // Caso contrário, se o tamanho mudou mas a categoria é a mesma, apenas atualiza os tamanhos.
            updateComponentSizes();
        }
    }

    /**
     * Recria o layout inteiro da janela. É útil quando há uma mudança significativa
     * de tamanho que requer mais do que apenas um ajuste de dimensões (ex: adicionar/remover o JScrollPane).
     */
    private void recreateLayout() {
        // Armazena o estado atual das caixas de letras antes de remover tudo.
        LetterBox[][] oldLetterBoxes = new LetterBox[ROW][COLUMN];
        for (int i = 0; i < ROW; i++) {
            System.arraycopy(letterBoxes[i], 0, oldLetterBoxes[i], 0, COLUMN);
        }

        mainFrame.getContentPane().removeAll();
        // Recria os painéis com as novas configurações de tela (isSmallScreen, etc.).
        createGamePanel();
        createKeyboardPanel();

        // Restaura o estado das caixas (texto, cores) no novo painel.
        restoreLetterBoxes(oldLetterBoxes);

        layoutComponents(); // Reorganiza os componentes na janela.
        updateComponentSizes(); // Ajusta os tamanhos finais.
        mainFrame.revalidate();
        mainFrame.repaint();
    }

    /**
     * Restaura as caixas de letras após o layout ter sido recriado.
     * Isso preserva o texto, as cores e o estado do jogo visualmente.
     * @param oldBoxes A matriz com o estado anterior das caixas.
     */
    private void restoreLetterBoxes(LetterBox[][] oldBoxes) {
        controlPanel.removeAll();

        // Calcula os novos tamanhos responsivos para as caixas e fontes.
        int boxSize = isVerySmallScreen ? 40 : (isSmallScreen ? 55 : 70);
        int fontSize = isVerySmallScreen ? 18 : (isSmallScreen ? 24 : 28);

        for (int row = 0; row < ROW; row++) {
            for (int col = 0; col < COLUMN; col++) {
                LetterBox oldBox = oldBoxes[row][col];
                LetterBox newBox = new LetterBox(1, 1);

                // Copia todas as propriedades importantes da caixa antiga para a nova.
                newBox.setText(oldBox.getText());
                newBox.setBackground(oldBox.getBackground());
                newBox.setOpaque(oldBox.isOpaque());
                newBox.setEnabled(oldBox.isEnabled());
                newBox.setEditable(oldBox.isEditable());
                newBox.setPreferredSize(new Dimension(boxSize, boxSize)); // Aplica novo tamanho.
                newBox.setHorizontalAlignment(JTextField.CENTER);
                newBox.setForeground(Color.WHITE);
                newBox.setFont(new Font("Arial", Font.BOLD, fontSize)); // Aplica nova fonte.
                newBox.setDisabledTextColor(Color.WHITE);
                newBox.setBorder(oldBox.getBorder()); // Mantém a borda/cor.

                // Adiciona o listener de teclado à nova caixa.
                newBox.addKeyListener(createLetterBoxKeyListener(row, col));

                letterBoxes[row][col] = newBox; // Substitui a caixa na matriz de referência.
                controlPanel.add(newBox);
            }
        }

        // Restaura o foco para a posição correta.
        if (currentRow < ROW && currentCol < COLUMN && letterBoxes[currentRow][currentCol].isEnabled()) {
            letterBoxes[currentRow][currentCol].requestFocusInWindow();
        }

        controlPanel.revalidate();
        controlPanel.repaint();
    }


    /**
     * Atualiza os tamanhos (dimensões, fontes) de todos os componentes com base nos flags de tela.
     */
    private void updateComponentSizes() {
        updateHeaderFont();
        updateWarnLabelFont();
        updateGamePanelSize();
        updateKeyboardSizes();

        // Percorre o cabeçalho para atualizar o tamanho dos botões.
        Component[] headerComponents = headerPanel.getComponents();
        for (Component comp : headerComponents) {
            if (comp instanceof JPanel) {
                updateHeaderButtonsInPanel((JPanel) comp);
            }
        }
    }

    /**
     * Método recursivo para encontrar e atualizar botões dentro de um painel aninhado.
     */
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

    // Métodos auxiliares para atualizar o tamanho de fontes e componentes específicos.
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
            // Calcula e define o tamanho exato do painel do grid.
            int totalWidth = (boxSize * COLUMN) + (gap * (COLUMN - 1));
            int totalHeight = (boxSize * ROW) + (gap * (ROW - 1));
            controlPanel.setPreferredSize(new Dimension(totalWidth, totalHeight));
        }
    }

    /**
     * Delega a atualização de tamanho para a classe VirtualKeyboard.
     */
    private void updateKeyboardSizes() {
        if (virtualKeyboard != null) {
            virtualKeyboard.updateSizes(isVerySmallScreen, isSmallScreen);
        }
    }

    // --- Métodos para Manipulação de Input e Lógica de Jogo ---

    /**
     * Cria e retorna um KeyAdapter para ser adicionado a cada LetterBox.
     * Este listener lida com a digitação, backspace, enter e navegação com as setas.
     */
    private KeyAdapter createLetterBoxKeyListener(final int row, final int col) {
        return new KeyAdapter() {
            @Override
            public void keyTyped(KeyEvent e) {
                if (row != currentRow) return; // Só permite digitar na linha ativa.
                char ch = e.getKeyChar();
                if (Character.isLetter(ch)) {
                    letterBoxes[row][col].setText(String.valueOf(ch).toUpperCase());
                    // Avança o foco para a próxima caixa.
                    if (currentCol == col && currentCol < COLUMN - 1) {
                        currentCol++;
                    }
                    SwingUtilities.invokeLater(() -> letterBoxes[row][currentCol].requestFocusInWindow());
                    e.consume(); // Consome o evento para evitar processamento duplicado.
                }
            }

            @Override
            public void keyPressed(KeyEvent e) {
                if (row != currentRow) return;
                int keyCode = e.getKeyCode();
                LetterBox currentBox = letterBoxes[currentRow][currentCol];

                if (keyCode == KeyEvent.VK_BACK_SPACE) {
                    // Se a caixa atual está vazia, move para a anterior e apaga.
                    if (currentBox.getText().isEmpty() && currentCol > 0) {
                        moveToPreviousColumn();
                        letterBoxes[currentRow][currentCol].setText("");
                    } else { // Se não, apenas apaga a atual.
                        currentBox.setText("");
                    }
                    setWarnMessage("");
                    e.consume();
                } else if (keyCode == KeyEvent.VK_ENTER && currentCol == COLUMN - 1) {
                    // Só submete se estiver na última coluna.
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

    /**
     * Manipula eventos de clique vindos do teclado virtual.
     * @param key A string que representa a tecla clicada ("A", "B", "ENTER", "BACK").
     */
    private void handleVirtualKey(String key) {
        if (key.length() == 1 && Character.isLetter(key.charAt(0))) {
            LetterBox currentBox = letterBoxes[currentRow][currentCol];
            currentBox.setText(key.toUpperCase());
            // Avança para a próxima coluna, se não for a última.
            if (currentCol < COLUMN - 1) {
                currentCol++;
                letterBoxes[currentRow][currentCol].requestFocus();
            }
        } else if ("ENTER".equalsIgnoreCase(key)) {
            submitGuess();
        } else if ("BACK".equalsIgnoreCase(key)) {
            LetterBox currentBox = letterBoxes[currentRow][currentCol];
            // Se a caixa atual está vazia e não é a primeira, move para trás e apaga.
            if (currentBox.getText().isEmpty() && currentCol > 0) {
                currentCol--;
                letterBoxes[currentRow][currentCol].setText("");
            } else { // Se não, apenas apaga o conteúdo da caixa atual.
                currentBox.setText("");
            }
            letterBoxes[currentRow][currentCol].requestFocus();
            setWarnMessage(""); // Limpa qualquer aviso.
        }
    }

    /**
     * Preenche o `controlPanel` com as instâncias de `LetterBox` e as configura.
     * Este método é chamado uma vez no início para montar o grid do jogo.
     */
    public void showEventDemo() {
        controlPanel.removeAll();
        int boxSize = isVerySmallScreen ? 40 : (isSmallScreen ? 55 : 70);
        int fontSize = isVerySmallScreen ? 18 : (isSmallScreen ? 24 : 28);

        for (int row = 0; row < ROW; row++) {
            for (int col = 0; col < COLUMN; col++) {
                LetterBox box = new LetterBox(1, 1);
                // Estilização padrão da caixa de letra.
                box.setBorder(new RoundedBorder(15, "#4c4347", 6));
                box.setOpaque(false);
                box.setHorizontalAlignment(JTextField.CENTER);
                box.setForeground(Color.WHITE);
                box.setFont(new Font("Arial", Font.BOLD, fontSize));
                box.setDisabledTextColor(Color.WHITE);
                box.setPreferredSize(new Dimension(boxSize, boxSize));

                // Desabilita caixas que não são da linha atual.
                if (row != currentRow) {
                    box.setOpaque(true);
                    box.setBackground(Color.decode("#615458"));
                    box.setBorder(new RoundedBorder(15, "#615458", 6));
                    box.setEnabled(false);
                    box.setEditable(false);
                } else { // Habilita apenas a linha atual.
                    box.setEnabled(true);
                    box.setEditable(true);
                }

                box.addKeyListener(createLetterBoxKeyListener(row, col));
                letterBoxes[row][col] = box;
                controlPanel.add(box);
            }
        }

        letterBoxes[0][0].requestFocusInWindow(); // Põe o foco na primeira caixa.
        controlPanel.revalidate();
        controlPanel.repaint();
        mainFrame.setVisible(true); // Torna a janela do jogo visível.
    }

    // Métodos para mover o cursor entre as colunas.
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

    /**
     * Processa a tentativa do jogador quando o Enter é pressionado.
     * Este é o coração da lógica de interação do jogo.
     */
    private void submitGuess() {
        StringBuilder guess = new StringBuilder();
        for (int col = 0; col < COLUMN; col++) {
            guess.append(letterBoxes[currentRow][col].getText());
        }

        if (guess.length() == COLUMN) { // Garante que a palavra está completa.
            boolean isValid = jogo.validateGuess(guess.toString());
            if (isValid) {
                guessProcessing(); // Colore as letras e o teclado.
                // Se o jogo não acabou (nem vitória, nem última tentativa)...
                if (currentRow < ROW - 1 && !hasWon()) {
                    currentRow++;
                    currentCol = 0;
                    // Habilita a próxima linha.
                    for (int c = 0; c < COLUMN; c++) {
                        letterBoxes[currentRow][c].setOpaque(false);
                        letterBoxes[currentRow][c].setBorder(new RoundedBorder(15, "#4c4347", 6));
                        letterBoxes[currentRow][c].setEnabled(true);
                        letterBoxes[currentRow][c].setEditable(true);
                    }
                    letterBoxes[currentRow][currentCol].requestFocus();
                } else { // Fim de jogo.
                    setWarnMessage("Fim do jogo !");
                    // Registra vitória ou derrota e desabilita o jogo.
                    if (this.hasWon()) {
                        usuario.getPerfil().registrarVitoria(currentRow + 1);
                        virtualKeyboard.setWon(true);
                    } else {
                        usuario.getPerfil().registrarDerrota(currentRow + 1);
                    }
                    // Desabilita todas as caixas de letra.
                    for (int r = 0; r < ROW; r++) {
                        for (int c = 0; c < COLUMN; c++) {
                            letterBoxes[r][c].setEditable(false);
                        }
                    }
                    // Exibe a tela de estatísticas.
                    statsOverlay = new StatsOverlay(usuario.getPerfil());
                    statsOverlay.show(this.hasWon(), mainFrame);
                }
            } else {
                setWarnMessage("Essa palavra não é aceita");
            }
        }
        controlPanel.repaint();
    }
    /**
     * Verifica se o jogador venceu o jogo.
     * @return true se o jogador acertou a palavra.
     */
    public boolean hasWon(){
        return jogo.getRightQuantityWord() == jogo.getWordLength();
    }

    /**
     * Atualiza a cor das LetterBoxes na linha atual com base no resultado da tentativa.
     */
    public void guessProcessing() {
        char[] resultado = jogo.getResultado();
        String guess = jogo.getpalavratentativa(); // Pega a palavra com acentos do controller.

        // Atualiza o texto nas caixas para mostrar acentos, se houver.
        for (int i = 0; i < COLUMN; i++) {
            letterBoxes[currentRow][i].setText(String.valueOf(guess.charAt(i)));
        }

        // Colore as caixas de acordo com o resultado e as desabilita.
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

        // Atualiza as cores do teclado virtual.
        updateKeyboardColors(guess, resultado);
        controlPanel.repaint();
    }


    /**
     * Delega a atualização das cores do teclado para a classe VirtualKeyboard.
     * @param guess A palavra tentada.
     * @param resultado O array de resultados ('G', 'Y', 'B').
     */
    private void updateKeyboardColors(String guess, char[] resultado) {
        virtualKeyboard.updateKeyboardColors(guess, resultado);
    }

    /**
     * Exibe ou oculta uma mensagem de aviso na área do cabeçalho.
     * @param message A mensagem a ser exibida.
     */
    private void setWarnMessage(String message) {
        warnLabel.setText(message);
        boolean hasText = message != null && !message.isEmpty();
        warnLabel.setOpaque(hasText); // O fundo colorido só aparece se houver texto.
        warnLabel.revalidate();
        warnLabel.repaint();
    }
}