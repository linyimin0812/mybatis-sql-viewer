package io.github.linyimin.plugin.ui;

import com.alibaba.fastjson.JSONObject;
import com.google.gson.GsonBuilder;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.ComboBox;
import com.intellij.util.ui.JBUI;
import io.github.linyimin.plugin.configuration.LexiconComponent;
import io.github.linyimin.plugin.configuration.model.Lexicon;
import io.github.linyimin.plugin.mock.enums.MockRandomParamTypeEnum;
import io.github.linyimin.plugin.mock.enums.MockTypeEnum;
import io.github.linyimin.plugin.mock.schema.Field;
import io.github.linyimin.plugin.sql.builder.SqlBuilder;
import org.apache.commons.lang3.StringUtils;
import org.fife.ui.rsyntaxtextarea.RSyntaxTextArea;
import org.fife.ui.rtextarea.RTextScrollPane;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.*;
import java.awt.*;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.*;
import java.util.List;

import static io.github.linyimin.plugin.constant.Constant.TABLE_ROW_HEIGHT;

/**
 * @author yiminlin
 * @date 2022/11/29 01:40
 **/
public class SpecifyTableTabbedPane implements TabbedChangeListener {

    public static final String MOCK_TYPE_COLUMN_NAME = "Mock Type";
    public static final String MOCK_VALUE_COLUMN_NAME = "Mock Value";

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

    private JButton lexiconButton;
    private JButton previewButton;
    private JButton mockButton;

    private JTable indexTable;

    private final Project project;
    private final JTabbedPane parent;

    public SpecifyTableTabbedPane(Project project, JTabbedPane parent) {

        this.project = project;
        this.parent = parent;

        initTableRule();

        initMockPanel();

        setTableRowHeight();

        addButtonListener();
    }

    private void addButtonListener() {
        this.previewButton.addActionListener(e -> previewMockData());
        this.lexiconButton.addActionListener(e -> triggerLexicon());
    }

    private void triggerLexicon() {
        LexiconDialog dialog = new LexiconDialog(project);
        dialog.setTitle("My Lexicon");

        LexiconComponent lexiconComponent = project.getComponent(LexiconComponent.class);
        List<Lexicon> lexicons = lexiconComponent.getConfig().getLexicons();

        Vector<Vector<String>> dataVector = new Vector<>();
        for (Lexicon lexicon : lexicons) {
            Vector<String> row = new Vector<>();
            row.add(lexicon.getName());
            row.add(lexicon.getContent());
            dataVector.add(row);
        }

        Vector<String> columnIdentifiers = new Vector<>();
        columnIdentifiers.add("name");
        columnIdentifiers.add("content");
        DefaultTableModel model = new DefaultTableModel(dataVector, columnIdentifiers);

        dialog.getLexiconTable().setModel(model);

        dialog.pack();
        dialog.setVisible(true);
    }

    private void previewMockData() {

        String mockNumStr = this.mockNum.getText();
        if (StringUtils.isBlank(mockNumStr) || !StringUtils.isNumeric(mockNumStr)) {
            this.mockConfigResultText.setText("mock number should be a integer.");
            return;
        }

        int rows = Integer.parseInt(mockNumStr);

        List<Field> fields = generateMockConfig();
        String tableName = parent.getTitleAt(parent.getSelectedIndex());

        try {
            String sql = SqlBuilder.buildInsertSql(project, tableName, fields, rows);
            this.mockConfigResultText.setText(sql);

        } catch (Exception e) {
            StringWriter sw = new StringWriter();
            e.printStackTrace(new PrintWriter(sw));
            this.mockConfigResultText.setText(String.format("generate sql error.\n%s", sw));
        }
    }

    private List<Field> generateMockConfig() {

        DefaultTableModel model = (DefaultTableModel) this.mockConfigTable.getModel();
        Vector<Map<String, String>> configs = new Vector<>();
        Vector dataVector = model.getDataVector();
        for (Object values : dataVector) {
            Map<String, String> config = new HashMap<>();
            Vector vector = (Vector) values;
            for (int i = 0; i < model.getColumnCount(); i++) {
                String value =  vector.get(i) == null ? StringUtils.EMPTY : (String) vector.get(i);
                config.put(model.getColumnName(i), value);
            }
            configs.add(config);
        }
        String config = new GsonBuilder().setPrettyPrinting().create().toJson(configs);
        this.mockConfigResultText.setText(config);

        return JSONObject.parseArray(config, Field.class);

    }

    private void initMockPanel() {

        this.mockConfigResultText = CustomTextField.createArea("sql");

        this.mockConfigResultPanel.setLayout(new BorderLayout());

        RTextScrollPane mockConfigScroll = new RTextScrollPane(mockConfigResultText);
        mockConfigScroll.setBorder(new EmptyBorder(JBUI.emptyInsets()));

        mockConfigResultPanel.add(mockConfigScroll);
    }

    private void setTableRowHeight() {
        this.mockConfigTable.setRowHeight(TABLE_ROW_HEIGHT);
        this.tableSchema.setRowHeight(TABLE_ROW_HEIGHT);
        this.indexTable.setRowHeight(TABLE_ROW_HEIGHT);
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

    public void setTables(DefaultTableModel metaModel, DefaultTableModel indexModel) {

        this.tableSchema.setModel(metaModel);

        DefaultTableModel mockTable = copyModel(metaModel);
        addMockColumns(mockTable);

        this.indexTable.setModel(indexModel);
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

        // TODO: 根据field类型自动填充mock类型
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
            List<Lexicon> lexicons = project.getComponent(LexiconComponent.class).getConfig().getLexicons();
            ComboBox<String> comboBox = new ComboBox<>();
            for (Lexicon lexicon : lexicons) {
                comboBox.addItem(lexicon.getName());
            }
            valueColumn.setCellEditor(new DefaultCellEditor(comboBox));
        }

        if (StringUtils.equals(type, MockTypeEnum.database.name())) {
            toolTipText = "field from table";
            valueColumn.setCellEditor(new DefaultCellEditor(new JTextField()));
        }

        if (StringUtils.equals(type, MockTypeEnum.none.name())) {
            valueColumn.setCellEditor(new DefaultCellEditor(new JTextField()));
        }

        DefaultTableCellRenderer renderer = new DefaultTableCellRenderer();
        renderer.setToolTipText(toolTipText);
        valueColumn.setCellRenderer(renderer);
    }

    @Override
    public void listen() {

    }
}
