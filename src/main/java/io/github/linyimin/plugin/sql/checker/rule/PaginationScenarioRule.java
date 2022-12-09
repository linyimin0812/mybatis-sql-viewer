package io.github.linyimin.plugin.sql.checker.rule;

import io.github.linyimin.plugin.sql.checker.Report;
import io.github.linyimin.plugin.sql.checker.enums.LevelEnum;
import net.sf.jsqlparser.statement.select.Limit;

/**
 * @author banzhe
 * @date 2022/12/08 13:54
 **/
public class PaginationScenarioRule extends SelectCheckRuleAbstract<Limit> {

    public PaginationScenarioRule() {
        super(Limit.class);
    }

    @Override
    Report doCheck(Limit item) {

        if (item.getOffset() != null) {

            String desc = "利用延迟关联或者子查询优化超多分页场景\n" +
                    "   MySQL并不是跳过offset行，而是取offset+N行，然后返回前放弃offset行，只返回N行，" +
                    "   那么当offset特别大的时候，效率就非常的低下，要么控制返回的总页数，要么对超过特定阈值的页数进行SQL改写。";
            String sample = "先快速定位要获取的id段，然后再关联\n" +
                    "   SELECT t1.* FROM table1 AS t1, (SELECT id FROM table1 WHERE 条件 LIMIT 100000, 20) AS t2 WHERE t1.id = t2.id;";

            return new Report().isPass(false).level(LevelEnum.recommended).desc(desc).sample(sample);
        }

        return new Report().isPass(true);
    }
}
