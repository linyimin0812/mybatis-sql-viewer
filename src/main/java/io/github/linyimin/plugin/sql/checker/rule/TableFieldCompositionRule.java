package io.github.linyimin.plugin.sql.checker.rule;

import com.alibaba.fastjson.JSONException;
import com.alibaba.fastjson.JSONObject;
import com.intellij.openapi.project.Project;
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
    public Report check(Project project, String target) {
        try {
            List<TableField> fields = ObjectUtils.defaultIfNull(JSONObject.parseArray(target, TableField.class), Collections.emptyList());

            Map<String, TableField> fieldMap = fields.stream().collect(Collectors.toMap(TableField::getName, Function.identity(), (o1, o2) -> o1));

            String desc = "表必备三字段：id，create_time，update_time。\n" +
                    "  其中id必为主键，类型为bigint unsigned、单表时递增、步长为1。create_time, update_time的类型均为datetime类型，前者现在时表示主动式创建，后者过去分词表示被动式更新。";
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
