package io.github.linyimin.plugin.sql.checker.rule;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import io.github.linyimin.plugin.mock.schema.IndexField;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.Arrays;

/**
 * @author banzhe
 * @date 2022/12/14 00:32
 **/
class IndexNamingRuleTest {
    private final IndexNamingRule checkRule = new IndexNamingRule();

    private final Gson gson = new GsonBuilder().setPrettyPrinting().create();

    @Test
    public void testCheck() {

        IndexField field1 = new IndexField();
        field1.setKeyName("idx_name_id");
        field1.setNonUnique(1);

        CheckField checkField = new CheckField(Arrays.asList(field1, field1));

        Assertions.assertTrue(checkRule.check(gson.toJson(checkField)).isPass());

        field1.setKeyName("idx_name_id");
        field1.setNonUnique(0);

        checkField = new CheckField(Arrays.asList(field1, field1));
        Assertions.assertFalse(checkRule.check(gson.toJson(checkField)).isPass());

        field1.setKeyName("uk_name_id");
        field1.setNonUnique(0);

        checkField = new CheckField(Arrays.asList(field1, field1));

        Assertions.assertTrue(checkRule.check(gson.toJson(checkField)).isPass());

    }
}