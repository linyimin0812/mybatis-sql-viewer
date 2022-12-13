package io.github.linyimin.plugin.sql.checker.rule;

import com.alibaba.fastjson.JSONObject;
import io.github.linyimin.plugin.mock.schema.Field;
import io.github.linyimin.plugin.sql.checker.Report;
import io.github.linyimin.plugin.sql.checker.enums.CheckScopeEnum;
import io.github.linyimin.plugin.sql.checker.enums.LevelEnum;
import org.apache.commons.lang3.StringUtils;

import java.util.Collections;
import java.util.List;

/**
 * @author banzhe
 * @date 2022/12/10 16:56
 **/
public class VarcharLengthLimitRule implements CheckRule {
    @Override
    public Report check(String target) {

        Field field = JSONObject.parseObject(target, Field.class);

        if (StringUtils.containsIgnoreCase(field.getActualType(), "varchar") && field.getFieldLength() > 5000) {

            String desc = "varchar是可变长字符串，不预先分配存储空间，长度不要超过5000，如果存储长度大于此值，定义字段类型为text，独立出来一张表，用主键来对应，避免影响其它字段索引效率。";

            return new Report().isPass(false).level(LevelEnum.mandatory).desc(desc);
        }

        return new Report().isPass(true);
    }

    @Override
    public List<CheckScopeEnum> scopes() {
        return Collections.singletonList(CheckScopeEnum.field);
    }
}
