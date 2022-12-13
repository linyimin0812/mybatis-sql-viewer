package io.github.linyimin.plugin.sql.checker.rule;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

/**
 * @author banzhe
 * @date 2022/12/09 20:15
 **/
class TableNameAndTableFieldNameCheckRuleTest {
    private final TableNameAndFieldNameCheckRule checkRule = new TableNameAndFieldNameCheckRule();

    @Test
    public void testCheck() {
        String name = "aliyun_admin";
        Assertions.assertTrue(checkRule.check(name).isPass());

        name = "rdc_config";
        Assertions.assertTrue(checkRule.check(name).isPass());

        name = "level3_name";
        Assertions.assertTrue(checkRule.check(name).isPass());

        name = "AliyunAdmin";
        Assertions.assertFalse(checkRule.check(name).isPass());

        name = "rdcConfig";
        Assertions.assertFalse(checkRule.check(name).isPass());

        name = "level_3_name";
        Assertions.assertFalse(checkRule.check(name).isPass());

    }
}