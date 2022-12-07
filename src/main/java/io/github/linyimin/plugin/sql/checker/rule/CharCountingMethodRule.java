package io.github.linyimin.plugin.sql.checker.rule;

import io.github.linyimin.plugin.sql.checker.Report;
import io.github.linyimin.plugin.sql.checker.enums.CheckScopeEnum;
import io.github.linyimin.plugin.sql.checker.enums.LevelEnum;
import net.sf.jsqlparser.JSQLParserException;
import net.sf.jsqlparser.expression.BinaryExpression;
import net.sf.jsqlparser.expression.Expression;
import net.sf.jsqlparser.expression.Function;
import net.sf.jsqlparser.parser.CCJSqlParserUtil;
import net.sf.jsqlparser.statement.select.*;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;

import java.util.Collections;
import java.util.List;

/**
 * @author banzhe
 * @date 2022/12/06 21:32
 * 字符计数方法
 **/
public class CharCountingMethodRule implements CheckRule {
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

        List<SelectItem> selectItems = plainSelect.getSelectItems();

        for (SelectItem item : selectItems) {
            Report report = check(item);

            if (!report.isPass()) {
                return report;
            }
        }

        Report report = new Report().isPass(true);

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

    private Report check(SelectItem item) {

        Report report = new Report().isPass(true);

        if (!(item instanceof  SelectExpressionItem)) {
            return report;
        }

        Expression expression = ((SelectExpressionItem) item).getExpression();
        if (!(expression instanceof Function)) {
            return report;
        }

        return check((Function) expression);
    }

    private Report check(Function function) {

        String desc = "因国际化需要，所有的字符存储于表示，均采用utf8字符集，那么字符统计方法需要注意。\n" +
                "   1）SELECT LENGTH(\"轻松工作\"); 返回为12\n" +
                "   2）SELECT CHARACTER_LENGTH(\"轻松工作\"); 返回为4";

        Report report = new Report().isPass(true);

        String name = function.getName();

        if (StringUtils.equalsAnyIgnoreCase(name,"CHARACTER_LENGTH", "LENGTH")) {
            return report.isPass(false).desc(desc).level(LevelEnum.for_reference);
        }

        return report;
    }

    private Report check(Join join) {

        Report report = new Report().isPass(true);

        FromItem item = join.getRightItem();


        if (item instanceof SubSelect) {
            SubSelect subSelect = (SubSelect) item;
            return check((PlainSelect) subSelect.getSelectBody());
        }

        return report;
    }

    private Report check(Expression expression) {

        if (expression instanceof Function) {
            return check((Function) expression);
        }

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
            SubSelect subSelect = (SubSelect) expression;
            return check((PlainSelect) subSelect.getSelectBody());
        }

        return report;

    }
    @Override
    public List<CheckScopeEnum> scopes() {
        return Collections.singletonList(CheckScopeEnum.select);
    }
}
