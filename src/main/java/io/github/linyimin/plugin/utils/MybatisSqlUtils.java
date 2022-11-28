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

    public static ProcessResult<String> getSqlFromXML(Project project, String qualifiedMethod, String params) {

        try {

            String namespace = qualifiedMethod.substring(0, qualifiedMethod.lastIndexOf("."));
            Optional<String> optional = MybatisXmlContentCache.acquireByNamespace(project, namespace).stream().map(XmlTag::getText).findFirst();

            if (!optional.isPresent()) {
                return ProcessResult.fail("Oops! The plugin can't find the mapper file.");
            }

            XMLMapperBuilder builder = new XMLMapperBuilder(new ByteArrayInputStream(optional.get().getBytes(Charsets.toCharset(Charset.defaultCharset()))));
            Map<String, SqlSource> sqlSourceMap = builder.parse();

            if (!sqlSourceMap.containsKey(qualifiedMethod)) {
                return ProcessResult.fail(String.format("Oops! There is not %s in mapper file!!!", qualifiedMethod));
            }

            return ProcessResult.success(sqlSourceMap.get(qualifiedMethod).getSql(params));
        } catch (Throwable t) {
            StringWriter sw = new StringWriter();
            t.printStackTrace(new PrintWriter(sw));
            return ProcessResult.fail(String.format("Oops! There are something wrong when generate sql statement.\n%s", sw));
        }

    }

    public static ProcessResult<String> getSqlFromAnnotation(Project project, String qualifiedMethod, String params) {
        // 处理annotation
        String clazzName = qualifiedMethod.substring(0, qualifiedMethod.lastIndexOf("."));
        String methodName = qualifiedMethod.substring(qualifiedMethod.lastIndexOf(".") + 1);

        List<PsiMethod> psiMethods = JavaUtils.findMethod(project, clazzName, methodName);

        if (psiMethods.isEmpty()) {
            return ProcessResult.fail("annotation is not exist.");
        }

        PsiAnnotation annotation = Arrays.stream(psiMethods.get(0).getAnnotations())
                .filter(psiAnnotation -> MYBATIS_SQL_ANNOTATIONS.contains(psiAnnotation.getQualifiedName()))
                .findFirst().orElse(null);


        if (annotation == null) {
            return ProcessResult.fail("There is no of annotation on the method.");
        }

        PsiAnnotationMemberValue value = annotation.findAttributeValue("value");
        if (value == null) {
            return ProcessResult.success("The annotation does not specify a value for the value field");
        }

        String content = String.valueOf(JavaPsiFacade.getInstance(project).getConstantEvaluationHelper().computeConstantExpression(value));


        if (StringUtils.isBlank(content)) {
            return ProcessResult.success("The value of annotation is empty.");
        }

        String sql = new XMLLanguageDriver().createSqlSource(content, null).getSql(params);

        return ProcessResult.success(sql);
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
