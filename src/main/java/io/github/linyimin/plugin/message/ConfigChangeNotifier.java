package io.github.linyimin.plugin.message;

import com.intellij.openapi.project.Project;
import com.intellij.util.messages.Topic;

/**
 * @author yiminlin
 * @date 2022/02/02 2:47 上午
 **/
public interface ConfigChangeNotifier {

    Topic<ConfigChangeNotifier> PARAM_CHANGE_TOPIC = Topic.create("param change", ConfigChangeNotifier.class);

    /**
     * @param project {@link Project}
     */
    default void configChanged(Project project) {

    }
}
