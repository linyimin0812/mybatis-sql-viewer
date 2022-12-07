package io.github.linyimin.plugin.sql.checker.rule;

import io.github.linyimin.plugin.sql.checker.Report;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

/**
 * @author banzhe
 * @date 2022/12/06 21:53
 **/
class CharCountingMethodRuleTest {

    CharCountingMethodRule checkRule = new CharCountingMethodRule();

    @Test
    void check() {

        String sql = "SELECT LENGTH(\"轻松工作\");";

        Report report = checkRule.check(sql);

        Assertions.assertFalse(report.isPass());

        sql = "SELECT CHARACTER_LENGTH(\"轻松工作\");";

        report = checkRule.check(sql);

        Assertions.assertFalse(report.isPass());

        sql = "SELECT id from t;";
        report = checkRule.check(sql);
        Assertions.assertTrue(report.isPass());

        sql = "SELECT id from t1, t2 where t1.id and t2.t1_id and LENGTH(t2.name) > 10";

        report = checkRule.check(sql);
        Assertions.assertFalse(report.isPass());

    }
}