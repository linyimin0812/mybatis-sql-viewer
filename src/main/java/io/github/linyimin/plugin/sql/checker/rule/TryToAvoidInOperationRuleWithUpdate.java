package io.github.linyimin.plugin.sql.checker.rule;

import io.github.linyimin.plugin.sql.checker.Report;
import io.github.linyimin.plugin.sql.checker.enums.LevelEnum;
import net.sf.jsqlparser.expression.operators.relational.InExpression;

/**
 * @author banzhe
 **/
public class TryToAvoidInOperationRuleWithUpdate extends UpdateCheckRuleAbstract<InExpression> {

    public TryToAvoidInOperationRuleWithUpdate() {
        super(InExpression.class);
    }

    @Override
    Report doCheck(InExpression item) {
        String desc = "IN操作能避免则避免，若实在避免不了，需要仔细评估IN后边的集合元素数量，控制在1000个之内";
        return new Report().isPass(false).level(LevelEnum.for_reference).desc(desc);
    }
}
