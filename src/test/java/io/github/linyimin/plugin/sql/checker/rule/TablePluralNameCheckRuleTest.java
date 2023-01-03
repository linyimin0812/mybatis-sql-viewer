package io.github.linyimin.plugin.sql.checker.rule;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

/**
 * @author banzhe
 * @date 2022/12/09 19:50
 **/
class TablePluralNameCheckRuleTest {
    private final TablePluralNameCheckRule checkRule = new TablePluralNameCheckRule();

    @Test
    public void testCheck() {
        String table = "app_admins";
        Assertions.assertFalse(checkRule.check(null, table).isPass());

        table = "app_admin";
        Assertions.assertTrue(checkRule.check(null, table).isPass());


        table = "app_apple";
        Assertions.assertTrue(checkRule.check(null, table).isPass());

        table = "app_apples";
        Assertions.assertFalse(checkRule.check(null, table).isPass());

        table = "actress";
        Assertions.assertTrue(checkRule.check(null, table).isPass());

        table = "baby";
        Assertions.assertTrue(checkRule.check(null, table).isPass());

        table = "babies";
        Assertions.assertFalse(checkRule.check(null, table).isPass());

    }
}