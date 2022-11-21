package io.github.linyimin.plugin.service;

import com.alibaba.fastjson.JSON;
import com.google.common.collect.Lists;
import com.intellij.openapi.project.Project;
import com.intellij.psi.*;
import com.intellij.psi.xml.XmlTag;
import com.intellij.psi.xml.XmlToken;
import io.github.linyimin.plugin.cache.MybatisXmlContentCache;
import io.github.linyimin.plugin.constant.Constant;
import io.github.linyimin.plugin.pojo2json.POJO2JSONParser;
import io.github.linyimin.plugin.pojo2json.POJO2JSONParserFactory;
import io.github.linyimin.plugin.provider.MapperXmlProcessor;
import io.github.linyimin.plugin.service.model.MybatisSqlConfiguration;
import io.github.linyimin.plugin.utils.MybatisSqlUtils;
import org.apache.commons.lang3.StringUtils;

import java.util.*;

/**
 * @author yiminlin
 * @date 2022/02/02 2:09 上午
 **/
public class SqlParamGenerateService {

    private final POJO2JSONParser parser = POJO2JSONParserFactory.RANDOM_POJO_2_JSON_PARSER;

    public void generate(PsiElement psiElement) {
        updateMybatisSqlConfig(psiElement);
    }

    public String generateSql(Project project, String methodQualifiedName, String params) {

        String namespace = methodQualifiedName.substring(0, methodQualifiedName.lastIndexOf("."));

        Optional<String> optional = MybatisXmlContentCache.acquireByNamespace(project, namespace).stream().map(XmlTag::getText).findFirst();

        if (!optional.isPresent()) {
            return "Oops! The plugin can't find the mapper file.";
        }

        return MybatisSqlUtils.getSql(optional.get(), methodQualifiedName, params);

    }

    private void updateMybatisSqlConfig(PsiElement psiElement) {

        MybatisSqlConfiguration sqlConfig = psiElement.getProject().getService(MybatisSqlStateComponent.class).getState();
        assert sqlConfig != null;

        PsiMethod psiMethod = null;

        if (psiElement instanceof PsiIdentifier && psiElement.getParent() instanceof PsiMethod) {

            psiMethod = (PsiMethod) psiElement.getParent();

        }

        String statementId = "";

        if (psiElement instanceof XmlToken && psiElement.getParent() instanceof XmlTag) {
            List<PsiMethod> methods = MapperXmlProcessor.processMapperMethod(psiElement.getParent());
            psiMethod = methods.stream().findFirst().orElse(null);

            statementId = MapperXmlProcessor.acquireStatementId(psiElement.getParent());
        }

        // 设置缓存, method qualified name and params
        if (Objects.nonNull(psiMethod)) {
            sqlConfig.setMethod(generateMethod(psiMethod));

            String params = generateMethodParam(psiMethod);
            sqlConfig.setParams(params);
        } else if (statementId != null) {
            // 找不到对应的接口方法
            sqlConfig.setMethod(statementId);
            sqlConfig.setParams("{}");
        }
    }

    private String generateMethod(PsiMethod method) {
        PsiClass psiClass = method.getContainingClass();
        assert psiClass != null;

        String methodName = method.getName();
        String qualifiedName = psiClass.getQualifiedName();
        return qualifiedName + "." + methodName;

    }

    private String generateMethodParam(PsiMethod method) {
        List<ParamNameType> paramNameTypes = getMethodBodyParamList(method);
        return parseParamNameTypeList(paramNameTypes);
    }

    /**
     * 将{@link ParamNameType} 列表转成json字符串
     * @param paramNameTypes {@link ParamNameType}
     * @return json字符串
     */
    private String parseParamNameTypeList(List<ParamNameType> paramNameTypes) {

        Map<String, Object> params = new HashMap<>();
        for (ParamNameType paramNameType : paramNameTypes) {
            PsiType type = paramNameType.psiType;
            String name = paramNameType.name;

            Object value = parser.parseFieldValueType(type, 0, new ArrayList<>(), new HashMap<>());

            if (value instanceof Map && paramNameTypes.size() == 1) {
                params.putAll((Map) value);
            } else {
                params.put(name, value);
            }
        }

        return JSON.toJSONString(params, true);
    }

    /**
     * 获取方法所有参数
     * @param psiMethod {@link PsiMethod}
     * @return param list {@link ParamNameType}
     */
    public List<ParamNameType> getMethodBodyParamList(PsiMethod psiMethod) {
        List<ParamNameType> result = new ArrayList<>();
        PsiParameterList parameterList = psiMethod.getParameterList();
        PsiParameter[] parameters = parameterList.getParameters();
        for (PsiParameter param : parameters) {

            String paramAnnotationValue = getParamAnnotationValue(param);
            String name = StringUtils.isBlank(paramAnnotationValue) ? param.getName() : paramAnnotationValue;

            ParamNameType paramNameType = new ParamNameType(name, param.getType());
            result.add(paramNameType);
        }
        return result;
    }

    /**
     * 获取Param注解的value
     * @param param {@link PsiParameter}
     * @return {org.apache.ibatis.annotations.Param} value的值
     */
    private String getParamAnnotationValue(PsiParameter param) {
        PsiAnnotation annotation = param.getAnnotation(Constant.PARAM_ANNOTATION);
        if (Objects.isNull(annotation)) {
            return StringUtils.EMPTY;
        }
        List<PsiNameValuePair> nameValuePairs = Lists. newArrayList(annotation.getParameterList().getAttributes());

        return nameValuePairs.stream()
                .map(PsiNameValuePair::getLiteralValue)
                .filter(Objects::nonNull)
                .findFirst()
                .orElse(StringUtils.EMPTY);
    }


    static class ParamNameType {
        private final String name;
        private final PsiType psiType;

        public ParamNameType(String name, PsiType psiType) {
            this.name = name;
            this.psiType = psiType;
        }
    }
}
