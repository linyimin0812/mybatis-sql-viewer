package io.github.linyimin.plugin.service;

import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.components.PersistentStateComponent;
import com.intellij.openapi.components.State;
import com.intellij.openapi.components.Storage;
import com.intellij.util.xmlb.XmlSerializerUtil;
import io.github.linyimin.plugin.service.model.DatasourceConfiguration;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;

/**
 * @author yiminlin
 * @date 2022/02/02 1:29 上午
 **/
@State(name = "datasource", storages = {@Storage("datasource.xml")})
public class MybatisDatasourceStateComponent implements PersistentStateComponent<DatasourceConfiguration> {

    private DatasourceConfiguration config;

    @Override
    public @Nullable DatasourceConfiguration getState() {

        if (Objects.isNull(config)) {
            config = new DatasourceConfiguration();
            config.setHost("localhost");
            config.setPort("3306");
            config.setUser("root");
            config.setPassword(StringUtils.EMPTY);
            config.setDatabase(StringUtils.EMPTY);
        }
        return config;
    }

    @Override
    public void loadState(@NotNull DatasourceConfiguration state) {
        XmlSerializerUtil.copyBean(state, Objects.requireNonNull(getState()));
    }

    public static MybatisDatasourceStateComponent getInstance() {
        return ApplicationManager.getApplication().getService(MybatisDatasourceStateComponent.class);
    }
}
