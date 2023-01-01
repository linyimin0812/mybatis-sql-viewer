package io.github.linyimin.plugin.ui.tree;

import com.intellij.ui.treeStructure.CachingSimpleNode;
import com.intellij.ui.treeStructure.SimpleNode;

import java.text.MessageFormat;

/**
 * @author banzhe
 * @date 2023/01/01 18:55
 **/
public abstract class BaseSimpleNode extends CachingSimpleNode {

    protected String name;

    public BaseSimpleNode(SimpleNode aParent, String name) {
        super(aParent);
        this.name = name;
    }


    /**
     * 检查是否为父子节点
     * @param parent parent
     * @param son son
     */
    protected <T extends BaseSimpleNode> void checkChild(T parent, T son) {
        if (parent != son.getParent()) {
            String format = MessageFormat.format("{0}的父节点类型{1}与{2}不匹配", son.getName(), son.getParent().getName(), son.getName());
            throw new RuntimeException(format);
        }
    }
}
