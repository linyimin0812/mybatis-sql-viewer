package io.github.linyimin.plugin.sql.checker.rule;

import com.alibaba.fastjson.JSONObject;
import com.intellij.openapi.project.Project;
import io.github.linyimin.plugin.mock.schema.IndexField;
import io.github.linyimin.plugin.sql.checker.Report;
import io.github.linyimin.plugin.sql.checker.enums.CheckScopeEnum;
import io.github.linyimin.plugin.sql.checker.enums.LevelEnum;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * @author banzhe
 * @date 2022/12/14 09:31
 **/
public class CombineIndexOrderRule implements CheckRule {
    @Override
    public Report check(Project project, String target) {
        CheckField checkField = JSONObject.parseObject(target, CheckField.class);

        Map<String, List<IndexField>> indexFieldMap = checkField.getIndexFields().stream().collect(Collectors.groupingBy(IndexField::getKeyName));

        for (List<IndexField> indexFields : indexFieldMap.values()) {
            if (indexFields.size() > 1) {
                String desc = "建立组合索引的时候，区分度最高的在最左边。\n" +
                        "  如果WHERE a = ? AND b = ?，a列的几乎接近于唯一值，那么只需要单建idx_a索引即可。\n" +
                        "  存在非等号和等号混合判断条件时，在建索引时，请把等号条件的列前置。如：WHERE c > ? AND d = ?，那么即使c的区分度更高，也必须把d放在索引的最前列，即建立组合索引idx_d_c。";
                return new Report().isPass(false).level(LevelEnum.mandatory).desc(desc);
            }
        }

        return new Report().isPass(true);
    }

    @Override
    public List<CheckScopeEnum> scopes() {
        return Collections.singletonList(CheckScopeEnum.index_field);
    }
}
