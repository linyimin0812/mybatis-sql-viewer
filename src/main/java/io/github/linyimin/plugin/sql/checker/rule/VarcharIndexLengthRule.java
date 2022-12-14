package io.github.linyimin.plugin.sql.checker.rule;

import com.alibaba.fastjson.JSONObject;
import io.github.linyimin.plugin.mock.enums.FieldTypeEnum;
import io.github.linyimin.plugin.mock.schema.IndexField;
import io.github.linyimin.plugin.mock.schema.TableField;
import io.github.linyimin.plugin.sql.checker.Report;
import io.github.linyimin.plugin.sql.checker.enums.CheckScopeEnum;
import io.github.linyimin.plugin.sql.checker.enums.LevelEnum;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * @author banzhe
 * @date 2022/12/14 08:26
 **/
public class VarcharIndexLengthRule implements CheckRule {
    @Override
    public Report check(String target) {

        CheckField checkField = JSONObject.parseObject(target, CheckField.class);

        Map<String, TableField> tableFieldMap = checkField.getTableFields().stream().collect(Collectors.toMap(TableField::getName, Function.identity(), (o1, o2) -> o1));
        Map<String, List<IndexField>> indexFieldMap = checkField.getIndexFields().stream().collect(Collectors.groupingBy(IndexField::getKeyName));

        for (List<IndexField> indexFields : indexFieldMap.values()) {
            for (IndexField indexField : indexFields) {
                TableField tableField = tableFieldMap.get(indexField.getColumnName());
                if (tableField == null) {
                    continue;
                }
                FieldTypeEnum type = FieldTypeEnum.resolve(TableField.parseType(tableField.getType()));
                if (type == FieldTypeEnum.VARCHAR && indexField.getSubPart() <= 0) {
                    String desc = "在varchar字段上建立索引时，必须指定索引长度，没必要对全字段建立索引，根据实际文本区分度决定索引长度。\n" +
                            "  索引的长度与区分度是一对矛盾体，一般字符串类型数据，长度为20的索引，区分度会高达90%以上，可以使用COUNT(DISTINCT LEFT(类名, 索引长度))/COUNT(*)来确定区分度。";
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
