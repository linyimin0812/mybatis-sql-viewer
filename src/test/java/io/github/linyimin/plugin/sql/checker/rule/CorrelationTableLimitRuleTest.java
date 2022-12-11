package io.github.linyimin.plugin.sql.checker.rule;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

/**
 * @author banzhe
 * @date 2022/12/09 11:53
 **/
class CorrelationTableLimitRuleTest {

    private final CorrelationTableLimitRule checkRUle = new CorrelationTableLimitRule();

    @Test
    public void testCheck() {

        String sql = "SELECT t1.id, t2.name, t3.course, t4.grade FROM t1, t2, t3, t4 ON t1.id = t2.user_id and t2.id = t3.course_id and t3.id = t4.grade_id;";
        Assertions.assertFalse(checkRUle.check(sql).isPass());

        sql = "SELECT t1.id, t2.name, t3.grade FROM t1, t2, t3 ON t1.id = t2.user_id and t2.id = t3.course_id;";
        Assertions.assertTrue(checkRUle.check(sql).isPass());

        sql = "SELECT t1.id FROM t1 WHERE t1.id = 100;";
        Assertions.assertTrue(checkRUle.check(sql).isPass());
    }

}