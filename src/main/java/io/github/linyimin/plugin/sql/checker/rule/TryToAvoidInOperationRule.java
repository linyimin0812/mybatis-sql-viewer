package io.github.linyimin.plugin.sql.checker.rule;

import com.alibaba.druid.sql.visitor.SchemaStatVisitor;
import com.alibaba.druid.stat.TableStat;
import io.github.linyimin.plugin.sql.checker.Report;
import io.github.linyimin.plugin.sql.checker.enums.CheckScopeEnum;
import io.github.linyimin.plugin.sql.checker.enums.LevelEnum;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author banzhe
 * @date 2022/12/06 16:54
 * 尽量避免使用in操作
 **/

public class TryToAvoidInOperationRule implements CheckRule {
    @Override
    public Report check(String target) {

        Report report = new Report().isPass(true);

        SchemaStatVisitor visitor = parseSql(target);

        List<TableStat.Condition> conditions = visitor.getConditions().stream()
                .filter(condition -> StringUtils.equalsIgnoreCase("IN", condition.getOperator()))
                .collect(Collectors.toList());


        if (CollectionUtils.isEmpty(conditions)) {
            return report;
        }

        String desc = "IN操作能避免则避免，若实在避免不了，需要仔细评估IN后边的集合元素数量，控制在1000个之内";

        return report.isPass(false).level(LevelEnum.for_reference).desc(desc);

    }

    @Override
    public List<CheckScopeEnum> scopes() {
        return Arrays.asList(CheckScopeEnum.select, CheckScopeEnum.delete, CheckScopeEnum.update);
    }
}
