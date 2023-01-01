package io.github.linyimin.plugin.provider.generate;

import com.intellij.codeInsight.daemon.GutterIconNavigationHandler;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.wm.ToolWindow;
import com.intellij.openapi.wm.ToolWindowManager;
import com.intellij.psi.PsiElement;
import com.intellij.ui.content.Content;
import com.intellij.util.messages.MessageBus;
import io.github.linyimin.plugin.message.ConfigChangeNotifier;
import io.github.linyimin.plugin.configuration.MybatisSqlStateComponent;
import io.github.linyimin.plugin.component.SqlParamGenerateComponent;
import io.github.linyimin.plugin.configuration.model.MybatisSqlConfiguration;
import io.github.linyimin.plugin.pojo2json.POJO2JSONParserFactory;

import java.awt.event.MouseEvent;
import java.util.Objects;

/**
 * @author yiminlin
 * @date 2022/02/01 7:28 下午
 **/
public class SqlGenerateNavigationHandler implements GutterIconNavigationHandler<PsiElement> {

    @Override
    public void navigate(MouseEvent e, PsiElement elt) {

        ToolWindow mybatisSqlViewerToolWindow = ToolWindowManager.getInstance(elt.getProject()).getToolWindow("mybatis-sql-viewer");

        if (Objects.isNull(mybatisSqlViewerToolWindow)) {
            return;
        }

        // clear cache
        resetConfig(elt.getProject());

        if (!mybatisSqlViewerToolWindow.isActive()) {

            SqlParamGenerateComponent.generate(elt, POJO2JSONParserFactory.RANDOM_POJO_2_JSON_PARSER, true);

            activateWindow(mybatisSqlViewerToolWindow);
            notifyParamChange(elt.getProject());
        }
    }

    private void resetConfig(Project project) {
        MybatisSqlConfiguration config = project.getService(MybatisSqlStateComponent.class).getConfiguration();
        assert config != null;

        config.reset();
    }

    /**
     * activate window
     * @param toolWindow {@link ToolWindow}
     */
    private void activateWindow(ToolWindow toolWindow) {
        toolWindow.setAutoHide(false);
        toolWindow.activate(null);
        Content content = toolWindow.getContentManager().getContent(0);
        assert content != null;
        toolWindow.getContentManager().setSelectedContent(content);
    }
    /**
     * send message to change param
     * @param project {@link Project}
     */
    private void notifyParamChange(Project project) {
        MessageBus messageBus = project.getMessageBus();
        messageBus.connect();
        ConfigChangeNotifier notifier = messageBus.syncPublisher(ConfigChangeNotifier.PARAM_CHANGE_TOPIC);
        notifier.configChanged();
    }

}
