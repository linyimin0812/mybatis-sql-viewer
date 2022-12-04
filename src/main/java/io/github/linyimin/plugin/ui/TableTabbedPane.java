package io.github.linyimin.plugin.ui;

import com.intellij.openapi.project.Project;
import io.github.linyimin.plugin.component.SqlParamGenerateComponent;
import io.github.linyimin.plugin.configuration.MybatisSqlStateComponent;
import io.github.linyimin.plugin.configuration.model.MybatisSqlConfiguration;
import io.github.linyimin.plugin.sql.executor.SqlExecutor;
import io.github.linyimin.plugin.sql.parser.SqlParser;
import io.github.linyimin.plugin.sql.result.SelectResult;
import org.apache.commons.lang3.StringUtils;

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

    public TableTabbedPane(Project project) {
        this.project = project;
    }

    public JTabbedPane getTableTabbedPanel() {
        return this.tableTabbedPanel;
    }

    @Override
    public void listen() {

        // 获取表列信息：DESC mybatis.CITY;
        // 获取表信息(编码)：show table status from `global_ug_usm_ae` like  'houyi_clc_plan';

        MybatisSqlConfiguration configuration = project.getService(MybatisSqlStateComponent.class).getConfiguration();

        if (StringUtils.isBlank(configuration.getSql())) {
            String sqlStr = SqlParamGenerateComponent.generateSql(project, configuration.getMethod(), configuration.getParams());
            configuration.setSql(sqlStr);
        }

        List<String> tables = SqlParser.getTableNames(configuration.getSql());

        tableTabbedPanel.removeAll();

        for (String table : tables) {

            String metaSql = TABLE_META_SQL_TEMPLATE.replace("${table}", table);
            String indexSql = TABLE_INDEX_SQL_TEMPLATE.replace("${table}", table);
            SpecifyTableTabbedPane tabbedPanel = new SpecifyTableTabbedPane(project, tableTabbedPanel);
            tableTabbedPanel.addTab(table, tabbedPanel.getSpecifyTablePanel());
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
                break;
            }
        }
    }
}
