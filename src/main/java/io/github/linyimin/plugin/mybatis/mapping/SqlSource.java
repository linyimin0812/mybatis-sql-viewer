package io.github.linyimin.plugin.mybatis.mapping;

import io.github.linyimin.plugin.component.SqlParamGenerateComponent;

import java.util.List;

/**
 * @author banzhe
 * @date 2022/11/13 14:59
 **/
public interface SqlSource {
    String getSql(List<SqlParamGenerateComponent.ParamNameType> types, Object parameterObject);
}
