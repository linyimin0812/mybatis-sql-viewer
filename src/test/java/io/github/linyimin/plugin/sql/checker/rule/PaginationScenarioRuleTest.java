package io.github.linyimin.plugin.sql.checker.rule;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

/**
 * @author banzhe
 * @date 2022/12/08 14:15
 **/
class PaginationScenarioRuleTest {
    private final PaginationScenarioRule checkRule = new PaginationScenarioRule();

    @Test
    public void testCheck() {

        String sql = "SELECT * FROM t1;";
        Assertions.assertTrue(checkRule.check(sql).isPass());

        sql = "SELECT * FROM t1 LIMIT 10;";
        Assertions.assertTrue(checkRule.check(sql).isPass());

        sql = "SELECT * FROM t1 LIMIT 0, 10;";
        Assertions.assertFalse(checkRule.check(sql).isPass());

        sql = "SELECT t1.id, t2.name FROM t1 LEFT JOIN (SELECT id, name from t2 LIMIT 100) AS t2 ON t1.id = t2.id;";
        Assertions.assertTrue(checkRule.check(sql).isPass());

        sql = "SELECT t1.id, t2.name FROM t1 LEFT JOIN (SELECT id, name from t2 LIMIT 10, 100) AS t2 ON t1.id = t2.id;";
        Assertions.assertFalse(checkRule.check(sql).isPass());

    }

}