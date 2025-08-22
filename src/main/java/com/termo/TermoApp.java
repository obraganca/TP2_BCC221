package com.termo;
import com.termo.controller.Login;
import com.termo.gui.GameWindow;

/**
 * A classe principal que serve como ponto de entrada para a aplicação do jogo Termo.
 */
public class TermoApp {
    /**
     * O método main, que é o primeiro a ser executado quando o programa é iniciado.
     * @param args Argumentos de linha de comando (não utilizados nesta aplicação).
     */
    public static void main(String[] args) {
        // SwingUtilities.invokeLater é usado para garantir que a criação e manipulação
        // da interface gráfica (GUI) ocorra na Event Dispatch Thread (EDT).
        javax.swing.SwingUtilities.invokeLater(() -> {
            // Cria uma nova instância da janela principal do jogo (GameWindow),
            // passando o caminho para o arquivo de palavras.
            // O método .showEventDemo() é então chamado para construir e exibir a janela.
            new GameWindow("/home/samuel/Documents/UFOP/BCC221/TP2/src/main/resources/datasource.txt");
        });
    }
}