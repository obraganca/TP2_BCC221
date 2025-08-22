package com.termo.gui;

import com.termo.gui.components.RoundedBorder;

import javax.swing.*;
import java.awt.*;
import java.text.Normalizer;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;

/**
 * Um painel que representa um teclado virtual para o jogo.
 * Ele lida com a exibição das teclas, o envio de eventos de clique e a atualização das cores das teclas
 * com base nos resultados das tentativas do jogador.
 */
public class VirtualKeyboard extends JPanel {
    // Mapeia cada caractere a um JButton para permitir fácil acesso e atualização.
    private final Map<Character, JButton> keyButtons = new HashMap<>();
    // Um "manipulador" (função) que será chamado quando uma tecla for pressionada.
    private final Consumer<String> keyHandler;
    // Flags para controle de layout responsivo em telas de diferentes tamanhos.
    private boolean isVerySmallScreen;
    private boolean isSmallScreen;
    // Flag para desabilitar o teclado quando o jogo é ganho.
    private boolean won;

    /**
     * Construtor do Teclado Virtual.
     * @param keyHandler Função a ser executada ao pressionar uma tecla.
     * @param isVerySmallScreen True se a tela for muito pequena.
     * @param isSmallScreen True se a tela for pequena.
     * @param won True se o jogo já foi ganho.
     */
    public VirtualKeyboard(Consumer<String> keyHandler, boolean isVerySmallScreen, boolean isSmallScreen, boolean won) {
        this.keyHandler = keyHandler;
        this.isVerySmallScreen = isVerySmallScreen;
        this.isSmallScreen = isSmallScreen;
        this.won = won;
        initializeKeyboard();
    }

    /**
     * Configura o layout do painel do teclado e inicializa as fileiras de teclas.
     */
    private void initializeKeyboard() {
        setLayout(new GridBagLayout());
        setOpaque(false); // Torna o painel transparente.
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(4, 4, 4, 4); // Espaçamento entre as teclas.
        gbc.fill = GridBagConstraints.HORIZONTAL; // Preenchimento horizontal.

        // Definição das teclas em cada fileira.
        String row1 = "QWERTYUIOP";
        String row2 = "ASDFGHJKL";
        String[] row3 = {"ENTER", "ZXCVBNM", "BACK"}; // Teclas especiais + letras.

        // Cria e adiciona a primeira fileira de teclas.
        JPanel r1 = createKeyboardRow(row1);
        gbc.gridy = 0;
        add(r1, gbc);

        // Cria e adiciona a segunda fileira de teclas.
        JPanel r2 = createKeyboardRow(row2);
        gbc.gridy = 1;
        add(r2, gbc);

        // Cria e adiciona a terceira fileira, que contém teclas especiais.
        JPanel r3 = createSpecialKeyRow(row3);
        gbc.gridy = 2;
        add(r3, gbc);
    }

    /**
     * Cria um painel (JPanel) para uma fileira de teclas padrão.
     * @param keys Uma string contendo os caracteres da fileira.
     * @return Um JPanel com os botões da fileira.
     */
    private JPanel createKeyboardRow(String keys) {
        JPanel rowPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 4, 0));
        rowPanel.setOpaque(false);

        for (char ch : keys.toCharArray()) {
            JButton button = createKeyButton(String.valueOf(ch));
            rowPanel.add(button);
            keyButtons.put(ch, button); // Armazena o botão no mapa para referência futura.
        }
        return rowPanel;
    }

    /**
     * Cria um painel para a fileira de teclas que inclui "ENTER" e "BACKSPACE".
     * @param keys Um array de strings definindo as teclas especiais e o grupo de letras.
     * @return Um JPanel com os botões da fileira especial.
     */
    private JPanel createSpecialKeyRow(String[] keys) {
        JPanel rowPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 4, 0));
        rowPanel.setOpaque(false);

        // Cria e adiciona o botão "ENTER".
        JButton enterBtn = createKeyButton(keys[0]);
        updateSpecialKeySize(enterBtn, true);
        rowPanel.add(enterBtn);

        // Adiciona as letras da terceira fileira.
        for (char ch : keys[1].toCharArray()) {
            JButton button = createKeyButton(String.valueOf(ch));
            rowPanel.add(button);
            keyButtons.put(ch, button);
        }

        // Cria e adiciona o botão "BACKSPACE" (com ícone de seta).
        JButton backBtn = createKeyButton(keys[2]);
        backBtn.setText("←");
        backBtn.setToolTipText("Backspace"); // Dica ao passar o mouse.
        updateSpecialKeySize(backBtn, false);
        rowPanel.add(backBtn);

        return rowPanel;
    }

    /**
     * Cria e estiliza um único botão de tecla.
     * @param label O texto (caractere) do botão.
     * @return Um JButton estilizado e configurado.
     */
    private JButton createKeyButton(String label) {
        JButton btn = new JButton(label);
        btn.setFocusable(false); // Impede que o botão receba foco de teclado.
        btn.setFont(new Font("Arial", Font.BOLD, getKeyboardFontSize()));
        updateKeySize(btn); // Define o tamanho com base no tamanho da tela.
        btn.setBackground(Color.decode("#4c4347"));
        btn.setForeground(Color.WHITE);
        btn.setBorder(new RoundedBorder(8, "#4c4347", 2)); // Borda arredondada customizada.
        btn.setOpaque(true);

        // Define a ação a ser executada quando o botão é clicado.
        btn.addActionListener(e -> {
            if(!won){ // Só processa o clique se o jogo não foi ganho.
                String key = label;
                if ("←".equals(label)) key = "BACK"; // Trata o caso especial do backspace.
                keyHandler.accept(key); // Chama a função de callback com a tecla pressionada.
            }
        });

        return btn;
    }

    /**
     * Atualiza as cores dos botões do teclado com base no resultado de uma tentativa.
     * @param guess A palavra que foi tentada pelo jogador.
     * @param resultado Um array de char ('G' para verde, 'Y' para amarelo, 'B' para cinza).
     */
    public void updateKeyboardColors(String guess, char[] resultado) {
        if (guess == null || resultado == null) return;

        // Cores padrão para os resultados.
        Color green = Color.decode("#3aa394");
        Color yellow = Color.decode("#d3ad69");
        Color disabledBg = Color.decode("#2f2a2c");
        Color white = Color.WHITE;

        for (int i = 0; i < Math.min(guess.length(), resultado.length); i++) {
            char chOrig = guess.charAt(i);
            // Normaliza o caractere para sua forma base (remove acentos/cedilha) para corresponder à tecla.
            String norm = Normalizer.normalize(String.valueOf(chOrig), Normalizer.Form.NFD).replaceAll("\\p{M}", "");
            if (norm.isEmpty()) continue;
            char ch = Character.toUpperCase(norm.charAt(0));

            JButton keyBtn = keyButtons.get(ch);
            if (keyBtn == null) continue;

            Color currentBg = keyBtn.getBackground();

            // Lógica de atualização de cores: Verde tem prioridade sobre amarelo.
            // Uma vez cinza, não muda mais, a menos que se torne verde ou amarelo.
            switch (resultado[i]) {
                case 'G':
                    keyBtn.setBackground(green);
                    keyBtn.setForeground(white);
                    keyBtn.setBorder(new RoundedBorder(8, "#3aa394", 2));
                    break;
                case 'Y':
                    if (!colorsEqual(currentBg, green)) { // Só atualiza para amarelo se não for verde.
                        keyBtn.setBackground(yellow);
                        keyBtn.setForeground(white);
                        keyBtn.setBorder(new RoundedBorder(8, "#d3ad69", 2));
                    }
                    break;
                case 'B':
                    // Só atualiza para cinza se não for verde nem amarelo.
                    if (!colorsEqual(currentBg, green) && !colorsEqual(currentBg, yellow)) {
                        keyBtn.setBackground(disabledBg);
                        keyBtn.setForeground(Color.decode("#bdb6b6"));
                        keyBtn.setBorder(new RoundedBorder(8, "#2f2a2c", 2));
                    }
                    break;
            }
        }
    }


    /**
     * Compara duas cores para ver se são iguais.
     * @param a Primeira cor.
     * @param b Segunda cor.
     * @return True se as cores forem iguais.
     */
    private boolean colorsEqual(Color a, Color b) {
        return a != null && b != null && a.getRGB() == b.getRGB();
    }

    /**
     * Atualiza os tamanhos de todas as teclas quando a janela é redimensionada.
     * @param isVerySmallScreen True se a tela agora é muito pequena.
     * @param isSmallScreen True se a tela agora é pequena.
     */
    public void updateSizes(boolean isVerySmallScreen, boolean isSmallScreen) {
        this.isVerySmallScreen = isVerySmallScreen;
        this.isSmallScreen = isSmallScreen;

        // Itera sobre todos os botões e atualiza seu tamanho e fonte.
        for (JButton btn : keyButtons.values()) {
            updateKeySize(btn);
            btn.setFont(new Font("Arial", Font.BOLD, getKeyboardFontSize()));
        }
        updateSpecialKeys(); // Atualiza também as teclas especiais.
    }

    /**
     * Encontra e atualiza o tamanho das teclas especiais (ENTER, BACKSPACE).
     */
    private void updateSpecialKeys() {
        // Itera através dos painéis de fileira e seus componentes (botões).
        for (Component comp : getComponents()) {
            if (comp instanceof JPanel) {
                for (Component btn : ((JPanel) comp).getComponents()) {
                    if (btn instanceof JButton) {
                        String text = ((JButton) btn).getText();
                        if ("ENTER".equals(text)) {
                            updateSpecialKeySize((JButton) btn, true);
                        } else if ("←".equals(text)) {
                            updateSpecialKeySize((JButton) btn, false);
                        }
                    }
                }
            }
        }
    }

    /**
     * Define o tamanho preferencial de um botão de tecla padrão com base no tamanho da tela.
     * @param btn O botão a ser redimensionado.
     */
    private void updateKeySize(JButton btn) {
        int size = isVerySmallScreen ? 35 : (isSmallScreen ? 50 : 70);
        btn.setPreferredSize(new Dimension(size, size));
    }

    /**
     * Define o tamanho preferencial de um botão de tecla especial (ENTER/BACK).
     * @param btn O botão a ser redimensionado.
     * @param isEnter True se o botão for "ENTER".
     */
    private void updateSpecialKeySize(JButton btn, boolean isEnter) {
        int height = isVerySmallScreen ? 35 : (isSmallScreen ? 40 : 48);
        int width = isVerySmallScreen ? 60 : (isSmallScreen ? 75 : 90);
        btn.setPreferredSize(new Dimension(width, height));
    }

    /**
     * Obtém o tamanho da fonte ideal para as teclas com base no tamanho da tela.
     * @return O tamanho da fonte.
     */
    private int getKeyboardFontSize() {
        return isVerySmallScreen ? 12 : (isSmallScreen ? 15 : 18);
    }

    /**
     * Define o estado de vitória para desabilitar/habilitar a funcionalidade do teclado.
     * @param won True se o jogo foi vencido.
     */
    public void setWon(boolean won){
        this.won=won;
    }
}