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
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import java.awt.*;
import java.awt.event.MouseEvent;
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

    private RTextScrollPane defaultParamsScroll;
    private RTextScrollPane randomParamsScroll;
    private RTextScrollPane fromDbParamScroll;

    private RSyntaxTextArea defaultParamsText;
    private RSyntaxTextArea randomParamsText;
    private RSyntaxTextArea fromDbParamsText;

    private JButton datasourceButton;

    private JTabbedPane sqlTabbedPanel;

    private JPanel statementPanel;
    private RSyntaxTextArea statementText;
    private RTextScrollPane statementScroll;

    private JPanel statementRulePanel;
    private RSyntaxTextArea statementRuleText;

    private JTable executeResultTable;
    private JTable executeHitIndexTable;

    private JPanel executeInfoPanel;
    private JScrollPane executeResultScroll;
    private JScrollPane executeHitIndexScroll;
    private JTabbedPane tableTabbedPanel;

    private RSyntaxTextArea executeInfoText;

    private final Project myProject;

    public JPanel getRoot() {
        return root;
    }

    @Override
    public JPanel getContent() {
        return getRoot();
    }

    public MybatisSqlViewerToolWindow(Project project) {

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

    private void createUIComponents() {
        executeResultTable = new JTable() {
            //Implement table cell tool tips.
            public String getToolTipText(@NotNull MouseEvent e) {
                String tip = null;
                java.awt.Point p = e.getPoint();
                int rowIndex = rowAtPoint(p);
                int colIndex = columnAtPoint(p);

                try {
                    //comment row, exclude heading
                    tip = getValueAt(rowIndex, colIndex).toString();
                } catch (RuntimeException e1) {
                    //catch null pointer exception if mouse is over an empty line
                }
                return tip;
            }
        };
    }

    private void initResultPanel() {

        executeInfoText = CustomTextField.createArea("json");
        executeInfoText.setRows(3);

        RTextScrollPane executeInfoTextScroll = new RTextScrollPane(executeInfoText);
        executeInfoTextScroll.setBorder(new ToolWindow.Border());

        executeInfoPanel.setLayout(new BorderLayout());
        executeInfoPanel.add(executeInfoTextScroll);
    }

    private void initSqlPanel() {

        statementText = CustomTextField.createArea("sql");

        statementPanel.setLayout(new BorderLayout());

        statementScroll = new RTextScrollPane(statementText);
        statementScroll.setBorder(new EmptyBorder(JBUI.emptyInsets()));

        statementPanel.add(statementScroll);

        statementRuleText = CustomTextField.createArea("sql");

        statementRulePanel.setLayout(new BorderLayout());

        RTextScrollPane statementRuleScroll = new RTextScrollPane(statementRuleText);
        statementRuleScroll.setBorder(new EmptyBorder(JBUI.emptyInsets()));

        statementRulePanel.add(statementRuleScroll);

    }

    private void initParamPanel() {
        defaultParamsText = CustomTextField.createArea("json");
        randomParamsText = CustomTextField.createArea("json");
        fromDbParamsText = CustomTextField.createArea("json");

        defaultParamPanel.setLayout(new BorderLayout());
        randomParamPanel.setLayout(new BorderLayout());

        defaultParamsScroll = new RTextScrollPane(defaultParamsText);
        defaultParamsScroll.setBorder(new EmptyBorder(JBUI.emptyInsets()));

        randomParamsScroll = new RTextScrollPane(randomParamsText);
        randomParamsScroll.setBorder(new EmptyBorder(JBUI.emptyInsets()));

        fromDbParamScroll = new RTextScrollPane(fromDbParamsText);
        fromDbParamScroll.setBorder(new EmptyBorder(JBUI.emptyInsets()));

        randomParamPanel.add(randomParamsScroll);
        defaultParamPanel.add(defaultParamsScroll);
    }


    /**
     * 刷新tool window配置内容
     */
    public void refresh(Project project) {

        MybatisSqlConfiguration config = project.getService(MybatisSqlStateComponent.class).getConfiguration();

        methodName.setText(config.getMethod());

        randomParamsText.setText(config.getParams());

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

    private void acquireTableSchemas() {

        // 获取表列信息：DESC mybatis.CITY;
        // 获取表信息(编码)：show table status from `global_ug_usm_ae` like  'houyi_clc_plan';

        MybatisSqlConfiguration configuration = myProject.getService(MybatisSqlStateComponent.class).getConfiguration();

        List<String> tables = SqlParser.getTableNames(configuration.getSql());

        tableTabbedPanel.removeAll();

        for (String table : tables) {

            String sql = String.format("DESC %s", table);
            TableTabbedPane tabbedPanel = new TableTabbedPane();
            tableTabbedPanel.addTab(table, tabbedPanel.getSpecifyTablePanel());
            try {
                SelectResult result = (SelectResult) SqlExecutor.executeSql(myProject, sql);
                tabbedPanel.getTableSchema().setModel(result.getModel());
                // TODO: 建表规约
                tabbedPanel.getTableRuleText().setText("TODO: 建表规约");
            } catch (Exception e) {
                StringWriter sw = new StringWriter();
                e.printStackTrace(new PrintWriter(sw));
                tabbedPanel.getTableRuleText().setText(sw.toString());
                break;
            }
        }
    }

    private void generateSql() {
        try {

            MybatisSqlConfiguration sqlConfig = myProject.getService(MybatisSqlStateComponent.class).getConfiguration();

            String sqlStr = SqlParamGenerateComponent.generateSql(myProject, sqlConfig.getMethod(), sqlConfig.getParams());
            sqlConfig.setSql(sqlStr);

            statementText.setText(sqlStr);

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

        // 点击param tab时生成对应参数
        if (selectedIndex == TabbedComponentType.params.index) {
            paramTabbedPanel.setSelectedIndex(0);
        }

        // 点击sql tab时生成sql
        if (selectedIndex == TabbedComponentType.sql.index) {
            sqlTabbedPanel.setSelectedIndex(0);
            generateSql();
            // TODO: SQL规范校验
            MybatisSqlConfiguration configuration = myProject.getService(MybatisSqlStateComponent.class).getConfiguration();
            if (configuration.getSql().contains("SELECT")) {
                statementRulePanel.setVisible(true);
                statementRuleText.setText("TODO: sql语句规约");
            } else {
                statementRulePanel.setVisible(false);
            }

        }

        // 点击table tab时获取table的schema信息
        if (selectedIndex == TabbedComponentType.table.index) {
            acquireTableSchemas();
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
    }

    private void sqlTabbedPanelListener() {

        int selectedIndex = sqlTabbedPanel.getSelectedIndex();

        // 完整SQL语句
        if (selectedIndex == SqlComponentType.statement.index) {
            generateSql();
        }

        // 执行SQL
        if (selectedIndex == SqlComponentType.result.getIndex()) {
            generateSql();
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

        this.defaultParamsScroll.getVerticalScrollBar().setUnitIncrement(unit);
        this.defaultParamsScroll.getHorizontalScrollBar().setUnitIncrement(unit);

        this.randomParamsScroll.getVerticalScrollBar().setUnitIncrement(unit);
        this.randomParamsScroll.getHorizontalScrollBar().setUnitIncrement(unit);

        this.fromDbParamScroll.getVerticalScrollBar().setUnitIncrement(unit);
        this.randomParamsScroll.getHorizontalScrollBar().setUnitIncrement(unit);

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
        random_param(0),
        default_param(1);

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
        result(1);
        private final int index;

        SqlComponentType(int index) {
            this.index = index;
        }

        public int getIndex() {
            return index;
        }
    }
}
