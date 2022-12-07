package io.github.linyimin.plugin.sql.parser;

import com.alibaba.druid.DbType;
import com.alibaba.druid.sql.SQLUtils;
import com.alibaba.druid.sql.ast.SQLName;
import com.alibaba.druid.sql.ast.SQLStatement;
import com.alibaba.druid.sql.ast.statement.SQLDeleteStatement;
import com.alibaba.druid.sql.ast.statement.SQLInsertStatement;
import com.alibaba.druid.sql.ast.statement.SQLUpdateStatement;
import com.alibaba.druid.sql.visitor.SchemaStatVisitor;
import io.github.linyimin.plugin.ProcessResult;
import net.sf.jsqlparser.util.validation.Validation;
import net.sf.jsqlparser.util.validation.ValidationError;
import net.sf.jsqlparser.util.validation.ValidationException;
import net.sf.jsqlparser.util.validation.feature.DatabaseType;

import java.util.Collections;
import java.util.List;
import java.util.Set;
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

    public static ProcessResult<String> validate(String sql) {
        Validation validation = new Validation(Collections.singletonList(DatabaseType.MYSQL), sql);
        ValidationException exception = validation.validate().stream().map(ValidationError::getErrors).flatMap(Set::stream).findFirst().orElse(null);
        if (exception == null) {
            return ProcessResult.success(null);
        }

        return ProcessResult.fail(exception.getMessage());
    }

}
