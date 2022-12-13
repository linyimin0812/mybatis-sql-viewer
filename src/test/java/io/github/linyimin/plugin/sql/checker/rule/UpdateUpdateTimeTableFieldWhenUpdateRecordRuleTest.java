package io.github.linyimin.plugin.sql.checker.rule;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * @author banzhe
 * @date 2022/12/10 20:51
 **/
class UpdateUpdateTimeTableFieldWhenUpdateRecordRuleTest {

    private final UpdateUpdateTimeFieldWhenUpdateRecordRule checkRule = new UpdateUpdateTimeFieldWhenUpdateRecordRule();

    @Test
    public void testCheck() {
        String sql = "UPDATE t SET name = 'jack', update_time = now() WHERE id = 1;";
        Assertions.assertTrue(checkRule.check(sql).isPass());

        sql = "UPDATE t SET name = 'jack' WHERE id = 1;";
        Assertions.assertFalse(checkRule.check(sql).isPass());
    }
}