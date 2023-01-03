package io.github.linyimin.plugin.sql.checker.rule;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

/**
 * @author banzhe
 **/
class TryToAvoidInOperationRuleWithUpdateTest {
    private final TryToAvoidInOperationRuleWithUpdate checkRule = new TryToAvoidInOperationRuleWithUpdate();

    @Test
    public void testCheck() {
        String sql = "UPDATE t SET name = 'test' WHERE id IN (1,2,3,4,5,6);";
        Assertions.assertFalse(checkRule.check(sql).isPass());

        sql = "UPDATE t SET name = 'test' WHERE id = 1;";
        Assertions.assertTrue(checkRule.check(sql).isPass());

        sql = "UPDATE t SET name = 'test' WHERE id IN (SELECT id FROM t2);";
        Assertions.assertFalse(checkRule.check(sql).isPass());

    }
}