package io.github.linyimin.plugin.view;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.SimpleToolWindowPanel;
import com.intellij.openapi.wm.ToolWindow;
import io.github.linyimin.plugin.dom.Constant;
import io.github.linyimin.plugin.service.MybatisSqlStateComponent;
import io.github.linyimin.plugin.service.SqlParamGenerateService;
import io.github.linyimin.plugin.service.model.MybatisSqlConfiguration;
import io.github.linyimin.plugin.utils.MybatisSqlUtils;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import java.sql.SQLException;


/**
 * @author yiminlin
 * @date 2022/02/01 12:31 下午
 **/
public class MybatisSqlViewerToolWindow extends SimpleToolWindowPanel {
    private JTextField methodName;
    private JTabbedPane tabbedPane;
    private JTextField host;
    private JTextField port;
    private JTextField user;
    private JTextField database;
    private JPasswordField password;
    private JTextField url;
    private JButton connectionTestButton;
    private JPanel root;
    private JTextArea connectionInfoTextArea;
    private JPanel params;
    private JTextArea result;
    private JTextArea sql;

    private final Project myProject;


    public JPanel getRoot() {
        return root;
    }

    public JTabbedPane getTabbedPane() {
        return tabbedPane;
    }

    @Override
    public JPanel getContent() {
        return getRoot();
    }

    public MybatisSqlViewerToolWindow(ToolWindow toolWindow, Project project) {
        super(true, false);
        this.myProject = project;
        addComponentListener();
    }


    /**
     * 刷新tool window配置内容
     */
    public void refresh(Project project) {
        MybatisSqlConfiguration config = project.getService(MybatisSqlStateComponent.class).getState();
        assert config != null;

        methodName.setText(config.getMethod());
        result.setText(config.getResult());
        ((JsonTextField) params).setText(config.getParams());
        sql.setText(config.getSql());
        // 默认每次打开，都展示第一个tab
        tabbedPane.setSelectedIndex(0);
    }

    private void createUIComponents() {
        params = new JsonTextField(this.myProject);

    }

    private void addComponentListener() {
        host.getDocument().addDocumentListener(new DatasourceChangeListener());
        port.getDocument().addDocumentListener(new DatasourceChangeListener());
        database.getDocument().addDocumentListener(new DatasourceChangeListener());

        ((JsonTextField) params).addDocumentListener(new com.intellij.openapi.editor.event.DocumentListener() {
            @Override
            public void documentChanged(com.intellij.openapi.editor.event.@NotNull DocumentEvent event) {
                MybatisSqlConfiguration config = myProject.getService(MybatisSqlStateComponent.class).getState();
                assert config != null;
                config.setParams(((JsonTextField) params).getText());
            }
        });

        // 监听button点击事件
        connectionTestButton.addActionListener((e) -> {

            String hostText = host.getText();
            String portText = port.getText();
            String userText = user.getText();
            String passwordText = String.valueOf(password.getPassword());
            String databaseText = database.getText();

            String urlText = String.format(Constant.DATABASE_URL_TEMPLATE, hostText, portText, databaseText);

            String connectionInfo = MybatisSqlUtils.mysqlConnectTest(urlText, userText, passwordText);
            connectionInfoTextArea.setText(connectionInfo);
        });

        // 监听tabbedpane点击事件
        tabbedPane.addChangeListener(e -> {

            int selectedIndex = tabbedPane.getSelectedIndex();

            // 点击sql tab时生成sql
            if (selectedIndex == TabbedComponentType.sql.index) {
                sql.setText("Loading...");
                generateSql();
            }

            // 点击result tab时执行sql语句并展示结果
            if (selectedIndex == TabbedComponentType.result.index) {
                result.setText("Loading...");
                generateSql();
                executeSql();
            }
        });
    }

    private void generateSql() {
        SqlParamGenerateService generateService = myProject.getService(SqlParamGenerateService.class);

        MybatisSqlConfiguration sqlConfig = myProject.getService(MybatisSqlStateComponent.class).getState();
        assert sqlConfig != null;

        String sqlStr = generateService.generateSql(myProject, sqlConfig.getMethod(), sqlConfig.getParams());
        sqlConfig.setSql(sqlStr);

        sql.setText(sqlStr);
    }

    private void executeSql() {
        String urlText = String.format(Constant.DATABASE_URL_TEMPLATE, host.getText(), port.getText(), database.getText());

        String passwordText = String.valueOf(password.getPassword());
        String resultText;
        try {
            resultText = MybatisSqlUtils.executeSql(urlText, user.getText(), passwordText, sql.getText());
        } catch (SQLException e) {
            resultText = "Execute Sql Failed. err: " + e.getMessage();
        }

        MybatisSqlConfiguration sqlConfig = myProject.getService(MybatisSqlStateComponent.class).getState();
        assert sqlConfig != null;
        sqlConfig.setResult(resultText);

        result.setText(resultText);

    }

    private class DatasourceChangeListener implements DocumentListener {

        @Override
        public void insertUpdate(DocumentEvent e) {
            updateUrlTextField();
        }

        @Override
        public void removeUpdate(DocumentEvent e) {
            updateUrlTextField();
        }

        @Override
        public void changedUpdate(DocumentEvent e) {
            updateUrlTextField();
        }

        private void updateUrlTextField() {
            String hostText = host.getText();
            String portText = port.getText();
            String databaseText = database.getText();
            String urlText = String.format(Constant.DATABASE_URL_TEMPLATE, hostText, portText, databaseText);
            url.setText(urlText);
        }
    }

    private enum TabbedComponentType {
        /**
         * Tanned类型对应的index
         */
        params(0),
        sql(1),
        result(2),
        datasource(3);

        private final int index;

        TabbedComponentType(int index) {
            this.index = index;
        }

        public int getIndex() {
            return index;
        }
    }
}
