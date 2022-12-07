package io.github.linyimin.plugin.sql.checker.rule;

import com.alibaba.druid.sql.SQLUtils;
import com.alibaba.druid.sql.ast.SQLStatement;
import com.alibaba.druid.sql.visitor.SchemaStatVisitor;
import io.github.linyimin.plugin.sql.checker.enums.CheckScopeEnum;
import io.github.linyimin.plugin.sql.checker.Report;

import java.util.List;

/**
 * @author banzhe
 * @date 2022/12/06 14:09
 **/
public interface CheckRule {
    /**
     * @param target 需要检查的对象(sql, table, xml)
     * @return 规则检查报告
     */
    Report check(String target);

    List<CheckScopeEnum> scopes();

    default SchemaStatVisitor parseSql(String sql) {

        SQLStatement statement = SQLUtils.parseSingleMysqlStatement(sql);
        SchemaStatVisitor visitor = new SchemaStatVisitor();
        statement.accept(visitor);

        return visitor;
    }

}
