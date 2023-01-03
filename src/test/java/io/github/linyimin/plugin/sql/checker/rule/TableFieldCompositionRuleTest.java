package io.github.linyimin.plugin.sql.checker.rule;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

/**
 * @author banzhe
 * @date 2022/12/09 21:31
 **/
class TableFieldCompositionRuleTest {
    private final TableFieldCompositionRule checkRule = new TableFieldCompositionRule();

    @Test
    public void testCheck() {

        String target = "[{\"name\":\"id\",\"nullable\":false},{\"name\":\"create_time\",\"type\":\"datetime\",\"nullable\":false},{\"name\":\"update_time\",\"type\":\"datetime\",\"nullable\":false}]";

        Assertions.assertTrue(checkRule.check(null, target).isPass());

        target = "[{\"name\":\"age\",\"nullable\":false},{\"name\":\"create_time\",\"type\":\"datetime\",\"nullable\":false},{\"name\":\"update_time\",\"type\":\"datetime\",\"nullable\":false}]";

        Assertions.assertFalse(checkRule.check(null, target).isPass());

    }

}