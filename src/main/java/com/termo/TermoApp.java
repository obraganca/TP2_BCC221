package com.termo;
import com.termo.controller.Login;
import com.termo.gui.GameWindow;

public class TermoApp {
    public static void main(String[] args) {
        javax.swing.SwingUtilities.invokeLater(() -> {
            new GameWindow(args[0]).showEventDemo();
        });

    }
}
