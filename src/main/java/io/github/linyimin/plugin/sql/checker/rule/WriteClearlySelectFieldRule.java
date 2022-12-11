package io.github.linyimin.plugin.sql.checker.rule;

import io.github.linyimin.plugin.sql.checker.Report;
import io.github.linyimin.plugin.sql.checker.enums.CheckScopeEnum;
import io.github.linyimin.plugin.sql.checker.enums.LevelEnum;
import net.sf.jsqlparser.parser.SimpleNode;
import net.sf.jsqlparser.statement.select.AllColumns;
import org.apache.commons.lang3.StringUtils;

import java.util.Collections;
import java.util.List;

/**
 * @author banzhe
 * @date 2022/12/06 14:49
 * 写明查询字段，禁止使用select *
 **/
public class WriteClearlySelectFieldRule extends SelectCheckRuleAbstract<AllColumns> {

    public WriteClearlySelectFieldRule() {
        super(AllColumns.class);
    }

    @Override
    Report doCheck(AllColumns item) {

        SimpleNode node = item.getASTNode();
        if (node != null && StringUtils.equalsIgnoreCase("SelectItem", node.toString())) {

            String desc = "在表查询中，一律不要使用*作为查询的字段列表，需要哪些字段必须明确写明\n" +
                    "   1）增加查询解析器解析成本。\n" +
                    "   2）增减字段容易与resultMap配置不一致。\n" +
                    "   3）无用字段增加网络消耗，尤其是text类型的字段。";

            return new Report().isPass(false).level(LevelEnum.mandatory).desc(desc).sample("SELECT id, name FROM t;");
        }
        return new Report().isPass(true);
    }

    @Override
    public List<CheckScopeEnum> scopes() {
        return Collections.singletonList(CheckScopeEnum.select);
    }
}
