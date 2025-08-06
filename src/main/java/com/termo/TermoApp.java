package com.termo;

import com.termo.gui.GameWindow;

public class TermoApp {
    public static void main(String[] args) {
        String resposta = "TESTE";
        javax.swing.SwingUtilities.invokeLater(() -> {
            new GameWindow(resposta).showEventDemo();
        });

    }
}
