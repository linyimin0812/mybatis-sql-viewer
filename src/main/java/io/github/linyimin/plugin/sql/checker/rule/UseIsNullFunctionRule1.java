package io.github.linyimin.plugin.sql.checker.rule;

import io.github.linyimin.plugin.sql.checker.Report;
import io.github.linyimin.plugin.sql.checker.enums.LevelEnum;
import net.sf.jsqlparser.expression.operators.relational.IsNullExpression;

/**
 * @author banzhe
 * @date 2022/12/07 15:41
 **/
public class UseIsNullFunctionRule1 extends UseIsNullFunctionRuleAbstract<IsNullExpression> {

    public UseIsNullFunctionRule1() {
        super(IsNullExpression.class);
    }

    @Override
    Report doCheck(IsNullExpression item) {
        return new Report().isPass(false).level(LevelEnum.mandatory).desc(DESC).sample(SAMPLE);
    }
}
