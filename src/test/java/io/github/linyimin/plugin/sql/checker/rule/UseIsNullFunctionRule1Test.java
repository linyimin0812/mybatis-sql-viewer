package io.github.linyimin.plugin.sql.checker.rule;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

/**
 * @author banzhe
 * @date 2022/12/07 16:53
 **/
class UseIsNullFunctionRule1Test {

    private final UseIsNullFunctionRule1 checkRule = new UseIsNullFunctionRule1();

    @Test
    public void testCheck() {

        String sql = "select * from table where column1 is null and column3 is not null;";
        Assertions.assertFalse(checkRule.check(sql).isPass());

        sql = "select * from table where  isnull(column1) and column3 is not null;";
        Assertions.assertFalse(checkRule.check(sql).isPass());

        sql = "select * from table where isnull(column1) and isnull(column3);";
        Assertions.assertTrue(checkRule.check(sql).isPass());

        sql = "select case when name is null then 'default' else name end as name from table where id = 1;";
        Assertions.assertFalse(checkRule.check(sql).isPass());

        sql = "select case when isnull(name) then 'default' else name end as name from table where id = 1;";
        Assertions.assertTrue(checkRule.check(sql).isPass());

    }
}