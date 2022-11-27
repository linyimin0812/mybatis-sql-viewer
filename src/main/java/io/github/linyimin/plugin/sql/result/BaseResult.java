package io.github.linyimin.plugin.sql.result;

import org.apache.commons.lang3.StringUtils;

/**
 * @author yiminlin
 * @date 2022/11/27 19:44
 **/
public class BaseResult {

    public BaseResult() {}

    public BaseResult(long cost) {
        this.cost = cost;
    }

    public BaseResult(String sql, long cost) {

        if (StringUtils.isNotBlank(sql)) {
            this.sql = sql.replaceAll("\n", " ");
            this.sql = this.sql.replaceAll("\\s+", " ");
        }

        this.cost = cost;
    }
    private long cost;

    private String sql;

    public long getCost() {
        return cost;
    }

    public void setCost(long cost) {
        this.cost = cost;
    }

    public String getSql() {
        return sql;
    }

    public void setSql(String sql) {
        this.sql = sql;
    }
}
