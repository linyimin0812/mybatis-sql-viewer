package io.github.linyimin.plugin.ui;

import com.intellij.util.ui.JBUI;
import org.fife.ui.rsyntaxtextarea.RSyntaxTextArea;
import org.fife.ui.rtextarea.RTextScrollPane;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;

/**
 * @author banzhe
 * @date 2022/12/11 20:15
 **/
public class InfoPane {

    private JPanel infoPane;

    private final RSyntaxTextArea errorText;

    public InfoPane() {

        errorText = CustomTextField.createArea("sql");
        RTextScrollPane errorScroll = new RTextScrollPane(errorText);
        errorScroll.setBorder(new EmptyBorder(JBUI.emptyInsets()));

        infoPane.setLayout(new BorderLayout());
        infoPane.add(errorScroll);
    }

    public void setText(String text) {
        errorText.setText(text);
    }

    public JPanel getInfoPane() {
        return infoPane;
    }

}
