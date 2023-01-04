package io.github.linyimin.plugin.configuration;

import com.intellij.openapi.components.PersistentStateComponent;
import com.intellij.openapi.components.State;
import com.intellij.openapi.components.Storage;
import com.intellij.util.xmlb.XmlSerializerUtil;
import io.github.linyimin.plugin.configuration.model.DatasourceConfig4Save;
import io.github.linyimin.plugin.configuration.model.DatasourceConfiguration;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * @author yiminlin
 * @date 2022/02/02 1:29 上午
 **/
@State(name = "datasource", storages = {@Storage("mybatis-sql-config.xml")})
public class DatasourceConfigComponent implements PersistentStateComponent<DatasourceConfig4Save> {

    private DatasourceConfig4Save config;

    @Override
    public @Nullable DatasourceConfig4Save getState() {

        if (Objects.isNull(config)) {

            config = new DatasourceConfig4Save();
            List<DatasourceConfiguration> configurations = new ArrayList<>();
            config.setCurrent(StringUtils.EMPTY);
            config.setConfigurations(configurations);

        }
        return config;
    }

    public DatasourceConfiguration getConfig() {

        if (this.config == null || this.config.getConfigurations() == null) {
            getState();
        }

        return this.config.getConfigurations().stream()
                .filter(temp -> StringUtils.equals(this.config.getCurrent(), temp.getName()))
                .findFirst()
                .orElse(null);
    }

    public void addDatasourceConfiguration(DatasourceConfiguration configuration) {
        this.config.getConfigurations().add(configuration);
    }

    @Override
    public void loadState(@NotNull DatasourceConfig4Save state) {
        XmlSerializerUtil.copyBean(state, Objects.requireNonNull(getState()));
    }

    public void setConfig(DatasourceConfig4Save config) {
        this.config = config;
    }

    public void setCurrent(String name) {
        this.config.setCurrent(name);
    }

    public String getHost() {
        if (this.getConfig() == null) {
            return StringUtils.EMPTY;
        }
        return this.getConfig().getHost();
    }

    public String getPort() {
        if (this.getConfig() == null) {
            return StringUtils.EMPTY;
        }
        return this.getConfig().getPort();
    }

    public String getUser() {
        if (this.getConfig() == null) {
            return StringUtils.EMPTY;
        }
        return this.getConfig().getUser();
    }

    public String getPassword() {
        if (this.getConfig() == null) {
            return StringUtils.EMPTY;
        }
        return this.getConfig().getPassword();
    }

    public String getDatabase() {
        if (this.getConfig() == null) {
            return StringUtils.EMPTY;
        }
        return this.getConfig().getDatabase();
    }

    public String getName() {
        if (this.getConfig() == null) {
            return StringUtils.EMPTY;
        }
        return this.getConfig().getName();
    }

    public List<String> getAllDatasourceNames() {
        return this.config.getConfigurations().stream().map(DatasourceConfiguration::getName).collect(Collectors.toList());
    }

    public void remove() {
        Optional<DatasourceConfiguration> optional = this.config.getConfigurations().stream()
                .filter(temp -> StringUtils.equals(this.config.getCurrent(), temp.getName()))
                .findFirst();

        if (!optional.isPresent()) {
            return;
        }
        List<DatasourceConfiguration> configurations = this.config.getConfigurations();
        configurations.remove(optional.get());
        if (configurations.size() > 0) {
            this.config.setCurrent(configurations.get(0).getName());
        } else {
            this.config.setCurrent(StringUtils.EMPTY);
        }
    }
}
