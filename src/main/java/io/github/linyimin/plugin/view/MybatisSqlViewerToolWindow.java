package io.github.linyimin.plugin.view;

import javax.swing.*;

/**
 * @author yiminlin
 * @date 2022/02/01 12:31 下午
 **/
public class MybatisSqlViewerToolWindow {
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

    public JPanel getContent() {
        return root;
    }
}
