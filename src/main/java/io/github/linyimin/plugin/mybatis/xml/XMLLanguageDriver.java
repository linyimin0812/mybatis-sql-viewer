package io.github.linyimin.plugin.mybatis.xml;

import io.github.linyimin.plugin.mybatis.mapping.SqlSource;
import io.github.linyimin.plugin.mybatis.parsing.PropertyParser;
import io.github.linyimin.plugin.mybatis.parsing.XNode;
import io.github.linyimin.plugin.mybatis.parsing.XPathParser;
import io.github.linyimin.plugin.mybatis.scripting.LanguageDriver;
import io.github.linyimin.plugin.mybatis.scripting.tags.DynamicSqlSource;
import io.github.linyimin.plugin.mybatis.scripting.tags.TextSqlNode;

import java.io.IOException;
import java.util.Properties;

/**
 * @author Eduardo Macarron
 */
public class XMLLanguageDriver implements LanguageDriver {

    @Override
    public SqlSource createSqlSource(XNode script, Class<?> parameterType) {
        XMLScriptBuilder builder = new XMLScriptBuilder(script, parameterType);
        return builder.parseScriptNode();
    }

    @Override
    public SqlSource createSqlSource(String script, Class<?> parameterType) {
        // issue #3
        if (script.startsWith("<script>")) {
            XPathParser parser = null;
            try {
                parser = new XPathParser(script);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
            return createSqlSource(parser.evalNode("/script"), parameterType);
        } else {
            // issue #127
            script = PropertyParser.parse(script, new Properties());
            TextSqlNode textSqlNode = new TextSqlNode(script);
            return new DynamicSqlSource(textSqlNode);
        }
    }

}
