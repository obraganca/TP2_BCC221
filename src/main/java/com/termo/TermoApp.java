package com.termo;
import com.termo.controller.Login;
import com.termo.gui.GameWindow;

public class TermoApp {
    public static void main(String[] args) {
        Login.setFilePath(args[2]);
        javax.swing.SwingUtilities.invokeLater(() -> {
            new GameWindow().showEventDemo();
        });

    }
}
