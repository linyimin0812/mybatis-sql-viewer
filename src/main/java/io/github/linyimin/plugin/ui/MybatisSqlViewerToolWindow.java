package io.github.linyimin.plugin.ui;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.SimpleToolWindowPanel;
import com.intellij.ui.JBColor;
import com.intellij.util.PsiNavigateUtil;
import com.intellij.util.ui.JBUI;
import io.github.linyimin.plugin.configuration.MybatisSqlStateComponent;
import io.github.linyimin.plugin.configuration.model.MybatisSqlConfiguration;
import io.github.linyimin.plugin.constant.Constant;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
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

    private final ParamTabbedPane paramTabbedPane;
    private final SqlTabbedPane sqlTabbedPane;

    private final TableTabbedPane tableTabbedPane;

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
        this.totalTabbedPanel.addTab("params", this.paramTabbedPane.getParamTabbedPanel());

        this.sqlTabbedPane = new SqlTabbedPane(myProject);
        this.totalTabbedPanel.addTab("sql", this.sqlTabbedPane.getSqlTabbedPanel());

        this.tableTabbedPane = new TableTabbedPane(myProject);
        this.totalTabbedPanel.addTab("table", this.tableTabbedPane.getTableTabbedPanel());

        methodName.setBorder(new EmptyBorder(JBUI.emptyInsets()));
        datasourceButton.setFocusPainted(false);
        initSourceLinkLabel();

        addComponentListener();

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
        this.sqlTabbedPane.getSqlTabbedPanel().setSelectedIndex(0);

    }

    private void addComponentListener() {

        datasourceButton.addActionListener((e) -> {
            DatasourceDialog dialog = new DatasourceDialog(myProject);
            dialog.pack();
            dialog.setVisible(true);
        });

        datasourceButton.addMouseListener(new MouseCursorAdapter(this.datasourceButton));

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

    private void totalTabbedPanelListener() {

        int selectedIndex = totalTabbedPanel.getSelectedIndex();

        // 点击param tab时生成对应参数
        if (selectedIndex == TabbedComponentType.params.index) {
            this.paramTabbedPane.listen();
        }

        // 点击sql tab时生成sql
        if (selectedIndex == TabbedComponentType.sql.index) {
            this.sqlTabbedPane.listen();
        }

        // 点击table tab时获取table的schema信息
        if (selectedIndex == TabbedComponentType.table.index) {
            this.tableTabbedPane.listen();

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
        /**
         * Tanned类型对应的index
         */
        params(0),
        sql(1),
        table(2);

        private final int index;

        TabbedComponentType(int index) {
            this.index = index;
        }

    }
}
