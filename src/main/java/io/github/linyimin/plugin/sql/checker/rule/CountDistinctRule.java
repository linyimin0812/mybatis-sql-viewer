package io.github.linyimin.plugin.sql.checker.rule;

import io.github.linyimin.plugin.sql.checker.Report;
import io.github.linyimin.plugin.sql.checker.enums.LevelEnum;
import net.sf.jsqlparser.expression.Function;
import org.apache.commons.lang3.StringUtils;

/**
 * @author banzhe
 * @date 2022/12/07 19:47
 **/
public class CountDistinctRule extends SelectCheckRuleAbstract<Function> {

    public CountDistinctRule() {
        super(Function.class);
    }

    @Override
    Report doCheck(Function item) {

        if (StringUtils.equalsIgnoreCase(item.getName(), "count") && item.isDistinct()) {

            String desc = "COUNT(DISTINCT column)计算该列除了NULL之外的不重复行数，注意COUNT(DISTINCT col1, col2)如果其中一列全为NULL，" +
                    "那么即使另一列有不同的值，也返回0";

            return new Report().isPass(false).level(LevelEnum.for_reference).desc(desc);
        }

        return new Report().isPass(true);
    }
}
