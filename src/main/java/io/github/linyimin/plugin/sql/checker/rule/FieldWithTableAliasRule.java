package io.github.linyimin.plugin.sql.checker.rule;

import io.github.linyimin.plugin.sql.checker.Report;
import io.github.linyimin.plugin.sql.checker.enums.CheckScopeEnum;

import java.util.Collections;
import java.util.List;

/**
 * @author banzhe
 * @date 2022/12/06 22:25
 **/
public class FieldWithTableAliasRule implements CheckRule {
    @Override
    public Report check(String target) {
        return null;
    }

    @Override
    public List<CheckScopeEnum> scopes() {
        return Collections.singletonList(CheckScopeEnum.select);
    }
}
