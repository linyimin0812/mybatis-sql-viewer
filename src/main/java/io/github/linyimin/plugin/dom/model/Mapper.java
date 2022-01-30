package io.github.linyimin.plugin.dom.model;

import com.intellij.util.xml.*;
import org.jetbrains.annotations.NotNull;

import java.util.List;

/**
 * @author yiminlin
 * @date 2022/01/24 1:08 am
 **/
public interface Mapper extends DomElement {

    @NotNull
    @SubTagsList({"insert", "update", "delete", "select"})
    List<IdDomElement> getDaoElements();

    @Required
    @NameValue
    @NotNull
    @Attribute("namespace")
    GenericAttributeValue<String> getNamespace();

    @NotNull
    @SubTagList("insert")
    List<IdDomElement> getInserts();

    @NotNull
    @SubTagList("update")
    List<IdDomElement> getUpdates();

    @NotNull
    @SubTagList("delete")
    List<IdDomElement> getDeletes();

    @NotNull
    @SubTagList("select")
    List<IdDomElement> getSelects();

}
