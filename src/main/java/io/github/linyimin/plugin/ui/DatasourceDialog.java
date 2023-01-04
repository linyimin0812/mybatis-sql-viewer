package io.github.linyimin.plugin.ui;

import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.progress.BackgroundTaskQueue;
import com.intellij.openapi.progress.ProgressIndicator;
import com.intellij.openapi.progress.Task;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.ComboBox;
import io.github.linyimin.plugin.configuration.DatasourceConfigComponent;
import io.github.linyimin.plugin.configuration.model.DatasourceConfiguration;
import io.github.linyimin.plugin.constant.Constant;
import io.github.linyimin.plugin.sql.DatasourceComponent;
import io.github.linyimin.plugin.sql.executor.SqlExecutor;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import java.awt.*;
import java.awt.event.*;
import java.util.List;

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
    // 通过addConfiguration button控制显示combo box或text field
    private JPanel namePanel;
    private JButton addConfiguration;
    private JButton deleteButton;

    private final JTextField nameText = new JTextField();
    private final JComboBox<String> nameComboBox = new ComboBox<>();

    private final Project project;
    private final BackgroundTaskQueue backgroundTaskQueue;

    public DatasourceDialog(Project project) {

        this.project = project;

        backgroundTaskQueue = new BackgroundTaskQueue(project, Constant.APPLICATION_NAME);

        setContentPane(contentPane);
        setModal(true);

        setLocationRelativeTo(null);
        getRootPane().setDefaultButton(saveConfiguration);

        namePanel.setLayout(new BorderLayout());

        host.getDocument().addDocumentListener(new DatasourceChangeListener());
        port.getDocument().addDocumentListener(new DatasourceChangeListener());
        database.getDocument().addDocumentListener(new DatasourceChangeListener());

        addButtonActionListener();
        addButtonMouseCursorAdapter();

        // call onCancel() when cross is clicked
        setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
        addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent e) {
                dispose();
            }
        });

        // call onCancel() on ESCAPE
        contentPane.registerKeyboardAction(e -> dispose(), KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0), JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT);

        this.deleteButton.setEnabled(true);

        initDatasource();

        nameComboBox.addActionListener(e -> datasourceChange());

    }

    private void addButtonMouseCursorAdapter() {
        this.saveConfiguration.addMouseListener(new MouseCursorAdapter(this.saveConfiguration));
        this.testConnection.addMouseListener(new MouseCursorAdapter(this.testConnection));
        this.addConfiguration.addMouseListener(new MouseCursorAdapter(this.addConfiguration));
        this.deleteButton.addMouseListener(new MouseCursorAdapter(this.deleteButton));
    }

    private void addButtonActionListener() {
        saveConfiguration.addActionListener((e) -> backgroundTaskQueue.run(new Task.Backgroundable(project, Constant.APPLICATION_NAME) {
            @Override
            public void run(@NotNull ProgressIndicator indicator) {
                updateDatasourceForPersistent();
                ApplicationManager.getApplication().invokeLater(() -> testResult.setText("Save success."));
            }
        }));

        // 监听button点击事件
        testConnection.addActionListener((e) -> backgroundTaskQueue.run(new Task.Backgroundable(project, Constant.APPLICATION_NAME) {
            @Override
            public void run(@NotNull ProgressIndicator indicator) {

                updateDatasourceForPersistent();
                String connectionInfo = SqlExecutor.testConnected(project);

                ApplicationManager.getApplication().invokeLater(() -> testResult.setText(connectionInfo));
            }
        }));

        addConfiguration.addActionListener(e -> addDatasource());

        deleteButton.addActionListener(e -> deleteDatasource());
    }

    private void deleteDatasource() {
        DatasourceConfigComponent component = ApplicationManager.getApplication().getComponent(DatasourceConfigComponent.class);
        String removedName = component.getName();
        component.remove();
        nameComboBox.removeItem(removedName);
        if (StringUtils.isBlank(component.getName())) {
            addDatasource();
        }
        nameComboBox.setSelectedItem(component.getName());
    }

    private void initDatasource() {

        DatasourceConfigComponent component = ApplicationManager.getApplication().getComponent(DatasourceConfigComponent.class);

        host.setText(component.getHost());
        port.setText(component.getPort());
        user.setText(component.getUser());
        password.setText(component.getPassword());
        database.setText(component.getDatabase());

        if (StringUtils.isNotBlank(component.getName())) {
            displayNameComboBox();
            String urlText = String.format(Constant.DATABASE_URL_TEMPLATE, component.getHost(), component.getPort(), component.getDatabase());
            url.setText(urlText);
        } else {
            addDatasource();
        }

    }

    private void addDatasource() {
        // 隐藏combobox, 显示text field用于创建新的数据源
        displayNameFieldText();
        this.deleteButton.setEnabled(false);
    }

    private void datasourceChange() {
        DatasourceConfigComponent component = ApplicationManager.getApplication().getComponent(DatasourceConfigComponent.class);

        String current = (String) nameComboBox.getSelectedItem();

        component.setCurrent(current);

        host.setText(component.getHost());
        port.setText(component.getPort());
        user.setText(component.getUser());
        password.setText(component.getPassword());
        database.setText(component.getDatabase());

        DatasourceComponent datasourceComponent = project.getService(DatasourceComponent.class);
        datasourceComponent.updateDatasource();

        backgroundTaskQueue.run(new Task.Backgroundable(project, Constant.APPLICATION_NAME) {
            @Override
            public void run(@NotNull ProgressIndicator indicator) {
                String connectionInfo = SqlExecutor.testConnected(project);
                ApplicationManager.getApplication().invokeLater(() -> testResult.setText(connectionInfo));
            }
        });
    }

    private void updateDatasourceForPersistent() {

        DatasourceConfigComponent component = ApplicationManager.getApplication().getComponent(DatasourceConfigComponent.class);

        DatasourceComponent datasourceComponent = project.getService(DatasourceComponent.class);

        String name = "";
        if (nameText.isVisible()) {
            name = nameText.getText();
        } else if (nameComboBox.isVisible()) {
            name = (String) nameComboBox.getSelectedItem();
        }

        component.setCurrent(name);

        DatasourceConfiguration configuration = component.getConfig();

        if (configuration == null) {
            configuration = new DatasourceConfiguration();
            component.addDatasourceConfiguration(configuration);
        }

        configuration.name(name).host(host.getText()).port(port.getText()).user(user.getText()).password(String.valueOf(password.getPassword())).database(database.getText());

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

    private void displayNameFieldText() {
        // 隐藏combobox, 显示text field用于创建新的数据源
        host.setText(StringUtils.EMPTY);
        port.setText(StringUtils.EMPTY);
        user.setText(StringUtils.EMPTY);
        password.setText(StringUtils.EMPTY);
        database.setText(StringUtils.EMPTY);

        nameText.setVisible(true);
        nameComboBox.setVisible(false);
        namePanel.remove(nameComboBox);
        namePanel.add(nameText);
        nameText.setText("");
    }

    private void displayNameComboBox() {

        DatasourceConfigComponent component = ApplicationManager.getApplication().getComponent(DatasourceConfigComponent.class);

        List<String> datasourceNames = component.getAllDatasourceNames();
        nameComboBox.removeAllItems();
        for (String name : datasourceNames) {
            nameComboBox.addItem(name);
        }
        nameComboBox.setSelectedItem(component.getName());

        namePanel.remove(nameText);
        namePanel.add(nameComboBox);

        nameText.setVisible(false);
        nameComboBox.setVisible(true);

    }
}
