package io.github.linyimin.plugin.ui;

import com.intellij.util.ui.JBUI;
import org.fife.ui.rsyntaxtextarea.RSyntaxTextArea;
import org.fife.ui.rtextarea.RTextScrollPane;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;

/**
 * @author yiminlin
 * @date 2022/11/29 01:40
 **/
public class TableTabbedPane {
    private JTabbedPane tabbedPane;
    private JPanel specifyTablePanel;
    private JPanel tableSchemaPanel;
    private JScrollPane tableSchemaScroll;
    private JTable tableSchema;

    private JPanel tableRulePanel;
    private RSyntaxTextArea tableRuleText;

    public TableTabbedPane() {

        initTableRule();
    }

    private void initTableRule() {

        tableRuleText = CustomTextField.createArea("sql");

        tableRulePanel.setLayout(new BorderLayout());

        RTextScrollPane tableRuleScroll = new RTextScrollPane(tableRuleText);
        tableRuleScroll.setBorder(new EmptyBorder(JBUI.emptyInsets()));

        tableRulePanel.add(tableRuleScroll);
    }

    public JTabbedPane getTabbedPane() {
        return tabbedPane;
    }

    public JPanel getSpecifyTablePanel() {
        return specifyTablePanel;
    }

    public JPanel getTableSchemaPanel() {
        return tableSchemaPanel;
    }

    public JPanel getTableRulePanel() {
        return tableRulePanel;
    }

    public JScrollPane getTableSchemaScroll() {
        return tableSchemaScroll;
    }

    public JTable getTableSchema() {
        return tableSchema;
    }

    public RSyntaxTextArea getTableRuleText() {
        return tableRuleText;
    }
}
