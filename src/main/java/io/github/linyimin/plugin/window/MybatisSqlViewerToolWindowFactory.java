package io.github.linyimin.plugin.window;

import com.intellij.openapi.project.DumbAware;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.wm.ToolWindow;
import com.intellij.openapi.wm.ToolWindowFactory;
import com.intellij.ui.content.Content;
import com.intellij.ui.content.ContentFactory;
import io.github.linyimin.plugin.view.MybatisSqlViewerToolWindow;
import org.jetbrains.annotations.NotNull;

/**
 * @author yiminlin
 * @date 2022/02/01 11:28 上午
 **/
public class MybatisSqlViewerToolWindowFactory implements ToolWindowFactory, DumbAware {

    @Override
    public void createToolWindowContent(@NotNull Project project, @NotNull ToolWindow toolWindow) {
        MybatisSqlViewerToolWindow  mybatisSqlViewerToolWindow = new MybatisSqlViewerToolWindow();
        ContentFactory contentFactory = ContentFactory.SERVICE.getInstance();
        Content content = contentFactory.createContent(mybatisSqlViewerToolWindow.getContent(), "", false);
        toolWindow.getContentManager().addContent(content);
    }

}
