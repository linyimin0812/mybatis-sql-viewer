package io.github.linyimin.plugin.sql.checker.rule;

import io.github.linyimin.plugin.sql.checker.Report;
import io.github.linyimin.plugin.sql.checker.enums.LevelEnum;
import net.sf.jsqlparser.expression.NullValue;
import net.sf.jsqlparser.expression.operators.relational.ComparisonOperator;

/**
 * @author banzhe
 * @date 2022/12/07 16:58
 **/
public class UseIsNullFunctionRule2 extends UseIsNullFunctionRuleAbstract<ComparisonOperator> {


    public UseIsNullFunctionRule2() {
        super(ComparisonOperator.class);
    }

    @Override
    Report doCheck(ComparisonOperator item) {

        if (item.getLeftExpression() instanceof NullValue) {
            return new Report().isPass(false).level(LevelEnum.mandatory).desc(DESC).sample(SAMPLE);
        }

        Report report = checkExpression(item.getLeftExpression());

        if (!report.isPass()) {
            return report;
        }

        if (item.getRightExpression() instanceof NullValue) {
            return new Report().isPass(false).level(LevelEnum.mandatory).desc(DESC).sample(SAMPLE);
        }

        return checkExpression(item.getRightExpression());

    }
}
