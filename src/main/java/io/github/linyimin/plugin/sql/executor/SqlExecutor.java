package io.github.linyimin.plugin.sql.executor;

import com.intellij.openapi.project.Project;
import com.intellij.util.containers.ContainerUtil;
import io.github.linyimin.plugin.constant.Constant;
import io.github.linyimin.plugin.sql.DatasourceComponent;
import io.github.linyimin.plugin.sql.converter.ResultConverter;
import io.github.linyimin.plugin.sql.parser.SqlParser;
import io.github.linyimin.plugin.sql.parser.SqlType;
import io.github.linyimin.plugin.sql.result.BaseResult;
import io.github.linyimin.plugin.sql.result.InsertResult;
import io.github.linyimin.plugin.sql.result.SelectResult;
import io.github.linyimin.plugin.sql.result.UpdateResult;
import org.apache.commons.lang3.StringUtils;
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

        try (Connection ignored = datasourceComponent.getConnection()) {
            return Constant.DATASOURCE_CONNECTED;
        } catch (Exception e) {
            StringWriter sw = new StringWriter();
            e.printStackTrace(new PrintWriter(sw));
            return String.format("Server can't Connect!\n%s", sw);
        }
    }

    public static String acquirePrimaryKey(Project project, String table) throws Exception {

        DatasourceComponent datasourceComponent = project.getService(DatasourceComponent.class);

        String sql = String.format("DESC %s;", table);

        try (Connection connection = datasourceComponent.getConnection(); Statement statement = connection.createStatement()) {
            statement.execute(sql);
            ResultSet rs = statement.getResultSet();

            while (rs.next()) {
                if (StringUtils.equals(rs.getString("Key"), "PRI")) {
                    return rs.getString("Field");
                }
            }
        }

        return "ID";
    }

    public static BaseResult executeSql(Project project, String sql, boolean needTotalRows) throws Exception {

        SqlType sqlType = SqlParser.getExecuteSqlType(sql);

        return SQL_EXECUTOR_MAP.get(sqlType).executeSql(project, sql, needTotalRows);

    }

    public static InsertResult saveMockData(Project project, String sql, boolean needTotalRows) throws Exception {
        return (InsertResult) new InsertExecutor().executeSql(project, sql, needTotalRows);
    }

    private static class InsertExecutor implements Executor {

        @Override
        public BaseResult executeSql(Project project, String sql, boolean needTotalRows) throws Exception {
            DatasourceComponent datasourceComponent = project.getService(DatasourceComponent.class);
            InsertResult result = new InsertResult();
            Connection connection = null;

            try {

                connection = datasourceComponent.getConnection();
                connection.setAutoCommit(false);

                try (Statement statement = connection.createStatement()) {
                    long cost = executeAndReturnCost(statement, sql);
                    result.setCost(cost);
                    result.setAffectedCount(statement.getUpdateCount());
                }

                List<String> tables = SqlParser.getTableNames(sql);

                if (needTotalRows) {
                    try (Statement stmt = connection.createStatement()) {
                        result.setTotalRows(acquireTotalRows(stmt, tables));
                    }
                }

                try (Statement statement = connection.createStatement()) {
                    result.setLastInsertId(acquireLastInsertId(statement, tables.get(0)));
                }

                connection.commit();

                return result;

            } catch (Exception e) {
                if (connection != null) {
                    connection.rollback();
                }
                throw e;
            } finally {
                if (connection != null) {
                    connection.close();
                }
            }
        }
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

        default long acquireLastInsertId(Statement statement, String table) throws SQLException {

            String maxIdStr = "max_id";
            String primaryKey = acquirePrimaryKey(statement, table);

            statement.execute(String.format("SELECT MAX(%s) AS %s FROM %s;", primaryKey, maxIdStr, table));
            ResultSet rs = statement.getResultSet();
            rs.next();
            return rs.getLong(maxIdStr);
        }

        default String acquirePrimaryKey(Statement statement, String table) throws SQLException {

            String sql = String.format("DESC %s;", table);
            statement.execute(sql);
            ResultSet rs = statement.getResultSet();

            while (rs.next()) {
                if (StringUtils.equals(rs.getString("Key"), "PRI")) {
                    return rs.getString("Field");
                }
            }

            return "ID";
        }
    }


}
