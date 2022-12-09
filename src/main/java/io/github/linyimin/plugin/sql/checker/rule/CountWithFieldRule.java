package io.github.linyimin.plugin.sql.checker.rule;

import io.github.linyimin.plugin.sql.checker.Report;
import io.github.linyimin.plugin.sql.checker.enums.LevelEnum;
import net.sf.jsqlparser.expression.Expression;
import net.sf.jsqlparser.expression.Function;
import net.sf.jsqlparser.expression.LongValue;
import net.sf.jsqlparser.expression.StringValue;
import net.sf.jsqlparser.schema.Column;
import net.sf.jsqlparser.statement.select.AllColumns;
import org.apache.commons.lang3.StringUtils;

import java.util.List;

/**
 * @author banzhe
 * @date 2022/12/07 20:03
 **/
public class CountWithFieldRule extends SelectCheckRuleAbstract<Function>  {

    public CountWithFieldRule() {
        super(Function.class);
    }

    @Override
    Report doCheck(Function item) {

        if (StringUtils.equalsIgnoreCase(item.getName(), "count") && !item.isDistinct()) {
            List<Expression> expressions = item.getParameters().getExpressions();
            if (expressions.size() > 1) {
                return new Report().isPass(true);
            }

            Expression expression = expressions.get(0);

            if (expression instanceof AllColumns) {
                return new Report().isPass(true);
            }

            if (expression instanceof LongValue || expression instanceof StringValue || expression instanceof Column) {
                String desc = "不要使用COUNT(列名)或COUNT(常量)来替代COUNT(*)，COUNT(*)是SQL92定义的标准统计行数的语法，跟数据库无关，跟NULL和非NULL无关。\n" +
                        "   COUNT(*)会统计值为NULL的行，而COUNT(列名)不会统计此列为NULL值的行。";
                return new Report().isPass(false).level(LevelEnum.recommended).desc(desc);
            }

            return checkExpression(expression);

        }

        return new Report().isPass(true);
    }
}
