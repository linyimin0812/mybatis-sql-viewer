package io.github.linyimin.plugin.sql.checker.rule;

import io.github.linyimin.plugin.sql.checker.Report;
import io.github.linyimin.plugin.sql.checker.enums.CheckScopeEnum;
import net.sf.jsqlparser.JSQLParserException;
import net.sf.jsqlparser.expression.*;
import net.sf.jsqlparser.expression.operators.relational.ExpressionList;
import net.sf.jsqlparser.parser.CCJSqlParserUtil;
import net.sf.jsqlparser.statement.select.*;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.ObjectUtils;

import java.util.Collections;
import java.util.List;

/**
 * @author banzhe
 * @date 2022/12/07 10:41
 **/
public abstract class SelectCheckRuleAbstract<T> implements CheckRule {

    private final Class<T> type;

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

            return checkPlainSelect(plainSelect);

        } catch (JSQLParserException e) {
            return report;
        }
    }

    @Override
    public List<CheckScopeEnum> scopes() {
        return Collections.singletonList(CheckScopeEnum.select);
    }

    protected Report checkPlainSelect(PlainSelect plainSelect) {

        List<SelectItem> selectItems = plainSelect.getSelectItems();

        for (SelectItem item : selectItems) {
            Report report = checkSelectItem(item);

            if (!report.isPass()) {
                return report;
            }
        }

        FromItem fromItem = plainSelect.getFromItem();
        if (fromItem != null) {
            Report report = checkFromItem(fromItem);
            if (!report.isPass()) {
                return report;
            }
        }

        List<Join> joins = plainSelect.getJoins();

        if (CollectionUtils.isNotEmpty(joins)) {
            for (Join join : joins) {
                Report report = checkJoin(join);
                if (!report.isPass()) {
                    return report;
                }
            }
        }

        Expression expression = plainSelect.getWhere();
        if (expression != null) {
            Report report = checkExpression(expression);
            if (!report.isPass()) {
                return report;
            }
        }

        expression = plainSelect.getHaving();
        if (expression != null) {
            Report report = checkExpression(expression);
            if (!report.isPass()) {
                return report;
            }
        }

        return new Report().isPass(true);

    }

    protected Report checkSelectItem(SelectItem item) {

        Report report = new Report().isPass(true);

        if (!(item instanceof SelectExpressionItem)) {
            return report;
        }

        return checkExpression(((SelectExpressionItem) item).getExpression());
    }

    protected Report checkJoin(Join join) {

        FromItem item = join.getRightItem();

        if (item instanceof SubSelect) {
            SubSelect subSelect = (SubSelect) item;
            return checkPlainSelect((PlainSelect) subSelect.getSelectBody());
        }

        return checkFromItem(item);

    }

    protected Report checkFromItem(FromItem item) {
        return new Report().isPass(true);
    }

    protected Report checkExpression(Expression expression) {

        if (this.type.isAssignableFrom(expression.getClass())) {
            return doCheck((T) expression);
        }

        if (expression instanceof BinaryExpression) {
            Report report = checkExpression(((BinaryExpression)expression).getLeftExpression());
            if (!report.isPass()) {
                return report;
            }
            report = checkExpression(((BinaryExpression)expression).getRightExpression());
            if (!report.isPass()) {
                return report;
            }
        }

        if (expression instanceof SubSelect) {
            SubSelect subSelect = (SubSelect) expression;
            return checkPlainSelect((PlainSelect) subSelect.getSelectBody());
        }

        if (expression instanceof Function) {
            Report report = checkFunction((Function) expression);
            if (!report.isPass()) {
                return report;
            }
        }

        if (expression instanceof CaseExpression) {
            Report report = checkCaseExpression((CaseExpression) expression);
            if (!report.isPass()) {
                return report;
            }
        }

        return new Report().isPass(true);

    }

    private Report checkFunction(Function expression) {
        ExpressionList list = expression.getParameters();
        List<Expression> expressionList = ObjectUtils.defaultIfNull(list.getExpressions(), Collections.emptyList());
        for (Expression paramExpression : expressionList) {
            Report report = checkExpression(paramExpression);
            if (!report.isPass()) {
                return report;
            }
        }

        return new Report().isPass(true);
    }

    private Report checkCaseExpression(CaseExpression expression) {

        Expression switchExpression = expression.getSwitchExpression();

        if (switchExpression != null) {
            Report report = checkExpression(switchExpression);
            if (!report.isPass()) {
                return report;
            }
        }

        Expression elseExpression = expression.getElseExpression();
        if (elseExpression != null) {
            Report report = checkExpression(elseExpression);
            if (!report.isPass()) {
                return report;
            }
        }

        List<WhenClause> whenClauses = ObjectUtils.defaultIfNull(expression.getWhenClauses(), Collections.emptyList());
        for (WhenClause whenClause : whenClauses) {

            Report report = checkExpression(whenClause.getWhenExpression());
            if (!report.isPass()) {
                return report;
            }

            report = checkExpression(whenClause.getThenExpression());
            if (!report.isPass()) {
                return report;
            }
        }

        return new Report().isPass(true);

    }
}
