package io.github.linyimin.plugin.dom.model;

import com.intellij.util.xml.*;

/**
 * @author yiminlin
 * @date 2022/01/24 1:08 am
 **/
public interface IdDomElement extends DomElement {

    @Required
    @Attribute("id")
    GenericAttributeValue<String> getId();
}
