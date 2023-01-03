package io.github.linyimin.plugin.sql.checker.rule;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

/**
 * @author banzhe
 * @date 2022/12/07 19:57
 **/
class CountDistinctRuleTest {

    private final CountDistinctRule checkRule = new CountDistinctRule();

    @Test
    public void testCheck() {
        String sql = "SELECT COUNT(DISTINCT a) FROM t;";
        Assertions.assertFalse(checkRule.check(null, sql).isPass());

        sql = "SELECT COUNT(a) FROM t;";
        Assertions.assertTrue(checkRule.check(null, sql).isPass());

        sql = "SELECT total FROM (SELECT COUNT(DISTINCT a) FROM t) AS t1;";
        Assertions.assertFalse(checkRule.check(null, sql).isPass());

        sql = "SELECT total FROM (SELECT COUNT(a) FROM t) AS t1;";
        Assertions.assertTrue(checkRule.check(null, sql).isPass());

        sql = "SELECT total FROM (SELECT COUNT(DISTINCT a, b) FROM t) AS t1;";
        Assertions.assertFalse(checkRule.check(null, sql).isPass());

    }
}