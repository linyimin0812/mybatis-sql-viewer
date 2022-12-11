package io.github.linyimin.plugin.sql.checker.rule;

import com.alibaba.fastjson.JSON;
import io.github.linyimin.plugin.mock.schema.Field;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

/**
 * @author banzhe
 * @date 2022/12/09 21:31
 **/
class TableFieldCompositionRuleTest {
    private final TableFieldCompositionRule checkRule = new TableFieldCompositionRule();

    @Test
    public void testCheck() {

        String target = "[{\"name\":\"id\",\"nullable\":false},{\"name\":\"create_time\",\"type\":\"datetime\",\"nullable\":false},{\"name\":\"update_time\",\"type\":\"datetime\",\"nullable\":false}]";

        Assertions.assertTrue(checkRule.check(target).isPass());

        target = "[{\"name\":\"age\",\"nullable\":false},{\"name\":\"create_time\",\"type\":\"datetime\",\"nullable\":false},{\"name\":\"update_time\",\"type\":\"datetime\",\"nullable\":false}]";

        Assertions.assertFalse(checkRule.check(target).isPass());

    }

}