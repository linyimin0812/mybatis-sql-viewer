package io.github.linyimin.plugin.sql.checker.rule;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

/**
 * @author banzhe
 **/
class TryToAvoidInOperationRuleWithDeleteTest {
    private final TryToAvoidInOperationRuleWithDelete checkRule = new TryToAvoidInOperationRuleWithDelete();

    @Test
    public void testCheck() {
        String sql = "DELETE FROM t WHERE id IN (1,2,3,4,5,6);";
        Assertions.assertFalse(checkRule.check(null, sql).isPass());

        sql = "DELETE FROM t WHERE id = 1;";
        Assertions.assertTrue(checkRule.check(null, sql).isPass());

        sql = "DELETE FROM t WHERE id IN (SELECT id FROM t2);";
        Assertions.assertFalse(checkRule.check(null, sql).isPass());

    }
}