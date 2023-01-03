package io.github.linyimin.plugin.sql.checker.rule;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

/**
 * @author banzhe
 * @date 2022/12/10 18:20
 **/
class BooleanFieldNameRuleTest {
    private final BooleanFieldNameRule checkRule = new BooleanFieldNameRule();

    @Test
    public void testCheck() {
        String field = "{\"name\":\"is_deleted\",\"type\":\"unsigned tinyint\",\"nullable\":false}";
        Assertions.assertTrue(checkRule.check(null, field).isPass());

        field = "{\"name\":\"deleted\",\"type\":\"tinyint\",\"nullable\":false}";
        Assertions.assertTrue(checkRule.check(null, field).isPass());

        field = "{\"name\":\"is_deleted\",\"type\":\"int\",\"nullable\":false}";
        Assertions.assertFalse(checkRule.check(null, field).isPass());

    }
}