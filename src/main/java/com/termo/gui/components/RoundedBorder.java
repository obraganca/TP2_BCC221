package com.termo.gui.components;

import javax.swing.border.AbstractBorder;
import java.awt.*;

public class RoundedBorder extends AbstractBorder {
    private int radius;
    private Color color;
    private int thickness;

    public RoundedBorder(int radius, String hexColor, int thickness) {
        this.radius = radius;
        this.color = Color.decode(hexColor);
        this.thickness = thickness;
    }

    @Override
    public void paintBorder(Component c, Graphics g, int x, int y,
                            int width, int height) {
        Graphics2D g2 = (Graphics2D) g;
        g2.setColor(color);
        g2.setStroke(new BasicStroke(thickness));
        g2.drawRoundRect(x, y, width-1, height-1, radius, radius);
    }

    @Override
    public Insets getBorderInsets(Component c) {
        return new Insets(radius+1, radius+1, radius+2, radius);
    }

    @Override
    public Insets getBorderInsets(Component c, Insets insets) {
        insets.left = insets.right = radius;
        insets.top = insets.bottom = radius;
        return insets;
    }
}