package io.github.linyimin.plugin.configuration.model;

/**
 * @author yiminlin
 * @date 2022/02/02 1:31 上午
 **/
public class DatasourceConfiguration {

    private String name;
    private String host;
    private String port;
    private String user;
    private String password;
    private String database;

    public DatasourceConfiguration name(String name) {
        this.name = name;
        return this;
    }

    public DatasourceConfiguration host(String host) {
        this.host = host;
        return this;
    }

    public DatasourceConfiguration port(String port) {
        this.port = port;
        return this;
    }

    public DatasourceConfiguration user(String user) {
        this.user = user;
        return this;
    }

    public DatasourceConfiguration password(String password) {
        this.password = password;
        return this;
    }

    public DatasourceConfiguration database(String database) {
        this.database = database;
        return this;
    }

    public String getHost() {
        return host;
    }

    public void setHost(String host) {
        this.host = host;
    }

    public String getPort() {
        return port;
    }

    public void setPort(String port) {
        this.port = port;
    }

    public String getUser() {
        return user;
    }

    public void setUser(String user) {
        this.user = user;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getDatabase() {
        return database;
    }

    public void setDatabase(String database) {
        this.database = database;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
