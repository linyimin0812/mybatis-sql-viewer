package io.github.linyimin.plugin.sql.checker;

import io.github.linyimin.plugin.sql.checker.enums.CheckScopeEnum;

/**
 * @author banzhe
 **/
public class IndexHitChecker extends Checker {
    @Override
    CheckScopeEnum scope() {
        return CheckScopeEnum.index_hit;
    }
}
