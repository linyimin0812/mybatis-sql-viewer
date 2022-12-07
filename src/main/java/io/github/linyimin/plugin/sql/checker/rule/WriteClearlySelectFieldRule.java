package io.github.linyimin.plugin.sql.checker.rule;

import com.alibaba.druid.sql.visitor.SchemaStatVisitor;
import com.alibaba.druid.stat.TableStat;
import io.github.linyimin.plugin.sql.checker.Report;
import io.github.linyimin.plugin.sql.checker.enums.CheckScopeEnum;
import io.github.linyimin.plugin.sql.checker.enums.LevelEnum;
import org.apache.commons.lang3.StringUtils;

import java.util.Collection;
import java.util.Collections;
import java.util.List;

/**
 * @author banzhe
 * @date 2022/12/06 14:49
 * 写明查询字段，禁止使用select *
 **/
public class WriteClearlySelectFieldRule implements CheckRule {

    @Override
    public Report check(String target) {

        String desc = "在表查询中，一律不要使用*作为查询的字段列表，需要哪些字段必须明确写明\n" +
                "   1）增加查询解析器解析成本。\n" +
                "   2）增减字段容易与resultMap配置不一致。\n" +
                "   3）无用字段增加网络消耗，尤其是text类型的字段。";

        Report report = new Report().isPass(true);

        SchemaStatVisitor visitor = parseSql(target);

        Collection<TableStat.Column> columns = visitor.getColumns();

        for (TableStat.Column column : columns) {
            if (StringUtils.equals(column.getName(), "*")) {
                return report.isPass(false).level(LevelEnum.mandatory).desc(desc).sample("SELECT id, name FROM t;");
            }
        }

        return report;
    }

    @Override
    public List<CheckScopeEnum> scopes() {
        return Collections.singletonList(CheckScopeEnum.select);
    }
}
