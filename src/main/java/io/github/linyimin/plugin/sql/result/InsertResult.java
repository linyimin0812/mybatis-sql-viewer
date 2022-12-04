package io.github.linyimin.plugin.sql.result;

/**
 * @author banzhe
 * @date 2022/12/04 18:09
 **/
public class InsertResult extends UpdateResult {

    private long lastInsertId;

    public InsertResult() {}

    public InsertResult(long cost, int affectedCount) {
        super(cost, affectedCount);
    }

    public long getLastInsertId() {
        return lastInsertId;
    }

    public void setLastInsertId(long lastInsertId) {
        this.lastInsertId = lastInsertId;
    }

}
