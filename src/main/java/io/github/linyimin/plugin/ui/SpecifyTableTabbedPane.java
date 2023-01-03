package io.github.linyimin.plugin.ui;

import com.alibaba.fastjson.JSONObject;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.progress.BackgroundTaskQueue;
import com.intellij.openapi.progress.ProgressIndicator;
import com.intellij.openapi.progress.Task;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.ComboBox;
import com.intellij.util.ui.JBUI;
import io.github.linyimin.plugin.ProcessResult;
import io.github.linyimin.plugin.configuration.LexiconComponent;
import io.github.linyimin.plugin.configuration.MockDataSaveComponent;
import io.github.linyimin.plugin.configuration.model.Lexicon;
import io.github.linyimin.plugin.configuration.model.MockDataPrimaryId4Save;
import io.github.linyimin.plugin.constant.Constant;
import io.github.linyimin.plugin.mock.enums.FieldTypeEnum;
import io.github.linyimin.plugin.mock.enums.MockRandomParamTypeEnum;
import io.github.linyimin.plugin.mock.enums.MockTypeEnum;
import io.github.linyimin.plugin.mock.schema.IndexField;
import io.github.linyimin.plugin.mock.schema.Model2Field;
import io.github.linyimin.plugin.mock.schema.TableField;
import io.github.linyimin.plugin.sql.builder.SqlBuilder;
import io.github.linyimin.plugin.sql.checker.Checker;
import io.github.linyimin.plugin.sql.checker.CheckerHolder;
import io.github.linyimin.plugin.sql.checker.Report;
import io.github.linyimin.plugin.sql.checker.enums.CheckScopeEnum;
import io.github.linyimin.plugin.sql.checker.rule.CheckField;
import io.github.linyimin.plugin.sql.converter.ResultConverter;
import io.github.linyimin.plugin.sql.executor.SqlExecutor;
import io.github.linyimin.plugin.sql.result.BaseResult;
import io.github.linyimin.plugin.sql.result.InsertResult;
import io.github.linyimin.plugin.sql.result.SelectResult;
import io.github.linyimin.plugin.utils.MockTypeUtils;
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

import static io.github.linyimin.plugin.constant.Constant.*;
import static javax.swing.ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED;

/**
 * @author yiminlin
 * @date 2022/11/29 01:40
 **/
public class SpecifyTableTabbedPane implements TabbedChangeListener {

    private JPanel specifyTablePanel;
    private JTabbedPane tabbedPane;

    private JPanel schemaTabbedPane;
    private JPanel schemaContentPane;


    private JPanel indexTabbedPane;
    private JPanel indexContentPane;

    private JPanel mockTabbedPane;
    private JPanel mockContentPane;

    private JTable tableSchema;
    private JScrollPane tableSchemaScroll;

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
    private JPanel indexRulePane;
    private RSyntaxTextArea indexRuleText;

    private final InfoPane schemaInfoPane;
    private final InfoPane indexInfoPane;
    private final InfoPane mockInfoPane;

    private ComboBox<String> mockTypes;

    private final Project project;
    private final JTabbedPane parent;

    private final BackgroundTaskQueue backgroundTaskQueue;

    public SpecifyTableTabbedPane(Project project, JTabbedPane parent) {

        this.project = project;
        this.parent = parent;
        this.backgroundTaskQueue = new BackgroundTaskQueue(project, Constant.APPLICATION_NAME);

        this.schemaInfoPane = new InfoPane();
        this.indexInfoPane = new InfoPane();
        this.mockInfoPane = new InfoPane();

        initTableSchemaPane();

        initTableIndexPane();

        initMockPanel();

        setTableRowHeight();

        initMockTypeCombobox();

        addButtonListener();
        addButtonMouseCursorAdapter();

        this.specifyTablePanel.addMouseListener(new MouseCursorAdapter(this.specifyTablePanel));
    }

    private void initMockTypeCombobox() {
        this.mockTypes = new ComboBox<>();
        for (MockTypeEnum type : MockTypeEnum.values()) {
            this.mockTypes.addItem(type.name());
        }

        this.mockTypes.setSelectedItem(MockTypeEnum.random.name());
    }

    private void initTableIndexPane() {
        this.indexTabbedPane.setLayout(new BorderLayout());
        this.indexTabbedPane.remove(this.indexContentPane);
        this.indexTabbedPane.add(this.indexInfoPane.getInfoPane());

        this.indexRuleText = CustomTextField.createArea("sql");

        this.indexRulePane.setLayout(new BorderLayout());

        RTextScrollPane indexRuleScroll = new RTextScrollPane(this.indexRuleText);
        indexRuleScroll.setBorder(new EmptyBorder(JBUI.emptyInsets()));
        indexRuleScroll.setHorizontalScrollBarPolicy(HORIZONTAL_SCROLLBAR_AS_NEEDED);

        indexRulePane.add(indexRuleScroll);

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

        this.mockButton.addActionListener(e -> backgroundTaskQueue.run(new Task.Backgroundable(project, Constant.APPLICATION_NAME) {
            @Override
            public void run(@NotNull ProgressIndicator indicator) {
                saveMockData();
            }
        }));

        this.cleanButton.addActionListener(e -> backgroundTaskQueue.run(new Task.Backgroundable(project, Constant.APPLICATION_NAME) {
            @Override
            public void run(@NotNull ProgressIndicator indicator) {
                cleanMockData();
            }
        }));
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

        List<TableField> fields = generateMockConfig();

        ProcessResult<String> result = checkMockConfig(fields);
        if (!result.isSuccess()) {
            this.mockConfigResultText.setText(result.getErrorMsg());
            return;
        }

        try {

            int rows = acquireMockNum(false);

            int index = 0;

            InsertResult insertResult = new InsertResult();
            int totalAffectedCount = 0;

            MockDataPrimaryId4Save.PrimaryIdInTable primaryIdInTable = new MockDataPrimaryId4Save.PrimaryIdInTable(parent.getTitleAt(parent.getSelectedIndex()), Long.MAX_VALUE, Long.MIN_VALUE);

            long start = System.currentTimeMillis();

            while (index < rows) {

                int end = Math.min(index + INSERT_ROWS, rows);

                int insertRows = end - index;

                String sql = acquireBatchInsertSql(fields, insertRows);

                index += INSERT_ROWS;

                boolean needTotalRows = index >= rows;

                insertResult = SqlExecutor.saveMockData(project, sql, needTotalRows);

                if (index == INSERT_ROWS) {
                    primaryIdInTable.setMinId(insertResult.getLastInsertId() - insertRows + 1);
                }

                totalAffectedCount += insertResult.getAffectedCount();

            }

            MockDataSaveComponent component = project.getComponent(MockDataSaveComponent.class);
            primaryIdInTable.setMaxId(insertResult.getLastInsertId());
            component.addPrimaryIdInTable(primaryIdInTable);

            insertResult.setAffectedCount(totalAffectedCount);
            insertResult.setCost(System.currentTimeMillis() - start);

            this.mockConfigResultText.setText(ResultConverter.convert2InsertInfo(insertResult));

        } catch (Exception e) {
            StringWriter sw = new StringWriter();
            e.printStackTrace(new PrintWriter(sw));
            this.mockConfigResultText.setText(String.format("save mock data error.\n%s", sw));
        }
    }

    private void triggerLexicon() {
        LexiconDialog dialog = new LexiconDialog();
        dialog.setTitle("My Lexicon");

        LexiconComponent lexiconComponent = ApplicationManager.getApplication().getComponent(LexiconComponent.class);
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

        List<TableField> fields = generateMockConfig();
        ProcessResult<String> checkResult = checkMockConfig(fields);
        if (!checkResult.isSuccess()) {
            this.mockConfigResultText.setText(checkResult.getErrorMsg());
            return;
        }

        try {
            int rows = acquireMockNum(true);
            List<String> sqls = new ArrayList<>(rows);
            for (int i = 0; i < rows; i++) {
                sqls.add(acquireInsertSql(fields));
            }

            this.mockConfigResultText.setText(String.join("\n", sqls));

        } catch (Exception e) {
            StringWriter sw = new StringWriter();
            e.printStackTrace(new PrintWriter(sw));
            this.mockConfigResultText.setText(String.format("generate mock data statement error.\n%s", sw));
        }

    }

    private String acquireInsertSql(List<TableField> fields) throws Exception {

        String tableName = parent.getTitleAt(parent.getSelectedIndex());

        return SqlBuilder.buildInsertSql(project, tableName, fields);
    }

    private ProcessResult<String> checkMockConfig(List<TableField> fields) {

        for (TableField field : fields) {
            if (!StringUtils.equals(field.getMockType(), MockTypeEnum.random.name())) {
                continue;
            }

            String type = FieldTypeEnum.resolve(TableField.parseType(field.getType())).getMockType().getValue();
            MockRandomParamTypeEnum mockParamType = MockRandomParamTypeEnum.resolve(field.getMockParam());
            if (!StringUtils.equals(mockParamType.getValue(), type)) {
                return ProcessResult.fail(String.format("The column %s type is %s, but configured as %s", field.getName(), field.getType(), field.getMockParam()));
            }
        }

        return ProcessResult.success(null);
    }

    private String acquireBatchInsertSql(List<TableField> fields, int rows) throws Exception {
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

    private List<TableField> generateMockConfig() {

        DefaultTableModel model = (DefaultTableModel) this.mockConfigTable.getModel();

        List<TableField> fields = Model2Field.parse(TableField.class, model);

        return fields.stream().filter(field -> !StringUtils.equals(MockTypeEnum.none.name(),field.getMockType())).collect(Collectors.toList());

    }

    private void initMockPanel() {

        this.mockConfigResultText = CustomTextField.createArea("sql");

        this.mockConfigResultPanel.setLayout(new BorderLayout());

        RTextScrollPane mockConfigScroll = new RTextScrollPane(mockConfigResultText);
        mockConfigScroll.setBorder(new EmptyBorder(JBUI.emptyInsets()));

        mockConfigResultPanel.add(mockConfigScroll);

        this.mockTabbedPane.setLayout(new BorderLayout());
        this.mockTabbedPane.remove(this.mockContentPane);
        this.mockTabbedPane.add(this.mockInfoPane.getInfoPane());

    }

    private void setTableRowHeight() {
        this.mockConfigTable.setRowHeight(TABLE_ROW_HEIGHT);
        this.tableSchema.setRowHeight(TABLE_ROW_HEIGHT);
        this.indexTable.setRowHeight(TABLE_ROW_HEIGHT);
    }

    private void initTableSchemaPane() {

        this.tableRuleText = CustomTextField.createArea("sql");

        this.tableRulePanel.setLayout(new BorderLayout());

        RTextScrollPane tableRuleScroll = new RTextScrollPane(tableRuleText);
        tableRuleScroll.setBorder(new EmptyBorder(JBUI.emptyInsets()));
        tableRuleScroll.setHorizontalScrollBarPolicy(HORIZONTAL_SCROLLBAR_AS_NEEDED);

        tableRulePanel.add(tableRuleScroll);

        this.schemaTabbedPane.setLayout(new BorderLayout());
        this.schemaTabbedPane.remove(this.schemaContentPane);
        this.schemaTabbedPane.add(this.schemaInfoPane.getInfoPane());

    }

    public JPanel getSpecifyTablePanel() {
        return specifyTablePanel;
    }

    private void setTables(DefaultTableModel metaModel, DefaultTableModel indexModel) {

        checkTableRule(metaModel);
        this.tableSchema.setModel(metaModel);

        this.mockConfigTable.setModel(MockTypeUtils.copyModel(metaModel));
        MockTypeUtils.addMockColumns(this.mockConfigTable, this.mockTypes, false);

        this.mockConfigResultText.setText("1. 「Lexicon」创建词库\n" +
                "2. 「Preview」根据配置生成50条insert预览语句\n" +
                "3. 「Mock」往数据库中插入mock数据\n" +
                "4. 「Clean」清空数据库中Mock的数据");

        this.indexTable.setModel(indexModel);
        checkIndexRule(metaModel, indexModel);
    }

    private void checkTableRule(DefaultTableModel model) {
        // check table name
        Checker checker = CheckerHolder.getChecker(CheckScopeEnum.naming_convention);
        List<Report> reports = checker.check(project, parent.getTitleAt(parent.getSelectedIndex()));

        boolean isPass = reports.stream().allMatch(Report::isPass);

        if (!isPass) {
            this.tableRuleText.setText(ResultConverter.convert2RuleInfo(CheckScopeEnum.naming_convention, reports));
            return;
        }

        // check table field
        checker = CheckerHolder.getChecker(CheckScopeEnum.field);
        List<TableField> tableFields = Model2Field.parse(TableField.class, model);
        for (TableField field : tableFields) {
            reports = checker.check(project, JSONObject.toJSONString(field));
            isPass = reports.stream().allMatch(Report::isPass);
            if (!isPass) {
                this.tableRuleText.setText(ResultConverter.convert2RuleInfo(CheckScopeEnum.naming_convention, reports));
                return;
            }
        }

        // check table field composition
        checker = CheckerHolder.getChecker(CheckScopeEnum.field_composition);
        reports = checker.check(project, JSONObject.toJSONString(tableFields));

        this.tableRuleText.setText(ResultConverter.convert2RuleInfo(CheckScopeEnum.naming_convention, reports));
    }

    private void checkIndexRule(DefaultTableModel tableModel, DefaultTableModel indexModel) {
        Checker checker = CheckerHolder.getChecker(CheckScopeEnum.index_field);
        if (checker == null) {
            this.indexRuleText.setText("No checker for index checker");
            return;
        }
        List<TableField> tableFields = Model2Field.parse(TableField.class, tableModel);
        List<IndexField> indexFields = Model2Field.parse(IndexField.class, indexModel);

        CheckField checkField = new CheckField(tableFields, indexFields);
        List<Report> reports = checker.check(project, JSONObject.toJSONString(checkField));
        this.indexRuleText.setText(ResultConverter.convert2RuleInfo(CheckScopeEnum.index_field, reports));
    }

    @Override
    public void listen() {

    }

    public void acquireTableSchema(String table) {

        this.resetInfoPane();

        String metaSql = TABLE_META_SQL_TEMPLATE.replace("${table}", table);
        String indexSql = TABLE_INDEX_SQL_TEMPLATE.replace("${table}", table);

        try {

            SelectResult metaResult = (SelectResult) SqlExecutor.executeSql(project, metaSql, false);
            SelectResult indexResult = (SelectResult) SqlExecutor.executeSql(project, indexSql, false);

            this.setTables(metaResult.getModel(), indexResult.getModel());

            this.resetContentPane();

        } catch (Exception e) {
            StringWriter sw = new StringWriter();
            e.printStackTrace(new PrintWriter(sw));
            this.schemaInfoPane.setText(sw.toString());
            this.indexInfoPane.setText(sw.toString());
            this.mockInfoPane.setText(sw.toString());
        }
    }

    private void resetInfoPane() {
        ApplicationManager.getApplication().invokeLater(() -> {
            this.schemaTabbedPane.remove(this.schemaContentPane);
            this.schemaTabbedPane.add(this.schemaInfoPane.getInfoPane());

            this.indexTabbedPane.remove(this.indexContentPane);
            this.indexTabbedPane.add(this.indexInfoPane.getInfoPane());

            this.mockTabbedPane.remove(this.mockContentPane);
            this.mockTabbedPane.add(this.mockInfoPane.getInfoPane());

            this.schemaInfoPane.setText("Loading table schema...");
        });
    }

    private void resetContentPane() {
        ApplicationManager.getApplication().invokeLater(() -> {
            this.schemaTabbedPane.remove(this.schemaInfoPane.getInfoPane());
            this.schemaTabbedPane.add(this.schemaContentPane);

            this.indexTabbedPane.remove(this.indexInfoPane.getInfoPane());
            this.indexTabbedPane.add(this.indexContentPane);

            this.mockTabbedPane.remove(this.mockInfoPane.getInfoPane());
            this.mockTabbedPane.add(this.mockContentPane);
        });
    }

}
