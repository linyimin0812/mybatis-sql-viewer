package io.github.linyimin.plugin.ui.tree;

import com.intellij.ui.tree.StructureTreeModel;
import com.intellij.ui.treeStructure.SimpleNode;
import com.intellij.ui.treeStructure.SimpleTreeStructure;
import io.github.linyimin.plugin.utils.IconUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * @author banzhe
 * @date 2023/01/01 18:57
 **/
public class RootTreeNode extends BaseSimpleNode {

    /**
     * tree child
     */
    private final List<NamespaceTreeNode> namespaceTreeNodeList = new ArrayList<>();

    /**
     * tree structure
     */
    public StructureTreeModel<SimpleTreeStructure> structureTreeModel;

    public RootTreeNode(String name) {
        super(null, name);
        this.getTemplatePresentation().setIcon(IconUtils.GENERATE_ICON);
    }

    /**
     * 每次展示当前 tree 层级的时候执行,折叠的时候不会执行
     */
    @Override
    protected SimpleNode[] buildChildren() {
        int size = namespaceTreeNodeList.size();
        return namespaceTreeNodeList.toArray(new NamespaceTreeNode[size]);
    }

    @Override
    public String getName() {
        return this.name;
    }


    public void add(NamespaceTreeNode namespaceTreeNode) {
        checkChild(this, namespaceTreeNode);
        this.namespaceTreeNodeList.add(namespaceTreeNode);
    }

    public void add(int index, NamespaceTreeNode namespaceTreeNode) {
        checkChild(this, namespaceTreeNode);
        this.namespaceTreeNodeList.add(index, namespaceTreeNode);
    }

    public void setStructureTreeModel(StructureTreeModel<SimpleTreeStructure> structureTreeModel) {
        this.structureTreeModel = structureTreeModel;
    }
}
