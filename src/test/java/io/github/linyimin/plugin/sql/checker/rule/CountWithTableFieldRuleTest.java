package io.github.linyimin.plugin.sql.checker.rule;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

/**
 * @author banzhe
 * @date 2022/12/07 20:55
 **/
class CountWithTableFieldRuleTest {

    private final CountWithFieldRule checkRule = new CountWithFieldRule();

    @Test
    public void testCheck() {
        String sql = "SELECT COUNT(1) FROM t;";
        Assertions.assertFalse(checkRule.check(sql).isPass());

        sql = "SELECT COUNT(id) FROM t;";
        Assertions.assertFalse(checkRule.check(sql).isPass());

        sql = "SELECT COUNT(*) FROM t;";
        Assertions.assertTrue(checkRule.check(sql).isPass());

        sql = "SELECT COUNT(distinct a) FROM t;";
        Assertions.assertTrue(checkRule.check(sql).isPass());

        sql = "SELECT total FROM (SELECT COUNT(1) FROM t) AS t1;";
        Assertions.assertFalse(checkRule.check(sql).isPass());

        sql = "SELECT total FROM (SELECT COUNT(id) FROM t) AS t1;";
        Assertions.assertFalse(checkRule.check(sql).isPass());

        sql = "SELECT total FROM (SELECT COUNT(*) FROM t) AS t1;";
        Assertions.assertTrue(checkRule.check(sql).isPass());

        sql = "SELECT total FROM (SELECT COUNT( distinct id) FROM t) AS t1;";
        Assertions.assertTrue(checkRule.check(sql).isPass());

    }

}