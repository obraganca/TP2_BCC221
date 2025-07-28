package com.termo;

import com.termo.gui.GameWindow;

public class TermoApp {
    public static void main(String[] args) {
        javax.swing.SwingUtilities.invokeLater(() -> {
            new GameWindow().showEventDemo();
        });
    }
}
