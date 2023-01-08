package io.github.linyimin.plugin.mybatis.scripting;

import io.github.linyimin.plugin.component.SqlParamGenerateComponent;
import io.github.linyimin.plugin.mybatis.mapping.SqlSource;

import java.util.List;

/**
 * @author Clinton Begin
 */
public class StaticSqlSource implements SqlSource {

    private final String sql;

    public StaticSqlSource(String sql) {
        this.sql = sql;
    }

    @Override
    public String getSql(List<SqlParamGenerateComponent.ParamNameType> types, Object parameterObject) {
        return "";
    }

}

