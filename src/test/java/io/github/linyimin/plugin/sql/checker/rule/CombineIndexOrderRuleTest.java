package io.github.linyimin.plugin.sql.checker.rule;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import io.github.linyimin.plugin.mock.schema.IndexField;
import io.github.linyimin.plugin.sql.checker.Report;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.Collections;

/**
 * @author banzhe
 **/
class CombineIndexOrderRuleTest {
    private final CombineIndexOrderRule checkRule = new CombineIndexOrderRule();

    private final Gson gson = new GsonBuilder().setPrettyPrinting().create();

    @Test
    public void test() {

        IndexField field1 = new IndexField();
        field1.setKeyName("idx_name_id");

        CheckField checkField = new CheckField(Arrays.asList(field1, field1));

        Report report = checkRule.check(null, gson.toJson(checkField));

        Assertions.assertFalse(report.isPass());

        checkField = new CheckField(Collections.singletonList(field1));

        report = checkRule.check(null, gson.toJson(checkField));
        Assertions.assertTrue(report.isPass());

    }
}