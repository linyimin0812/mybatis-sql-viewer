package io.github.linyimin.plugin.sql.checker.rule;

import io.github.linyimin.plugin.mock.schema.IndexField;
import io.github.linyimin.plugin.mock.schema.TableField;

import java.util.List;

/**
 * @author banzhe
 * @date 2022/12/14 00:30
 **/
public class CheckField {
    private List<TableField> tableFields;
    private List<IndexField> indexFields;

    public CheckField() {}

    public CheckField(List<TableField> tableFields, List<IndexField> indexFields) {
        this.tableFields = tableFields;
        this.indexFields = indexFields;
    }

    public CheckField(List<IndexField> indexFields) {
        this.indexFields = indexFields;
    }


    public List<TableField> getTableFields() {
        return tableFields;
    }

    public void setTableFields(List<TableField> tableFields) {
        this.tableFields = tableFields;
    }

    public List<IndexField> getIndexFields() {
        return indexFields;
    }

    public void setIndexFields(List<IndexField> indexFields) {
        this.indexFields = indexFields;
    }
}
