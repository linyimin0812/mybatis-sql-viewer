package io.github.linyimin.plugin.sql.checker.rule;

import net.sf.jsqlparser.JSQLParserException;
import net.sf.jsqlparser.parser.CCJSqlParserUtil;
import net.sf.jsqlparser.statement.select.PlainSelect;
import net.sf.jsqlparser.statement.select.Select;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

/**
 * @author banzhe
 * @date 2022/12/09 11:21
 **/
class WriteClearlySelectFieldRuleTest {

    private final WriteClearlySelectFieldRule checkRule = new WriteClearlySelectFieldRule();

    @Test
    public void testCheck() {
        String sql = "SELECT * FROM t;";
        Assertions.assertFalse(checkRule.check(sql).isPass());

        sql = "SELECT COUNT(*) FROM t;";
        Assertions.assertTrue(checkRule.check(sql).isPass());

        sql = "SELECT t1.id, t2.name FROM t1 LEFT JOIN (SELECT * FROM student) AS t2 ON t1.id = t2.id;";
        Assertions.assertFalse(checkRule.check(sql).isPass());

        sql = "SELECT t1.id, t2.name FROM t1 LEFT JOIN (SELECT * FROM student) AS t2 ON t1.id = t2.id;";
        Assertions.assertFalse(checkRule.check(sql).isPass());

    }
}