package io.github.linyimin.plugin.ui;

import com.intellij.notification.NotificationGroup;
import com.intellij.notification.NotificationType;
import com.intellij.openapi.project.Project;
import io.github.linyimin.plugin.constant.Constant;

/**
 * @author banzhe
 * @date 2022/12/30 17:56
 **/
public class Notifier {

    private static final NotificationGroup GROUP = NotificationGroup.balloonGroup(Constant.APPLICATION_NAME);


    public static void notifyWarning(Project project, String title, String message) {
        GROUP.createNotification(NotificationType.WARNING).setTitle(title).setContent(message).notify(project);
    }


    public static void notifyError(Project project, String title, String message) {
        GROUP.createNotification(NotificationType.ERROR).setTitle(title).setContent(message).notify(project);
    }
}
