package io.github.linyimin.plugin.ui;

import com.intellij.openapi.ui.Messages;
import com.intellij.ui.JBColor;
import com.intellij.util.ui.UIUtil;
import io.github.linyimin.plugin.constant.Constant;
import org.fife.ui.rsyntaxtextarea.*;

import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import java.awt.*;

/**
 * @author banzhe
 * @date 2022/11/22 16:17
 **/
public class CustomTextField {

    public static RSyntaxTextArea createArea(String type) {

        RSyntaxTextArea area = new RSyntaxTextArea();
        area.setDocument(new MaxLengthDocument(5000000));

        if ("JSON".equalsIgnoreCase(type)) {
            area.setSyntaxEditingStyle(SyntaxConstants.SYNTAX_STYLE_JSON);
        } else if ("SQL".equalsIgnoreCase(type)) {
            area.setSyntaxEditingStyle(SyntaxConstants.SYNTAX_STYLE_SQL);
        }

        if (UIUtil.isUnderDarcula()) {
            try {
                Theme theme = Theme.load(CustomTextField.class.getResourceAsStream("/org/fife/ui/rsyntaxtextarea/themes/dark.xml"));
                theme.apply(area);
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else {
            SyntaxScheme scheme = area.getSyntaxScheme();

            scheme.getStyle(Token.LITERAL_BOOLEAN).foreground = new JBColor(new Color(167, 29, 93), new Color(167, 29, 93));

            scheme.getStyle(Token.LITERAL_NUMBER_DECIMAL_INT).foreground = new JBColor(new Color(0, 134, 179), new Color(0, 134, 179));

            scheme.getStyle(Token.LITERAL_STRING_DOUBLE_QUOTE).foreground = new JBColor(new Color(24, 54, 145), new Color(24, 54, 145));
            scheme.getStyle(Token.VARIABLE).foreground = new JBColor(new Color(0, 134, 179), new Color(0, 134, 179));
            scheme.getStyle(Token.LITERAL_CHAR).foreground = new JBColor(new Color(99, 163, 92), new Color(99, 163, 92));

            scheme.getStyle(Token.SEPARATOR).foreground = new JBColor(new Color(99, 163, 92), new Color(99, 163, 92));
            scheme.getStyle(Token.OPERATOR).foreground = new JBColor(new Color(99, 163, 92), new Color(99, 163, 92));

            scheme.getStyle(Token.RESERVED_WORD).foreground = new JBColor(new Color(167, 29, 93), new Color(167, 29, 93));
            scheme.getStyle(Token.RESERVED_WORD_2).foreground = new JBColor(new Color(167, 29, 93), new Color(167, 29, 93));
        }

        area.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 13));

        area.setCodeFoldingEnabled(true);
        area.setAntiAliasingEnabled(true);
        area.setAutoscrolls(true);

        return area;
    }

    public static class MaxLengthDocument extends RSyntaxDocument {
        int maxChars;

        public MaxLengthDocument(int max) {
            super(SYNTAX_STYLE_NONE);
            maxChars = max;
        }

        @Override
        public void insertString(int offset, String s, AttributeSet a) throws BadLocationException {
            try {
                if (getLength() + s.length() > maxChars) {
                    Toolkit.getDefaultToolkit().beep();
                    tipDia("内容过长，最大" + maxChars + "个字符!");
                    return;
                }
                super.insertString(offset, s, a);
            } catch (Exception e) {
                System.out.println(e.getMessage());
            }
        }
    }

    private static void tipDia(String msg) {
        Messages.showInfoMessage(msg, Constant.APPLICATION_NAME);
    }

}
