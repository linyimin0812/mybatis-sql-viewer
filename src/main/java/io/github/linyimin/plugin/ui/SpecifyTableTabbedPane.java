package io.github.linyimin.plugin.ui;

import com.alibaba.fastjson.JSONObject;
import com.google.gson.GsonBuilder;
import com.intellij.openapi.progress.BackgroundTaskQueue;
import com.intellij.openapi.progress.ProgressIndicator;
import com.intellij.openapi.progress.Task;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.ComboBox;
import com.intellij.util.ui.JBUI;
import io.github.linyimin.plugin.configuration.LexiconComponent;
import io.github.linyimin.plugin.configuration.MockDataSaveComponent;
import io.github.linyimin.plugin.configuration.model.Lexicon;
import io.github.linyimin.plugin.configuration.model.MockDataPrimaryId4Save;
import io.github.linyimin.plugin.constant.Constant;
import io.github.linyimin.plugin.mock.enums.MockRandomParamTypeEnum;
import io.github.linyimin.plugin.mock.enums.MockTypeEnum;
import io.github.linyimin.plugin.mock.schema.Field;
import io.github.linyimin.plugin.sql.builder.SqlBuilder;
import io.github.linyimin.plugin.sql.converter.ResultConverter;
import io.github.linyimin.plugin.sql.executor.SqlExecutor;
import io.github.linyimin.plugin.sql.result.BaseResult;
import io.github.linyimin.plugin.sql.result.InsertResult;
import org.apache.commons.lang3.StringUtils;
import org.fife.ui.rsyntaxtextarea.RSyntaxTextArea;
import org.fife.ui.rtextarea.RTextScrollPane;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.*;
import java.awt.*;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.*;
import java.util.List;
import java.util.stream.Collectors;

import static io.github.linyimin.plugin.constant.Constant.INSERT_ROWS;
import static io.github.linyimin.plugin.constant.Constant.TABLE_ROW_HEIGHT;
import static javax.swing.ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED;

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
    private JButton cleanButton;

    private JTable indexTable;

    private final Project project;
    private final JTabbedPane parent;

    private final BackgroundTaskQueue backgroundTaskQueue;

    public SpecifyTableTabbedPane(Project project, JTabbedPane parent) {

        this.project = project;
        this.parent = parent;
        this.backgroundTaskQueue = new BackgroundTaskQueue(project, Constant.APPLICATION_NAME);

        initTableRule();

        initMockPanel();

        setTableRowHeight();

        addButtonListener();
        addButtonMouseCursorAdapter();
    }

    private void addButtonMouseCursorAdapter() {
        this.lexiconButton.addMouseListener(new MouseCursorAdapter(this.lexiconButton));
        this.previewButton.addMouseListener(new MouseCursorAdapter(this.previewButton));
        this.mockButton.addMouseListener(new MouseCursorAdapter(this.mockButton));
        this.cleanButton.addMouseListener(new MouseCursorAdapter(this.cleanButton));
    }

    private void addButtonListener() {

        this.previewButton.addActionListener(e -> previewMockData());
        this.lexiconButton.addActionListener(e -> triggerLexicon());

        this.mockButton.addActionListener(e -> {
            backgroundTaskQueue.run(new Task.Backgroundable(project, Constant.APPLICATION_NAME) {
                @Override
                public void run(@NotNull ProgressIndicator indicator) {
                    saveMockData();
                }
            });
        });

        this.cleanButton.addActionListener(e -> {
            backgroundTaskQueue.run(new Task.Backgroundable(project, Constant.APPLICATION_NAME) {
                @Override
                public void run(@NotNull ProgressIndicator indicator) {
                    cleanMockData();
                }
            });
        });
    }

    private void cleanMockData() {

        this.mockConfigResultText.setText("Cleaning mock data...");

        MockDataSaveComponent component = project.getComponent(MockDataSaveComponent.class);
        String table = parent.getTitleAt(parent.getSelectedIndex());

        MockDataPrimaryId4Save.PrimaryIdInTable primaryIdInTable = component.getPrimaryIdInTable(table);

        if (primaryIdInTable == null) {
            this.mockConfigResultText.setText("There is no mock data to clean.");
            return;
        }

        try {
            String primaryId = SqlExecutor.acquirePrimaryKey(project, table);

            String sql = String.format("DELETE FROM %s WHERE %s BETWEEN %s AND %s;", table, primaryId, primaryIdInTable.getMinId(), primaryIdInTable.getMaxId());

            BaseResult result = SqlExecutor.executeSql(project, sql, true);

            component.remove(table);

            this.mockConfigResultText.setText(ResultConverter.convert2ExecuteInfo(result));

        } catch (Exception e) {
            StringWriter sw = new StringWriter();
            e.printStackTrace(new PrintWriter(sw));
            this.mockConfigResultText.setText(String.format("clean mock data failed.\n%s", sw));
        }

    }

    private void saveMockData() {

        this.mockConfigResultText.setText("Saving mock data...");

        try {

            int rows = acquireMockNum(false);

            int index = 0;

            InsertResult result = new InsertResult();
            int totalAffectedCount = 0;

            MockDataPrimaryId4Save.PrimaryIdInTable primaryIdInTable = new MockDataPrimaryId4Save.PrimaryIdInTable(parent.getTitleAt(parent.getSelectedIndex()), Long.MAX_VALUE, Long.MIN_VALUE);

            long start = System.currentTimeMillis();

            while (index < rows) {

                int end = Math.min(index + INSERT_ROWS, rows);

                int insertRows = end - index;

                String sql = acquireBatchInsertSql(insertRows);

                index += INSERT_ROWS;

                boolean needTotalRows = index >= rows;

                result = SqlExecutor.saveMockData(project, sql, needTotalRows);

                if (index == INSERT_ROWS) {
                    primaryIdInTable.setMinId(result.getLastInsertId() - insertRows + 1);
                }

                totalAffectedCount += result.getAffectedCount();

            }

            MockDataSaveComponent component = project.getComponent(MockDataSaveComponent.class);
            primaryIdInTable.setMaxId(result.getLastInsertId());
            component.addPrimaryIdInTable(primaryIdInTable);

            result.setAffectedCount(totalAffectedCount);
            result.setCost(System.currentTimeMillis() - start);

            this.mockConfigResultText.setText(ResultConverter.convert2InsertInfo(result));

        } catch (Exception e) {
            StringWriter sw = new StringWriter();
            e.printStackTrace(new PrintWriter(sw));
            this.mockConfigResultText.setText(String.format("save mock data error.\n%s", sw));
        }
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
        try {
            int rows = acquireMockNum(true);
            List<String> sqls = new ArrayList<>(rows);
            for (int i = 0; i < rows; i++) {
                sqls.add(acquireInsertSql());
            }

            this.mockConfigResultText.setText(String.join("\n", sqls));

        } catch (Exception e) {
            StringWriter sw = new StringWriter();
            e.printStackTrace(new PrintWriter(sw));
            this.mockConfigResultText.setText(String.format("generate mock data statement error.\n%s", sw));
        }

    }

    private String acquireInsertSql() throws Exception {

        List<Field> fields = generateMockConfig();
        String tableName = parent.getTitleAt(parent.getSelectedIndex());

        return SqlBuilder.buildInsertSql(project, tableName, fields);
    }

    private String acquireBatchInsertSql(int rows) throws Exception {
        List<Field> fields = generateMockConfig();
        String tableName = parent.getTitleAt(parent.getSelectedIndex());
        return SqlBuilder.buildInsertSqlBatch(project, tableName, fields, rows);
    }

    private int acquireMockNum(boolean isPreview) {

        String mockNumStr = this.mockNum.getText();
        if (StringUtils.isBlank(mockNumStr) || !StringUtils.isNumeric(mockNumStr)) {
            mockNumStr = "100";
        }

        int rows = Integer.parseInt(mockNumStr);

        return isPreview ? Math.min(rows, 50) : rows;
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

        List<Field> fields = JSONObject.parseArray(config, Field.class);

        return fields.stream().filter(field -> !StringUtils.equals(MockTypeEnum.none.name(),field.getMockType())).collect(Collectors.toList());

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

        this.tableRuleText = CustomTextField.createArea("sql");

        this.tableRulePanel.setLayout(new BorderLayout());

        RTextScrollPane tableRuleScroll = new RTextScrollPane(tableRuleText);
        tableRuleScroll.setBorder(new EmptyBorder(JBUI.emptyInsets()));
        tableRuleScroll.setHorizontalScrollBarPolicy(HORIZONTAL_SCROLLBAR_AS_NEEDED);

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
