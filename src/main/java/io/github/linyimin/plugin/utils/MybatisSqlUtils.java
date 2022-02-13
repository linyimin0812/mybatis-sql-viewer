package io.github.linyimin.plugin.utils;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.TypeReference;
import com.github.vertical_blank.sqlformatter.SqlFormatter;
import com.intellij.openapi.ui.Messages;
import io.github.linyimin.plugin.compile.MybatisPojoCompile;
import io.github.linyimin.plugin.dom.Constant;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.io.IOUtils;
import org.apache.ibatis.io.Resources;
import org.apache.ibatis.mapping.BoundSql;
import org.apache.ibatis.mapping.MappedStatement;
import org.apache.ibatis.mapping.ParameterMapping;
import org.apache.ibatis.mapping.ParameterMode;
import org.apache.ibatis.reflection.MetaObject;
import org.apache.ibatis.session.Configuration;
import org.apache.ibatis.session.SqlSessionFactory;
import org.apache.ibatis.session.SqlSessionFactoryBuilder;
import org.apache.ibatis.type.TypeHandlerRegistry;

import java.io.InputStream;
import java.nio.charset.Charset;
import java.sql.*;
import java.util.*;

/**
 * @author yiminlin
 * @date 2022/02/05 3:15 上午
 **/
public class MybatisSqlUtils {

    public static String getSql(String mybatisConfiguration, String qualifiedMethod, String params, boolean isCheck) {

        ClassLoader currentClassLoader = Thread.currentThread().getContextClassLoader();

        try {

            Thread.currentThread().setContextClassLoader(MybatisPojoCompile.classLoader);

            InputStream in = IOUtils.toInputStream(mybatisConfiguration, Charset.defaultCharset());
            Resources.setDefaultClassLoader(MybatisPojoCompile.classLoader);

            SqlSessionFactory sqlSessionFactory = new SqlSessionFactoryBuilder().build(in);
            Configuration configuration = sqlSessionFactory.getConfiguration();

            BoundSql sql = getBoundSql(configuration, qualifiedMethod, params);

            return formatSql(configuration, sql);
        } catch (Exception e) {
            if (!isCheck) {
                Messages.showInfoMessage(e.getMessage(), Constant.APPLICATION_NAME);
            }
            throw e;
        } finally {
            Thread.currentThread().setContextClassLoader(currentClassLoader);
        }
    }

    private static String formatSql(Configuration configuration, BoundSql boundSql) {

        // TODO: 待`tableName` format bug修复后，去掉replace
        String sql = boundSql.getSql().replace("`", "");

        // 填充占位符, 不知吃储存过程调用
        Object paramObject = boundSql.getParameterObject();
        List<ParameterMapping> parameterMappingList = boundSql.getParameterMappings();
        TypeHandlerRegistry typeHandlerRegistry = configuration.getTypeHandlerRegistry();

        if (CollectionUtils.isEmpty(parameterMappingList)) {
            return SqlFormatter.format(sql);
        }

        List<String> parameters = new ArrayList<>();
        MetaObject metaObject = Objects.isNull(paramObject) ? null : configuration.newMetaObject(paramObject);

        for (ParameterMapping parameterMapping : parameterMappingList) {
            if (parameterMapping.getMode() == ParameterMode.OUT) {
                continue;
            }

            // 参数值
            Object value;
            String propertyName = parameterMapping.getProperty();

            if (boundSql.hasAdditionalParameter(propertyName)) {
                value = boundSql.getAdditionalParameter(propertyName);
            } else if (Objects.isNull(paramObject)) {
                value = null;
            } else if (typeHandlerRegistry.hasTypeHandler(paramObject.getClass())) {
                value = paramObject;
            } else {
                value = Objects.isNull(metaObject) ? null : metaObject.getValue(propertyName);
            }

            if (Number.class.isAssignableFrom(parameterMapping.getJavaType())) {
                parameters.add(Objects.isNull(value) ? "0" : String.valueOf(value));
            } else {
                String builder = "'" + (Objects.isNull(value) ? "" : value) + "'";
                parameters.add(builder);
            }
        }

        for (String value : parameters) {
            sql = sql.replaceFirst("\\?", value);
        }

        return SqlFormatter.format(sql);
    }

    private static BoundSql getBoundSql(Configuration configuration, String qualifiedMethod, String params) {

        Map<String, Object> map = JSON.parseObject(params, new TypeReference<>(){});

        MappedStatement ms = configuration.getMappedStatement(qualifiedMethod);

        return ms.getBoundSql(map);
    }

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

        } catch(Exception e) {
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
