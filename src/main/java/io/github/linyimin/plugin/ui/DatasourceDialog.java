package io.github.linyimin.plugin.ui;

import com.intellij.openapi.project.Project;
import io.github.linyimin.plugin.configuration.MybatisDatasourceStateComponent;
import io.github.linyimin.plugin.constant.Constant;
import io.github.linyimin.plugin.sql.DatasourceComponent;
import io.github.linyimin.plugin.sql.executor.SqlExecutor;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import java.awt.event.*;

public class DatasourceDialog extends JDialog {
    private JPanel contentPane;
    private JButton saveConfiguration;
    private JButton testConnection;
    private JTextField host;
    private JTextField port;
    private JTextField user;
    private JPasswordField password;
    private JTextField database;
    private JTextField url;
    private JTextArea testResult;
    private final Project project;

    public DatasourceDialog(Project project) {

        this.project = project;

        setContentPane(contentPane);
        setModal(true);

        setLocationRelativeTo(null);

        getRootPane().setDefaultButton(saveConfiguration);

        initDatasource();

        host.getDocument().addDocumentListener(new DatasourceChangeListener());
        port.getDocument().addDocumentListener(new DatasourceChangeListener());
        database.getDocument().addDocumentListener(new DatasourceChangeListener());

        saveConfiguration.addActionListener((e) -> {
            updateDatasourceForPersistent();

            testResult.setText("Save success.");
        });

        // 监听button点击事件
        testConnection.addActionListener((e) -> {

            updateDatasourceForPersistent();

            String connectionInfo = SqlExecutor.testConnected(project);
            testResult.setText(connectionInfo);

        });

        // call onCancel() when cross is clicked
        setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
        addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent e) {
                dispose();
            }
        });

        // call onCancel() on ESCAPE
        contentPane.registerKeyboardAction(e -> dispose(), KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0), JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT);
    }

    private void initDatasource() {
        MybatisDatasourceStateComponent component = project.getComponent(MybatisDatasourceStateComponent.class);
        host.setText(component.getHost());
        port.setText(component.getPort());
        user.setText(component.getUser());
        password.setText(component.getPassword());
        database.setText(component.getDatabase());

        String urlText = String.format(Constant.DATABASE_URL_TEMPLATE, component.getHost(), component.getPort(), component.getDatabase());
        url.setText(urlText);
    }

    private void updateDatasourceForPersistent() {
        MybatisDatasourceStateComponent component = project.getComponent(MybatisDatasourceStateComponent.class);

        DatasourceComponent datasourceComponent = project.getService(DatasourceComponent.class);

        component.getState()
                .host(host.getText())
                .port(port.getText())
                .user(user.getText())
                .password(String.valueOf(password.getPassword()))
                .database(database.getText());

        datasourceComponent.updateDatasource();

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
}
