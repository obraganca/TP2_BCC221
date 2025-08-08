package com.termo.gui.components;

import javax.swing.*;
import javax.swing.text.*;
import java.awt.*;

public class LetterBox extends JTextField {
    private int limit;

    public LetterBox(int columns, int limit) {
        super(columns);
        this.limit = limit;
        setDocument(new LimitDocument());
    }

    protected Document createDefaultModel() {
        return new LimitDocument();
    }

    private class LimitDocument extends PlainDocument {
        @Override
        public void insertString(int offs, String str, AttributeSet a)
                throws BadLocationException {
            if (str == null) return;

            if ((getLength() + str.length()) <= limit) {
                super.insertString(offs, str.toUpperCase(), a);
            } else {
                Toolkit.getDefaultToolkit().beep();
            }
        }
    }
}