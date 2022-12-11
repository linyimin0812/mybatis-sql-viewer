package io.github.linyimin.plugin.sql.checker.rule;

import com.alibaba.druid.sql.dialect.mysql.parser.MySqlLexer;
import com.alibaba.druid.sql.parser.Token;
import io.github.linyimin.plugin.sql.checker.Report;
import io.github.linyimin.plugin.sql.checker.enums.CheckScopeEnum;
import io.github.linyimin.plugin.sql.checker.enums.LevelEnum;

import java.util.Collections;
import java.util.List;

/**
 * @author banzhe
 * @date 2022/12/09 20:31
 **/
public class DisableReservedWordRule implements CheckRule {

    @Override
    public Report check(String target) {
        Token token = MySqlLexer.DEFAULT_MYSQL_KEYWORDS.getKeyword(target);
        if (token != null) {
            String desc = "禁用保留字，如desc, range, match, delayed等，请参考MySQL官方保留字。";
            return new Report().isPass(false).level(LevelEnum.mandatory).desc(desc);
        }

        return new Report().isPass(true);
    }

    @Override
    public List<CheckScopeEnum> scopes() {
        return Collections.singletonList(CheckScopeEnum.field);
    }
}
