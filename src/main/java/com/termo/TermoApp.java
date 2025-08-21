package com.termo;
import com.termo.controller.Login;
import com.termo.gui.GameWindow;

public class TermoApp {
    public static void main(String[] args) {
        Login.setFilePath("/home/obraganca/Documents/TP2_BCC221/src/main/resources/datasource.txt");
        javax.swing.SwingUtilities.invokeLater(() -> {
            new GameWindow().showEventDemo();
        });

    }
}
