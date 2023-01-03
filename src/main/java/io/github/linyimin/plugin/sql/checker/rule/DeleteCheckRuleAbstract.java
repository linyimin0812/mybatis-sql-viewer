package io.github.linyimin.plugin.sql.checker.rule;

import io.github.linyimin.plugin.sql.checker.Report;
import io.github.linyimin.plugin.sql.checker.enums.CheckScopeEnum;
import net.sf.jsqlparser.JSQLParserException;
import net.sf.jsqlparser.expression.Expression;
import net.sf.jsqlparser.parser.CCJSqlParserUtil;
import net.sf.jsqlparser.statement.delete.Delete;
import net.sf.jsqlparser.statement.select.Join;
import net.sf.jsqlparser.statement.select.Limit;
import org.apache.commons.collections.CollectionUtils;

import java.util.Collections;
import java.util.List;

/**
 * @author banzhe
 **/
public abstract class DeleteCheckRuleAbstract<T> extends CheckRuleAbstract<T> {

    public DeleteCheckRuleAbstract(Class<T> type) {
        super(type);
    }

    @Override
    public Report check(String target) {
        Report report = new Report().isPass(true);

        try {
            Delete delete = (Delete) CCJSqlParserUtil.parse(target);

            Expression expression = delete.getWhere();

            if (expression != null) {
                report = checkExpression(expression);
                if (!report.isPass()) {
                    return report;
                }
            }

            List<Join> joins = delete.getJoins();

            if (CollectionUtils.isNotEmpty(joins)) {
                for (Join join : joins) {
                    report = checkJoin(join);
                    if (!report.isPass()) {
                        return report;
                    }
                }
            }

            Limit limit = delete.getLimit();
            if (limit != null) {
                report = checkLimit(limit);
                if (!report.isPass()) {
                    return report;
                }
            }

            return report;

        } catch (JSQLParserException e) {
            return report;
        }
    }

    @Override
    public List<CheckScopeEnum> scopes() {
        return Collections.singletonList(CheckScopeEnum.delete);
    }
}
