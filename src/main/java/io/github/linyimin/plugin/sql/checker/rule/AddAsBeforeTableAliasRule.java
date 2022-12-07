package io.github.linyimin.plugin.sql.checker.rule;

import io.github.linyimin.plugin.sql.checker.Report;
import io.github.linyimin.plugin.sql.checker.enums.LevelEnum;
import net.sf.jsqlparser.expression.Alias;
import net.sf.jsqlparser.statement.select.FromItem;

/**
 * @author banzhe
 * @date 2022/12/06 17:17
 * sql语句中表的别名前加as
 **/
public class AddAsBeforeTableAliasRule extends SelectCheckRuleAbstract<Alias> {

    public AddAsBeforeTableAliasRule() {
        super(Alias.class);
    }

    @Override
    Report doCheck(Alias alias) {

        String desc = "SQL语句中表的别名前加AS，并且以t1、t2、t3的顺序依次命名。\n" +
                "   1）别名可以是表的简称，或者是依照表在SQL语句中出现的顺序，以t1、t2、t3的命名方式。\n" +
                "   2）别名前加AS是别名更容易辨别。";

        String sample = "SELECT t1.name FROM table_first AS t1, table_second AS t2 WHERE t1.id=t2.id;";

        Report report = new Report().isPass(true);

        if (alias != null && !alias.isUseAs()) {
            return report.isPass(false).level(LevelEnum.recommended).desc(desc).sample(sample);
        }

        return report;
    }

    @Override
    protected Report checkFromItem(FromItem item) {
        if (item.getAlias() == null) {
            return new Report().isPass(true);
        }

        return doCheck(item.getAlias());
    }
}
