package io.github.linyimin.plugin.ui;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.SimpleToolWindowPanel;
import com.intellij.ui.JBColor;
import com.intellij.util.PsiNavigateUtil;
import com.intellij.util.ui.JBUI;
import com.intellij.util.ui.UIUtil;
import io.github.linyimin.plugin.configuration.GlobalConfig;
import io.github.linyimin.plugin.configuration.MybatisSqlStateComponent;
import io.github.linyimin.plugin.configuration.model.MybatisSqlConfiguration;
import io.github.linyimin.plugin.constant.Constant;
import org.apache.commons.lang3.StringUtils;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.IOException;
import java.net.URI;

/**
 * @author yiminlin
 * @date 2022/02/01 12:31 下午
 **/
public class MybatisSqlViewerToolWindow extends SimpleToolWindowPanel {

    private JFormattedTextField methodName;
    private JTabbedPane totalTabbedPanel;
    private JPanel root;

    private JButton datasourceButton;
    private JButton jumpButton;
    private JLabel sourceLink;
    private JCheckBox mybatisModeCheckBox;
    private JPanel mybatisModePanel;
    private JPanel methodNamePanel;
    private JButton mybatisSqlScanButton;

    private final ParamTabbedPane paramTabbedPane;
    private final SqlTabbedPane sqlTabbedPane;

    private final TableTabbedPane tableTabbedPane;
    private final MybatisSqlScannerPanel  mybatisSqlScannerPanel;

    private final Project myProject;

    public JPanel getRoot() {
        return root;
    }

    @Override
    public JPanel getContent() {
        return getRoot();
    }

    public MybatisSqlViewerToolWindow(Project project) {

        super(true, false);
        this.myProject = project;

        this.paramTabbedPane = new ParamTabbedPane(myProject);
        this.totalTabbedPanel.addTab(TabbedComponentType.params.name(), this.paramTabbedPane.getParamTabbedPanel());

        this.sqlTabbedPane = new SqlTabbedPane(myProject);
        this.totalTabbedPanel.addTab(TabbedComponentType.sql.name(), this.sqlTabbedPane.getSqlTabbedPanel());

        this.tableTabbedPane = new TableTabbedPane(myProject);
        this.totalTabbedPanel.addTab(TabbedComponentType.table.name(), this.tableTabbedPane.getTableTabbedPanel());

        this.mybatisSqlScannerPanel = new MybatisSqlScannerPanel(project);

        if (UIUtil.isUnderDarcula()) {
            methodName.setBorder(Constant.LINE_BORDER);
        } else {
            methodName.setBorder(new EmptyBorder(JBUI.emptyInsets()));
        }

        initSourceLinkLabel();

        addComponentListener();

        this.mybatisModePanel.setBorder(Constant.LINE_BORDER);

        this.mybatisModeCheckBox.addItemListener(e -> this.mybatisModeCheckBoxListener());
        this.mybatisModeCheckBox.addMouseListener(new MouseCursorAdapter(this.mybatisModeCheckBox));

        this.totalTabbedPanel.addMouseListener(new MouseCursorAdapter(this.totalTabbedPanel));

        this.totalTabbedPanel.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent event) {
                int index = totalTabbedPanel.indexOfTab(TabbedComponentType.project.name());
                if (event.getClickCount() == 2 && totalTabbedPanel.indexAtLocation(event.getX(), event.getY()) == index) {
                    mybatisSqlScannerPanel.listen();
                }
            }
        });

    }

    /**
     * 刷新tool window配置内容
     */
    public void refresh(Project project) {

        MybatisSqlConfiguration config = project.getService(MybatisSqlStateComponent.class).getConfiguration();

        methodName.setText(config.getMethod());

        this.paramTabbedPane.getRandomParamsText().setText(config.getParams());

        // 默认每次打开，都展示第一个tab
        totalTabbedPanel.setSelectedIndex(0);
        this.paramTabbedPane.getParamTabbedPanel().setSelectedIndex(0);

    }

    private void addComponentListener() {

        datasourceButton.addActionListener((e) -> {
            DatasourceDialog dialog = new DatasourceDialog(myProject);
            dialog.pack();
            dialog.setVisible(true);
        });

        datasourceButton.addMouseListener(new MouseCursorAdapter(this.datasourceButton));

        this.mybatisSqlScanButton.addMouseListener(new MouseCursorAdapter(this.mybatisSqlScanButton));
        this.mybatisSqlScanButton.addActionListener(e -> {
            int projectIndex = this.totalTabbedPanel.indexOfTab(TabbedComponentType.project.name());
            if (projectIndex < 0) {
                this.totalTabbedPanel.insertTab(TabbedComponentType.project.name(), null, this.mybatisSqlScannerPanel.getScannerResultPanel(), null, TabbedComponentType.project.index);
            }
            this.totalTabbedPanel.setSelectedIndex(TabbedComponentType.project.index);
        });

        jumpButton.addMouseListener(new MouseCursorAdapter(this.jumpButton));
        jumpButton.addActionListener(e -> {
            MybatisSqlConfiguration config = myProject.getService(MybatisSqlStateComponent.class).getConfiguration();
            if (config.getPsiElement() != null) {
                PsiNavigateUtil.navigate(config.getPsiElement());
            }
        });

        // 监听tabbedpane点击事件
        totalTabbedPanel.addChangeListener(e -> totalTabbedPanelListener());

    }

    private void mybatisModeCheckBoxListener() {

        boolean isSelected = this.mybatisModeCheckBox.isSelected();

        GlobalConfig.isMybatisMode = isSelected;

        int paramsIndex = this.totalTabbedPanel.indexOfTab(TabbedComponentType.params.name());

        if (isSelected) {
            if (paramsIndex < 0) {
                this.methodNamePanel.setVisible(true);
                this.mybatisSqlScanButton.setVisible(true);
                this.totalTabbedPanel.insertTab(TabbedComponentType.params.name(), null, this.paramTabbedPane.getParamTabbedPanel(), null, TabbedComponentType.params.index);

                this.totalTabbedPanel.setSelectedIndex(0);
            }

            return;
        }

        if (paramsIndex >= 0) {
            this.totalTabbedPanel.removeTabAt(paramsIndex);
            this.methodNamePanel.setVisible(false);
            this.mybatisSqlScanButton.setVisible(false);
            this.totalTabbedPanel.setSelectedIndex(0);
        }

        int projectIndex = this.totalTabbedPanel.indexOfTab(TabbedComponentType.project.name());

        if (projectIndex >= 0) {
            this.totalTabbedPanel.removeTabAt(projectIndex);
            this.totalTabbedPanel.setSelectedIndex(0);
        }

    }

    private void totalTabbedPanelListener() {

        int selectedIndex = totalTabbedPanel.getSelectedIndex();
        String title = totalTabbedPanel.getTitleAt(selectedIndex);

        // 点击param tab时生成对应参数
        if (StringUtils.equals(title, TabbedComponentType.params.name())) {
            this.paramTabbedPane.listen();
        }

        // 点击sql tab时生成sql
        if (StringUtils.equals(title, TabbedComponentType.sql.name())) {
            this.sqlTabbedPane.listen();
        }

        // 点击table tab时获取table的schema信息
        if (StringUtils.equals(title, TabbedComponentType.table.name())) {
            this.tableTabbedPane.listen();
        }

        // 扫描项目中mybatis sql语句
        if (StringUtils.equals(title, TabbedComponentType.project.name())) {
            this.mybatisSqlScannerPanel.listen();
        }

    }

    private void initSourceLinkLabel() {
        this.sourceLink.setForeground(JBColor.BLUE);
        this.sourceLink.addMouseListener(new MouseCursorAdapter(this.sourceLink) {
            @Override
            public void mouseClicked(MouseEvent e) {
                try {
                    Desktop.getDesktop().browse(URI.create(Constant.SOURCE_CODE));
                } catch (IOException ex) {
                    throw new RuntimeException(ex);
                }
            }
        });
    }

    private enum TabbedComponentType {
        params(0),
        sql(1),
        table(2),
        project(3);

        private final int index;

        TabbedComponentType(int index) {
            this.index = index;
        }
    }
}
