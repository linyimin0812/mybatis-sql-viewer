package io.github.linyimin.plugin.sql.checker.rule;

import com.intellij.openapi.project.Project;
import io.github.linyimin.plugin.sql.checker.Report;
import io.github.linyimin.plugin.sql.checker.enums.CheckScopeEnum;
import net.sf.jsqlparser.JSQLParserException;
import net.sf.jsqlparser.expression.Expression;
import net.sf.jsqlparser.parser.CCJSqlParserUtil;
import net.sf.jsqlparser.statement.select.*;
import net.sf.jsqlparser.statement.update.Update;
import org.apache.commons.collections.CollectionUtils;

import java.util.Collections;
import java.util.List;

/**
 * @author banzhe
 **/
public abstract class UpdateCheckRuleAbstract<T> extends CheckRuleAbstract<T> {

    public UpdateCheckRuleAbstract(Class<T> type) {
        super(type);
    }

    @Override
    public Report check(Project project, String target) {
        Report report = new Report().isPass(true);

        try {
            Update update = (Update) CCJSqlParserUtil.parse(target);
            FromItem fromItem = update.getFromItem();
            if (fromItem != null) {
                report = checkFromItem(fromItem);
                if (!report.isPass()) {
                    return report;
                }
            }

            Expression expression = update.getWhere();

            if (expression != null) {
                report = checkExpression(expression);
                if (!report.isPass()) {
                    return report;
                }
            }

            List<Join> joins = update.getJoins();

            if (CollectionUtils.isNotEmpty(joins)) {
                for (Join join : joins) {
                    report = checkJoin(join);
                    if (!report.isPass()) {
                        return report;
                    }
                }
            }

            Limit limit = update.getLimit();
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
        return Collections.singletonList(CheckScopeEnum.update);
    }

}
