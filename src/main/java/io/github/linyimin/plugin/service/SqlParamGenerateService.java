package io.github.linyimin.plugin.service;

import com.alibaba.fastjson.JSON;
import com.google.common.collect.Lists;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.Messages;
import com.intellij.psi.*;
import com.intellij.psi.impl.source.PsiClassReferenceType;
import com.intellij.psi.util.PsiUtil;
import com.intellij.psi.xml.XmlElement;
import com.intellij.psi.xml.XmlTag;
import com.intellij.psi.xml.XmlToken;
import com.siyeh.ig.psiutils.CollectionUtils;
import io.github.linyimin.plugin.compile.MybatisPojoCompile;
import io.github.linyimin.plugin.dom.Constant;
import io.github.linyimin.plugin.dom.model.MybatisConfiguration;
import io.github.linyimin.plugin.provider.MapperXmlProcessor;
import io.github.linyimin.plugin.service.model.MybatisSqlConfiguration;
import io.github.linyimin.plugin.utils.JavaUtils;
import io.github.linyimin.plugin.utils.MapperDomUtils;
import io.github.linyimin.plugin.utils.MybatisSqlUtils;
import org.apache.commons.collections.MapUtils;
import org.apache.commons.lang3.StringUtils;
import java.util.*;

/**
 * @author yiminlin
 * @date 2022/02/02 2:09 上午
 **/
public class SqlParamGenerateService {

    public void generate(PsiElement psiElement) {
        updateMybatisSqlConfig(psiElement);
    }

    public String generateSql(Project project, String methodQualifiedName, String params) {

        List<PsiMethod> psiMethods = JavaUtils.findMethod(project, methodQualifiedName);

        if (org.apache.commons.collections.CollectionUtils.isEmpty(psiMethods)) {
            Messages.showInfoMessage(String.format("method %s is not exist.", methodQualifiedName), Constant.APPLICATION_NAME);
            return StringUtils.EMPTY;
        }

        String mybatisConfig = getMybatisConfiguration(project, psiMethods.get(0));

        boolean isNeedCompile = checkNeedCompile(mybatisConfig, methodQualifiedName, params);

        if (isNeedCompile) {
            MybatisPojoCompile.compile(project);
        }

        return MybatisSqlUtils.getSql(mybatisConfig, methodQualifiedName, params);

    }

    private boolean checkNeedCompile(String mybatisConfig, String methodQualifiedName, String params) {
        if (Objects.isNull(MybatisPojoCompile.classLoader)) {
            return true;
        }

        try {
            MybatisSqlUtils.getSql(mybatisConfig, methodQualifiedName, params);
        } catch (Exception | NoClassDefFoundError e) {
            return true;
        }

        return false;
    }

    private String getMybatisConfiguration(Project project, PsiMethod psiMethod) {

        MybatisConfiguration mybatisConfiguration = MapperDomUtils.findConfiguration(project, psiMethod);
        if (Objects.isNull(mybatisConfiguration)) {
            Messages.showInfoMessage("Mybatis配置文件不存在", Constant.APPLICATION_NAME);
            return StringUtils.EMPTY;
        }

        if (Objects.isNull(mybatisConfiguration.getParent().getXmlElement())) {
            Messages.showInfoMessage("Mybatis配置文件错误", Constant.APPLICATION_NAME);
            return StringUtils.EMPTY;
        }

        String mybatisConfig = mybatisConfiguration.getParent().getXmlElement().getText();

        // 不处理plugins
        XmlElement xmlElement = mybatisConfiguration.getXmlElement();

        if (Objects.nonNull(xmlElement) && xmlElement instanceof XmlTag) {
            XmlTag[] tags = ((XmlTag) xmlElement).findSubTags("plugins");
            if (tags.length > 0) {
                String plugins = tags[0].getText();
                mybatisConfig = mybatisConfig.replace(plugins, StringUtils.EMPTY);
            }
        }

        return mybatisConfig;
    }


    private void updateMybatisSqlConfig(PsiElement psiElement) {

        MybatisSqlConfiguration sqlConfig = psiElement.getProject().getService(MybatisSqlStateComponent.class).getState();
        assert sqlConfig != null;

        PsiMethod psiMethod = null;

        if (psiElement instanceof PsiIdentifier && psiElement.getParent() instanceof PsiMethod) {

            psiMethod = (PsiMethod) psiElement.getParent();

        }

        if (psiElement instanceof XmlToken && psiElement.getParent() instanceof XmlTag) {
            List<PsiMethod> methods = MapperXmlProcessor.processMapperMethod(psiElement.getParent());
            psiMethod = methods.stream().findFirst().orElse(null);
        }

        // 设置缓存, method qualified name and params
        if (Objects.nonNull(psiMethod)) {
            sqlConfig.setMethod(generateMethod(psiMethod));

            String params = generateMethodParam(psiMethod);
            sqlConfig.setParams(params);
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

            PsiClass psiClass = paramNameType.psiClass;
            PsiType type = paramNameType.psiType;
            String name = paramNameType.name;

            Map<String, Object> param = new HashMap<>();

            if (Objects.isNull(psiClass) || isNormalType(psiClass.getQualifiedName())) {
                String paramType;
                if (Objects.isNull(psiClass)) {
                    if (type instanceof PsiArrayType) {
                        paramType = ((PsiArrayType) type).getComponentType().getCanonicalText();
                    } else {
                        paramType = type.getCanonicalText();
                    }
                } else {
                    paramType = psiClass.getQualifiedName();
                }
                param.put(name, getPrimitiveDefaultValue(name, paramType));
            } else {
                Map<String, Object> classParam = getFieldFromClass(psiClass);
                if (paramNameType.isParamAnnotation) {
                    param.put(name, classParam);
                } else {
                    param.putAll(classParam);
                }
            }

            // 数组或者列表
            if (type instanceof PsiArrayType || CollectionUtils.isCollectionClassOrInterface(type)) {
               param.put(name, Lists.newArrayList(param.get(name)));
            }

            params.putAll(param);
        }

        return JSON.toJSONString(params, true);
    }

    private Map<String, Object> getFieldFromClass(PsiClass psiClass) {
        Map<String, Object> param = new HashMap<>();

        if (Objects.isNull(psiClass)) {
            return param;
        }

        for (PsiField field : psiClass.getAllFields()) {
            PsiType type = field.getType();
            String fieldName = field.getName();
            // 1. 基本类型
            if (type instanceof PsiPrimitiveType || isNormalType(type)) {
                Object value = getPrimitiveDefaultValue(fieldName, type);
                param.put(fieldName, value);
                continue;
            }
            // 2. 引用类型

            // 2.1 数组
            if (type instanceof PsiArrayType) {
                PsiType deepType = type.getDeepComponentType();
                List<Object> list = new ArrayList<>();
                // 1. 基本类型
                if (deepType instanceof PsiPrimitiveType || isNormalType(deepType)) {
                    Object value = getPrimitiveDefaultValue(fieldName, deepType);
                    list.add(value);
                } else {
                    PsiClass psiClassInArray = PsiUtil.resolveClassInType(deepType);
                    if (Objects.nonNull(psiClassInArray) && !StringUtils.equals(psiClassInArray.getQualifiedName(), psiClass.getQualifiedName())) {
                        Map<String, Object> temp = getFieldFromClass(psiClassInArray);
                        if (MapUtils.isNotEmpty(temp)) {
                            list.add(temp);
                        }
                    }
                }

                param.put(fieldName, list);

                continue;
            }

            // 2.2 列表
            if (CollectionUtils.isCollectionClassOrInterface(type)) {
                PsiType iterableType = PsiUtil.extractIterableTypeParameter(type, false);
                //无泛型指定
                if (iterableType == null) {
                    continue;
                }

                List<Object> list = new ArrayList<>();

                // 基本类型
                if (iterableType instanceof PsiPrimitiveType || isNormalType(iterableType)) {
                    Object value = getPrimitiveDefaultValue(fieldName, iterableType);
                    list.add(value);
                } else {
                    PsiClass iterableClass = PsiUtil.resolveClassInClassTypeOnly(iterableType);
                    if (Objects.nonNull(iterableClass) && !StringUtils.equals(iterableClass.getQualifiedName(), psiClass.getQualifiedName())) {
                        Map<String, Object> temp = getFieldFromClass(iterableClass);
                        if (MapUtils.isNotEmpty(temp)) {
                            list.add(temp);
                        }
                    }
                }

                param.put(fieldName, list);
                continue;

            }

            // class
            PsiClass typePsiClass = PsiUtil.resolveClassInType(type);
            if (Objects.nonNull(typePsiClass) && StringUtils.equals(typePsiClass.getQualifiedName(), psiClass.getQualifiedName())) {
                param.put(fieldName, new HashMap<>());
            } else {
                Map<String, Object> temp = getFieldFromClass(typePsiClass);
                if (MapUtils.isNotEmpty(temp)) {
                    param.put(fieldName, temp);
                }
            }


        }

        return param;

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

            PsiClass psiClass = null;
            if (CollectionUtils.isCollectionClassOrInterface(param.getType())) {
                PsiClassReferenceType t = (PsiClassReferenceType) PsiUtil.extractIterableTypeParameter(param.getType(), false);
                if (t != null) {
                    psiClass = t.resolve();
                }
            } else {
                psiClass = PsiUtil.resolveClassInType(param.getType());
            }

            if (Objects.isNull(psiClass)) {
                continue;
            }

            String paramAnnotationValue = getParamAnnotationValue(param);
            String name = StringUtils.isBlank(paramAnnotationValue) ? param.getName() : paramAnnotationValue;

            ParamNameType paramNameType = new ParamNameType(name, psiClass, param.getType(), StringUtils.isNotEmpty(paramAnnotationValue));
            result.add(paramNameType);
        }
        return result;
    }

    /**
     * 获取Param注解的value
     * @param param {@link PsiParameter}
     * @return {@link org.apache.ibatis.annotations.Param} value的值
     */
    private String getParamAnnotationValue(PsiParameter param) {
        PsiAnnotation annotation = param.getAnnotation(Constant.PARAM_ANNOTATION);
        if (Objects.isNull(annotation)) {
            return StringUtils.EMPTY;
        }
        List<PsiNameValuePair> nameValuePairs = Lists. newArrayList(annotation.getParameterList().getAttributes());

        return nameValuePairs.stream()
                .map(PsiNameValuePair::getLiteralValue)
                .findFirst()
                .orElse(StringUtils.EMPTY);
    }

    private Object getPrimitiveDefaultValue(String name, PsiType type) {
        return getPrimitiveDefaultValue(name, type.getCanonicalText());
    }

    private Object getPrimitiveDefaultValue(String name, String type) {

        if (!Constant.normalTypes.containsKey(type)) {
            return null;
        }

        if (StringUtils.equals(String.class.getName(), type)) {
            String value = StringUtils.substring(UUID.randomUUID().toString(), 0, 8);
            return name + "_" + value;
        }

        return Constant.normalTypes.get(type);
    }

    private boolean isNormalType(PsiType type) {
        String fieldTypeName = type.getCanonicalText();
        return Constant.normalTypes.containsKey(fieldTypeName);
    }

    private boolean isNormalType(String type) {
        return Constant.normalTypes.containsKey(type);
    }

    static class ParamNameType {
        private final String name;
        private final PsiClass psiClass;
        private final PsiType psiType;
        private final boolean isParamAnnotation;

        public ParamNameType(String name, PsiClass psiClass, PsiType psiType, boolean isParamAnnotation) {
            this.name = name;
            this.psiClass = psiClass;
            this.psiType = psiType;
            this.isParamAnnotation = isParamAnnotation;
        }
    }

}
