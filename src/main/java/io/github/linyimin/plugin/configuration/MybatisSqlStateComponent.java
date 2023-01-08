package io.github.linyimin.plugin.configuration;

import io.github.linyimin.plugin.configuration.model.MybatisSqlConfiguration;
import org.apache.commons.lang3.StringUtils;

import java.util.Objects;

/**
 * @author yiminlin
 * @date 2022/02/02 1:29 上午
 **/
public class MybatisSqlStateComponent {

    private MybatisSqlConfiguration config;

    public MybatisSqlConfiguration getConfiguration() {

        if (Objects.isNull(config)) {
            config = new MybatisSqlConfiguration();
            config.setMethod(StringUtils.EMPTY);
            config.setParams(StringUtils.EMPTY);
            config.setSql(StringUtils.EMPTY);
        }

        return config;
    }
}
