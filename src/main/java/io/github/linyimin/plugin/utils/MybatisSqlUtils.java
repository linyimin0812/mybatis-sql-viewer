package io.github.linyimin.plugin.utils;

import com.alibaba.fastjson.JSON;
import com.intellij.openapi.project.Project;
import com.intellij.psi.*;
import com.intellij.psi.xml.XmlTag;
import io.github.linyimin.plugin.ProcessResult;
import io.github.linyimin.plugin.cache.MybatisXmlContentCache;
import io.github.linyimin.plugin.mybatis.mapping.SqlSource;
import io.github.linyimin.plugin.mybatis.xml.XMLLanguageDriver;
import io.github.linyimin.plugin.mybatis.xml.XMLMapperBuilder;
import org.apache.commons.io.Charsets;
import org.apache.commons.lang3.StringUtils;

import javax.swing.table.DefaultTableModel;
import java.io.ByteArrayInputStream;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.nio.charset.Charset;
import java.sql.*;
import java.util.*;

import static io.github.linyimin.plugin.constant.Constant.MYBATIS_SQL_ANNOTATIONS;

/**
 * @author yiminlin
 * @date 2022/02/05 3:15 上午`
 **/
public class MybatisSqlUtils {

    public static String mysqlConnectTest(String url, String user, String password) {
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
        } catch (ClassNotFoundException e) {
            return "com.mysql.cj.jdbc.Driver class not found";
        }

        try (Connection ignored = DriverManager.getConnection(url, user, password)) {
            return "Server Connected.";
        } catch (SQLException ex) {
            return "Server can't Connect! err: " + ex.getMessage();
        }
    }

    public static DefaultTableModel acquireTableSchema(String url, String user, String password, String sql) throws SQLException {
        Connection connection = null;
        Statement stmt = null;
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
            connection = DriverManager.getConnection(url,user,password);
            stmt = connection.createStatement();
            stmt.execute(sql);
            return buildTableModel(stmt.getResultSet());

        } catch(Throwable e) {
            return null;
        } finally {
            if (Objects.nonNull(connection)) {
                connection.close();
            }

            if (Objects.nonNull(stmt)) {
                stmt.close();
            }
        }
    }

    private static DefaultTableModel buildTableModel(ResultSet rs) throws SQLException {

        ResultSetMetaData metaData = rs.getMetaData();

        // names of columns
        Vector<String> columnNames = new Vector<>();
        int columnCount = metaData.getColumnCount();
        for (int column = 1; column <= columnCount; column++) {
            columnNames.add(metaData.getColumnLabel(column));
        }

        // data of the table
        Vector<Vector<Object>> data = new Vector<>();
        while (rs.next()) {
            Vector<Object> vector = new Vector<>();
            for (int columnIndex = 1; columnIndex <= columnCount; columnIndex++) {
                vector.add(rs.getObject(columnIndex));
            }
            data.add(vector);
        }

        return new DefaultTableModel(data, columnNames);

    }


    public static String executeSql(String url, String user, String password, String sql) throws SQLException {
        Connection connection = null;
        Statement stmt = null;
        StringBuilder sb = new StringBuilder();
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
            connection = DriverManager.getConnection(url,user,password);
            stmt = connection.createStatement();
            boolean isSuccess = stmt.execute(sql);
            if (isSuccess) {

                List<Map<String, Object>> resultList = new ArrayList<>();

                ResultSet resultSet = stmt.getResultSet();
                ResultSetMetaData metaData = resultSet.getMetaData();
                int numOfCol = metaData.getColumnCount();
                int rows = 0;
                while (resultSet.next() && rows < 10) {
                    Map<String, Object> rowMap = new HashMap<>();
                    for(int i = 1; i <= numOfCol; i++) {
                        rowMap.put(metaData.getColumnName(i), resultSet.getObject(i));
                    }
                    resultList.add(rowMap);
                    rows++;
                }

                sb.append(JSON.toJSONString(resultList, true));

            } else {
                int row = stmt.getUpdateCount();
                sb.append(String.format("Query OK, %d row affected", row));
            }

        } catch(Throwable e) {
            sb.append("Query Failed, err: ").append(e.getMessage());
        } finally {
            if (Objects.nonNull(connection)) {
                connection.close();
            }

            if (Objects.nonNull(stmt)) {
                stmt.close();
            }
        }

        return sb.toString();
    }
}
