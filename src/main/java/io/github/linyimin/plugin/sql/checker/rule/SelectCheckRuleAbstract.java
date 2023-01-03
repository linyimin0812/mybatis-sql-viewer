package io.github.linyimin.plugin.sql.checker.rule;

import io.github.linyimin.plugin.sql.checker.Report;
import io.github.linyimin.plugin.sql.checker.enums.CheckScopeEnum;
import net.sf.jsqlparser.JSQLParserException;
import net.sf.jsqlparser.parser.CCJSqlParserUtil;
import net.sf.jsqlparser.statement.select.*;

import java.util.Collections;
import java.util.List;

/**
 * @author banzhe
 * @date 2022/12/07 10:41
 **/
public abstract class SelectCheckRuleAbstract<T> extends CheckRuleAbstract<T> {

    public SelectCheckRuleAbstract(Class<T> type) {
        super(type);
    }

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
}
