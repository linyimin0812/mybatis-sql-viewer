package io.github.linyimin.plugin.sql.checker.rule;

import io.github.linyimin.plugin.sql.checker.Report;
import io.github.linyimin.plugin.sql.checker.enums.LevelEnum;
import net.sf.jsqlparser.expression.Expression;
import net.sf.jsqlparser.expression.Function;
import org.apache.commons.lang3.StringUtils;

import java.util.List;

/**
 * @author banzhe
 * @date 2022/12/07 17:25
 **/
public class AvoidSumFunctionNPERule extends SelectCheckRuleAbstract<Function> {

    public AvoidSumFunctionNPERule() {
        super(Function.class);
    }

    @Override
    Report doCheck(Function item) {

        Report report = new Report().isPass(true);

        if (StringUtils.equalsIgnoreCase(item.getName(), "sum")) {

            String desc = "当某一列的值全是NULL时，SUM(column)的返回结果为NULL，因此在使用SUM()方法时需要注意NPE问题。";
            String sample = "可以使用如下方式来避免SUM的NPE问题：SELECT IFNULL(SUM(column), 0) FROM table;";

            return report.isPass(false).level(LevelEnum.mandatory).desc(desc).sample(sample);
        }

        if (StringUtils.equalsIgnoreCase(item.getName(), "ifnull")) {
            return report;
        }

        List<Expression> expressions = item.getParameters().getExpressions();

        for (Expression expression : expressions) {
            report = checkExpression(expression);
            if (!report.isPass()) {
                return report;
            }
        }

        return report;
    }
}
