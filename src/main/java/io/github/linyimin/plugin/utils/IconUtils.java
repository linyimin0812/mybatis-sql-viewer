package io.github.linyimin.plugin.utils;

import com.intellij.openapi.util.IconLoader;

import javax.swing.*;

/**
 * @author yiminlin
 * @date 2022/01/23 4:41 pm
 * @description load icon
 **/
public interface IconUtils {
    Icon JAVA_TO_XML_ICON = IconLoader.getIcon("/images/java_to_xml.svg", IconUtils.class);
    Icon XML_TO_JAVA_ICON = IconLoader.getIcon("/images/xml_to_java.svg", IconUtils.class);
    Icon GENERATE_ICON = IconLoader.getIcon("/images/mybatis-sql-viewer.svg", IconUtils.class);
}
