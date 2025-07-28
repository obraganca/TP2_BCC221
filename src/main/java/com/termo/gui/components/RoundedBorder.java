package com.termo.gui.components;


import javax.swing.border.AbstractBorder;
import java.awt.*;

public class RoundedBorder extends AbstractBorder {
    private int radius;
    private String color;
    private int stroke;

    public RoundedBorder(int radius, String color, int stroke) {
        this.radius = radius;
        this.color = color;
        this.stroke = stroke;
    }

    @Override
    public void paintBorder(Component c, Graphics g, int x, int y, int width, int height) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setColor(Color.decode(this.color)); // Border color
        g2.setStroke(new BasicStroke(this.stroke)); // Border thickness
        g2.drawRoundRect(x, y, width - 2, height - 2, radius, radius);

        g2.dispose();
    }

    @Override
    public Insets getBorderInsets(Component c) {
        return new Insets(radius / 2, radius / 2, radius / 2, radius / 2);
    }

    @Override
    public Insets getBorderInsets(Component c, Insets insets) {
        insets.set(radius / 2, radius / 2, radius / 2, radius / 2);
        return insets;
    }
}
