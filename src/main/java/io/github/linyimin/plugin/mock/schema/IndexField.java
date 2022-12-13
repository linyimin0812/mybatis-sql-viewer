package io.github.linyimin.plugin.mock.schema;

import com.alibaba.fastjson.annotation.JSONField;

/**
 * @author banzhe
 * @date 2022/12/13 23:20
 **/
public class IndexField {
    @JSONField(name = "Table")
    private String table;
    @JSONField(name = "Non_unique")
    private int nonUnique;
    @JSONField(name = "Key_name")
    private String keyName;
    @JSONField(name = "Seq_in_index")
    private int seqInIndex;
    @JSONField(name = "Column_name")
    private String columnName;
    @JSONField(name = "Sub_part")
    private int subPart;
    @JSONField(name = "Null")
    private String nullable;
    @JSONField(name = "Comment")
    private String comment;
    @JSONField(name = "Index_comment")
    private String indexComment;

    public String getTable() {
        return table;
    }

    public void setTable(String table) {
        this.table = table;
    }

    public int getNonUnique() {
        return nonUnique;
    }

    public void setNonUnique(int nonUnique) {
        this.nonUnique = nonUnique;
    }

    public String getKeyName() {
        return keyName;
    }

    public boolean isUnique() {
        return this.nonUnique == 0;
    }

    public void setKeyName(String keyName) {
        this.keyName = keyName;
    }

    public int getSeqInIndex() {
        return seqInIndex;
    }

    public void setSeqInIndex(int seqInIndex) {
        this.seqInIndex = seqInIndex;
    }

    public String getColumnName() {
        return columnName;
    }

    public void setColumnName(String columnName) {
        this.columnName = columnName;
    }

    public int getSubPart() {
        return subPart;
    }

    public void setSubPart(int subPart) {
        this.subPart = subPart;
    }

    public String getNullable() {
        return nullable;
    }

    public void setNullable(String nullable) {
        this.nullable = nullable;
    }

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }

    public String getIndexComment() {
        return indexComment;
    }

    public void setIndexComment(String indexComment) {
        this.indexComment = indexComment;
    }
}
