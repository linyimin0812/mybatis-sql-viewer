package io.github.linyimin.plugin.sql.checker.rule;

import io.github.linyimin.plugin.sql.checker.Report;
import io.github.linyimin.plugin.sql.checker.enums.CheckScopeEnum;
import io.github.linyimin.plugin.sql.checker.enums.LevelEnum;
import io.github.linyimin.plugin.utils.NameUtils;
import org.apache.commons.lang3.StringUtils;

import java.util.Collections;
import java.util.List;

/**
 * @author banzhe
 * @date 2022/12/09 18:56
 **/
public class TablePluralNameCheckRule implements CheckRule {
    @Override
    public Report check(String target) {

        String plural = NameUtils.pluralize(target);

        if (StringUtils.equalsIgnoreCase(plural, target)) {
            String desc = "表名不使用复数名词\n" +
                    "   表名应该仅仅表示表里面的实体内容，不应该表示实体数量，对应于DO类名也是单数形式，符合表达习惯。";

            return new Report().isPass(false).desc(desc).level(LevelEnum.mandatory);
        }

        return new Report().isPass(true);
    }

    @Override
    public List<CheckScopeEnum> scopes() {
        return Collections.singletonList(CheckScopeEnum.table);
    }
}
