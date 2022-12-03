package io.github.linyimin.plugin.configuration.model;

import java.util.ArrayList;
import java.util.List;

/**
 * @author banzhe
 * @date 2022/12/02 20:31
 **/
public class DatasourceConfig4Save {

    private String current;
    private List<DatasourceConfiguration> configurations = new ArrayList<>();

    public String getCurrent() {
        return current;
    }

    public void setCurrent(String current) {
        this.current = current;
    }

    public List<DatasourceConfiguration> getConfigurations() {
        return configurations;
    }

    public void setConfigurations(List<DatasourceConfiguration> configurations) {
        this.configurations = configurations;
    }

}
