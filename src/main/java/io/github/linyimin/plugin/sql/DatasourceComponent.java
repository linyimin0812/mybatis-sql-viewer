package io.github.linyimin.plugin.sql;

import com.alibaba.druid.pool.DruidDataSource;
import com.alibaba.druid.pool.DruidDataSourceFactory;
import com.intellij.openapi.project.Project;
import io.github.linyimin.plugin.configuration.MybatisDatasourceStateComponent;
import io.github.linyimin.plugin.constant.Constant;

import java.sql.Connection;
import java.util.Properties;

/**
 * @author banzhe
 * @date 2022/11/26 21:48
 **/
public class DatasourceComponent {

    private final Project project;

    public DatasourceComponent(Project project) {
        this.project = project;
    }

    private DruidDataSource dataSource;

    public Connection getConnection() throws Exception {
        if (dataSource == null) {
            dataSource = createDatasource(project);
        }

        try {
            return dataSource.getConnection(1000);
        } catch (Exception e) {
            dataSource.close();
            throw e;
        }
    }

    public void updateDatasource() {
        try {
            if (dataSource != null) {
                dataSource.close();
            }
            dataSource = createDatasource(project);
        } catch (Exception ignored) {
            if (dataSource != null) {
                dataSource.close();
            }
        }

    }

    private DruidDataSource createDatasource(Project project) throws Exception {

        MybatisDatasourceStateComponent component = project.getComponent(MybatisDatasourceStateComponent.class);

        Properties properties = new Properties();

        String url = String.format(Constant.DATABASE_URL_TEMPLATE, component.getHost(), component.getPort(), component.getDatabase());
        properties.put(DruidDataSourceFactory.PROP_URL, url);
        properties.put(DruidDataSourceFactory.PROP_USERNAME, component.getUser());
        properties.put(DruidDataSourceFactory.PROP_PASSWORD, component.getPassword());
        properties.put(DruidDataSourceFactory.PROP_DRIVERCLASSNAME, "com.mysql.cj.jdbc.Driver");

        return (DruidDataSource) DruidDataSourceFactory.createDataSource(properties);

    }

}
