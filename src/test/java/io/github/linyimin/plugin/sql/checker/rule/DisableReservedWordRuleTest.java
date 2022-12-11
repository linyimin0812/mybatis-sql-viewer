package io.github.linyimin.plugin.sql.checker.rule;


import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

/**
 * @author banzhe
 * @date 2022/12/09 20:37
 **/
class DisableReservedWordRuleTest {

    private final DisableReservedWordRule checkRule = new DisableReservedWordRule();

    @Test
    public void testCheck() {
        String name = "desc";
        Assertions.assertFalse(checkRule.check(name).isPass());

        name = "cast";
        Assertions.assertFalse(checkRule.check(name).isPass());

        name = "between";
        Assertions.assertFalse(checkRule.check(name).isPass());

        name = "create";
        Assertions.assertFalse(checkRule.check(name).isPass());

        name = "id";
        Assertions.assertTrue(checkRule.check(name).isPass());

        name = "create_time";
        Assertions.assertTrue(checkRule.check(name).isPass());

        name = "update_time";
        Assertions.assertTrue(checkRule.check(name).isPass());

        name = "name";
        Assertions.assertTrue(checkRule.check(name).isPass());
    }

}