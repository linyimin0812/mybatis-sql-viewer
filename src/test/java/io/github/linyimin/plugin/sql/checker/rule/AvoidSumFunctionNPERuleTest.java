package io.github.linyimin.plugin.sql.checker.rule;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

/**
 * @author banzhe
 * @date 2022/12/07 17:42
 **/
class AvoidSumFunctionNPERuleTest {

    private final AvoidSumFunctionNPERule checkRule = new AvoidSumFunctionNPERule();

    @Test
    public void testCheck() {
        String sql = "SELECT SUM(col1) FROM t1;";
        Assertions.assertFalse(checkRule.check(null, sql).isPass());

        sql = "SELECT IFNULL(SUM(col1), 0) FROM t1;";
        Assertions.assertTrue(checkRule.check(null, sql).isPass());

        sql = "SELECT total FROM (SELECT SUM(grade) AS total FROM t) AS t1;";
        Assertions.assertFalse(checkRule.check(null, sql).isPass());


        sql = "SELECT total FROM (SELECT IFNULL(SUM(grade), 0) AS total FROM t) AS t1;";
        Assertions.assertTrue(checkRule.check(null, sql).isPass());

    }
}