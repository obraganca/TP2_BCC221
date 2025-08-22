package com.termo.gui.components;

import javax.swing.*;
import javax.swing.text.*;
import java.awt.*;

/**
 * Uma classe que estende JTextField para criar uma "caixa de letra".
 * A principal característica é limitar o número de caracteres que podem ser inseridos
 * e converter automaticamente a entrada para maiúsculas.
 */
public class LetterBox extends JTextField {
    // O número máximo de caracteres permitidos no campo.
    private int limit;

    /**
     * Construtor da LetterBox.
     * @param columns O número de colunas para cálculo da largura preferencial (herdado de JTextField).
     * @param limit O número máximo de caracteres permitidos.
     */
    public LetterBox(int columns, int limit) {
        super(columns);
        this.limit = limit;
        // Define o "documento" interno do campo de texto para a nossa implementação customizada.
        setDocument(new LimitDocument());
    }

    /**
     * Sobrescreve o método que cria o modelo de documento padrão para garantir que
     * nossa classe LimitDocument seja sempre usada.
     */
    @Override
    protected Document createDefaultModel() {
        return new LimitDocument();
    }

    /**
     * Uma classe interna que estende PlainDocument para impor o limite de caracteres.
     * O "Document" é o modelo que armazena e gerencia o texto de um componente de texto Swing.
     */
    private class LimitDocument extends PlainDocument {
        /**
         * Este método é chamado sempre que texto é inserido no campo.
         * Nós o sobrescrevemos para adicionar nossa lógica de validação.
         * @param offs O deslocamento inicial para a inserção.
         * @param str A string a ser inserida.
         * @param a O conjunto de atributos para o conteúdo inserido.
         * @throws BadLocationException Se a inserção for em uma posição inválida.
         */
        @Override
        public void insertString(int offs, String str, AttributeSet a)
                throws BadLocationException {
            if (str == null) return; // Não faz nada se a string for nula.

            // Verifica se o comprimento atual mais o da nova string não ultrapassa o limite.
            if ((getLength() + str.length()) <= limit) {
                // Se estiver dentro do limite, chama o método da superclasse para inserir o texto,
                // convertendo-o para maiúsculas.
                super.insertString(offs, str.toUpperCase(), a);
            } else {
                // Se o limite for excedido, emite um som de "beep" do sistema para alertar o usuário.
                Toolkit.getDefaultToolkit().beep();
            }
        }
    }
}