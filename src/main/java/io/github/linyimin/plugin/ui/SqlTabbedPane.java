package io.github.linyimin.plugin.ui;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.Messages;
import com.intellij.ui.JBColor;
import com.intellij.util.ui.JBUI;
import io.github.linyimin.plugin.ProcessResult;
import io.github.linyimin.plugin.component.SqlParamGenerateComponent;
import io.github.linyimin.plugin.configuration.MybatisSqlStateComponent;
import io.github.linyimin.plugin.configuration.model.MybatisSqlConfiguration;
import io.github.linyimin.plugin.constant.Constant;
import io.github.linyimin.plugin.sql.checker.CheckerHolder;
import io.github.linyimin.plugin.sql.checker.Report;
import io.github.linyimin.plugin.sql.converter.ResultConverter;
import io.github.linyimin.plugin.sql.executor.SqlExecutor;
import io.github.linyimin.plugin.sql.parser.SqlParser;
import io.github.linyimin.plugin.sql.parser.SqlType;
import io.github.linyimin.plugin.sql.result.BaseResult;
import io.github.linyimin.plugin.sql.result.SelectResult;
import org.apache.commons.lang3.StringUtils;
import org.fife.ui.rsyntaxtextarea.RSyntaxTextArea;
import org.fife.ui.rtextarea.RTextScrollPane;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import java.awt.*;
import java.awt.event.MouseEvent;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

import static io.github.linyimin.plugin.constant.Constant.*;

/**
 * @author banzhe
 * @date 2022/12/03 17:27
 **/
public class SqlTabbedPane implements TabbedChangeListener {

    private JPanel panel1;

    private JTabbedPane sqlTabbedPanel;

    // sql语句
    private JPanel statementPanel;
    private RSyntaxTextArea statementText;
    private RTextScrollPane statementScroll;

    // sql规约
    private JPanel statementRulePanel;
    private RSyntaxTextArea statementRuleText;

    // sql执行信息
    private RSyntaxTextArea executeInfoText;
    private JPanel executeInfoPanel;

    // sql执行计划
    private JTable executeHitIndexTable;
    private JScrollPane executeHitIndexScroll;

    // sql执行结果
    private JTable executeResultTable;
    private JScrollPane executeResultScroll;

    private final Project project;

    public SqlTabbedPane(Project project) {
        this.project = project;

        initSqlPanel();
        initResultPanel();
        setTableRowHeight();
        setScrollUnitIncrement();
        // 监听sql tabbed panel的点击事件
        sqlTabbedPanel.addChangeListener(e -> sqlTabbedPanelListener());
    }


    private void createUIComponents() {
        this.executeResultTable = new JTable() {
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
        executeInfoTextScroll.setBorder(new LineBorder(JBColor.GRAY));

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

    private void setTableRowHeight() {
        this.executeResultTable.setRowHeight(TABLE_ROW_HEIGHT);
        this.executeHitIndexTable.setRowHeight(TABLE_ROW_HEIGHT);
    }

    private void setScrollUnitIncrement() {
        int unit = 16;
        this.statementScroll.getVerticalScrollBar().setUnitIncrement(unit);
        this.statementScroll.getHorizontalScrollBar().setUnitIncrement(unit);
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

    private void generateSql() {
        try {

            MybatisSqlConfiguration sqlConfig = project.getService(MybatisSqlStateComponent.class).getConfiguration();

            String sqlStr = SqlParamGenerateComponent.generateSql(project, sqlConfig.getMethod(), sqlConfig.getParams());
            sqlConfig.setSql(sqlStr);

            statementText.setText(sqlStr);

        } catch (Throwable e) {
            Messages.showInfoMessage("generate sql error. err: " + e.getMessage(), Constant.APPLICATION_NAME);
        }
    }

    private void executeSql() {

        MybatisSqlConfiguration sqlConfig = project.getService(MybatisSqlStateComponent.class).getConfiguration();

        executeHitIndexScroll.setVisible(true);

        try {
            BaseResult executeResult = SqlExecutor.executeSql(project, sqlConfig.getSql(), true);
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

        MybatisSqlConfiguration sqlConfig = project.getService(MybatisSqlStateComponent.class).getConfiguration();

        String explainSql = String.format("explain %s", sqlConfig.getSql());
        SelectResult executeResult = (SelectResult) SqlExecutor.executeSql(project, explainSql, false);

        executeHitIndexTable.setModel(executeResult.getModel());

    }

    public JTabbedPane getSqlTabbedPanel() {
        return sqlTabbedPanel;
    }

    @Override
    public void listen() {

        this.sqlTabbedPanel.setSelectedIndex(0);

        generateSql();
        // TODO: SQL规范校验
        MybatisSqlConfiguration configuration = project.getService(MybatisSqlStateComponent.class).getConfiguration();

        ProcessResult<String> validateResult = SqlParser.validate(configuration.getSql());

        if (!validateResult.isSuccess()) {
            statementRuleText.setText(validateResult.getErrorMsg());
        } else {
            List<Report> reports = CheckerHolder.getCheckers().stream().map(checker -> checker.check(configuration.getSql())).flatMap(Collection::stream).collect(Collectors.toList());
            String ruleInfo = ResultConverter.convert2RuleInfo(reports);

            if (StringUtils.isBlank(ruleInfo)) {
                statementRulePanel.setVisible(false);
            } else {

                statementRulePanel.setVisible(true);
                statementRuleText.setText(ruleInfo);
            }
//            statementRuleText.setText("TODO: sql语句规约");
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
