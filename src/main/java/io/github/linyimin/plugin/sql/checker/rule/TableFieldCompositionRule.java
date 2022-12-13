package io.github.linyimin.plugin.sql.checker.rule;

import com.alibaba.fastjson.JSONException;
import com.alibaba.fastjson.JSONObject;
import io.github.linyimin.plugin.mock.schema.TableField;
import io.github.linyimin.plugin.sql.checker.Report;
import io.github.linyimin.plugin.sql.checker.enums.CheckScopeEnum;
import io.github.linyimin.plugin.sql.checker.enums.LevelEnum;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * @author banzhe
 * @date 2022/12/09 21:14
 **/
public class TableFieldCompositionRule implements CheckRule {
    @Override
    public Report check(String target) {
        try {
            List<TableField> fields = ObjectUtils.defaultIfNull(JSONObject.parseArray(target, TableField.class), Collections.emptyList());

            Map<String, TableField> fieldMap = fields.stream().collect(Collectors.toMap(TableField::getName, Function.identity(), (o1, o2) -> o1));

            String desc = "";
            if (!fieldMap.containsKey("id")) {
                return new Report().isPass(false).level(LevelEnum.mandatory).desc(desc);
            }

            if (!fieldMap.containsKey("create_time")) {
                return new Report().isPass(false).level(LevelEnum.mandatory).desc(desc);
            }

            TableField field = fieldMap.get("create_time");
            if (!StringUtils.contains(field.getActualType(), "datetime")) {
                return new Report().isPass(false).level(LevelEnum.mandatory).desc(desc);
            }

            if (!fieldMap.containsKey("update_time")) {
                return new Report().isPass(false).level(LevelEnum.mandatory).desc(desc);
            }

            field = fieldMap.get("update_time");
            if (!StringUtils.contains(field.getActualType(), "datetime")) {
                return new Report().isPass(false).level(LevelEnum.mandatory).desc(desc);
            }

            return new Report().isPass(true);

        } catch (JSONException e) {
            return new Report().isPass(true);
        }
    }

    @Override
    public List<CheckScopeEnum> scopes() {
        return Collections.singletonList(CheckScopeEnum.field_composition);
    }
}
