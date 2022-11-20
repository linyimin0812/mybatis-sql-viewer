package io.github.linyimin.plugin.mybatis.mapping;

/**
 * @author banzhe
 * @date 2022/11/13 14:59
 **/
public interface SqlSource {
    String getSql(Object parameterObject);
}
