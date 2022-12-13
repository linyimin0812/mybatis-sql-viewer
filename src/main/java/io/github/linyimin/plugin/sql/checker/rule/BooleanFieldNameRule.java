package io.github.linyimin.plugin.sql.checker.rule;

import com.alibaba.fastjson.JSONObject;
import io.github.linyimin.plugin.mock.schema.Field;
import io.github.linyimin.plugin.sql.checker.Report;
import io.github.linyimin.plugin.sql.checker.enums.CheckScopeEnum;
import io.github.linyimin.plugin.sql.checker.enums.LevelEnum;
import org.apache.commons.lang3.StringUtils;

import java.util.Collections;
import java.util.List;
import java.util.regex.Pattern;

/**
 * @author banzhe
 * @date 2022/12/10 17:50
 **/
public class BooleanFieldNameRule implements CheckRule {

    private final Pattern START_WITH_IS_PREFIX = Pattern.compile("^is_.*");

    @Override
    public Report check(String target) {

        Field field = JSONObject.parseObject(target, Field.class);

        if (START_WITH_IS_PREFIX.matcher(field.getName()).find() && !StringUtils.equalsIgnoreCase(field.getActualType(), "unsigned tinyint")) {

            String desc = "表达是与否概念的字段，必须使用is_xxx的命名方式，数据类型是unsigned tinyint(1表示是，0表示否)。\n" +
                    "   任何字段如何为非负数，必须是unsigned。\n" +
                    "   POJO类中任何布尔类型的变量，都不要is前缀，所以，需要在<resultMap>设置从is_xxx到xxx的映射关系。\n" +
                    "   数据库表示是否的值，使用tinyint类型，坚持is_xxx的命名方式是为了明确其取值含义与取值范围。";

            return new Report().isPass(false).desc(desc).level(LevelEnum.mandatory);
        }

        return new Report().isPass(true);
    }

    @Override
    public List<CheckScopeEnum> scopes() {
        return Collections.singletonList(CheckScopeEnum.field);
    }
}
