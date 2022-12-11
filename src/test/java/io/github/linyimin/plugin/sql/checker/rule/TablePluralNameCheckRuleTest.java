package io.github.linyimin.plugin.sql.checker.rule;

import io.github.linyimin.plugin.sql.checker.rule.TablePluralNameCheckRule;
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
        Assertions.assertFalse(checkRule.check(table).isPass());

        table = "app_admin";
        Assertions.assertTrue(checkRule.check(table).isPass());


        table = "app_apple";
        Assertions.assertTrue(checkRule.check(table).isPass());

        table = "app_apples";
        Assertions.assertFalse(checkRule.check(table).isPass());

        table = "actress";
        Assertions.assertTrue(checkRule.check(table).isPass());

        table = "baby";
        Assertions.assertTrue(checkRule.check(table).isPass());

        table = "babies";
        Assertions.assertFalse(checkRule.check(table).isPass());

    }
}