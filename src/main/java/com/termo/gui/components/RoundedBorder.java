package com.termo.gui.components;

import javax.swing.border.AbstractBorder;
import java.awt.*;

/**
 * Uma classe customizada de borda que desenha uma linha com cantos arredondados.
 * Herda de AbstractBorder para permitir a criação de uma borda personalizada para componentes Swing.
 */
public class RoundedBorder extends AbstractBorder {
    private int radius;     // O raio de arredondamento dos cantos.
    private Color color;    // A cor da borda.
    private int thickness;  // A espessura da linha da borda.

    /**
     * Construtor da borda arredondada.
     * @param radius O raio dos cantos. Quanto maior, mais arredondado.
     * @param hexColor A cor da borda em formato de string hexadecimal (ex: "#FFFFFF").
     * @param thickness A espessura da linha em pixels.
     */
    public RoundedBorder(int radius, String hexColor, int thickness) {
        this.radius = radius;
        this.color = Color.decode(hexColor); // Decodifica a string de cor para um objeto Color.
        this.thickness = thickness;
    }

    /**
     * O método principal que desenha a borda no componente.
     * Este método é chamado pelo sistema de renderização do Swing.
     * @param c O componente ao qual a borda está sendo pintada.
     * @param g O objeto Graphics usado para desenhar.
     * @param x A coordenada x do canto superior esquerdo da área da borda.
     * @param y A coordenada y do canto superior esquerdo da área da borda.
     * @param width A largura da área da borda.
     * @param height A altura da área da borda.
     */
    @Override
    public void paintBorder(Component c, Graphics g, int x, int y,
                            int width, int height) {
        // Converte o objeto Graphics para Graphics2D para ter acesso a mais funcionalidades, como a espessura da linha.
        Graphics2D g2 = (Graphics2D) g;
        g2.setColor(color); // Define a cor do pincel.
        // Define a espessura da linha.
        g2.setStroke(new BasicStroke(thickness));
        // Desenha o retângulo com cantos arredondados. (width-1, height-1) é usado para garantir que a linha caiba dentro dos limites.
        g2.drawRoundRect(x, y, width-1, height-1, radius, radius);
    }

    /**
     * Retorna o espaço (preenchimento) que a borda ocupa dentro do componente.
     * Isso é usado pelos gerenciadores de layout para posicionar o conteúdo do componente corretamente.
     * @param c O componente.
     * @return Um objeto Insets com o espaçamento superior, esquerdo, inferior e direito.
     */
    @Override
    public Insets getBorderInsets(Component c) {
        return new Insets(radius+1, radius+1, radius+2, radius);
    }

    /**
     * Uma versão mais eficiente do método acima, que reutiliza um objeto Insets existente.
     * @param c O componente.
     * @param insets O objeto Insets a ser preenchido.
     * @return O objeto Insets preenchido com os valores de espaçamento da borda.
     */
    @Override
    public Insets getBorderInsets(Component c, Insets insets) {
        insets.left = insets.right = radius;
        insets.top = insets.bottom = radius;
        return insets;
    }
}