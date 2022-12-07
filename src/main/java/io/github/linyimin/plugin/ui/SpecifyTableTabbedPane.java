package io.github.linyimin.plugin.ui;

import com.alibaba.fastjson.JSONObject;
import com.google.gson.GsonBuilder;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.ComboBox;
import com.intellij.util.ui.JBUI;
import io.github.linyimin.plugin.configuration.LexiconComponent;
import io.github.linyimin.plugin.configuration.MockDataSaveComponent;
import io.github.linyimin.plugin.configuration.model.Lexicon;
import io.github.linyimin.plugin.configuration.model.MockDataPrimaryId4Save;
import io.github.linyimin.plugin.mock.enums.MockRandomParamTypeEnum;
import io.github.linyimin.plugin.mock.enums.MockTypeEnum;
import io.github.linyimin.plugin.mock.schema.Field;
import io.github.linyimin.plugin.sql.builder.SqlBuilder;
import io.github.linyimin.plugin.sql.converter.ResultConverter;
import io.github.linyimin.plugin.sql.executor.SqlExecutor;
import io.github.linyimin.plugin.sql.result.BaseResult;
import io.github.linyimin.plugin.sql.result.InsertResult;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.Pair;
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
import java.util.concurrent.*;
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

    private final ThreadPoolExecutor EXECUTOR_POOL = new ThreadPoolExecutor(8, 8, 0, TimeUnit.SECONDS, new LinkedBlockingQueue<>());

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
        this.mockButton.addActionListener(e -> saveMockData());
        this.cleanButton.addActionListener(e -> cleanMockData());
    }

    private void cleanMockData() {
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

        try {
            List<String> sqls = acquireInsertSqls(false, true);

            MockDataPrimaryId4Save.PrimaryIdInTable primaryIdInTable = new MockDataPrimaryId4Save.PrimaryIdInTable(parent.getTitleAt(parent.getSelectedIndex()), Long.MAX_VALUE, Long.MIN_VALUE);

            List<Future<InsertResult>> tasks = new ArrayList<>();

            for (String sql : sqls) {
                Future<InsertResult> task = EXECUTOR_POOL.submit(() -> SqlExecutor.saveMockData(project, sql));
                tasks.add(task);
            }

            int affectedCount = 0;
            InsertResult result = null;
            long totalRows= 0;

            for (Future<InsertResult> future : tasks) {

                result = future.get();

                long minId = Math.min(result.getLastInsertId() - INSERT_ROWS + 1, primaryIdInTable.getMinId());
                long maxId = Math.max(result.getLastInsertId(), primaryIdInTable.getMaxId());

                primaryIdInTable.setMinId(minId);
                primaryIdInTable.setMaxId(maxId);

                affectedCount += result.getAffectedCount();
                totalRows = Math.max(totalRows, result.getTotalRows().get(0).getValue());
            }

            result.setAffectedCount(affectedCount);
            Pair<String, Long> pair = Pair.of(result.getTotalRows().get(0).getLeft(), result.getTotalRows().get(0).getRight());
            result.setTotalRows(Collections.singletonList(pair));

            MockDataSaveComponent component = project.getComponent(MockDataSaveComponent.class);
            component.addPrimaryIdInTable(primaryIdInTable);

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
            List<String> sqls = acquireInsertSqls(true, false);
            this.mockConfigResultText.setText(String.join("\n", sqls));
        } catch (Exception e) {
            StringWriter sw = new StringWriter();
            e.printStackTrace(new PrintWriter(sw));
            this.mockConfigResultText.setText(String.format("generate mock data statement error.\n%s", sw));
        }

    }

    private List<String> acquireInsertSqls(boolean isPreview, boolean batch) throws Exception {

        String mockNumStr = this.mockNum.getText();
        if (StringUtils.isBlank(mockNumStr) || !StringUtils.isNumeric(mockNumStr)) {
            throw new Exception("mock number should be an integer.");
        }

        int rows = Integer.parseInt(mockNumStr);

        if (isPreview) {
            // 最多预览50条数据，避免等待多长时间
            rows = Math.min(rows, 50);
        }

        List<Field> fields = generateMockConfig();
        String tableName = parent.getTitleAt(parent.getSelectedIndex());

        return SqlBuilder.buildInsertSql(project, tableName, fields, rows, batch);
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
