package io.github.linyimin.plugin.sql.checker.rule;


import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import io.github.linyimin.plugin.mock.schema.TableField;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

/**
 * @author banzhe
 * @date 2022/12/09 20:37
 **/
class DisableReservedWordRuleTest {

    private final DisableReservedWordRule checkRule = new DisableReservedWordRule();

    private final TableField field = new TableField();
    private final Gson gson = new GsonBuilder().setPrettyPrinting().create();

    @Test
    public void testCheck() {

        field.setName("desc");
        Assertions.assertFalse(checkRule.check(gson.toJson(field)).isPass());

        field.setName("cast");
        Assertions.assertFalse(checkRule.check(gson.toJson(field)).isPass());

        field.setName("between");
        Assertions.assertFalse(checkRule.check(gson.toJson(field)).isPass());

        field.setName("create");
        Assertions.assertFalse(checkRule.check(gson.toJson(field)).isPass());

        field.setName("id");
        Assertions.assertTrue(checkRule.check(gson.toJson(field)).isPass());

        field.setName("create_time");
        Assertions.assertTrue(checkRule.check(gson.toJson(field)).isPass());

        field.setName("update_time");
        Assertions.assertTrue(checkRule.check(gson.toJson(field)).isPass());

        field.setName("name");
        Assertions.assertTrue(checkRule.check(gson.toJson(field)).isPass());
    }

}