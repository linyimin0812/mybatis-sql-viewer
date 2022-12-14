package io.github.linyimin.plugin.sql.checker.rule;

import io.github.linyimin.plugin.sql.checker.Report;
import io.github.linyimin.plugin.sql.checker.enums.CheckScopeEnum;
import io.github.linyimin.plugin.sql.checker.enums.LevelEnum;
import net.sf.jsqlparser.expression.operators.relational.InExpression;

import java.util.Arrays;
import java.util.List;

/**
 * @author banzhe
 * @date 2022/12/06 16:54
 * 尽量避免使用in操作
 **/

public class TryToAvoidInOperationRule extends SelectCheckRuleAbstract<InExpression> {


    public TryToAvoidInOperationRule() {
        super(InExpression.class);
    }

    @Override
    Report doCheck(InExpression item) {
        String desc = "IN操作能避免则避免，若实在避免不了，需要仔细评估IN后边的集合元素数量，控制在1000个之内";
        return new Report().isPass(false).level(LevelEnum.for_reference).desc(desc);
    }

    @Override
    public List<CheckScopeEnum> scopes() {
        // TODO: 需要分开， Select Update Delete之间不能转换，存在ClassCastException
        return Arrays.asList(CheckScopeEnum.select, CheckScopeEnum.delete, CheckScopeEnum.update);
    }
}
