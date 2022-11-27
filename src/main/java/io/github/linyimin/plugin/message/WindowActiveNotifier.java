package io.github.linyimin.plugin.message;

import com.intellij.util.messages.Topic;

import static io.github.linyimin.plugin.constant.Constant.APPLICATION_NAME;

/**
 * @author banzhe
 * @date 2022/11/26 21:28
 **/
public class WindowActiveNotifier implements ConfigChangeNotifier {

    Topic<ConfigChangeNotifier> PARAM_CHANGE_TOPIC = Topic.create(String.format("%s_window_active", APPLICATION_NAME), ConfigChangeNotifier.class);



}
