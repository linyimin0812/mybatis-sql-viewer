package io.github.linyimin.plugin.mybatis.xml;

import io.github.linyimin.plugin.mybatis.mapping.SqlSource;
import io.github.linyimin.plugin.mybatis.parsing.XNode;

import java.util.Map;

/**
 * @author Clinton Begin
 */
public class XMLStatementBuilder {

    private final XNode context;
    private final Map<String, XNode> sqlFragments;

    public XMLStatementBuilder(XNode context, Map<String, XNode> sqlFragments) {
        this.context = context;
        this.sqlFragments = sqlFragments;
    }

    public SqlSource parseStatementNode() {

        // Include Fragments before parsing
        XMLIncludeTransformer includeParser = new XMLIncludeTransformer(sqlFragments);
        includeParser.applyIncludes(context.getNode());

        return new XMLLanguageDriver().createSqlSource(context);

    }

}

