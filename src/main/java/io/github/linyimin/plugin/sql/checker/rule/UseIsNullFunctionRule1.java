package io.github.linyimin.plugin.sql.checker.rule;

import io.github.linyimin.plugin.sql.checker.Report;
import net.sf.jsqlparser.expression.operators.relational.ComparisonOperator;
import net.sf.jsqlparser.statement.select.*;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.ObjectUtils;

import java.util.Collections;
import java.util.List;

/**
 * @author banzhe
 * @date 2022/12/07 15:41
 **/
public class UseIsNullFunctionRule1 extends SelectCheckRuleAbstract<ComparisonOperator> {

    @Override
    protected Report checkPlainSelect(PlainSelect plainSelect) {

        FromItem fromItem = plainSelect.getFromItem();

        if (fromItem instanceof SubSelect) {
            Report report = checkPlainSelect((PlainSelect) ((SubSelect) fromItem).getSelectBody());

            if (!report.isPass()) {
                return report;
            }
        }

        List<Join> joins = plainSelect.getJoins();

        if (CollectionUtils.isEmpty(joins)) {
            return new Report().isPass(true);
        }

        List<SelectItem> selectItems = ObjectUtils.defaultIfNull(plainSelect.getSelectItems(), Collections.emptyList());

        for (SelectItem selectItem : selectItems) {
            Report report = checkSelectItem(selectItem);
            if (!report.isPass()) {
                return report;
            }
        }

        for (Join join : joins) {
            Report report = checkJoin(join);
            if (!report.isPass()) {
                return report;
            }
        }

        return new Report().isPass(true);

    }

    public UseIsNullFunctionRule1() {
        super(ComparisonOperator.class);
    }

    @Override
    Report doCheck(ComparisonOperator item) {
        return null;
    }
}
