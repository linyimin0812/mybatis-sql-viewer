package io.github.linyimin.plugin.ui;

import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.progress.BackgroundTaskQueue;
import com.intellij.openapi.progress.ProgressIndicator;
import com.intellij.openapi.progress.Task;
import com.intellij.openapi.project.Project;
import io.github.linyimin.plugin.ProcessResult;
import io.github.linyimin.plugin.component.SqlParamGenerateComponent;
import io.github.linyimin.plugin.configuration.MybatisSqlStateComponent;
import io.github.linyimin.plugin.configuration.model.MybatisSqlConfiguration;
import io.github.linyimin.plugin.constant.Constant;
import io.github.linyimin.plugin.sql.parser.SqlParser;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.List;

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
        this.tableTabbedPanel.addMouseListener(new MouseCursorAdapter(this.tableTabbedPanel));
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
                    SqlParamGenerateComponent.generateSql(project, configuration.getMethod(), configuration.getParams(), true);
                }

                if (isInvalid(configuration.getSql(), infoPane)) {
                    return;
                }

                List<String> tables = SqlParser.getTableNames(configuration.getSql());

                ApplicationManager.getApplication().invokeLater(() -> {
                    tableTabbedPanel.removeAll();
                });

                for (String table : tables) {
                    SpecifyTableTabbedPane tabbedPanel = new SpecifyTableTabbedPane(project, tableTabbedPanel);

                    ApplicationManager.getApplication().invokeLater(() -> {
                        tableTabbedPanel.addTab(table, tabbedPanel.getSpecifyTablePanel());
                    });

                    tabbedPanel.acquireTableSchema(table);
                }
            }
        });
    }

    private InfoPane createInfoPane() {

        InfoPane infoPane = new InfoPane();

        ApplicationManager.getApplication().invokeLater(() -> {
            tableTabbedPanel.removeAll();
            tableTabbedPanel.addTab("info", infoPane.getInfoPane());
            infoPane.setText("Retrieving table schema...");
        });

        return infoPane;
    }

    private boolean isInvalid(String sql, InfoPane infoPane) {

        if (StringUtils.isBlank(sql)) {
            ApplicationManager.getApplication().invokeLater(() -> infoPane.setText(Constant.INPUT_SQL_PROMPT));
            return true;
        }

        try {
            ProcessResult<String> validateResult = SqlParser.validate(sql);
            if (!validateResult.isSuccess()) {
                sql = StringUtils.replace(sql, "\n", " ").replaceAll("\\s+", " ");
                String prompt = String.format("%s\n%s", sql, validateResult.getErrorMsg());
                ApplicationManager.getApplication().invokeLater(() -> infoPane.setText(prompt));
                return true;
            }

        } catch (Exception e) {
            StringWriter sw = new StringWriter();
            e.printStackTrace(new PrintWriter(sw));
            String prompt = String.format("SQL parse error.%s", sw);
            ApplicationManager.getApplication().invokeLater(() -> infoPane.setText(prompt));
            return true;
        }

        return false;
    }
}
