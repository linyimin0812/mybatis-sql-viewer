package io.github.linyimin.plugin.sql.checker.rule;

import com.alibaba.fastjson.JSONObject;
import io.github.linyimin.plugin.mock.schema.IndexField;
import io.github.linyimin.plugin.sql.checker.Report;
import io.github.linyimin.plugin.sql.checker.enums.CheckScopeEnum;
import io.github.linyimin.plugin.sql.checker.enums.LevelEnum;
import org.apache.commons.lang3.StringUtils;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * @author banzhe
 * @date 2022/12/13 23:06
 **/
public class IndexNamingRule implements CheckRule {

    @Override
    public Report check(String target) {

        CheckField checkField = JSONObject.parseObject(target, CheckField.class);

        Map<String, List<IndexField>> indexFieldMap = checkField.getIndexFields().stream().collect(Collectors.groupingBy(IndexField::getKeyName));

        String desc = "主键索引名为pk_字段名；唯一索引名为uk_字段名；普通索引名为idx_字段名。\n" +
                "   pk_即primary key；uk_即unique key；idx_即index的简称。";

        for (List<IndexField> indexFields : indexFieldMap.values()) {
            for (IndexField indexField : indexFields) {
                if (StringUtils.equalsIgnoreCase(indexField.getKeyName(), "primary")) {
                    continue;
                }
                if (indexField.isUnique() && !StringUtils.startsWith(indexField.getKeyName(), "uk_")) {

                    return new Report().isPass(false).level(LevelEnum.mandatory).desc(desc);
                }

                if (!indexField.isUnique() && !StringUtils.startsWith(indexField.getKeyName(), "idx_")) {
                    return new Report().isPass(false).level(LevelEnum.mandatory).desc(desc);
                }
            }
        }
        return new Report().isPass(true);
    }

    @Override
    public List<CheckScopeEnum> scopes() {
        return Collections.singletonList(CheckScopeEnum.index);
    }

}
