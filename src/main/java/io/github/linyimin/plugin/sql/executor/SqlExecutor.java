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
import org.apache.commons.lang3.tuple.Pair;

import javax.swing.table.DefaultTableModel;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
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

        DatasourceComponent datasourceComponent = project.getService(DatasourceComponent.class);

        try {
            datasourceComponent.getConnection();
            return "Server Connected.";
        } catch (Exception e) {
            StringWriter sw = new StringWriter();
            e.printStackTrace(new PrintWriter(sw));
            return String.format("Server can't Connect!\n%s", sw);
        }
    }

    public static BaseResult executeSql(Project project, String sql, boolean needTotalRows) throws Exception {

        SqlType sqlType = SqlParser.getSqlType(sql);

        return SQL_EXECUTOR_MAP.get(sqlType).executeSql(project, sql, needTotalRows);

    }

    private static class SelectExecutor implements Executor {

        @Override
        public BaseResult executeSql(Project project, String sql, boolean needTotalRows) throws Exception {

            DatasourceComponent datasourceComponent = project.getService(DatasourceComponent.class);


            try (Connection connection = datasourceComponent.getConnection()) {

                SelectResult result;


                try (Statement stmt = connection.createStatement()) {
                    long cost = executeAndReturnCost(stmt, sql);

                    DefaultTableModel model = ResultConverter.convert2TableModel(stmt.getResultSet());

                    result = new SelectResult(sql, cost, model);
                }

                if (!needTotalRows) {
                    return result;
                }

                try (Statement stmt = connection.createStatement()) {
                    result.setTotalRows(acquireTotalRows(stmt, SqlParser.getTableNames(sql)));
                }

                return result;

            }
        }
    }

    private static class UpdateExecutor implements Executor {

        @Override
        public BaseResult executeSql(Project project, String sql, boolean needTotalRows) throws Exception {

            DatasourceComponent datasourceComponent = project.getService(DatasourceComponent.class);

            try (Connection connection = datasourceComponent.getConnection(); ) {

                UpdateResult result;

                try (Statement stmt = connection.createStatement()) {
                    long cost = executeAndReturnCost(stmt, sql);
                    result = new UpdateResult(sql, cost, stmt.getUpdateCount());
                }

                if (!needTotalRows) {
                    return result;
                }

                try (Statement stmt = connection.createStatement()) {
                    result.setTotalRows(acquireTotalRows(stmt, SqlParser.getTableNames(sql)));
                }

                return result;
            }
        }
    }

    private interface Executor {
        BaseResult executeSql(Project project, String sql, boolean needTotalRows) throws Exception;

        default long executeAndReturnCost(Statement statement, String sql) throws SQLException {
            long start = System.currentTimeMillis();

            statement.execute(sql);

            return System.currentTimeMillis() - start;
        }

        default List<Pair<String /*table*/, Long /* number of record */>> acquireTotalRows(Statement statement, List<String> tables) throws SQLException {

            List<Pair<String, Long>> pairs = new ArrayList<>();

            String sqlTemplate = "SELECT COUNT(1) AS total FROM %s";

            for (String table : tables) {
                statement.execute(String.format(sqlTemplate, table));
                ResultSet rs = statement.getResultSet();
                rs.next();
                Pair<String, Long> pair = Pair.of(table, statement.getResultSet().getLong("total"));
                pairs.add(pair);
            }
            return pairs;
        }
    }
}
