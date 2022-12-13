package io.github.linyimin.plugin.sql.checker.rule;

import com.alibaba.fastjson.JSONObject;
import io.github.linyimin.plugin.mock.schema.TableField;
import io.github.linyimin.plugin.sql.checker.Report;
import io.github.linyimin.plugin.sql.checker.enums.CheckScopeEnum;
import io.github.linyimin.plugin.sql.checker.enums.LevelEnum;
import org.apache.commons.lang3.StringUtils;

import java.util.Collections;
import java.util.List;

/**
 * @author banzhe
 * @date 2022/12/10 17:11
 **/
public class DisableFloatAndDoubleRule implements CheckRule {
    @Override
    public Report check(String target) {
        TableField field = JSONObject.parseObject(target, TableField.class);

        if (StringUtils.containsIgnoreCase(field.getActualType(), "float") || StringUtils.equalsIgnoreCase(field.getActualType(), "double")) {

            String desc = "小数类型为decimal，禁止使用float和double。\n" +
                    "   在存储的时候，float和double都存在精度损失的问题，很可能在比较值的时候，得不到正确的结果。如果存储的数据范围超过decimal的范围，建议将数据拆成整数和小数并分开存储。";

            return new Report().isPass(false).level(LevelEnum.mandatory).desc(desc);
        }

        return new Report().isPass(true);
    }

    @Override
    public List<CheckScopeEnum> scopes() {
        return Collections.singletonList(CheckScopeEnum.field);
    }
}
