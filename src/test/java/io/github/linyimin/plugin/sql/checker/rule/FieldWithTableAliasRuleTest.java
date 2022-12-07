package io.github.linyimin.plugin.sql.checker.rule;

import io.github.linyimin.plugin.sql.checker.Report;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

/**
 * @author banzhe
 * @date 2022/12/07 15:16
 **/
class FieldWithTableAliasRuleTest {

    private final FieldWithTableAliasRule checkRule = new FieldWithTableAliasRule();

    @Test
    public void checkTest() {

        String sql = "SELECT t1.name FROM table_first AS t1, table_second AS t2 WHERE t1.id = t2.id;";
        Report report = checkRule.check(sql);
        Assertions.assertTrue(report.isPass());

        sql = "SELECT name FROM table_first AS t1, table_second AS t2 WHERE t1.id = t2.id;";
        report = checkRule.check(sql);
        Assertions.assertFalse(report.isPass());

        sql = "SELECT id FROM (SELECT id FROM t);";
        Assertions.assertTrue(checkRule.check(sql).isPass());

        sql = "SELECT name FROM (SELECT name FROM table_first AS t1, table_second AS t2 WHERE t1.id = t2.id);";
        Assertions.assertFalse(checkRule.check(sql).isPass());

        sql = "SELECT name FROM (SELECT t1.name FROM table_first AS t1, table_second AS t2 WHERE t1.id = t2.id);";
        Assertions.assertTrue(checkRule.check(sql).isPass());


        sql = "SELECT CASE WHEN name = 1 THEN 1 ELSE 0 END AS id FROM (SELECT t1.name FROM table_first AS t1, table_second AS t2 WHERE t1.id = t2.id);";
        Assertions.assertTrue(checkRule.check(sql).isPass());

        sql = "SELECT gender FROM (SELECT CASE WHEN name = 1 THEN 1 ELSE 0 END AS gender FROM table_first AS t1, table_second AS t2 WHERE t1.id = t2.id);";
        Assertions.assertFalse(checkRule.check(sql).isPass());

        sql = "SELECT gender FROM (SELECT CASE WHEN t1.name = 1 THEN 1 ELSE 0 END AS gender FROM table_first AS t1, table_second AS t2 WHERE t1.id = t2.id);";
        Assertions.assertTrue(checkRule.check(sql).isPass());

    }

}