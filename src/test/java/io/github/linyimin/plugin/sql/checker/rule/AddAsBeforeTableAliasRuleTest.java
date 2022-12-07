package io.github.linyimin.plugin.sql.checker.rule;


import io.github.linyimin.plugin.sql.checker.Report;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

/**
 * @author banzhe
 * @date 2022/12/06 20:53
 **/
public class AddAsBeforeTableAliasRuleTest {

    private final AddAsBeforeTableAliasRule checkRule = new AddAsBeforeTableAliasRule();

    @Test
    public void check() {

        String sql = "SELECT * FROM t1 a WHERE column1 = (SELECT MAX(column2) FROM t2) and column2 < 1 and column2 > 1 or column2 <> 8 and column2 = 0;";

        Report report = checkRule.check(sql);

        Assertions.assertFalse(report.isPass());

        sql = "SELECT * FROM t1 AS a WHERE column1 = (SELECT MAX(column2) FROM t2) and column2 < 1 and column2 > 1 or column2 <> 8 and column2 = 0;";

        report = checkRule.check(sql);

        Assertions.assertTrue(report.isPass());

    }
}