package io.github.linyimin.plugin.ui;

import com.intellij.openapi.project.Project;
import com.intellij.util.ui.JBUI;
import io.github.linyimin.plugin.component.SqlParamGenerateComponent;
import io.github.linyimin.plugin.configuration.GlobalConfig;
import io.github.linyimin.plugin.configuration.MybatisSqlStateComponent;
import io.github.linyimin.plugin.configuration.model.MybatisSqlConfiguration;
import io.github.linyimin.plugin.pojo2json.POJO2JSONParserFactory;
import org.apache.commons.lang3.StringUtils;
import org.fife.ui.rsyntaxtextarea.RSyntaxTextArea;
import org.fife.ui.rtextarea.RTextScrollPane;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.Arrays;
import java.util.List;

/**
 * @author banzhe
 * @date 2022/12/03 16:44
 **/
public class ParamTabbedPane implements TabbedChangeListener {

    private JTabbedPane paramTabbedPanel;

    private JPanel randomParamPanel;
    private JPanel defaultParamPanel;

    private RTextScrollPane defaultParamsScroll;
    private RTextScrollPane randomParamsScroll;

    private RSyntaxTextArea defaultParamsText;
    private RSyntaxTextArea randomParamsText;

    private final Project project;

    public ParamTabbedPane(Project project) {
        this.project = project;

        initParamPanel();
        setScrollUnitIncrement();

        this.paramTabbedPanel.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent event) {
                if (!GlobalConfig.isMybatisMode || event.getClickCount() != 2 || paramTabbedPanel.indexAtLocation(event.getX(), event.getY()) < 0) {
                    return;
                }
                paramTabbedPanelListener(true);
            }
        });

    }

    public JTabbedPane getParamTabbedPanel() {
        return paramTabbedPanel;
    }

    public JPanel getRandomParamPanel() {
        return randomParamPanel;
    }

    public JPanel getDefaultParamPanel() {
        return defaultParamPanel;
    }

    public RSyntaxTextArea getDefaultParamsText() {
        return defaultParamsText;
    }

    public RSyntaxTextArea getRandomParamsText() {
        return randomParamsText;
    }

    private void initParamPanel() {

        defaultParamsText = CustomTextField.createArea("json");
        randomParamsText = CustomTextField.createArea("json");

        defaultParamPanel.setLayout(new BorderLayout());
        randomParamPanel.setLayout(new BorderLayout());

        defaultParamsScroll = new RTextScrollPane(defaultParamsText);
        defaultParamsScroll.setBorder(new EmptyBorder(JBUI.emptyInsets()));

        randomParamsScroll = new RTextScrollPane(randomParamsText);
        randomParamsScroll.setBorder(new EmptyBorder(JBUI.emptyInsets()));

        randomParamPanel.add(randomParamsScroll);
        defaultParamPanel.add(defaultParamsScroll);

        paramTabbedPanel.addChangeListener(e -> paramTabbedPanelListener(false));

        addParamsTextListener();

    }

    private void setScrollUnitIncrement() {

        int unit = 16;

        this.defaultParamsScroll.getVerticalScrollBar().setUnitIncrement(unit);
        this.defaultParamsScroll.getHorizontalScrollBar().setUnitIncrement(unit);

        this.randomParamsScroll.getVerticalScrollBar().setUnitIncrement(unit);
        this.randomParamsScroll.getHorizontalScrollBar().setUnitIncrement(unit);

    }

    private void addParamsTextListener() {

        List<RSyntaxTextArea> areaList = Arrays.asList(defaultParamsText, randomParamsText);

        for (RSyntaxTextArea area : areaList) {
            area.getDocument().addDocumentListener(new DocumentListener() {
                @Override
                public void insertUpdate(DocumentEvent e) {
                    updateParams();
                }

                @Override
                public void removeUpdate(DocumentEvent e) {
                    updateParams();
                }

                @Override
                public void changedUpdate(DocumentEvent e) {
                    updateParams();
                }

                private void updateParams() {
                    MybatisSqlConfiguration config = project.getService(MybatisSqlStateComponent.class).getConfiguration();
                    config.setParams(area.getText());
                }
            });
        }
    }

    private void paramTabbedPanelListener(boolean forceUpdate) {

        int selectedIndex = paramTabbedPanel.getSelectedIndex();

        MybatisSqlConfiguration configuration = project.getService(MybatisSqlStateComponent.class).getConfiguration();

        // 获取参数默认值
        if (selectedIndex == ParamComponentType.default_param.index) {

            if (forceUpdate || StringUtils.isBlank(configuration.getParams()) || !configuration.isDefaultParams()) {
                SqlParamGenerateComponent.generate(configuration.getPsiElement(), POJO2JSONParserFactory.DEFAULT_POJO_2_JSON_PARSER);
            }
            defaultParamsText.setText(configuration.getParams());
        }

        // 获取参数随机值
        if (selectedIndex == ParamComponentType.random_param.index) {
            if (forceUpdate || StringUtils.isBlank(configuration.getParams()) || configuration.isDefaultParams()) {
                SqlParamGenerateComponent.generate(configuration.getPsiElement(), POJO2JSONParserFactory.RANDOM_POJO_2_JSON_PARSER);
            }

            randomParamsText.setText(configuration.getParams());
        }
    }

    @Override
    public void listen() {
        this.paramTabbedPanel.setSelectedIndex(0);
    }

    private enum ParamComponentType {
        /**
         * Tanned类型对应的index
         */
        random_param(0),
        default_param(1);

        private final int index;

        ParamComponentType(int index) {
            this.index = index;
        }

    }
}
