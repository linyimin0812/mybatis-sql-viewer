package io.github.linyimin.plugin.sql.checker.rule;

import io.github.linyimin.plugin.sql.checker.Report;
import io.github.linyimin.plugin.sql.checker.enums.CheckScopeEnum;
import net.sf.jsqlparser.JSQLParserException;
import net.sf.jsqlparser.expression.BinaryExpression;
import net.sf.jsqlparser.expression.Expression;
import net.sf.jsqlparser.expression.Function;
import net.sf.jsqlparser.parser.CCJSqlParserUtil;
import net.sf.jsqlparser.statement.select.*;
import org.apache.commons.collections.CollectionUtils;

import java.util.Collections;
import java.util.List;

/**
 * @author banzhe
 * @date 2022/12/07 10:41
 **/
public abstract class SelectCheckRuleAbstract<T> implements CheckRule {

    private Class<T> type;

    public SelectCheckRuleAbstract(Class<T> type) {
        this.type = type;
    }

    abstract Report doCheck(T item);

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

    @Override
    public List<CheckScopeEnum> scopes() {
        return Collections.singletonList(CheckScopeEnum.select);
    }

    private Report check(PlainSelect plainSelect) {

        List<SelectItem> selectItems = plainSelect.getSelectItems();

        for (SelectItem item : selectItems) {
            Report report = check(item);

            if (!report.isPass()) {
                return report;
            }
        }

        List<Join> joins = plainSelect.getJoins();

        if (CollectionUtils.isNotEmpty(joins)) {
            for (Join join : joins) {
                Report report = check(join);
                if (!report.isPass()) {
                    return report;
                }
            }
        }

        Expression expression = plainSelect.getWhere();
        if (expression != null) {
            Report report = check(expression);
            if (!report.isPass()) {
                return report;
            }
        }

        expression = plainSelect.getHaving();
        if (expression != null) {
            Report report = check(expression);
            if (!report.isPass()) {
                return report;
            }
        }

        return new Report().isPass(true);

    }

    private Report check(SelectItem item) {

        Report report = new Report().isPass(true);

        if (!(item instanceof SelectExpressionItem)) {
            return report;
        }

        Expression expression = ((SelectExpressionItem) item).getExpression();


        if (!this.type.isAssignableFrom(expression.getClass())) {
            return report;
        }

        return doCheck((T) expression);
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

        if (this.type.isAssignableFrom(expression.getClass())) {
            return doCheck((T) expression);
        }

        if (expression instanceof BinaryExpression) {
            Report report = check(((BinaryExpression)expression).getLeftExpression());
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

        return new Report().isPass(true);

    }
}
