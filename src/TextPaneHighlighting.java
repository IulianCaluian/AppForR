import java.awt.*;
import javax.swing.*;
import javax.swing.text.*;

public class TextPaneHighlighting {

    private static final long serialVersionUID = 1L;
    private Highlighter.HighlightPainter cyanPainter;
    private Highlighter.HighlightPainter redPainter;

    public TextPaneHighlighting() {

    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(new Runnable() {

            @Override
            public void run() {
                TextPaneHighlighting tph = new TextPaneHighlighting();
            }
        });
    }
}