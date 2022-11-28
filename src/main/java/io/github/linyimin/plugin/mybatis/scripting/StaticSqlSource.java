package io.github.linyimin.plugin.mybatis.scripting;

import io.github.linyimin.plugin.mybatis.mapping.SqlSource;

/**
 * @author Clinton Begin
 */
public class StaticSqlSource implements SqlSource {

    private final String sql;

    public StaticSqlSource(String sql) {
        this.sql = sql;
    }

    @Override
    public String getSql(Object parameterObject) {
        return "";
    }

}

