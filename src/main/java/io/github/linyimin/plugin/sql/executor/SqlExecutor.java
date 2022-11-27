package io.github.linyimin.plugin.sql.executor;

import com.intellij.openapi.project.Project;
import com.intellij.util.containers.ContainerUtil;
import io.github.linyimin.plugin.sql.DatasourceComponent;
import io.github.linyimin.plugin.sql.converter.ResultConverter;
import io.github.linyimin.plugin.sql.parser.SqlParser;
import io.github.linyimin.plugin.sql.parser.SqlType;
import io.github.linyimin.plugin.sql.result.BaseResult;
import io.github.linyimin.plugin.sql.result.SelectResult;
import io.github.linyimin.plugin.sql.result.UpdateResult;

import javax.swing.table.DefaultTableModel;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Map;

/**
 * @author yiminlin
 * @date 2022/11/27 18:26
 **/
public class SqlExecutor {

    private static final Map<SqlType, Executor> SQL_EXECUTOR_MAP = new ContainerUtil.ImmutableMapBuilder<SqlType, Executor>()
            .put(SqlType.select, new SelectExecutor())
            .put(SqlType.update, new UpdateExecutor())
            .build();

    public static String testConnected(Project project) {

        DatasourceComponent datasourceComponent = project.getComponent(DatasourceComponent.class);

        try {
            datasourceComponent.getConnection();
            return "Server Connected.";
        } catch (Exception e) {
            StringWriter sw = new StringWriter();
            e.printStackTrace(new PrintWriter(sw));
            return String.format("Server can't Connect!\n%s", sw);
        }
    }

    public static BaseResult executeSql(Project project, String sql) throws Exception {

        SqlType sqlType = SqlParser.getSqlType(sql);

        return SQL_EXECUTOR_MAP.get(sqlType).executeSql(project, sql);

    }

    private static class SelectExecutor implements Executor {

        @Override
        public BaseResult executeSql(Project project, String sql) throws Exception {

            DatasourceComponent datasourceComponent = project.getComponent(DatasourceComponent.class);

            try (Connection connection = datasourceComponent.getConnection(); Statement stmt = connection.createStatement()) {

                long cost = executeAndReturnCost(stmt, sql);

                DefaultTableModel model = ResultConverter.convert2TableModel(stmt.getResultSet());

                return new SelectResult(sql, cost, model);
            }
        }
    }

    private static class UpdateExecutor implements Executor {

        @Override
        public BaseResult executeSql(Project project, String sql) throws Exception {

            DatasourceComponent datasourceComponent = project.getComponent(DatasourceComponent.class);

            try (Connection connection = datasourceComponent.getConnection(); Statement stmt = connection.createStatement()) {
                long cost = executeAndReturnCost(stmt, sql);

                return new UpdateResult(sql, cost, stmt.getUpdateCount());
            }
        }
    }

    private interface Executor {
        BaseResult executeSql(Project project, String sql) throws Exception;

        default long executeAndReturnCost(Statement statement, String sql) throws SQLException {
            long start = System.currentTimeMillis();

            statement.execute(sql);

            return System.currentTimeMillis() - start;
        }

    }
}
