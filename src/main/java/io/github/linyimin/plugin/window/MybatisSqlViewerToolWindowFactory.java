package io.github.linyimin.plugin.window;

import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.project.DumbAware;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.wm.ToolWindow;
import com.intellij.openapi.wm.ToolWindowFactory;
import com.intellij.ui.content.Content;
import com.intellij.ui.content.ContentFactory;
import com.intellij.util.messages.MessageBus;
import com.intellij.util.messages.MessageBusConnection;
import io.github.linyimin.plugin.message.ConfigChangeNotifier;
import io.github.linyimin.plugin.view.MybatisSqlViewerToolWindow;
import org.jetbrains.annotations.NotNull;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author yiminlin
 * @date 2022/02/01 11:28 上午
 **/
public class MybatisSqlViewerToolWindowFactory implements ToolWindowFactory, DumbAware {

    private final Map<Project, MybatisSqlViewerToolWindow> windowMap = new ConcurrentHashMap<>();

    @Override
    public void createToolWindowContent(@NotNull Project project, @NotNull ToolWindow toolWindow) {
        MybatisSqlViewerToolWindow  mybatisSqlViewerToolWindow = new MybatisSqlViewerToolWindow(toolWindow, project);
        ContentFactory contentFactory = ApplicationManager.getApplication().getService(ContentFactory.class);
        Content content = contentFactory.createContent(mybatisSqlViewerToolWindow.getContent(), "", false);
        toolWindow.getContentManager().addContent(content);

        windowMap.put(project, mybatisSqlViewerToolWindow);

        subscribeParamChange(project);

    }

    private void subscribeParamChange(Project project) {
        MessageBus messageBus = project.getMessageBus();
        MessageBusConnection connect = messageBus.connect();
        connect.subscribe(ConfigChangeNotifier.PARAM_CHANGE_TOPIC, new ConfigChangeNotifier() {
            @Override
            public void configChanged(Project project1) {
                windowMap.get(project1).refresh(project1);
            }
        });
    }

}
