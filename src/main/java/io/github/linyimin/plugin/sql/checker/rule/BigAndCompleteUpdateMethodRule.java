package io.github.linyimin.plugin.sql.checker.rule;

import com.intellij.openapi.project.Project;
import io.github.linyimin.plugin.sql.checker.Report;
import io.github.linyimin.plugin.sql.checker.enums.CheckScopeEnum;
import io.github.linyimin.plugin.sql.checker.enums.LevelEnum;
import net.sf.jsqlparser.JSQLParserException;
import net.sf.jsqlparser.parser.CCJSqlParserUtil;
import net.sf.jsqlparser.statement.update.Update;

import java.util.Collections;
import java.util.List;

/**
 * @author banzhe
 * @date 2022/12/10 20:57
 **/
public class BigAndCompleteUpdateMethodRule implements CheckRule {
    @Override
    public Report check(Project project, String target) {

        try {
            Update updateStatement = (Update) CCJSqlParserUtil.parse(target);

            if (updateStatement.getUpdateSets().size() > 5) {
                String desc = "不要写一个大而全的数据更新接口。传入为POJO类，不管是不是自己的目标更新字段，都进行UPDATE table SET c1 = v1, c2 = v2, c3 = v3;\n" +
                        "   这是不对的。执行SQL时，不要更新无改动的字段，一是容易出错；二是效率低；三是增加binlog存储。";
                return new Report().isPass(false).level(LevelEnum.recommended).desc(desc);
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
