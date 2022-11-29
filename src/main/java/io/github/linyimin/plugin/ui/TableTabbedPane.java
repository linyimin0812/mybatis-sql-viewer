package io.github.linyimin.plugin.ui;

import com.intellij.openapi.ui.ComboBox;
import com.intellij.util.ui.JBUI;
import io.github.linyimin.plugin.mock.enums.MockRandomParamTypeEnum;
import io.github.linyimin.plugin.mock.enums.MockTypeEnum;
import org.apache.commons.lang3.StringUtils;
import org.fife.ui.rsyntaxtextarea.RSyntaxTextArea;
import org.fife.ui.rtextarea.RTextScrollPane;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.*;
import java.awt.*;
import java.util.Vector;

/**
 * @author yiminlin
 * @date 2022/11/29 01:40
 **/
public class TableTabbedPane {

    public static final String MOCK_TYPE_COLUMN_NAME = "mock type";
    public static final String MOCK_VALUE_COLUMN_NAME = "mock value";

    private JTabbedPane tabbedPane;
    private JPanel specifyTablePanel;
    private JPanel tableSchemaPanel;
    private JScrollPane tableSchemaScroll;
    private JTable tableSchema;

    private RSyntaxTextArea tableRuleText;
    private JPanel tableRulePanel;

    private JScrollPane mockConfigScroll;
    private JTable mockConfigTable;
    private JTextField mockNum;

    private JPanel mockConfigResultPanel;
    private RSyntaxTextArea mockConfigResultText;


    private JButton saveConfigButton;
    private JButton newLexiconButton;
    private JButton previewButton;
    private JButton mockButton;

    public TableTabbedPane() {

        initTableRule();

        initMockPanel();
    }

    private void initMockPanel() {

        this.mockConfigResultText = CustomTextField.createArea("sql");

        this.mockConfigResultPanel.setLayout(new BorderLayout());

        RTextScrollPane mockConfigScroll = new RTextScrollPane(mockConfigResultText);
        mockConfigScroll.setBorder(new EmptyBorder(JBUI.emptyInsets()));

        mockConfigResultPanel.add(mockConfigScroll);
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

    public JScrollPane getMockConfigScroll() {
        return mockConfigScroll;
    }

    public JTable getMockConfigTable() {
        return mockConfigTable;
    }

    public void setMockConfigTable(DefaultTableModel model) {
        DefaultTableModel mockTable = copyModel(model);
        addMockColumns(mockTable);
    }

    public JPanel getMockConfigResultPanel() {
        return mockConfigResultPanel;
    }

    public JTextField getMockNum() {
        return mockNum;
    }

    public RSyntaxTextArea getMockConfigResultText() {
        return mockConfigResultText;
    }

    private DefaultTableModel copyModel(DefaultTableModel model) {

        Vector<String> columnIdentifiers = new Vector<>();
        for (int i = 0; i < model.getColumnCount(); i++) {
            columnIdentifiers.add(model.getColumnName(i));
        }

        Vector dataVector = new Vector(model.getDataVector());

        return new DefaultTableModel(dataVector, columnIdentifiers);

    }

    private void addMockColumns(DefaultTableModel model) {

        Vector<String> mockTypes = new Vector<>();
        for (int i = 0; i < model.getRowCount(); i++) {
            mockTypes.add(MockTypeEnum.random.name());
        }

        model.addColumn(MOCK_TYPE_COLUMN_NAME, mockTypes);
        model.addColumn(MOCK_VALUE_COLUMN_NAME, new Vector<>());

        this.mockConfigTable.setModel(model);

        TableColumnModel columnModel = this.mockConfigTable.getColumnModel();

        TableColumn typeColumn = columnModel.getColumn(columnModel.getColumnIndex(MOCK_TYPE_COLUMN_NAME));

        ComboBox<String> typeComboBox = new ComboBox<>();
        for (MockTypeEnum type : MockTypeEnum.values()) {
            typeComboBox.addItem(type.name());
        }

        typeComboBox.setSelectedIndex(0);

        typeColumn.setCellEditor(new DefaultCellEditor(typeComboBox));

        TableColumn valueColumn = columnModel.getColumn(columnModel.getColumnIndex(MOCK_VALUE_COLUMN_NAME));

        valueColumn.setCellEditor(new DefaultCellEditor(new ComboBox<>()));

        typeComboBox.addActionListener(e -> mockTypeSelectionListener(typeComboBox, valueColumn));

    }

    private void mockTypeSelectionListener(ComboBox<String> typeCombobox, TableColumn valueColumn) {

        String type = (String) typeCombobox.getSelectedItem();

        String toolTipText = "";

        if (StringUtils.equals(type, MockTypeEnum.random.name())) {
            toolTipText = "random rule";
            ComboBox<String> comboBox = new ComboBox<>();
            for (MockRandomParamTypeEnum randomType : MockRandomParamTypeEnum.values()) {
                comboBox.addItem(randomType.name());
            }
            valueColumn.setCellEditor(new DefaultCellEditor(comboBox));
        }

        if (StringUtils.equals(type, MockTypeEnum.increment.name())) {
            toolTipText = "initial value";
            valueColumn.setCellEditor(new DefaultCellEditor(new JTextField()));
        }

        if (StringUtils.equals(type, MockTypeEnum.fixed.name())) {
            toolTipText = "constant value";
            valueColumn.setCellEditor(new DefaultCellEditor(new JTextField()));
        }

        if (StringUtils.equals(type, MockTypeEnum.regex.name())) {
            toolTipText = "regular expression";
            valueColumn.setCellEditor(new DefaultCellEditor(new JTextField()));
        }

        if (StringUtils.equals(type, MockTypeEnum.lexicon.name())) {
            toolTipText = "lexicon";
            // TODO: 获取词库列表
            valueColumn.setCellEditor(new DefaultCellEditor(new ComboBox<>()));
        }

        if (StringUtils.equals(type, MockTypeEnum.database.name())) {
            toolTipText = "field from table";
            valueColumn.setCellEditor(new DefaultCellEditor(new JTextField()));
        }

        if (StringUtils.equals(type, MockTypeEnum.none.name())) {
            valueColumn.setCellEditor(new DefaultCellEditor(new JTextField("————")));
        }

        DefaultTableCellRenderer renderer = new DefaultTableCellRenderer();
        renderer.setToolTipText(toolTipText);
        valueColumn.setCellRenderer(renderer);
    }
}
