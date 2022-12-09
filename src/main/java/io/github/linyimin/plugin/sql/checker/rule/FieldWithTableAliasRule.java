package io.github.linyimin.plugin.sql.checker.rule;

import io.github.linyimin.plugin.sql.checker.Report;
import io.github.linyimin.plugin.sql.checker.enums.LevelEnum;
import net.sf.jsqlparser.schema.Column;
import net.sf.jsqlparser.statement.select.*;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.ObjectUtils;

import java.util.Collections;
import java.util.List;

/**
 * @author banzhe
 * @date 2022/12/06 22:25
 **/
public class FieldWithTableAliasRule extends SelectCheckRuleAbstract<Column> {

    public FieldWithTableAliasRule() {
        super(Column.class);
    }

    @Override
    Report doCheck(Column item) {

        if (item.getTable() == null) {

            String desc = "对于数据库中表记录的查询和变更，只要涉及多个表，都需要在列名前加表的别名(或表名)进行限定\n" +
                    "   因为对于多表进行查询记录、更新记录、删除记录时，如果对操作列没有限定表的别名(或表名)，并且操作列在多个表中存在时，就会抛异常。";

            String sample = "SELECT t1.name FROM table_first AS t1, table_second AS t2 WHERE t1.id = t2.id;\n" +
                    "   在某业务中，由于多表关联查询语句没有加表的别名(或表名)的限制，正常运行两年后，最近在某个表中增加一个同名字段，在预发环境做数据变更后，线上查询语句出现1052异常：\n" +
                    "   Column 'name' in field list is ambiguous。";

            return new Report().isPass(false).level(LevelEnum.mandatory).desc(desc).sample(sample);
        }
        return new Report().isPass(true);
    }

    @Override
    protected Report checkPlainSelect(PlainSelect plainSelect) {

        FromItem fromItem = plainSelect.getFromItem();

        Report report = checkFromItem(fromItem);

        if (!report.isPass()) {
            return report;
        }

        List<Join> joins = plainSelect.getJoins();

        if (CollectionUtils.isEmpty(joins)) {
            return new Report().isPass(true);
        }

        List<SelectItem> selectItems = ObjectUtils.defaultIfNull(plainSelect.getSelectItems(), Collections.emptyList());

        for (SelectItem selectItem : selectItems) {
            report = checkSelectItem(selectItem);
            if (!report.isPass()) {
                return report;
            }
        }

        for (Join join : joins) {
            report = checkJoin(join);
            if (!report.isPass()) {
                return report;
            }
        }

        return new Report().isPass(true);

    }

}
