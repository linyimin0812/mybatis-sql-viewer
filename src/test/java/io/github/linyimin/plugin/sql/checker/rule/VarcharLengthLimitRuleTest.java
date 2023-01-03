package io.github.linyimin.plugin.sql.checker.rule;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

/**
 * @author banzhe
 * @date 2022/12/10 17:03
 **/
class VarcharLengthLimitRuleTest {
    private final VarcharLengthLimitRule checkRule = new VarcharLengthLimitRule();

    @Test
    public void testCheck() {
        String field = "{\"name\":\"desc\",\"type\":\"varchar(1024)\",\"nullable\":false}";

        Assertions.assertTrue(checkRule.check(null, field).isPass());

        field = "{\"name\":\"desc\",\"type\":\"varchar(10240)\",\"nullable\":false}";

        Assertions.assertFalse(checkRule.check(null, field).isPass());

        field = "{\"name\":\"desc\",\"type\":\"bigint\",\"nullable\":false}";

        Assertions.assertTrue(checkRule.check(null, field).isPass());
    }
}