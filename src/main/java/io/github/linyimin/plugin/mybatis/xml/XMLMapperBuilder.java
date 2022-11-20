package io.github.linyimin.plugin.mybatis.xml;


import io.github.linyimin.plugin.mybatis.mapping.SqlSource;
import io.github.linyimin.plugin.mybatis.parsing.XNode;
import io.github.linyimin.plugin.mybatis.parsing.XPathParser;
import org.w3c.dom.Document;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


/**
 * @author banzhe
 * @date 2022/11/13 14:23
 **/
public class XMLMapperBuilder {

    private final XPathParser parser;
    private final Map<String, XNode> sqlFragments;

    public XMLMapperBuilder(String path) throws IOException {
        parser = new XPathParser(path);
        sqlFragments = new HashMap<>();
    }

    public XMLMapperBuilder(InputStream inputStream) {
        parser = new XPathParser(inputStream);
        sqlFragments = new HashMap<>();
    }

    public XMLMapperBuilder(Document document) {
        parser = new XPathParser(document);
        sqlFragments = new HashMap<>();
    }

    public XMLMapperBuilder(String resource, boolean validation) {
        parser = new XPathParser(resource, validation);
        sqlFragments = new HashMap<>();
    }

    public Map<String, SqlSource> parse() {

        Map<String, SqlSource> sqlSourceMap = new HashMap<>();

        XNode context = this.parser.evalNode("/mapper");

        String namespace = context.getStringAttribute("namespace");

        sqlElement(context.evalNodes("/mapper/sql"));

        List<XNode> statements = context.evalNodes("select|insert|update|delete");

        for (XNode node : statements) {

            XMLStatementBuilder statementBuilder = new XMLStatementBuilder(node, sqlFragments);
            SqlSource sqlSource = statementBuilder.parseStatementNode();

            String id = node.getStringAttribute("id");
            String methodName = (namespace == null || namespace.length() == 0) ? id : String.format("%s.%s", namespace, id);

            sqlSourceMap.put(methodName, sqlSource);
        }

        return sqlSourceMap;

    }

    private void sqlElement(List<XNode> nodes) {
        for (XNode context : nodes) {
            String id = context.getStringAttribute("id");
            sqlFragments.put(id, context);
        }
    }
}
