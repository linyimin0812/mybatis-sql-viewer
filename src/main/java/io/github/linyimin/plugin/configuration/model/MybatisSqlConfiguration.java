package io.github.linyimin.plugin.configuration.model;

import com.intellij.psi.PsiElement;
import org.apache.commons.lang3.StringUtils;

/**
 * @author yiminlin
 * @date 2022/02/02 1:35 上午
 **/
public class MybatisSqlConfiguration {

    private PsiElement psiElement;

    private String method;
    private String params;
    private String sql;
    private boolean defaultParams;
    private boolean updateSql = true;

    public String getMethod() {
        return method;
    }

    public void setMethod(String method) {
        this.method = method;
    }

    public String getParams() {
        return params;
    }

    public void setParams(String params) {
        this.params = params;
    }

    public String getSql() {
        return sql;
    }

    public void setSql(String sql) {
        this.sql = sql;
    }

    public PsiElement getPsiElement() {
        return psiElement;
    }

    public void setPsiElement(PsiElement psiElement) {
        this.psiElement = psiElement;
    }

    public boolean isDefaultParams() {
        return defaultParams;
    }

    public void setDefaultParams(boolean defaultParams) {
        this.defaultParams = defaultParams;
    }

    public void reset() {
        method = StringUtils.EMPTY;
        params = StringUtils.EMPTY;
        sql = StringUtils.EMPTY;
        psiElement = null;
        defaultParams = false;
        updateSql = true;
    }

    public boolean isUpdateSql() {
        return updateSql;
    }

    public void setUpdateSql(boolean updateSql) {
        this.updateSql = updateSql;
    }
}
