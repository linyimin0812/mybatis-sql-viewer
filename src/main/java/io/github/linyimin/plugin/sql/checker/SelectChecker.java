package io.github.linyimin.plugin.sql.checker;

import io.github.linyimin.plugin.sql.checker.enums.CheckScopeEnum;

/**
 * @author banzhe
 * @date 2022/12/06 14:43
 **/
public class SelectChecker extends Checker {
    @Override
    CheckScopeEnum scope() {
        return CheckScopeEnum.select;
    }
}
