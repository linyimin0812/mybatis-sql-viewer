package io.github.linyimin.plugin.mybatis.scripting.tags;

import com.github.vertical_blank.sqlformatter.SqlFormatter;
import com.github.vertical_blank.sqlformatter.core.FormatConfig;
import com.github.vertical_blank.sqlformatter.languages.Dialect;
import io.github.linyimin.plugin.mybatis.mapping.SqlSource;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author Clinton Begin
 */
public class DynamicSqlSource implements SqlSource {

    private final SqlNode rootSqlNode;

    private final Pattern pattern = Pattern.compile("[$, #]\\{(.*?)}");

    public DynamicSqlSource(SqlNode rootSqlNode) {
        this.rootSqlNode = rootSqlNode;
    }

    @Override
    public String getSql(Object parameterObject) {

        DynamicContext context = new DynamicContext(parameterObject);

        rootSqlNode.apply(context);

        String sql = parameterize(context.getSql(), context);

        FormatConfig formatConfig = FormatConfig.builder().uppercase(true).maxColumnLength(100).build();

        return SqlFormatter.of(Dialect.MySql).format(sql, formatConfig);
    }

    private String parameterize(String preparedStatement, DynamicContext context) {

        List<String> params = extractPlaceholder(preparedStatement);

        String parameterizeSql = preparedStatement;

        for (String param : params) {
            parameterizeSql = setParameter(parameterizeSql, param, context);
        }

        return parameterizeSql;

    }

    private String setParameter(String parameterizeSql, String param, DynamicContext context) {

        String realParam = param;
        // #{id:INTEGER}
        if (param.contains(":")) {
            realParam = realParam.substring(0, param.lastIndexOf(":"));
        }
        // #{elementIds, typeHandler=club.linyimin.dao.handler.ListToJsonTypeHandler}
        if (param.contains(",")) {
            realParam = realParam.substring(0, param.lastIndexOf(","));
        }

        Object value = context.getBindings().get(realParam);

        if (value == null) {
            return parameterizeSql;
        }

        if (value.getClass() == String.class || value.getClass() == Character.class || value.getClass() == Date.class) {
            value = "'" + value +"'";
        }

        return parameterizeSql.replaceAll("[$, #]\\{" + param + "}", value.toString());

    }

    private List<String> extractPlaceholder(String preparedStatement) {
        Matcher matcher = pattern.matcher(preparedStatement);

        List<String> params = new ArrayList<>();

        while (matcher.find()) {
            for (int i = 1; i <= matcher.groupCount(); i++) {
                params.add(matcher.group(i));
            }
        }

        return params;
    }

}
