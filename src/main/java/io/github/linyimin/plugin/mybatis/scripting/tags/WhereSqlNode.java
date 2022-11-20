package io.github.linyimin.plugin.mybatis.scripting.tags;


import java.util.Arrays;
import java.util.List;

/**
 * @author Clinton Begin
 */
public class WhereSqlNode extends TrimSqlNode {

    private static List<String> prefixList = Arrays.asList("AND ","OR ","AND\n", "OR\n", "AND\r", "OR\r", "AND\t", "OR\t");

    public WhereSqlNode(SqlNode contents) {
        super(contents, "WHERE", prefixList, null, null);
    }

}
