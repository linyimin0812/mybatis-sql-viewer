package io.github.linyimin.plugin.ui;

import com.intellij.openapi.progress.BackgroundTaskQueue;
import com.intellij.openapi.progress.ProgressIndicator;
import com.intellij.openapi.progress.Task;
import com.intellij.openapi.project.Project;
import io.github.linyimin.plugin.ProcessResult;
import io.github.linyimin.plugin.component.SqlParamGenerateComponent;
import io.github.linyimin.plugin.configuration.MybatisSqlStateComponent;
import io.github.linyimin.plugin.configuration.model.MybatisSqlConfiguration;
import io.github.linyimin.plugin.constant.Constant;
import io.github.linyimin.plugin.sql.executor.SqlExecutor;
import io.github.linyimin.plugin.sql.parser.SqlParser;
import io.github.linyimin.plugin.sql.result.SelectResult;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.List;

import static io.github.linyimin.plugin.constant.Constant.TABLE_INDEX_SQL_TEMPLATE;
import static io.github.linyimin.plugin.constant.Constant.TABLE_META_SQL_TEMPLATE;

/**
 * @author banzhe
 * @date 2022/12/03 20:39
 **/
public class TableTabbedPane implements TabbedChangeListener {
    private JPanel panel1;

    private JTabbedPane tableTabbedPanel;

    private final Project project;

    private final BackgroundTaskQueue backgroundTaskQueue;

    public TableTabbedPane(Project project) {
        this.project = project;
        this.backgroundTaskQueue = new BackgroundTaskQueue(project, Constant.APPLICATION_NAME);
    }

    public JTabbedPane getTableTabbedPanel() {
        return this.tableTabbedPanel;
    }

    @Override
    public void listen() {

        // 获取表列信息：DESC mybatis.CITY;
        // 获取表信息(编码)：show table status from `global_ug_usm_ae` like  'houyi_clc_plan';
        backgroundTaskQueue.run(new Task.Backgroundable(project, Constant.APPLICATION_NAME) {
            @Override
            public void run(@NotNull ProgressIndicator indicator) {

                InfoPane infoPane = createInfoPane();

                MybatisSqlConfiguration configuration = project.getService(MybatisSqlStateComponent.class).getConfiguration();

                if (StringUtils.isBlank(configuration.getSql())) {
                    String sqlStr = SqlParamGenerateComponent.generateSql(project, configuration.getMethod(), configuration.getParams());
                    configuration.setSql(sqlStr);
                }

                if (isInvalid(configuration.getSql(), infoPane)) {
                    return;
                }

                List<String> tables = SqlParser.getTableNames(configuration.getSql());

                tableTabbedPanel.removeAll();

                for (String table : tables) {
                    acquireTableSchema(table);
                }
            }
        });
    }

    private void acquireTableSchema(String table) {
        String metaSql = TABLE_META_SQL_TEMPLATE.replace("${table}", table);
        String indexSql = TABLE_INDEX_SQL_TEMPLATE.replace("${table}", table);

        SpecifyTableTabbedPane tabbedPanel = new SpecifyTableTabbedPane(project, tableTabbedPanel);
        tableTabbedPanel.addTab(table, tabbedPanel.getSpecifyTablePanel());

        tabbedPanel.getTableRuleText().setText("Loading table schema...");

        try {
            SelectResult metaResult = (SelectResult) SqlExecutor.executeSql(project, metaSql, false);
            SelectResult indexResult = (SelectResult) SqlExecutor.executeSql(project, indexSql, false);
            // TODO: 建表规约
            tabbedPanel.getTableRuleText().setText("TODO: 建表规约");

            tabbedPanel.setTables(metaResult.getModel(), indexResult.getModel());
            tabbedPanel.getMockConfigResultText().setText("TODO: mock configuration result");

        } catch (Exception e) {
            StringWriter sw = new StringWriter();
            e.printStackTrace(new PrintWriter(sw));
            tabbedPanel.getTableRuleText().setText(sw.toString());
        }
    }

    private InfoPane createInfoPane() {

        tableTabbedPanel.removeAll();

        InfoPane infoPane = new InfoPane();
        tableTabbedPanel.addTab("info", infoPane.getInfoPane());
        infoPane.setText("Retrieving table schema...");

        return infoPane;
    }

    private boolean isInvalid(String sql, InfoPane infoPane) {
        try {
            ProcessResult<String> validateResult = SqlParser.validate(sql);
            if (!validateResult.isSuccess()) {
                sql = StringUtils.replace(sql, "\n", " ").replaceAll("\\s+", " ");
                infoPane.setText(String.format("%s\n%s", sql, validateResult.getErrorMsg()));
                return true;
            }

        } catch (Exception e) {
            StringWriter sw = new StringWriter();
            e.printStackTrace(new PrintWriter(sw));
            infoPane.setText(String.format("SQL parse error.%s", sw));
            return true;
        }

        return false;
    }
}
