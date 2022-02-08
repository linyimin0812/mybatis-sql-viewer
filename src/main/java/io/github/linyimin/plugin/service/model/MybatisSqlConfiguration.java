package io.github.linyimin.plugin.service.model;

import org.apache.commons.lang3.StringUtils;

/**
 * @author yiminlin
 * @date 2022/02/02 1:35 上午
 **/
public class MybatisSqlConfiguration {

    private String method;
    private String params;
    private String sql;
    private String result;

    public String getMethod() {
        return method;
    }

    public void setMethod(String method) {
        this.method = method;
    }

    public String getParams() {
        return params;
    }

    public void setParams(String params) {
        this.params = params;
    }

    public String getSql() {
        return sql;
    }

    public void setSql(String sql) {
        this.sql = sql;
    }

    public String getResult() {
        return result;
    }

    public void setResult(String result) {
        this.result = result;
    }

    public void reset() {
        method = StringUtils.EMPTY;
        params = StringUtils.EMPTY;
        sql = StringUtils.EMPTY;
        result = StringUtils.EMPTY;
    }
}
