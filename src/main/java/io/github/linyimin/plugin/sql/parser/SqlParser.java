package io.github.linyimin.plugin.sql.parser;

import com.alibaba.druid.DbType;
import com.alibaba.druid.sql.SQLUtils;
import com.alibaba.druid.sql.ast.SQLName;
import com.alibaba.druid.sql.ast.SQLStatement;
import com.alibaba.druid.sql.ast.statement.SQLDeleteStatement;
import com.alibaba.druid.sql.ast.statement.SQLInsertStatement;
import com.alibaba.druid.sql.ast.statement.SQLUpdateStatement;
import com.alibaba.druid.sql.visitor.SchemaStatVisitor;

import java.util.List;
import java.util.stream.Collectors;

/**
 * @author yiminlin
 * @date 2022/11/27 21:43
 **/
public class SqlParser {

    public static List<String> getTableNames(String sql) {
        SQLStatement statement = SQLUtils.parseSingleMysqlStatement(sql);

        SchemaStatVisitor statVisitor = SQLUtils.createSchemaStatVisitor(DbType.mysql);
        statement.accept(statVisitor);

        return statVisitor.getOriginalTables().stream()
                .map(SQLName::getSimpleName)
                .map(table -> table.replaceAll("`", "")).distinct().collect(Collectors.toList());
    }

    public static SqlType getSqlType(String sql) {
        SQLStatement statement = SQLUtils.parseSingleMysqlStatement(sql);

        if (statement instanceof SQLUpdateStatement || statement instanceof SQLInsertStatement || statement instanceof SQLDeleteStatement) {
            return SqlType.update;
        }

        return SqlType.select;

    }

}
