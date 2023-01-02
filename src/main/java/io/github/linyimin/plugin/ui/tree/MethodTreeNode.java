package io.github.linyimin.plugin.ui.tree;

import com.intellij.ui.treeStructure.SimpleNode;
import io.github.linyimin.plugin.configuration.model.MybatisSqlConfiguration;
import io.github.linyimin.plugin.ui.MybatisSqlScannerPanel;

import javax.swing.*;

/**
 * @author banzhe
 * @date 2023/01/01 19:00
 **/
public class MethodTreeNode extends BaseSimpleNode {

    private MybatisSqlConfiguration configuration;
    private MybatisSqlScannerPanel mybatisSqlScannerPanel;

    public MethodTreeNode(SimpleNode aParent, String name, Icon icon) {
        // 父节点和name赋值
        super(aParent, name);
        this.getTemplatePresentation().setIcon(icon);
    }

    @Override
    protected SimpleNode[] buildChildren() {
        return new SimpleNode[0];
    }

    @Override
    public String getName() {
        return this.name;
    }

    public MybatisSqlConfiguration getConfiguration() {
        return configuration;
    }

    public MethodTreeNode setConfiguration(MybatisSqlConfiguration configuration) {
        this.configuration = configuration;
        return this;
    }

    public MybatisSqlScannerPanel getMybatisSqlScannerPanel() {
        return mybatisSqlScannerPanel;
    }

    public MethodTreeNode setMybatisSqlScannerPanel(MybatisSqlScannerPanel mybatisSqlScannerPanel) {
        this.mybatisSqlScannerPanel = mybatisSqlScannerPanel;
        return this;
    }
}
