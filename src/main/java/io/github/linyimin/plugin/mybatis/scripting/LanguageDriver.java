package io.github.linyimin.plugin.mybatis.scripting;

import io.github.linyimin.plugin.mybatis.mapping.SqlSource;
import io.github.linyimin.plugin.mybatis.parsing.XNode;

public interface LanguageDriver {

    /**
     * Creates an {@link SqlSource} that will hold the statement read from a mapper xml file.
     * It is called during startup, when the mapped statement is read from a class or an xml file.
     *
     * @param script XNode parsed from a XML file
     * @return the sql source
     */
    SqlSource createSqlSource(XNode script);

    /**
     * Creates an {@link SqlSource} that will hold the statement read from an annotation.
     * It is called during startup, when the mapped statement is read from a class or an xml file.
     *
     * @param script The content of the annotation
     * @return the sql source
     */
    SqlSource createSqlSource(String script);

}
