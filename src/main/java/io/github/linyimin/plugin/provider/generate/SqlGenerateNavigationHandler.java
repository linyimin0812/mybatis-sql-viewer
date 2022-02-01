package io.github.linyimin.plugin.provider.generate;

import com.intellij.codeInsight.daemon.GutterIconNavigationHandler;
import com.intellij.openapi.wm.ToolWindow;
import com.intellij.openapi.wm.ToolWindowManager;
import com.intellij.psi.PsiElement;
import com.intellij.ui.content.Content;
import io.github.linyimin.plugin.window.MybatisSqlViewerToolWindowFactory;

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

        mybatisSqlViewerToolWindow.setAutoHide(true);

        if (!mybatisSqlViewerToolWindow.isActive()) {
            mybatisSqlViewerToolWindow.activate(null);
            Content content = mybatisSqlViewerToolWindow.getContentManager().getContent(0);
            assert content != null;
            mybatisSqlViewerToolWindow.getContentManager().setSelectedContent(content);
        }
    }
}
