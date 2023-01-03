package io.github.linyimin.plugin.sql.checker.rule;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * @author banzhe
 * @date 2022/12/10 21:03
 **/
class BigAndCompleteUpdateMethodRuleTest {
    private final BigAndCompleteUpdateMethodRule checkRule = new BigAndCompleteUpdateMethodRule();

    @Test
    public void testCheck() {
        String sql = "UPDATE table SET c1 = v1, c2 = v2, c3 = v3;";
        Assertions.assertTrue(checkRule.check(null, sql).isPass());

        sql = "UPDATE table SET c1 = v1, c2 = v2, c3 = v3, c4 = v4, c5 = v5, c6 = v6;";
        Assertions.assertFalse(checkRule.check(null, sql).isPass());
    }
}