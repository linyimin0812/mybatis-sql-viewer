package io.github.linyimin.plugin.ui;

import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.progress.BackgroundTaskQueue;
import com.intellij.openapi.progress.ProgressIndicator;
import com.intellij.openapi.progress.Task;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.Computable;
import com.intellij.psi.xml.XmlTag;
import com.intellij.ui.components.JBScrollPane;
import com.intellij.ui.tree.AsyncTreeModel;
import com.intellij.ui.tree.StructureTreeModel;
import com.intellij.ui.treeStructure.SimpleNode;
import com.intellij.ui.treeStructure.SimpleTree;
import com.intellij.ui.treeStructure.SimpleTreeStructure;
import com.intellij.ui.treeStructure.Tree;
import com.intellij.util.ui.JBUI;
import io.github.linyimin.plugin.ProcessResult;
import io.github.linyimin.plugin.cache.MybatisXmlContentCache;
import io.github.linyimin.plugin.component.SqlParamGenerateComponent;
import io.github.linyimin.plugin.configuration.model.MybatisSqlConfiguration;
import io.github.linyimin.plugin.constant.Constant;
import io.github.linyimin.plugin.pojo2json.RandomPOJO2JSONParser;
import io.github.linyimin.plugin.ui.tree.MethodTreeNode;
import io.github.linyimin.plugin.ui.tree.NamespaceTreeNode;
import io.github.linyimin.plugin.ui.tree.RootTreeNode;
import io.github.linyimin.plugin.ui.tree.TreeListener;
import org.fife.ui.rsyntaxtextarea.RSyntaxTextArea;
import org.fife.ui.rtextarea.RTextScrollPane;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.util.List;
import java.util.Set;

/**
 * @author banzhe
 * @date 2023/01/01 17:18
 **/
public class MybatisSqlScannerPanel implements TabbedChangeListener {
    private JPanel scannerResultPanel;
    private JPanel scannerResultContentPanel;
    private JRadioButton allRadioButton;
    private JRadioButton complianceWithSpecRadioButton;
    private JRadioButton doesNotMeetSpecRadioButton;
    private JRadioButton fullTableScanRadioButton;
    private JPanel scanTreeResultPanel;

    private JPanel sqlAndExplainPanel;

    private JPanel statementPanel;
    private RSyntaxTextArea statementText;

    private JPanel statementRulePanel;
    private RSyntaxTextArea statementRuleText;

    private JPanel indexPanel;
    private JButton jumpButton;
    private JTable indexTable;
    private JPanel sqlContentPanel;
    private JPanel sqlPanel;
    private JScrollPane indexScrollPane;

    private final Project project;
    private final InfoPane infoPane;
    private final TreeListener treeListener;

    private final RootTreeNode root = new RootTreeNode("Mybatis Sql");

    private final BackgroundTaskQueue backgroundTaskQueue;

    public MybatisSqlScannerPanel(Project project) {
        this.project = project;
        this.infoPane = new InfoPane();
        this.backgroundTaskQueue = new BackgroundTaskQueue(project, Constant.APPLICATION_NAME);
        initRadioButtonGroup();
        initText();
        this.scanTreeResultPanel.setBorder(Constant.LINE_BORDER);
        this.treeListener = new TreeListener(this);

    }

    public JPanel getScannerResultPanel() {
        return this.scannerResultPanel;
    }

    public JButton getJumpButton() {
        return this.jumpButton;
    }

    public RSyntaxTextArea getStatementText() {
        return this.statementText;
    }

    public RSyntaxTextArea getStatementRuleText() {
        return this.statementRuleText;
    }

    public Project getProject() {
        return this.project;
    }

    public JTable getIndexTable() {
        return this.indexTable;
    }

    private void initRadioButtonGroup() {
        ButtonGroup buttonGroup = new ButtonGroup();
        buttonGroup.add(allRadioButton);
        buttonGroup.add(complianceWithSpecRadioButton);
        buttonGroup.add(doesNotMeetSpecRadioButton);
        buttonGroup.add(fullTableScanRadioButton);

        this.allRadioButton.addMouseListener(new MouseCursorAdapter(this.allRadioButton));
        this.complianceWithSpecRadioButton.addMouseListener(new MouseCursorAdapter(this.complianceWithSpecRadioButton));
        this.doesNotMeetSpecRadioButton.addMouseListener(new MouseCursorAdapter(this.doesNotMeetSpecRadioButton));
        this.fullTableScanRadioButton.addMouseListener(new MouseCursorAdapter(this.fullTableScanRadioButton));

    }

    private void initText() {

        statementText = CustomTextField.createArea("sql");

        statementPanel.setLayout(new BorderLayout());

        RTextScrollPane statementScroll = new RTextScrollPane(statementText);
        statementScroll.setBorder(new EmptyBorder(JBUI.emptyInsets()));

        statementPanel.add(statementScroll);

        statementRuleText = CustomTextField.createArea("sql");

        statementRulePanel.setLayout(new BorderLayout());

        RTextScrollPane statementRuleScroll = new RTextScrollPane(statementRuleText);
        statementRuleScroll.setBorder(new EmptyBorder(JBUI.emptyInsets()));

        statementRulePanel.add(statementRuleScroll);
    }

    @Override
    public void listen() {

        backgroundTaskQueue.run(new Task.Backgroundable(project, Constant.APPLICATION_NAME) {
            @Override
            public void run(@NotNull ProgressIndicator indicator) {
                ApplicationManager.getApplication().invokeLater(() -> {
                    scannerResultPanel.setLayout(new BorderLayout());
                    scannerResultPanel.remove(scannerResultContentPanel);
                    scannerResultPanel.add(infoPane.getInfoPane());
                    infoPane.setText("Scan mybatis sql...");
                });
                createTree();
                ApplicationManager.getApplication().invokeLater(() -> {
                    scannerResultPanel.remove(infoPane.getInfoPane());
                    scannerResultPanel.add(scannerResultContentPanel);
                });
            }
        });
    }

    private void createTree() {

        scanMybatisSql();

        // 设置 tree structure
        SimpleTreeStructure treeStructure = new SimpleTreeStructure.Impl(root);
        // 设置 tree root 节点的结构 structure
        StructureTreeModel<SimpleTreeStructure> structureTreeModel = new StructureTreeModel<>(treeStructure, () -> {});
        // structure 保存到 root 节点
        root.setStructureTreeModel(structureTreeModel);
        // 设置 tree 节点的 model
        AsyncTreeModel asyncTreeModel = new AsyncTreeModel(structureTreeModel, () -> {});
        // 创建树状模型
        Tree simpleTree = new SimpleTree();
        // tree model
        simpleTree.setModel(asyncTreeModel);
        // 绑定监听器
        simpleTree.addMouseListener(this.treeListener);
        simpleTree.addMouseListener(new MouseCursorAdapter(simpleTree));

        ApplicationManager.getApplication().invokeLater(() -> {
            scanTreeResultPanel.removeAll();
            JScrollPane scrollPane = new JBScrollPane(simpleTree, JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED, JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
            scanTreeResultPanel.setLayout(new BorderLayout());
            scanTreeResultPanel.add(scrollPane);
            // 默认选中第一个方法
            if (root.getChildAt(0) != null && root.getChildAt(0).getChildAt(0) != null) {
                SimpleNode node = root.getChildAt(0).getChildAt(0);
                structureTreeModel.expand(node, simpleTree, simpleTree::setSelectionPath);
                this.treeListener.updateSqlPanel(((MethodTreeNode)node).getConfiguration());
            }

        });

    }

    private void scanMybatisSql(String namespace) {
        NamespaceTreeNode namespaceTreeNode = new NamespaceTreeNode(root, namespace);
        root.add(namespaceTreeNode);

        Set<XmlTag> methods = ApplicationManager.getApplication().runReadAction((Computable<Set<XmlTag>>) () -> MybatisXmlContentCache.acquireMethodsByNamespace(project, namespace));

        for (XmlTag method : methods) {
            ProcessResult<MybatisSqlConfiguration> result = ApplicationManager.getApplication().runReadAction(
                    (Computable<ProcessResult<MybatisSqlConfiguration>>) () -> SqlParamGenerateComponent.generate(method.getFirstChild(), new RandomPOJO2JSONParser(), false)
            );
            if (result.isSuccess()) {
                MybatisSqlConfiguration configuration = result.getData();
                MethodTreeNode methodTreeNode = new MethodTreeNode(namespaceTreeNode, configuration.getMethod());
                methodTreeNode.setMybatisSqlScannerPanel(this).setConfiguration(configuration);
                namespaceTreeNode.add(methodTreeNode);

                ProcessResult<String> sqlResult = ApplicationManager.getApplication().runReadAction((Computable<ProcessResult<String>>) () -> SqlParamGenerateComponent.generateSql(project, configuration.getMethod(), configuration.getParams(), false));

                if (sqlResult.isSuccess()) {
                    configuration.setRawSql(sqlResult.getData());
                } else {
                    // TODO: 无法生成错误提示
                }

            } else {
                // TODO: 无法生成错误提示
            }

        }
    }

    private void scanMybatisSql() {
        // mapper 列表节点
        List<String> namespaces = ApplicationManager.getApplication().runReadAction((Computable<List<String>>) () -> MybatisXmlContentCache.acquireByNamespace(project, true));
        for (String namespace : namespaces) {
            scanMybatisSql(namespace);
        }
    }

    public JPanel getSqlContentPanel() {
        return sqlContentPanel;
    }

    public JPanel getSqlPanel() {
        return sqlPanel;
    }

    public InfoPane getInfoPane() {
        return infoPane;
    }

    public JPanel getIndexPanel() {
        return indexPanel;
    }

    public JScrollPane getIndexScrollPane() {
        return indexScrollPane;
    }
}
