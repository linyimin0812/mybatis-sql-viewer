package io.github.linyimin.plugin.sql.checker.rule;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

/**
 * @author banzhe
 * @date 2022/12/10 17:14
 **/
class DisableFloatAndDoubleRuleTest {

    private final DisableFloatAndDoubleRule checkRule = new DisableFloatAndDoubleRule();

    @Test
    public void testCheck() {
        String field = "{\"name\":\"desc\",\"type\":\"varchar(1024)\",\"nullable\":false}";
        Assertions.assertTrue(checkRule.check(field).isPass());

        field = "{\"name\":\"desc\",\"type\":\"bigint\",\"nullable\":false}";
        Assertions.assertTrue(checkRule.check(field).isPass());

        field = "{\"name\":\"desc\",\"type\":\"decimal\",\"nullable\":false}";
        Assertions.assertTrue(checkRule.check(field).isPass());

        field = "{\"name\":\"desc\",\"type\":\"float\",\"nullable\":false}";
        Assertions.assertFalse(checkRule.check(field).isPass());

        field = "{\"name\":\"desc\",\"type\":\"double\",\"nullable\":false}";
        Assertions.assertFalse(checkRule.check(field).isPass());
    }
}