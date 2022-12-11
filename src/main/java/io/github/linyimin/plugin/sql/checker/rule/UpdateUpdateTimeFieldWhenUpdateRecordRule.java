package io.github.linyimin.plugin.sql.checker.rule;

import io.github.linyimin.plugin.sql.checker.Report;
import io.github.linyimin.plugin.sql.checker.enums.CheckScopeEnum;
import io.github.linyimin.plugin.sql.checker.enums.LevelEnum;
import net.sf.jsqlparser.JSQLParserException;
import net.sf.jsqlparser.parser.CCJSqlParserUtil;
import net.sf.jsqlparser.schema.Column;
import net.sf.jsqlparser.statement.update.Update;
import net.sf.jsqlparser.statement.update.UpdateSet;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author banzhe
 * @date 2022/12/10 20:42
 **/
public class UpdateUpdateTimeFieldWhenUpdateRecordRule implements CheckRule {
    @Override
    public Report check(String target) {

        try {
            Update statement = (Update) CCJSqlParserUtil.parse(target);

            List<String> updateFields = statement.getUpdateSets().stream()
                    .map(UpdateSet::getColumns).flatMap(Collection::stream)
                    .map(Column::getColumnName)
                    .collect(Collectors.toList());

            if (!updateFields.contains("update_time")) {
                String desc = "更新数据表记录时，必须同时更新记录对应的update_time字段值为当前时间。";
                return new Report().isPass(false).level(LevelEnum.mandatory).desc(desc);
            }

            return new Report().isPass(true);

        } catch (JSQLParserException e) {
            return new Report().isPass(true);
        }
    }

    @Override
    public List<CheckScopeEnum> scopes() {
        return Collections.singletonList(CheckScopeEnum.update);
    }
}
