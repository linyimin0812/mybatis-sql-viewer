package io.github.linyimin.plugin.service;

import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.components.PersistentStateComponent;
import com.intellij.openapi.components.State;
import com.intellij.openapi.components.Storage;
import com.intellij.openapi.project.Project;
import com.intellij.util.xmlb.XmlSerializerUtil;
import io.github.linyimin.plugin.service.model.MybatisSqlConfiguration;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;

/**
 * @author yiminlin
 * @date 2022/02/02 1:29 上午
 **/
@State(name = "mybatis-sql-config", storages = {@Storage("mybatis-sql-config.xml")})
public class MybatisSqlStateComponent implements PersistentStateComponent<MybatisSqlConfiguration> {

    private MybatisSqlConfiguration config;

    @Override
    public @Nullable MybatisSqlConfiguration getState() {

        if (Objects.isNull(config)) {
            config = new MybatisSqlConfiguration();
            config.setMethod(StringUtils.EMPTY);
            config.setParams(StringUtils.EMPTY);
            config.setSql(StringUtils.EMPTY);
            config.setResult(StringUtils.EMPTY);
        }

        return config;
    }

    @Override
    public void loadState(@NotNull MybatisSqlConfiguration state) {
        XmlSerializerUtil.copyBean(state, Objects.requireNonNull(getState()));
    }

}
