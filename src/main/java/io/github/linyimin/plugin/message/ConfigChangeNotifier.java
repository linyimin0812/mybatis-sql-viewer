package io.github.linyimin.plugin.message;

import com.intellij.util.messages.Topic;

import static io.github.linyimin.plugin.constant.Constant.APPLICATION_NAME;

/**
 * @author yiminlin
 * @date 2022/02/02 2:47 上午
 **/
public interface ConfigChangeNotifier {

    Topic<ConfigChangeNotifier> PARAM_CHANGE_TOPIC = Topic.create(String.format("%s_window_active", APPLICATION_NAME), ConfigChangeNotifier.class);
    Topic<ConfigChangeNotifier> SCAN_ICON_CLICK_TOPIC = Topic.create(String.format("%s_scan_icon_click", APPLICATION_NAME), ConfigChangeNotifier.class);
    /**
     */
    default void configChanged() {

    }

    default void configChanged(String config) {

    }
}
