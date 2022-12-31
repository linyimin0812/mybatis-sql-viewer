package io.github.linyimin.plugin.ui;

import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.progress.BackgroundTaskQueue;
import com.intellij.openapi.progress.ProgressIndicator;
import com.intellij.openapi.progress.Task;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.ComboBox;
import com.intellij.util.ui.JBUI;
import io.github.linyimin.plugin.ProcessResult;
import io.github.linyimin.plugin.component.SqlParamGenerateComponent;
import io.github.linyimin.plugin.configuration.MybatisSqlStateComponent;
import io.github.linyimin.plugin.configuration.model.MybatisSqlConfiguration;
import io.github.linyimin.plugin.constant.Constant;
import io.github.linyimin.plugin.mock.enums.MockTypeEnum;
import io.github.linyimin.plugin.mock.generator.DataGenerator;
import io.github.linyimin.plugin.mock.generator.DataGeneratorFactory;
import io.github.linyimin.plugin.mock.schema.Model2Field;
import io.github.linyimin.plugin.mock.schema.TableField;
import io.github.linyimin.plugin.sql.executor.SqlExecutor;
import io.github.linyimin.plugin.sql.formatter.MysqlFormatter;
import io.github.linyimin.plugin.sql.parser.SqlParser;
import io.github.linyimin.plugin.sql.result.BaseResult;
import io.github.linyimin.plugin.sql.result.SelectResult;
import io.github.linyimin.plugin.utils.MockTypeUtils;
import net.sf.jsqlparser.JSQLParserException;
import net.sf.jsqlparser.expression.*;
import net.sf.jsqlparser.expression.operators.relational.Between;
import net.sf.jsqlparser.expression.operators.relational.ExpressionList;
import net.sf.jsqlparser.expression.operators.relational.InExpression;
import net.sf.jsqlparser.parser.CCJSqlParserUtil;
import net.sf.jsqlparser.schema.Column;
import net.sf.jsqlparser.schema.Table;
import net.sf.jsqlparser.statement.Statement;
import net.sf.jsqlparser.statement.delete.Delete;
import net.sf.jsqlparser.statement.select.*;
import net.sf.jsqlparser.statement.update.Update;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.fife.ui.rsyntaxtextarea.RSyntaxTextArea;
import org.fife.ui.rtextarea.RTextScrollPane;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.*;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import static io.github.linyimin.plugin.constant.Constant.*;

/**
 * @author banzhe
 * @date 2022/12/15 14:59
 **/
public class SqlStressTabbedPane {

    private JPanel sqlStressPane;
    private JPanel reportPane;
    private JPanel configTabbedPane;
    private JPanel sqlContentPane;
    private JPanel sqlPane;
    private JPanel sqlTemplatePane;
    private JPanel paramConfigPane;
    private JPanel configPane;
    private JPanel stressPane;
    private JTable paramsConfigTable;
    private JButton stressButton;
    private JTextField stressDurationText;
    private JTextField incrementDurationText;
    private JComboBox valueTypeCombobox;
    private JPanel sqlStressContentPane;
    private JTextField concurrentNumText;
    private JComboBox trafficModelCombobox;
    private JTextField successRateText;
    private JTextField averageRtText;
    private JTextField tpsText;
    private JTextField concurrentNumTextField;
    private JTextField errorNumText;
    private JTextField totalRequestText;
    private JPanel successRatePanel;
    private JPanel averageRtPanel;
    private JPanel tpsPanel;
    private JPanel concurrentNumPanel;
    private JPanel errorNumPanel;
    private JPanel totalRequestPanel;
    private JPanel successRateChartPanel;
    private JPanel averageRtChartPanel;
    private JPanel tpsChartPanel;
    private JTabbedPane sqlStressTabbedPane;
    private JTextField maxRtText;
    private JPanel maxRtPanel;
    private JTextField maxTpsText;
    private JPanel maxTpsPanel;
    private JPanel tp90Panel;
    private JTextField tp90Text;
    private JPanel tp99Panel;
    private JTextField tp99Text;
    private JButton stopButton;
    private JPanel sqlAndLabelPanel;
    private JPanel sqlTemplateAndLabelPanel;

    private RSyntaxTextArea sqlText;
    private RSyntaxTextArea sqlTemplateText;

    private ComboBox<String> mockTypes;

    private final InfoPane infoPane;
    private final Project project;
    private final BackgroundTaskQueue backgroundTaskQueue;

    private final Pattern FIELD_NAME_PATTERN = Pattern.compile("\\$\\{([\\w.]+)}");

    private final AtomicBoolean isStop = new AtomicBoolean(false);

    private LineChart successRateLineChart;
    private LineChart averageRtLineChart;
    private LineChart tpsLineChart;

    SqlStressTabbedPane(Project project) {

        this.project = project;
        this.backgroundTaskQueue = new BackgroundTaskQueue(project, Constant.APPLICATION_NAME);

        initSqlText();
        initSqlTemplateText();

        this.infoPane = new InfoPane();

        this.paramConfigPane.setBorder(Constant.LINE_BORDER);
        this.stressPane.setBorder(Constant.LINE_BORDER);

        this.configTabbedPane.setLayout(new BorderLayout());
        this.configTabbedPane.add(this.sqlStressContentPane);

        this.initMockTypeCombobox();
        this.paramsConfigTable.setRowHeight(TABLE_ROW_HEIGHT);

        this.stressButton.addMouseListener(new MouseCursorAdapter(this.stressButton));
        this.stressButton.addActionListener(e -> stressButtonAction());

        this.stopButton.addMouseListener(new MouseCursorAdapter(this.stopButton));
        this.stopButton.addActionListener(e -> this.isStop.set(true));

        this.initReportTextFields();
        this.initChartPanel();

    }

    private void initReportTextFields() {
        this.successRateText.setBorder(JBUI.Borders.empty());
        this.successRatePanel.setBorder(Constant.LINE_BORDER);

        this.tp90Text.setBorder(JBUI.Borders.empty());
        this.tp90Panel.setBorder(LINE_BORDER);
        this.tp99Text.setBorder(JBUI.Borders.empty());
        this.tp99Panel.setBorder(LINE_BORDER);

        this.maxRtText.setBorder(JBUI.Borders.empty());
        this.maxRtPanel.setBorder(LINE_BORDER);

        this.averageRtText.setBorder(JBUI.Borders.empty());
        this.averageRtPanel.setBorder(Constant.LINE_BORDER);

        this.maxTpsText.setBorder(JBUI.Borders.empty());
        this.maxTpsPanel.setBorder(LINE_BORDER);
        this.tpsText.setBorder(JBUI.Borders.empty());
        this.tpsPanel.setBorder(Constant.LINE_BORDER);

        this.concurrentNumTextField.setBorder(JBUI.Borders.empty());
        this.concurrentNumPanel.setBorder(LINE_BORDER);

        this.errorNumText.setBorder(JBUI.Borders.empty());
        this.errorNumPanel.setBorder(LINE_BORDER);

        this.totalRequestText.setBorder(JBUI.Borders.empty());
        this.totalRequestPanel.setBorder(LINE_BORDER);

    }

    private void initChartPanel() {
        this.successRateChartPanel.setLayout(new BorderLayout());
        this.successRateChartPanel.setBorder(LINE_BORDER);

        this.averageRtChartPanel.setLayout(new BorderLayout());
        this.averageRtChartPanel.setBorder(LINE_BORDER);

        this.tpsChartPanel.setLayout(new BorderLayout());
        this.tpsChartPanel.setBorder(LINE_BORDER);

        this.successRateLineChart = new LineChart("请求成功率", "时间(s)", "请求成功率(%)");
        this.successRateChartPanel.add(this.successRateLineChart.getChartPanel());

        this.averageRtLineChart = new LineChart("RT", "时间(s)", "平均RT(ms)");
        this.averageRtChartPanel.add(this.averageRtLineChart.getChartPanel());

        this.tpsLineChart = new LineChart("TPS", "时间(s)", "TPS");
        this.tpsChartPanel.add(this.tpsLineChart.getChartPanel());

    }

    private void stressButtonAction() {

        ProcessResult<Void> checkResult = checkStressConfig();
        if (!checkResult.isSuccess()) {
            Notifier.notifyError(project, "sql stress configuration", checkResult.getErrorMsg());
            return;
        }

        String text = this.stressButton.getText();
        if (StringUtils.equals(text, "stress")) {
            this.stressButton.setText("stop");
            this.sqlStressTabbedPane.setSelectedIndex(1);
        } else {
            this.stressButton.setText("stress");
            this.isStop.set(true);
            return;
        }

        backgroundTaskQueue.run(new Task.Backgroundable(project, Constant.APPLICATION_NAME) {
            @Override
            public void run(@NotNull ProgressIndicator indicator) {
                try {
                    isStop.set(false);
                    doStress();
                } catch (Exception e) {
                    ApplicationManager.getApplication().invokeLater(() -> {
                        StringWriter sw = new StringWriter();
                        e.printStackTrace(new PrintWriter(sw));
                        SqlStressTabbedPane.this.configTabbedPane.remove(SqlStressTabbedPane.this.sqlStressContentPane);
                        SqlStressTabbedPane.this.configTabbedPane.add(SqlStressTabbedPane.this.infoPane.getInfoPane());
                        SqlStressTabbedPane.this.infoPane.setText(String.format("%s", sw));
                    });

                } finally {
                    isStop.set(true);
                }
            }
        });
    }

    private void doStress() {

        StressMetrics stressMetrics = new StressMetrics();

        int concurrentNum = Integer.parseInt(this.concurrentNumText.getText());

        Thread[] threads = new Thread[concurrentNum];

        for (int i = 0; i < concurrentNum; i++) {
            threads[i] = new Thread(doExecute(stressMetrics));
        }

        String trafficModel = (String) this.trafficModelCombobox.getSelectedItem();

        if (StringUtils.equals(trafficModel, "increase in a constant rate")) {
            int incrementDuration = Integer.parseInt(this.incrementDurationText.getText());
            int waitTimeMillis = Math.max(incrementDuration / concurrentNum, 1);
            for (int i = 0; i < concurrentNum; i++) {
                threads[i].start();
                stressMetrics.setConcurrentNum(i + 1);
                for (int j = 0; j < waitTimeMillis && !isStop.get(); j++) {
                    try {
                        displayMetrics(stressMetrics);
                        Thread.sleep(1000);
                    } catch (InterruptedException ignored) {
                        isStop.set(true);
                        return;
                    }
                }
            }
        } else if (StringUtils.equals(trafficModel, "fixed concurrent threads")) {
            for (int i = 0; i < concurrentNum; i++) {
                threads[i].start();
                stressMetrics.setConcurrentNum(i + 1);
            }
        }

        int stressDuration = Integer.parseInt(this.stressDurationText.getText()) * 60 * 1000;
        long end = System.currentTimeMillis() + stressDuration;
        long now = System.currentTimeMillis();

        while (end > now && !isStop.get()) {
            try {
                displayMetrics(stressMetrics);
                Thread.sleep(1000);
                now = System.currentTimeMillis();
            } catch (InterruptedException ignored) {
                break;
            }
        }

        displayMetrics(stressMetrics);

        isStop.set(true);
        this.stressButton.setText("stress");

    }

    private void displayMetrics(StressMetrics metrics) {

        ApplicationManager.getApplication().invokeLater(() -> {

            metrics.addConcurrentNum();

            this.successRateText.setText(metrics.successRate());

            Pair<Long, Long> tp = metrics.tp99And90();
            this.tp99Text.setText(String.valueOf(tp.getRight()));
            this.tp90Text.setText(String.valueOf(tp.getLeft()));

            this.maxRtText.setText(String.valueOf(metrics.maxRt()));
            this.averageRtText.setText(metrics.averageRt());

            this.maxTpsText.setText(String.valueOf(metrics.maxTps()));
            this.tpsText.setText(metrics.tps());

            this.concurrentNumTextField.setText(String.valueOf(metrics.getConcurrentNum()));
            this.errorNumText.setText(metrics.failedCount());
            this.totalRequestText.setText(metrics.total());

            Map<Long, Long> concurrentNumMap = metrics.concurrentNumMap();

            this.successRateLineChart.updateDataset(metrics.successRateMap(), concurrentNumMap);
            this.averageRtLineChart.updateDataset(metrics.averageRtMap(), concurrentNumMap);
            this.tpsLineChart.updateDataset(metrics.tpsMap(), concurrentNumMap);

        });

    }

    private Runnable doExecute(StressMetrics stressMetrics) {
        return () -> {
            while (true) {
                if (isStop.get()) {
                    return;
                }
                try {
                    String sql = acquireSql();
                    BaseResult result = SqlExecutor.executeSql(project, sql, false);
                    stressMetrics.addSuccess(result.getCost());
                } catch (Exception e) {
                    stressMetrics.addFailed();
                }
            }

        };
    }

    private String acquireSql() throws Exception {
        String sqlType = (String) this.valueTypeCombobox.getSelectedItem();
        if (StringUtils.equals(sqlType, "use sql directly")) {
            return this.sqlText.getText();
        } else if (StringUtils.equals(sqlType, "configure parameters")) {
            String templateSql = this.sqlTemplateText.getText();
            List<TableField> fields = generateMockConfig();
            for (TableField field : fields) {
                templateSql = mockBetweenData(templateSql, field);
                templateSql = mockInData(templateSql, field);
                templateSql = mockOrdinaryData(templateSql, field);
            }
            return templateSql;
        }

        return StringUtils.EMPTY;
    }

    private ProcessResult<Void> checkStressConfig() {

        String duration = this.incrementDurationText.getText();
        if (StringUtils.isBlank(duration) || !StringUtils.isNumeric(duration)) {
            return ProcessResult.fail("increment duration should be an integer.");
        }

        String concurrentNum = this.concurrentNumText.getText();
        if (StringUtils.isBlank(concurrentNum) || !StringUtils.isNumeric(concurrentNum)) {
            return ProcessResult.fail("concurrent number should be an integer.");
        }

        int number = Integer.parseInt(concurrentNum);
        if (number > 10) {
            return ProcessResult.fail("The number of concurrency should be less than or equal to 10");
        }

        duration = this.stressDurationText.getText();
        if (StringUtils.isBlank(duration) || !StringUtils.isNumeric(duration)) {
            return ProcessResult.fail("stress duration should be an integer.");
        }

        number = Integer.parseInt(duration);
        if (number > 60) {
            return ProcessResult.fail("The stress test time should be less than 1 hour");
        }

       return ProcessResult.success(null);

    }

    private String mockOrdinaryData(String templateSql, TableField field) throws Exception {
        String key = String.format("%s.%s", field.getTable(), field.getName());
        Object data = mockData(field);
        if (data instanceof Number) {
            templateSql = StringUtils.replaceIgnoreCase(templateSql, String.format("'${%s}'", key), data.toString());
        } else {
            templateSql = StringUtils.replaceIgnoreCase(templateSql, String.format("${%s}", key), data.toString());
        }

        return templateSql;
    }

    private String mockInData(String templateSql, TableField field) throws Exception {
        String key = String.format("%s.%s", field.getTable(), field.getName());
        Pattern pattern = Pattern.compile(String.format("__in__\\$\\{(%s)}__(\\d+)__", key), Pattern.CASE_INSENSITIVE);

        Matcher matcher = pattern.matcher(templateSql);
        if (!matcher.find()) {
            return templateSql;
        }

        int size = matcher.groupCount();

        for (int i = 0; i < size; i++) {
            Object data = mockData(field);
            if (data instanceof Number) {
                templateSql = StringUtils.replaceIgnoreCase(templateSql, String.format("'__in__${%s}__%s__'", key, i), data.toString());
            } else {
                templateSql = StringUtils.replaceIgnoreCase(templateSql, String.format("__in__${%s}__%s__", key, i), data.toString());
            }
        }

        return templateSql;
    }

    private String mockBetweenData(String sql, TableField field) throws Exception {

        String key = String.format("%s.%s", field.getTable(), field.getName());
        Pattern between = Pattern.compile(String.format("__between__\\$\\{(%s)}__(start|end)__", key), Pattern.CASE_INSENSITIVE);
        Matcher matcher = between.matcher(sql);
        if (matcher.find()) {
            Object start = mockData(field);
            Object end = mockData(field);

            if (start instanceof Number) {
                sql = StringUtils.replaceIgnoreCase(sql, String.format("'__between__${%s}__start__'", key), start.toString());
                sql = StringUtils.replaceIgnoreCase(sql, String.format("'__between__${%s}__end__'", key), end.toString());
            } else {
                sql = StringUtils.replaceIgnoreCase(sql, String.format("__between__${%s}__start__", key), start.toString());
                sql = StringUtils.replaceIgnoreCase(sql, String.format("__between__${%s}__end__", key), end.toString());
            }
        }

        return sql;
    }

    private Object mockData(TableField field) throws Exception {

        MockTypeEnum type = MockTypeEnum.valueOf(field.getMockType());
        DataGenerator generator = DataGeneratorFactory.getGenerator(type);

        return generator.generate(project, field);
    }

    private List<TableField> generateMockConfig() {

        DefaultTableModel model = (DefaultTableModel) this.paramsConfigTable.getModel();

        List<TableField> fields = Model2Field.parse(TableField.class, model);

        return fields.stream().filter(field -> !StringUtils.equals(MockTypeEnum.none.name(),field.getMockType())).collect(Collectors.toList());

    }

    private void initSqlText() {

        this.sqlText = CustomTextField.createArea("sql");
        this.sqlPane.setLayout(new BorderLayout());
        this.sqlAndLabelPanel.setBorder(LINE_BORDER);

        RTextScrollPane sqlTextScroll = new RTextScrollPane(this.sqlText);
        sqlTextScroll.setBorder(new EmptyBorder(JBUI.emptyInsets()));
        this.sqlPane.add(sqlTextScroll);
    }

    private void initMockTypeCombobox() {
        this.mockTypes = new ComboBox<>();
        for (MockTypeEnum type : MockTypeEnum.values()) {
            this.mockTypes.addItem(type.name());
        }

        this.mockTypes.setSelectedItem(MockTypeEnum.random.name());
    }

    private void initSqlTemplateText() {

        this.sqlTemplateText = CustomTextField.createArea("sql");
        this.sqlTemplatePane.setLayout(new BorderLayout());
        this.sqlTemplateAndLabelPanel.setBorder(LINE_BORDER);

        RTextScrollPane sqlTemplateTextScroll = new RTextScrollPane(this.sqlTemplateText);
        sqlTemplateTextScroll.setBorder(new EmptyBorder(JBUI.emptyInsets()));
        this.sqlTemplatePane.add(sqlTemplateTextScroll);

    }

    public JPanel getSqlStressPane() {
        return sqlStressPane;
    }

    public void updateStressConfig() {

        ApplicationManager.getApplication().invokeLater(() -> {
            this.configTabbedPane.remove(this.sqlStressContentPane);
            this.configTabbedPane.add(this.infoPane.getInfoPane());
            this.infoPane.setText("Loading sql statement...");
        });

        this.backgroundTaskQueue.run(new Task.Backgroundable(project, Constant.APPLICATION_NAME) {
            @Override
            public void run(@NotNull ProgressIndicator indicator) {
                try {
                    ProcessResult<String> result = updateSql();
                    generateMockConfigTable(result);
                } catch (Exception e) {
                    StringWriter sw = new StringWriter();
                    e.printStackTrace(new PrintWriter(sw));
                    ApplicationManager.getApplication().invokeLater(() -> {
                        SqlStressTabbedPane.this.configTabbedPane.remove(SqlStressTabbedPane.this.sqlStressContentPane);
                        SqlStressTabbedPane.this.configTabbedPane.add(SqlStressTabbedPane.this.infoPane.getInfoPane());
                        SqlStressTabbedPane.this.infoPane.setText(String.format("%s", sw));
                    });
                }
            }
        });
    }

    private ProcessResult<String> updateSql() {

        MybatisSqlConfiguration sqlConfig = project.getService(MybatisSqlStateComponent.class).getConfiguration();
        if (StringUtils.isBlank(sqlConfig.getSql())) {
            ProcessResult<String> result = SqlParamGenerateComponent.generateSql(project);

            if (!result.isSuccess()) {
                ApplicationManager.getApplication().invokeLater(() -> this.infoPane.setText(result.getErrorMsg()));
                return result;
            }
        }

        ProcessResult<String> validateResult = SqlParser.validate(sqlConfig.getSql());
        if (!validateResult.isSuccess()) {
            ApplicationManager.getApplication().invokeLater(() -> this.infoPane.setText(validateResult.getErrorMsg()));
            return validateResult;
        }

        ProcessResult<String> result = getTemplateSql(sqlConfig.getSql());

        ApplicationManager.getApplication().invokeLater(() -> {
            this.configTabbedPane.remove(this.infoPane.getInfoPane());
            this.configTabbedPane.add(this.sqlStressContentPane);
            this.sqlText.setText(sqlConfig.getSql());
            if (result.isSuccess()) {
                this.sqlTemplateText.setText(MysqlFormatter.format(result.getData()));
            } else {
                this.sqlTemplateText.setText(result.getErrorMsg());
            }
        });

        return result;
    }

    private void generateMockConfigTable(ProcessResult<String> templateResult) throws Exception {
        MybatisSqlConfiguration sqlConfig = project.getService(MybatisSqlStateComponent.class).getConfiguration();
        if (StringUtils.isBlank(sqlConfig.getSql()) || !templateResult.isSuccess()) {
            Vector<String> identifiers = getMockTableColumns(true);
            this.paramsConfigTable.setModel(new DefaultTableModel(identifiers, 0));
            return;
        }
        List<String> tables = SqlParser.getTableNames(sqlConfig.getSql());

        Vector<Vector<Object>> data = new Vector<>();
        Vector<String> columnNames = getMockTableColumns(false);

        for (String table : tables) {
            String metaSql = TABLE_META_SQL_TEMPLATE.replace("${table}", table);
            SelectResult metaResult = (SelectResult) SqlExecutor.executeSql(project, metaSql, false);

            if (metaResult.getModel().getRowCount() == 0) {
                this.configTabbedPane.remove(this.sqlStressContentPane);
                this.configTabbedPane.add(this.infoPane.getInfoPane());

                this.infoPane.setText(String.format("Schema of table %s is empty. Please verify that the table exists or the database configuration is correct", table));

                return;
            }

            // 添加表名列
            for (int r = 0; r < metaResult.getModel().getRowCount(); r++) {
                Vector rowData = new Vector();
                rowData.add(table);
                rowData.addAll((Collection) metaResult.getModel().getDataVector().get(r));
                data.add(rowData);
            }
        }

        data = filterByField(templateResult.getData(), data);

        this.paramsConfigTable.setModel(new DefaultTableModel(data, columnNames));
        MockTypeUtils.addMockColumns(this.paramsConfigTable, this.mockTypes, true);

    }

    private Vector<Vector<Object>> filterByField(String templateSql, Vector<Vector<Object>> data) {

        Vector<Vector<Object>> result = new Vector<>();

        Map<Object, Vector<Object>> map = new HashMap<>();
        for (Vector<Object> vector : data) {
            String key = String.format("%s.%s", vector.get(0).toString().toLowerCase(), vector.get(1).toString().toLowerCase());
            map.put(key, vector);
            map.put(vector.get(1).toString().toLowerCase(), vector);
        }

        Matcher matcher = FIELD_NAME_PATTERN.matcher(templateSql);

        Set<String> keys = new HashSet<>();
        while (matcher.find()) {
            for (int i = 1; i <= matcher.groupCount(); i++) {
                String key = matcher.group(i).toLowerCase();
                if (keys.contains(key)) {
                    continue;
                }
                result.add(map.get(key));
                keys.add(key);
            }
        }

        return result;
    }

    private ProcessResult<String> getTemplateSql(String sql) {
        try {
            Statement statement = CCJSqlParserUtil.parse(sql);

            if (statement instanceof Select) {
                return getTemplateSelectSql((Select) statement);
            }
            if (statement instanceof Update) {
                return getTemplateUpdateSql((Update) statement);
            }

            if (statement instanceof Delete) {
                return getTemplateDeleteSql((Delete) statement);
            }

            return ProcessResult.fail(String.format("%s is not supported.", statement.getClass().getSimpleName()));

        } catch (JSQLParserException e) {
            StringWriter sw = new StringWriter();
            e.printStackTrace(new PrintWriter(sw));
            return ProcessResult.fail(String.format("getTemplateSql error.\n%s", sw));
        }
    }

    private ProcessResult<String> getTemplateDeleteSql(Delete statement) {
        List<Join> joins = statement.getJoins();
        if (CollectionUtils.isNotEmpty(joins)) {
            for (Join join : joins) {
                this.processJoin(join);
            }
        }


        Expression expression = statement.getWhere();
        if (expression != null) {
            String table = StringUtils.EMPTY;
            if (statement.getTable() != null) {
                table = statement.getTable().getFullyQualifiedName();
                table = StringUtils.replace(table, "`", "");
            }
            this.processExpression(table, expression);
        }

        return ProcessResult.success(statement.toString());
    }

    private ProcessResult<String> getTemplateUpdateSql(Update statement) {
        List<Join> joins = statement.getJoins();
        if (CollectionUtils.isNotEmpty(joins)) {
            for (Join join : joins) {
                this.processJoin(join);
            }
        }

        Expression expression = statement.getWhere();
        if (expression != null) {
            String table = StringUtils.EMPTY;
            if (statement.getTable() != null) {
                table = statement.getTable().getFullyQualifiedName();
                table = StringUtils.replace(table, "`", "");
            }
            this.processExpression(table, expression);
        }

        return ProcessResult.success(statement.toString());
    }

    private ProcessResult<String> getTemplateSelectSql(Select statement) {
        PlainSelect select = (PlainSelect) statement.getSelectBody();
        this.processPlainSelect(select);

        return ProcessResult.success(statement.toString());
    }

    private void processPlainSelect(PlainSelect select) {
        List<Join> joins = select.getJoins();
        if (CollectionUtils.isNotEmpty(joins)) {
            for (Join join : joins) {
                this.processJoin(join);
            }
        }

        String table = StringUtils.EMPTY;
        if (select.getFromItem() instanceof Table) {
            table = ((Table) select.getFromItem()).getFullyQualifiedName();
            table = StringUtils.replace(table, "`", "");
        }

        Expression expression = select.getWhere();
        if (expression != null) {
            this.processExpression(table, expression);
        }

        expression = select.getHaving();
        if (expression != null) {
            this.processExpression(table, expression);
        }

    }

    private void processJoin(Join join) {
        FromItem fromItem = join.getRightItem();
        if (fromItem instanceof SubSelect) {
            this.processPlainSelect((PlainSelect) ((SubSelect) fromItem).getSelectBody());
        }
    }

    private void processExpression(String table, Expression expression) {

        if (expression instanceof Parenthesis) {
            processExpression(table, ((Parenthesis) expression).getExpression());
            return;
        }

        if (expression instanceof BinaryExpression) {

            Expression left = ((BinaryExpression) expression).getLeftExpression();
            Expression right = ((BinaryExpression) expression).getRightExpression();

            if (isValue(right) && left instanceof Column) {
                String columnName = ((Column) left).getFullyQualifiedName();

                String value;
                if (StringUtils.isNotBlank(table)) {
                    value = String.format("${%s.%s}", table, columnName);
                } else {
                    value = String.format("${%s}", columnName);
                }

                ((BinaryExpression) expression).setRightExpression(new StringValue(value));
                return;
            } else if (right instanceof SubSelect) {
                this.processPlainSelect((PlainSelect) ((SubSelect) right).getSelectBody());
            }

            processExpression(table, left);
            processExpression(table, right);

        }

        if (expression instanceof InExpression) {

            Expression left = ((InExpression)expression).getLeftExpression();

            ExpressionList list = ((ExpressionList)((InExpression)expression).getRightItemsList());

            if (list != null && CollectionUtils.isNotEmpty(list.getExpressions())) {
                List<Expression> expressions = list.getExpressions();

                if (isValue(expressions.get(0)) && left instanceof Column) {
                    String columnName = ((Column) left).getColumnName();
                    List<Expression> values = new ArrayList<>(expressions.size());
                    for (int i = 0; i < expressions.size(); i++) {
                        if (StringUtils.isNotBlank(table)) {
                            values.add(new StringValue(String.format("__in__${%s.%s}__%s__", table, columnName, i)));
                        } else {
                            values.add(new StringValue(String.format("__in__${%s}__%s__", columnName, i)));
                        }
                    }
                    ((InExpression) expression).setRightItemsList(new ExpressionList(values));
                }
            }

            Expression right = ((InExpression)expression).getRightExpression();
            if (right instanceof SubSelect) {
                this.processPlainSelect((PlainSelect) ((SubSelect) right).getSelectBody());
            }

        }

        if (expression instanceof Between) {

            Expression left = ((Between) expression).getLeftExpression();

            Expression start = ((Between) expression).getBetweenExpressionStart();
            Expression end = ((Between) expression).getBetweenExpressionEnd();

            if (isValue(start) && left instanceof Column) {
                String columnName = ((Column) left).getColumnName();
                if (StringUtils.isNotBlank(columnName)) {
                    if (StringUtils.isNotBlank(table)) {
                        ((Between)expression).setBetweenExpressionStart(new StringValue(String.format("__between__${%s.%s}__start__", table, columnName)));
                    } else {
                        ((Between)expression).setBetweenExpressionStart(new StringValue(String.format("__between__${%s}__start__", columnName)));
                    }
                }
            } else {
                this.processExpression(table, start);
            }

            if (isValue(end) && left instanceof Column) {
                String columnName = ((Column) left).getColumnName();
                if (StringUtils.isNotBlank(table)) {
                    ((Between)expression).setBetweenExpressionEnd(new StringValue(String.format("__between__${%s.%s}__end__", table, columnName)));
                } else {
                    ((Between)expression).setBetweenExpressionEnd(new StringValue(String.format("__between__${%s}__end__", columnName)));
                }
            } else {
                this.processExpression(table, start);
            }
        }

    }

    private boolean isValue(Expression expression) {
        List<Class<? extends Expression>> values = Arrays.asList(
                DateValue.class, DoubleValue.class,
                HexValue.class, LongValue.class,
                StringValue.class, TimestampValue.class,
                TimeValue.class
        );

        return values.stream().anyMatch(value -> value.isAssignableFrom(expression.getClass()));
    }

    private Vector<String> getMockTableColumns(boolean includeMock) {

        Vector<String> identifiers = new Vector<>();

        identifiers.add("Table");
        identifiers.add("Name");
        identifiers.add("Type");
        identifiers.add("Nullable");
        identifiers.add("Default");
        identifiers.add("Key");
        identifiers.add("Extra");
        identifiers.add("Comment");

        if (includeMock) {
            identifiers.add("Mock Type");
            identifiers.add("Mock Value");
        }

        return identifiers;
    }
}
