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
import io.github.linyimin.plugin.pojo2json.DefaultPOJO2JSONParser;
import io.github.linyimin.plugin.sql.checker.Checker;
import io.github.linyimin.plugin.sql.checker.CheckerHolder;
import io.github.linyimin.plugin.sql.checker.Report;
import io.github.linyimin.plugin.sql.checker.enums.CheckScopeEnum;
import io.github.linyimin.plugin.sql.parser.SqlParser;
import io.github.linyimin.plugin.ui.tree.MethodTreeNode;
import io.github.linyimin.plugin.ui.tree.NamespaceTreeNode;
import io.github.linyimin.plugin.ui.tree.RootTreeNode;
import io.github.linyimin.plugin.ui.tree.TreeListener;
import io.github.linyimin.plugin.utils.IconUtils;
import org.apache.commons.collections.CollectionUtils;
import org.fife.ui.rsyntaxtextarea.RSyntaxTextArea;
import org.fife.ui.rtextarea.RTextScrollPane;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.util.ArrayList;
import java.util.Arrays;
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
    private JRadioButton errorRadioButton;

    private final Project project;
    private final InfoPane infoPane;
    private final TreeListener treeListener;

    private RootTreeNode allRoot = new RootTreeNode(Constant.ROOT_NAME);

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
        buttonGroup.add(errorRadioButton);

        this.allRadioButton.addMouseListener(new MouseCursorAdapter(this.allRadioButton));
        this.complianceWithSpecRadioButton.addMouseListener(new MouseCursorAdapter(this.complianceWithSpecRadioButton));
        this.doesNotMeetSpecRadioButton.addMouseListener(new MouseCursorAdapter(this.doesNotMeetSpecRadioButton));
        this.fullTableScanRadioButton.addMouseListener(new MouseCursorAdapter(this.fullTableScanRadioButton));
        this.errorRadioButton.addMouseListener(new MouseCursorAdapter(this.errorRadioButton));

        this.allRadioButton.addActionListener(new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (allRadioButton.isSelected()) {
                    backgroundTaskQueue.run(new Task.Backgroundable(project, Constant.APPLICATION_NAME) {
                        @Override
                        public void run(@NotNull ProgressIndicator indicator) {
                            RootTreeNode root = getRootByType(FilterType.all);
                            createTree(root);
                        }
                    });
                }
            }
        });

        this.complianceWithSpecRadioButton.addActionListener(new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (complianceWithSpecRadioButton.isSelected()) {
                    backgroundTaskQueue.run(new Task.Backgroundable(project, Constant.APPLICATION_NAME) {
                        @Override
                        public void run(@NotNull ProgressIndicator indicator) {
                            RootTreeNode root = getRootByType(FilterType.compliance_spec);
                            createTree(root);
                        }
                    });
                }
            }
        });

        this.doesNotMeetSpecRadioButton.addActionListener(new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (doesNotMeetSpecRadioButton.isSelected()) {
                    backgroundTaskQueue.run(new Task.Backgroundable(project, Constant.APPLICATION_NAME) {
                        @Override
                        public void run(@NotNull ProgressIndicator indicator) {
                            RootTreeNode root = getRootByType(FilterType.not_meet_spec);
                            createTree(root);
                        }
                    });
                }
            }
        });

        this.fullTableScanRadioButton.addActionListener(new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (fullTableScanRadioButton.isSelected()) {
                    backgroundTaskQueue.run(new Task.Backgroundable(project, Constant.APPLICATION_NAME) {
                        @Override
                        public void run(@NotNull ProgressIndicator indicator) {
                            RootTreeNode root = getRootByType(FilterType.full_table_scan);
                            createTree(root);
                        }
                    });
                }
            }
        });

        this.errorRadioButton.addActionListener(new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (errorRadioButton.isSelected()) {
                    backgroundTaskQueue.run(new Task.Backgroundable(project, Constant.APPLICATION_NAME) {
                        @Override
                        public void run(@NotNull ProgressIndicator indicator) {
                            RootTreeNode root = getRootByType(FilterType.error);
                            createTree(root);
                        }
                    });
                }
            }
        });

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
                    allRadioButton.setSelected(true);
                    scannerResultPanel.setLayout(new BorderLayout());
                    scannerResultPanel.remove(scannerResultContentPanel);
                    scannerResultPanel.add(infoPane.getInfoPane());
                    infoPane.setText("Scan mybatis sql...");
                });
                allRoot = scanMybatisSql();
                createTree(allRoot);
                ApplicationManager.getApplication().invokeLater(() -> {
                    scannerResultPanel.remove(infoPane.getInfoPane());
                    scannerResultPanel.add(scannerResultContentPanel);
                });
            }
        });
    }

    private RootTreeNode getRootByType(FilterType filterType) {
        RootTreeNode root = new RootTreeNode(Constant.ROOT_NAME);

        SimpleNode[] simpleNodes = allRoot.getChildren();
        if (simpleNodes.length == 0) {
            return root;
        }
        for (SimpleNode node : simpleNodes) {

            SimpleNode[] methodNodes = node.getChildren();
            if (methodNodes.length == 0) {
                continue;
            }

            NamespaceTreeNode namespaceTreeNode = new NamespaceTreeNode(root, node.getName());

            for (SimpleNode methodNode : methodNodes) {

                Icon icon = methodNode.getPresentation().getIcon(true);

                FilterType type = FilterType.resolveByIcon(icon);

                if (filterType == FilterType.all || filterType == type) {
                    MethodTreeNode methodTreeNode = new MethodTreeNode(namespaceTreeNode, methodNode.getName(), icon);
                    methodTreeNode.setMybatisSqlScannerPanel(this).setConfiguration(((MethodTreeNode)methodNode).getConfiguration());
                    namespaceTreeNode.add(methodTreeNode);
                }
            }

            if (namespaceTreeNode.childrenSize() > 0) {
                root.add(namespaceTreeNode);
            }
        }

        return root;
    }

    private void createTree(RootTreeNode root) {

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

    private void scanMybatisSql(RootTreeNode root, String namespace) {

        NamespaceTreeNode namespaceTreeNode = new NamespaceTreeNode(root, namespace);
        root.add(namespaceTreeNode);

        Set<XmlTag> methods = ApplicationManager.getApplication().runReadAction((Computable<Set<XmlTag>>) () -> MybatisXmlContentCache.acquireMethodsByNamespace(project, namespace));

        for (XmlTag method : methods) {
            ProcessResult<MybatisSqlConfiguration> result = ApplicationManager.getApplication().runReadAction(
                    (Computable<ProcessResult<MybatisSqlConfiguration>>) () -> SqlParamGenerateComponent.generate(method.getFirstChild(), new DefaultPOJO2JSONParser(), false)
            );

            MybatisSqlConfiguration configuration = result.getData();

            MethodTreeNode methodTreeNode;

            if (result.isSuccess()) {

                ProcessResult<String> sqlResult = ApplicationManager.getApplication().runReadAction((Computable<ProcessResult<String>>) () -> SqlParamGenerateComponent.generateSql(project, configuration.getMethod(), configuration.getParams(), false));

                if (sqlResult.isSuccess()) {
                    configuration.setSql(sqlResult.getData());
                    Icon icon = sqlCheck(sqlResult.getData());
                    methodTreeNode = new MethodTreeNode(namespaceTreeNode, configuration.getMethod(), icon);

                } else {
                    methodTreeNode = new MethodTreeNode(namespaceTreeNode, configuration.getMethod(), IconUtils.ERROR_ICON);
                }

            } else {
                methodTreeNode = new MethodTreeNode(namespaceTreeNode, configuration.getMethod(), IconUtils.ERROR_ICON);
            }
            methodTreeNode.setMybatisSqlScannerPanel(this).setConfiguration(configuration);
            namespaceTreeNode.add(methodTreeNode);
        }
    }

    private Icon sqlCheck(String sql) {
        try {
            // 语法校验
            ProcessResult<String> validateResult = SqlParser.validate(sql);

            if (!validateResult.isSuccess()) {
                return IconUtils.ERROR_ICON;
            }

            // 索引检查
            ProcessResult<Boolean> indexCheckResult = checkIndex(sql);
            if (!indexCheckResult.isSuccess()) {
                return IconUtils.ERROR_ICON;
            }

            if (!indexCheckResult.getData()) {
                return IconUtils.FULL_SCAN_ICON;
            }

            ProcessResult<Boolean> ruleCheckResult = checkRule(sql);
            if (!ruleCheckResult.isSuccess()) {
                return IconUtils.ERROR_ICON;
            }

            if (!ruleCheckResult.getData()) {
                return IconUtils.NOT_MEET_SPEC_ICON;
            }

            return IconUtils.MAJOR_ICON;
        } catch (Exception e) {
            return IconUtils.ERROR_ICON;
        }
    }

    private ProcessResult<Boolean> checkIndex(String sql) {
        Checker checker = CheckerHolder.getChecker(CheckScopeEnum.index_hit);
        List<Report> reports = checker.check(project, sql);
        boolean allPass = reports.stream().allMatch(Report::isPass);
        return ProcessResult.success(allPass);
    }

    private ProcessResult<Boolean> checkRule(String sql) {

        CheckScopeEnum scope = SqlParser.getCheckScope(sql);
        Checker checker = CheckerHolder.getChecker(scope);

        if (checker == null) {
            return ProcessResult.success(true);
        }

        List<Report> reports = checker.check(project, sql);
        if (CollectionUtils.isEmpty(reports)) {
            return ProcessResult.success(true);
        }

        boolean meetRule = reports.stream().allMatch(Report::isPass);

        return ProcessResult.success(meetRule);
    }

    private RootTreeNode scanMybatisSql() {
        RootTreeNode root = new RootTreeNode(Constant.ROOT_NAME);
        // mapper 列表节点
        List<String> namespaces = ApplicationManager.getApplication().runReadAction((Computable<List<String>>) () -> MybatisXmlContentCache.acquireByNamespace(project, true));
        for (String namespace : namespaces) {
            scanMybatisSql(root, namespace);
        }

        return root;
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

    public enum FilterType {

        all(null),
        compliance_spec(IconUtils.MAJOR_ICON),
        not_meet_spec(IconUtils.NOT_MEET_SPEC_ICON),
        full_table_scan(IconUtils.FULL_SCAN_ICON),
        error(IconUtils.ERROR_ICON);

        private final Icon icon;

        FilterType(Icon icon) {
            this.icon = icon;
        }

        public static FilterType resolveByIcon(Icon icon) {
            return Arrays.stream(FilterType.values()).filter(type -> type.icon == icon).findFirst().orElse(FilterType.all);
        }
    }
}
