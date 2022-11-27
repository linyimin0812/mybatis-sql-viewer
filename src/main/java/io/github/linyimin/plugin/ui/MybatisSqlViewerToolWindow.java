package io.github.linyimin.plugin.ui;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.Messages;
import com.intellij.openapi.ui.SimpleToolWindowPanel;
import com.intellij.openapi.wm.ToolWindow;
import com.intellij.util.ui.JBUI;
import io.github.linyimin.plugin.constant.Constant;
import io.github.linyimin.plugin.configuration.MybatisSqlStateComponent;
import io.github.linyimin.plugin.component.SqlParamGenerateComponent;
import io.github.linyimin.plugin.configuration.model.MybatisSqlConfiguration;
import io.github.linyimin.plugin.pojo2json.POJO2JSONParserFactory;
import io.github.linyimin.plugin.sql.executor.SqlExecutor;
import io.github.linyimin.plugin.sql.converter.ResultConverter;
import io.github.linyimin.plugin.sql.parser.SqlParser;
import io.github.linyimin.plugin.sql.parser.SqlType;
import io.github.linyimin.plugin.sql.result.BaseResult;
import io.github.linyimin.plugin.sql.result.SelectResult;
import org.fife.ui.rsyntaxtextarea.RSyntaxTextArea;
import org.fife.ui.rtextarea.RTextScrollPane;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import java.awt.*;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Arrays;
import java.util.List;


/**
 * @author yiminlin
 * @date 2022/02/01 12:31 下午
 **/
public class MybatisSqlViewerToolWindow extends SimpleToolWindowPanel {

    private JFormattedTextField methodName;
    private JTabbedPane totalTabbedPanel;
    private JPanel root;
    private JTabbedPane paramTabbedPanel;

    private JPanel defaultParamPanel;
    private JPanel randomParamPanel;
    private JPanel fromDbParamPanel;

    private RTextScrollPane defaultParamsScroll;
    private RTextScrollPane randomParamsScroll;
    private RTextScrollPane fromDbParamScroll;

    private RSyntaxTextArea defaultParamsText;
    private RSyntaxTextArea randomParamsText;
    private RSyntaxTextArea fromDbParamsText;


    private JTable tableSchema;
    private JScrollPane tableSchemaScroll;

    private JButton datasourceButton;

    private JTabbedPane sqlTabbedPanel;

    private JPanel statementPanel;
    private RSyntaxTextArea statementText;
    private RTextScrollPane statementScroll;

    private JPanel templatePanel;
    private RSyntaxTextArea templateSqlText;
    private RTextScrollPane templateSqlScroll;


    private JTable executeResultTable;
    private JTable executeHitIndexTable;

    private JPanel executeInfoPanel;
    private JScrollPane executeResultScroll;
    private JScrollPane executeHitIndexScroll;

    private RTextScrollPane executeInfoTextScroll;
    private RSyntaxTextArea executeInfoText;

    private final Project myProject;

    public JPanel getRoot() {
        return root;
    }

    @Override
    public JPanel getContent() {
        return getRoot();
    }

    public MybatisSqlViewerToolWindow(ToolWindow toolWindow, Project project) {

        super(true, false);
        this.myProject = project;

        initParamPanel();

        initSqlPanel();

        initResultPanel();

        methodName.setBorder(new EmptyBorder(JBUI.emptyInsets()));

        datasourceButton.setFocusPainted(false);

        setScrollUnitIncrement();

        addComponentListener();

    }

    private void initResultPanel() {
        executeInfoText = CustomTextField.createArea("json");
        executeInfoText.setRows(3);

        executeInfoTextScroll = new RTextScrollPane(executeInfoText);
        executeInfoTextScroll.setBorder(new EmptyBorder(JBUI.emptyInsets()));
        executeInfoTextScroll.setSize(-1, 50);

        executeInfoPanel.setLayout(new BorderLayout());
        executeInfoPanel.add(executeInfoTextScroll);
    }

    private void initSqlPanel() {

        statementText = CustomTextField.createArea("sql");
        templateSqlText = CustomTextField.createArea("sql");

        templateSqlText.setEditable(false);

        statementPanel.setLayout(new BorderLayout());
        templatePanel.setLayout(new BorderLayout());

        statementScroll = new RTextScrollPane(statementText);
        statementScroll.setBorder(new EmptyBorder(JBUI.emptyInsets()));
        statementPanel.add(statementScroll);

        templateSqlScroll = new RTextScrollPane(templateSqlText);
        templateSqlScroll.setBorder(new EmptyBorder(JBUI.emptyInsets()));
        templatePanel.add(templateSqlScroll);

    }

    private void initParamPanel() {
        defaultParamsText = CustomTextField.createArea("json");
        randomParamsText = CustomTextField.createArea("json");
        fromDbParamsText = CustomTextField.createArea("json");

        defaultParamPanel.setLayout(new BorderLayout());
        randomParamPanel.setLayout(new BorderLayout());
        fromDbParamPanel.setLayout(new BorderLayout());

        defaultParamsScroll = new RTextScrollPane(defaultParamsText);
        defaultParamsScroll.setBorder(new EmptyBorder(JBUI.emptyInsets()));

        randomParamsScroll = new RTextScrollPane(randomParamsText);
        randomParamsScroll.setBorder(new EmptyBorder(JBUI.emptyInsets()));

        fromDbParamScroll = new RTextScrollPane(fromDbParamsText);
        fromDbParamScroll.setBorder(new EmptyBorder(JBUI.emptyInsets()));

        randomParamPanel.add(randomParamsScroll);
        defaultParamPanel.add(defaultParamsScroll);
        fromDbParamPanel.add(fromDbParamScroll);
    }


    /**
     * 刷新tool window配置内容
     */
    public void refresh(Project project) {
        MybatisSqlConfiguration config = project.getService(MybatisSqlStateComponent.class).getConfiguration();

        methodName.setText(config.getMethod());

        defaultParamsText.setText(config.getParams());

        statementText.setText(config.getSql());

        // 默认每次打开，都展示第一个tab
        totalTabbedPanel.setSelectedIndex(0);
        paramTabbedPanel.setSelectedIndex(0);
        sqlTabbedPanel.setSelectedIndex(0);

    }

    private void addComponentListener() {

        addParamsTextListener();

        datasourceButton.addActionListener((e) -> {
            DatasourceDialog dialog = new DatasourceDialog(myProject);
            dialog.pack();
            dialog.setVisible(true);
        });

        // 监听tabbedpane点击事件
        totalTabbedPanel.addChangeListener(e -> totalTabbedPanelListener());

        // 监听param tabbed panel的点击事件
        paramTabbedPanel.addChangeListener(e -> paramTabbedPanelListener());

        // 监听sql tabbed panel的点击事件
        sqlTabbedPanel.addChangeListener(e -> sqlTabbedPanelListener());
    }

    private void acquireTableSchema() {

        // 获取表列信息：DESC mybatis.CITY;
        // 获取表信息(编码)：show table status from `global_ug_usm_ae` like  'houyi_clc_plan';

        MybatisSqlConfiguration configuration = myProject.getComponent(MybatisSqlStateComponent.class).getConfiguration();
        String tableName = SqlParser.getTableNames(configuration.getSql()).get(0);
        String sql = String.format("DESC %s", tableName);

        try {
            SelectResult result = (SelectResult) SqlExecutor.executeSql(myProject, sql);
            tableSchema.setModel(result.getModel());
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private void generateSql(boolean isTemplate) {
        try {

            MybatisSqlConfiguration sqlConfig = myProject.getService(MybatisSqlStateComponent.class).getConfiguration();

            String sqlStr = SqlParamGenerateComponent.generateSql(myProject, sqlConfig.getMethod(), sqlConfig.getParams(), isTemplate);
            sqlConfig.setSql(sqlStr);

            if (isTemplate) {
                templateSqlText.setText(sqlStr);
            } else {
                statementText.setText(sqlStr);
            }
        } catch (Throwable e) {
            Messages.showInfoMessage("generate sql error. err: " + e.getMessage(), Constant.APPLICATION_NAME);
        }
    }

    private void executeSql() {

        MybatisSqlConfiguration sqlConfig = myProject.getService(MybatisSqlStateComponent.class).getConfiguration();

        executeHitIndexScroll.setVisible(true);

        try {
            BaseResult executeResult = SqlExecutor.executeSql(myProject, sqlConfig.getSql());
            SqlType sqlType = SqlParser.getSqlType(sqlConfig.getSql());
            if (sqlType == SqlType.select) {
                executeResultScroll.setVisible(true);

                executeResultTable.setModel(((SelectResult) executeResult).getModel());
            } else {
                executeResultScroll.setVisible(false);
            }

            executeInfoText.setText(ResultConverter.convert2ExecuteInfo(executeResult));

            acquireExecuteIndex();

        } catch (Exception e) {
            StringWriter sw = new StringWriter();
            e.printStackTrace(new PrintWriter(sw));
            executeInfoText.setText(String.format("Execute Sql Failed.\n%s", sw));
            executeHitIndexScroll.setVisible(false);
            executeResultScroll.setVisible(false);
        }

    }

    private void acquireExecuteIndex() throws Exception {

        MybatisSqlConfiguration sqlConfig = myProject.getService(MybatisSqlStateComponent.class).getConfiguration();

        String explainSql = String.format("explain %s", sqlConfig.getSql());
        SelectResult executeResult = (SelectResult) SqlExecutor.executeSql(myProject, explainSql);

        executeHitIndexTable.setModel(executeResult.getModel());

    }

    private void totalTabbedPanelListener() {
        int selectedIndex = totalTabbedPanel.getSelectedIndex();

        // 点击sql tab时生成sql
        if (selectedIndex == TabbedComponentType.sql.index) {
            generateSql(false);
        }

        // 点击table tab时获取table的schema信息
        if (selectedIndex == TabbedComponentType.table.index) {
            acquireTableSchema();
        }
    }

    private void paramTabbedPanelListener() {

        int selectedIndex = paramTabbedPanel.getSelectedIndex();

        MybatisSqlConfiguration configuration = myProject.getService(MybatisSqlStateComponent.class).getConfiguration();

        // 获取参数默认值
        if (selectedIndex == ParamComponentType.default_param.index) {
            SqlParamGenerateComponent.generate(configuration.getPsiElement(), POJO2JSONParserFactory.DEFAULT_POJO_2_JSON_PARSER);
            defaultParamsText.setText(configuration.getParams());
        }

        // 获取参数随机值
        if (selectedIndex == ParamComponentType.random_param.index) {
            SqlParamGenerateComponent.generate(configuration.getPsiElement(), POJO2JSONParserFactory.RANDOM_POJO_2_JSON_PARSER);
            randomParamsText.setText(configuration.getParams());
        }

        // 从db中获取参数值
        if (selectedIndex == ParamComponentType.from_db_param.index) {
            SqlParamGenerateComponent.generate(configuration.getPsiElement(), POJO2JSONParserFactory.RANDOM_POJO_2_JSON_PARSER);
            fromDbParamsText.setText(configuration.getParams());
        }
    }

    private void sqlTabbedPanelListener() {

        int selectedIndex = sqlTabbedPanel.getSelectedIndex();

        // 完整SQL语句
        if (selectedIndex == SqlComponentType.statement.index) {
            generateSql(false);
        }

        // SQL模板
        if (selectedIndex == SqlComponentType.template.index) {
            generateSql(true);
        }
        // 执行SQL
        if (selectedIndex == SqlComponentType.result.getIndex()) {
            generateSql(false);
            executeSql();
        }
    }

    private void addParamsTextListener() {
        List<RSyntaxTextArea> areaList = Arrays.asList(defaultParamsText, randomParamsText, fromDbParamsText);

        for (RSyntaxTextArea area : areaList) {
            area.getDocument().addDocumentListener(new DocumentListener() {
                @Override
                public void insertUpdate(DocumentEvent e) {
                    updateParams();
                }

                @Override
                public void removeUpdate(DocumentEvent e) {
                    updateParams();
                }

                @Override
                public void changedUpdate(DocumentEvent e) {
                    updateParams();
                }

                private void updateParams() {
                    MybatisSqlConfiguration config = myProject.getService(MybatisSqlStateComponent.class).getConfiguration();
                    config.setParams(area.getText());
                }
            });
        }
    }

    private void setScrollUnitIncrement() {

        int unit = 16;

        this.statementScroll.getVerticalScrollBar().setUnitIncrement(unit);
        this.statementScroll.getHorizontalScrollBar().setUnitIncrement(unit);

        this.templateSqlScroll.getVerticalScrollBar().setUnitIncrement(unit);
        this.templateSqlScroll.getHorizontalScrollBar().setUnitIncrement(unit);

        this.defaultParamsScroll.getVerticalScrollBar().setUnitIncrement(unit);
        this.defaultParamsScroll.getHorizontalScrollBar().setUnitIncrement(unit);

        this.randomParamsScroll.getVerticalScrollBar().setUnitIncrement(unit);
        this.randomParamsScroll.getHorizontalScrollBar().setUnitIncrement(unit);

        this.fromDbParamScroll.getVerticalScrollBar().setUnitIncrement(unit);
        this.randomParamsScroll.getHorizontalScrollBar().setUnitIncrement(unit);

    }

    private void createUIComponents() {
        // TODO: place custom component creation code here
    }

    private enum TabbedComponentType {
        /**
         * Tanned类型对应的index
         */
        params(0),
        sql(1),
        table(2);

        private final int index;

        TabbedComponentType(int index) {
            this.index = index;
        }

    }

    private enum ParamComponentType {
        /**
         * Tanned类型对应的index
         */
        default_param(0),
        random_param(1),
        from_db_param(2);

        private final int index;

        ParamComponentType(int index) {
            this.index = index;
        }

    }

    private enum SqlComponentType {
        /**
         * Tanned类型对应的index
         */
        statement(0),
        template(1),
        result(2);
        private final int index;

        SqlComponentType(int index) {
            this.index = index;
        }

        public int getIndex() {
            return index;
        }
    }
}
