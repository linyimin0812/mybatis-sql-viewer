package io.github.linyimin.plugin.sql.checker.rule;

import com.alibaba.druid.sql.visitor.SchemaStatVisitor;
import com.alibaba.druid.stat.TableStat;
import io.github.linyimin.plugin.sql.checker.Report;
import io.github.linyimin.plugin.sql.checker.enums.CheckScopeEnum;
import io.github.linyimin.plugin.sql.checker.enums.LevelEnum;

import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * @author banzhe
 * @date 2022/12/09 11:46
 **/
public class CorrelationTableLimitRule implements CheckRule {
    @Override
    public Report check(String target) {

        SchemaStatVisitor visitor = parseSql(target);

        Map<TableStat.Name, TableStat> tableStatMap = visitor.getTables();

        if (tableStatMap.size() > 3) {

            String desc = "超过三个表禁止join。需要join的字段，数据类型保持绝对一致；多表关联查询时，保证被关联的字段需要有索引。\n" +
                    "   即使双表join也要注意表索引、SQL性能。";

            return new Report().isPass(false).level(LevelEnum.mandatory).desc(desc);
        }

        return new Report().isPass(true);
    }

    @Override
    public List<CheckScopeEnum> scopes() {
        return Collections.singletonList(CheckScopeEnum.select);
    }
}
