package io.github.linyimin.plugin.ui.tree;

import com.intellij.ui.treeStructure.SimpleNode;
import io.github.linyimin.plugin.utils.IconUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * @author banzhe
 * @date 2023/01/01 18:59
 **/
public class NamespaceTreeNode extends BaseSimpleNode {

    private final List<MethodTreeNode> methodTreeNodeList = new ArrayList<>();

    public NamespaceTreeNode(SimpleNode aParent, String name) {
        // 父节点和name赋值
        super(aParent, name);
        this.getTemplatePresentation().setIcon(IconUtils.GENERATE_ICON);
    }

    @Override
    protected SimpleNode[] buildChildren() {
        int size = this.methodTreeNodeList.size();
        return methodTreeNodeList.toArray(new SimpleNode[size]);
    }

    @Override
    public String getName() {
        return this.name;
    }

    public void add(MethodTreeNode methodTreeNode) {
        checkChild(this, methodTreeNode);
        this.methodTreeNodeList.add(methodTreeNode);
    }

    public void add(int index, MethodTreeNode methodTreeNode) {
        checkChild(this, methodTreeNode);
        this.methodTreeNodeList.add(index, methodTreeNode);
    }

    public int childrenSize() {
        return this.methodTreeNodeList.size();
    }
}
