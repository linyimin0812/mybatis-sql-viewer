package io.github.linyimin.plugin.sql.checker.rule;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

/**
 * @author banzhe
 * @date 2022/12/07 17:06
 **/
class UseIsNullFunctionRule2Test {

    private final UseIsNullFunctionRule2 checkRule = new UseIsNullFunctionRule2();

    @Test
    public void testCheck() {
        String sql = "select * from table where column1 is null and column3 is not null;";
        Assertions.assertTrue(checkRule.check(null, sql).isPass());

        sql = "select * from table where  column1 is null and column3 <> null;";
        Assertions.assertFalse(checkRule.check(null, sql).isPass());

        sql = "select * from table where isnull(column1) and isnull(column3);";
        Assertions.assertTrue(checkRule.check(null, sql).isPass());

        sql = "select case when name = null then 'default' else name end as name from table where id = 1;";
        Assertions.assertFalse(checkRule.check(null, sql).isPass());

        sql = "select case when name is null then 'default' else name end as name from table where id = 1;";
        Assertions.assertTrue(checkRule.check(null, sql).isPass());
    }

}