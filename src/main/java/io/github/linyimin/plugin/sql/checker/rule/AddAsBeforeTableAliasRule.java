package io.github.linyimin.plugin.sql.checker.rule;

import io.github.linyimin.plugin.sql.checker.Report;
import io.github.linyimin.plugin.sql.checker.enums.CheckScopeEnum;
import io.github.linyimin.plugin.sql.checker.enums.LevelEnum;
import net.sf.jsqlparser.JSQLParserException;
import net.sf.jsqlparser.expression.Alias;
import net.sf.jsqlparser.expression.BinaryExpression;
import net.sf.jsqlparser.expression.Expression;
import net.sf.jsqlparser.parser.CCJSqlParserUtil;
import net.sf.jsqlparser.statement.select.*;
import org.apache.commons.collections.CollectionUtils;

import java.util.Collections;
import java.util.List;

/**
 * @author banzhe
 * @date 2022/12/06 17:17
 * sql语句中表的别名前加as
 **/
public class AddAsBeforeTableAliasRule implements CheckRule {

    @Override
    public Report check(String target) {

        Report report = new Report().isPass(true);

        try {
            Select select = (Select) CCJSqlParserUtil.parse(target);
            PlainSelect plainSelect = (PlainSelect) select.getSelectBody();

            return check(plainSelect);

        } catch (JSQLParserException e) {
            return report;
        }
    }

    private Report check(PlainSelect plainSelect) {

        Report report = check(plainSelect.getFromItem().getAlias());
        if (!report.isPass()) {
            return report;
        }

        List<Join> joins = plainSelect.getJoins();

        if (CollectionUtils.isNotEmpty(joins)) {
            for (Join join : joins) {
                report = check(join);
                if (!report.isPass()) {
                    return report;
                }
            }
        }

        Expression expression = plainSelect.getWhere();
        if (expression != null) {
            report = check(expression);
            if (!report.isPass()) {
                return report;
            }
        }

        expression = plainSelect.getHaving();
        if (expression != null) {
            report = check(expression);
            if (!report.isPass()) {
                return report;
            }
        }

        return report;

    }

    private Report check(Alias alias) {

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

    private Report check(Join join) {

        FromItem item = join.getRightItem();

        Report report = check(item.getAlias());

        if (!report.isPass()) {
            return report;
        }

        if (item instanceof SubSelect) {
            check((SubSelect) item);
        }

        return report;
    }

    private Report check(SubSelect subSelect) {
        Report report = check(subSelect.getAlias());
        if (!report.isPass()) {
            return report;
        }

        return check((PlainSelect) subSelect.getSelectBody());
    }

    private Report check(Expression expression) {

        Report report = new Report().isPass(true);

        if (expression instanceof BinaryExpression) {
            report = check(((BinaryExpression)expression).getLeftExpression());
            if (!report.isPass()) {
                return report;
            }
            report = check(((BinaryExpression)expression).getRightExpression());
            if (!report.isPass()) {
                return report;
            }
        }

        if (expression instanceof SubSelect) {
            return check((SubSelect) expression);
        }

        return report;

    }

    @Override
    public List<CheckScopeEnum> scopes() {
        return Collections.singletonList(CheckScopeEnum.select);
    }
}
