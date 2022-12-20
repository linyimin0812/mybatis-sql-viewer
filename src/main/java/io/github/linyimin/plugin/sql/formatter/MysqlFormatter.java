package io.github.linyimin.plugin.sql.formatter;

import com.github.vertical_blank.sqlformatter.SqlFormatter;
import com.github.vertical_blank.sqlformatter.core.FormatConfig;
import com.github.vertical_blank.sqlformatter.languages.Dialect;

/**
 * @author banzhe
 * @date 2022/12/17 02:43
 **/
public class MysqlFormatter {
    public static String format(String sql) {
        FormatConfig config = FormatConfig.builder()
                .indent("  ")
                .linesBetweenQueries(1)
                .build();

        return SqlFormatter.of(Dialect.MySql).format(sql, config);
    }
}
