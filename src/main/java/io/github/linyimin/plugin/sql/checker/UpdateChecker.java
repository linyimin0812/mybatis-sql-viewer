package io.github.linyimin.plugin.sql.checker;

import io.github.linyimin.plugin.sql.checker.enums.CheckScopeEnum;

/**
 * @author banzhe
 * @date 2022/12/14 00:40
 **/
public class UpdateChecker extends Checker {
    @Override
    CheckScopeEnum scope() {
        return CheckScopeEnum.update;
    }
}
