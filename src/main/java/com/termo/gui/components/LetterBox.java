package com.termo.gui.components;

import javax.swing.*;
import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import javax.swing.text.PlainDocument;
import java.awt.*;

public class LetterBox extends JTextField {

    private int limit;

    public LetterBox(int columns, int limit){
        super(columns);
        this.limit = limit;
    }


    public LetterBox( String text, int columns, int max )
    {
        super( text, columns );
        this.limit = limit;
    }

    protected Document createDefaultModel()
    {
        return new LimitDocument();
    }

    private class LimitDocument extends PlainDocument{
        public void insertString(int offs, String str, AttributeSet a)
                throws BadLocationException {
            StringBuffer buffer = new StringBuffer( getText( 0, getLength() ) );
            if ( ( buffer.length() + str.length() ) <= limit )
            {
                super.insertString(offs, str, a);
            }
            else
            {
                Toolkit.getDefaultToolkit().beep();
            }
        }
    }
}
