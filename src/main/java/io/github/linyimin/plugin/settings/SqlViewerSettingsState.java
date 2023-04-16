package io.github.linyimin.plugin.settings;

import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.components.PersistentStateComponent;
import com.intellij.openapi.components.State;
import com.intellij.openapi.components.Storage;
import com.intellij.util.xmlb.XmlSerializerUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * @author yiminlin
 * @date 2023/04/16 14:18
 **/
@State(name = "io.github.linyimin.plugin.settings.SqlViewerSettingsState", storages = {@Storage("mybatis-sql-config.xml")})
public class SqlViewerSettingsState implements PersistentStateComponent<SqlViewerSettingsState> {

    public boolean fileJumpEnable = true;

    @Override
    public @Nullable SqlViewerSettingsState getState() {
        return this;
    }

    @Override
    public void loadState(@NotNull SqlViewerSettingsState state) {
        XmlSerializerUtil.copyBean(state, this);
    }

    public static SqlViewerSettingsState getInstance() {
        return ApplicationManager.getApplication().getService(SqlViewerSettingsState.class);
    }
}
