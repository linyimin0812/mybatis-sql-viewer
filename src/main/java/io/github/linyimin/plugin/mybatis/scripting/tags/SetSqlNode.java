package io.github.linyimin.plugin.mybatis.scripting.tags;


import java.util.Collections;
import java.util.List;

/**
 * @author Clinton Begin
 */
public class SetSqlNode extends TrimSqlNode {

    private static final List<String> COMMA = Collections.singletonList(",");

    public SetSqlNode(SqlNode contents) {
        super(contents, "SET", COMMA, null, COMMA);
    }

}
