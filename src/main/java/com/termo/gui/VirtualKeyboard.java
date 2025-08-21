package com.termo.gui;

import com.termo.gui.components.RoundedBorder;

import javax.swing.*;
import java.awt.*;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;

public class VirtualKeyboard extends JPanel {
    private final Map<Character, JButton> keyButtons = new HashMap<>();
    private final Consumer<String> keyHandler;
    private boolean isVerySmallScreen;
    private boolean isSmallScreen;
    private boolean won;

    public VirtualKeyboard(Consumer<String> keyHandler, boolean isVerySmallScreen, boolean isSmallScreen, boolean won) {
        this.keyHandler = keyHandler;
        this.isVerySmallScreen = isVerySmallScreen;
        this.isSmallScreen = isSmallScreen;
        this.won = won;
        initializeKeyboard();
    }

    private void initializeKeyboard() {
        setLayout(new GridBagLayout());
        setOpaque(false);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(4, 4, 4, 4);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        String row1 = "QWERTYUIOP";
        String row2 = "ASDFGHJKL";
        String[] row3 = {"ENTER", "ZXCVBNM", "BACK"};

        // Row 1
        JPanel r1 = createKeyboardRow(row1);
        gbc.gridy = 0;
        add(r1, gbc);

        // Row 2
        JPanel r2 = createKeyboardRow(row2);
        gbc.gridy = 1;
        add(r2, gbc);

        // Row 3
        JPanel r3 = createSpecialKeyRow(row3);
        gbc.gridy = 2;
        add(r3, gbc);
    }

    private JPanel createKeyboardRow(String keys) {
        JPanel rowPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 4, 0));
        rowPanel.setOpaque(false);

        for (char ch : keys.toCharArray()) {
            JButton button = createKeyButton(String.valueOf(ch));
            rowPanel.add(button);
            keyButtons.put(ch, button);
        }
        return rowPanel;
    }

    private JPanel createSpecialKeyRow(String[] keys) {
        JPanel rowPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 4, 0));
        rowPanel.setOpaque(false);

        JButton enterBtn = createKeyButton(keys[0]);
        updateSpecialKeySize(enterBtn, true);
        rowPanel.add(enterBtn);

        for (char ch : keys[1].toCharArray()) {
            JButton button = createKeyButton(String.valueOf(ch));
            rowPanel.add(button);
            keyButtons.put(ch, button);
        }

        JButton backBtn = createKeyButton(keys[2]);
        backBtn.setText("←");
        backBtn.setToolTipText("Backspace");
        updateSpecialKeySize(backBtn, false);
        rowPanel.add(backBtn);

        return rowPanel;
    }

    private JButton createKeyButton(String label) {
        JButton btn = new JButton(label);
        btn.setFocusable(false);
        btn.setFont(new Font("Arial", Font.BOLD, getKeyboardFontSize()));
        updateKeySize(btn);
        btn.setBackground(Color.decode("#4c4347"));
        btn.setForeground(Color.WHITE);
        btn.setBorder(new RoundedBorder(8, "#4c4347", 2));
        btn.setOpaque(true);

        btn.addActionListener(e -> {
            if(!won){
                String key = label;
                if ("←".equals(label)) key = "BACK";
                keyHandler.accept(key);
            }
        });

        return btn;
    }

    public void updateKeyboardColors(String guess, char[] resultado) {
        if (guess == null || resultado == null) return;

        Color green = Color.decode("#3aa394");
        Color yellow = Color.decode("#d3ad69");
        Color disabledBg = Color.decode("#2f2a2c");
        Color white = Color.WHITE;

        for (int i = 0; i < Math.min(guess.length(), resultado.length); i++) {
            char ch = Character.toUpperCase(guess.charAt(i));
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
        return a != null && b != null && a.getRGB() == b.getRGB();
    }

    public void updateSizes(boolean isVerySmallScreen, boolean isSmallScreen) {
        this.isVerySmallScreen = isVerySmallScreen;
        this.isSmallScreen = isSmallScreen;

        for (JButton btn : keyButtons.values()) {
            updateKeySize(btn);
            btn.setFont(new Font("Arial", Font.BOLD, getKeyboardFontSize()));
        }
        updateSpecialKeys();
    }

    private void updateSpecialKeys() {
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

    public void setWon(boolean won){
        this.won=won;
    }
}