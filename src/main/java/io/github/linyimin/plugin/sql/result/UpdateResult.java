package io.github.linyimin.plugin.sql.result;


/**
 * @author yiminlin
 * @date 2022/11/27 19:53
 **/
public class UpdateResult extends BaseResult {

    public UpdateResult() {}

    public UpdateResult(long cost, int affectedCount) {
        super(cost);
        this.affectedCount = affectedCount;
    }

    public UpdateResult(String sql, long cost, int affectedCount) {
        super(sql, cost);
        this.affectedCount = affectedCount;
    }

    private int affectedCount;

    public int getAffectedCount() {
        return affectedCount;
    }

    public void setAffectedCount(int affectedCount) {
        this.affectedCount = affectedCount;
    }
}
