package io.github.linyimin.plugin.sql.result;

import javax.swing.table.DefaultTableModel;

/**
 * @author yiminlin
 * @date 2022/11/27 19:45
 **/
public class SelectResult extends BaseResult {

    public SelectResult() {

    }

    public SelectResult(long cost, DefaultTableModel model) {
        super(cost);
        this.model = model;
    }

    public SelectResult(String sql, long cost, DefaultTableModel model) {
        super(sql, cost);
        this.model = model;
    }

    private DefaultTableModel model;

    public DefaultTableModel getModel() {
        return model;
    }

    public void setModel(DefaultTableModel model) {
        this.model = model;
    }
}
