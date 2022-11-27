package io.github.linyimin.plugin.component;

import com.google.common.collect.Lists;
import com.google.gson.GsonBuilder;
import com.intellij.openapi.project.Project;
import com.intellij.psi.*;
import com.intellij.psi.xml.XmlTag;
import com.intellij.psi.xml.XmlToken;
import io.github.linyimin.plugin.ProcessResult;
import io.github.linyimin.plugin.cache.MybatisXmlContentCache;
import io.github.linyimin.plugin.configuration.MybatisSqlStateComponent;
import io.github.linyimin.plugin.constant.Constant;
import io.github.linyimin.plugin.mybatis.mapping.SqlSource;
import io.github.linyimin.plugin.mybatis.xml.XMLLanguageDriver;
import io.github.linyimin.plugin.mybatis.xml.XMLMapperBuilder;
import io.github.linyimin.plugin.pojo2json.POJO2JSONParser;
import io.github.linyimin.plugin.provider.MapperXmlProcessor;
import io.github.linyimin.plugin.configuration.model.MybatisSqlConfiguration;
import io.github.linyimin.plugin.utils.JavaUtils;
import org.apache.commons.io.Charsets;
import org.apache.commons.lang3.StringUtils;

import java.io.ByteArrayInputStream;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.nio.charset.Charset;
import java.util.*;

import static io.github.linyimin.plugin.constant.Constant.MYBATIS_SQL_ANNOTATIONS;

/**
 * @author yiminlin
 * @date 2022/02/02 2:09 上午
 **/
public class SqlParamGenerateComponent {

    public static void generate(PsiElement psiElement, POJO2JSONParser parser) {

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

        MybatisSqlConfiguration sqlConfig = psiElement.getProject().getService(MybatisSqlStateComponent.class).getConfiguration();

        sqlConfig.setPsiElement(psiElement);

        // 设置缓存, method qualified name and params
        if (Objects.nonNull(psiMethod)) {

            sqlConfig.setMethod(acquireMethodName(psiMethod));

            sqlConfig.setParams(generateMethodParam(psiMethod, parser));
        } else if (statementId != null) {
            // 找不到对应的接口方法
            sqlConfig.setMethod(statementId);
            sqlConfig.setParams("{}");
        }

    }

    public static String generateSql(Project project, String methodQualifiedName, String params, boolean isTemplate) {

        ProcessResult<String> processResult = getSqlFromAnnotation(project, methodQualifiedName, params, isTemplate);

        if (processResult.isSuccess()) {
            return processResult.getData();
        }

        processResult = getSqlFromXml(project, methodQualifiedName, params, isTemplate);

        return processResult.isSuccess() ? processResult.getData() : processResult.getErrorMsg();

    }

    private static ProcessResult<String> getSqlFromAnnotation(Project project, String qualifiedMethod, String params, boolean isTemplate) {
        // 处理annotation
        String clazzName = qualifiedMethod.substring(0, qualifiedMethod.lastIndexOf("."));
        String methodName = qualifiedMethod.substring(qualifiedMethod.lastIndexOf(".") + 1);

        List<PsiMethod> psiMethods = JavaUtils.findMethod(project, clazzName, methodName);

        if (psiMethods.isEmpty()) {
            return ProcessResult.fail("annotation is not exist.");
        }

        PsiAnnotation annotation = Arrays.stream(psiMethods.get(0).getAnnotations())
                .filter(psiAnnotation -> MYBATIS_SQL_ANNOTATIONS.contains(psiAnnotation.getQualifiedName()))
                .findFirst().orElse(null);


        if (annotation == null) {
            return ProcessResult.fail("There is no of annotation on the method.");
        }

        PsiAnnotationMemberValue value = annotation.findAttributeValue("value");
        if (value == null) {
            return ProcessResult.success("The annotation does not specify a value for the value field");
        }

        String content = String.valueOf(JavaPsiFacade.getInstance(project).getConstantEvaluationHelper().computeConstantExpression(value));


        if (StringUtils.isBlank(content)) {
            return ProcessResult.success("The value of annotation is empty.");
        }

        String sql = new XMLLanguageDriver().createSqlSource(content).getSql(params, isTemplate);

        return ProcessResult.success(sql);
    }

    private static ProcessResult<String> getSqlFromXml(Project project, String qualifiedMethod, String params, boolean isTemplate) {
        try {

            String namespace = qualifiedMethod.substring(0, qualifiedMethod.lastIndexOf("."));
            Optional<String> optional = MybatisXmlContentCache.acquireByNamespace(project, namespace).stream().map(XmlTag::getText).findFirst();

            if (!optional.isPresent()) {
                return ProcessResult.fail("Oops! The plugin can't find the mapper file.");
            }

            XMLMapperBuilder builder = new XMLMapperBuilder(new ByteArrayInputStream(optional.get().getBytes(Charsets.toCharset(Charset.defaultCharset()))));
            Map<String, SqlSource> sqlSourceMap = builder.parse();

            if (!sqlSourceMap.containsKey(qualifiedMethod)) {
                return ProcessResult.fail(String.format("Oops! There is not %s in mapper file!!!", qualifiedMethod));
            }

            return ProcessResult.success(sqlSourceMap.get(qualifiedMethod).getSql(params, isTemplate));
        } catch (Throwable t) {
            StringWriter sw = new StringWriter();
            t.printStackTrace(new PrintWriter(sw));
            return ProcessResult.fail(String.format("Oops! There are something wrong when generate sql statement.\n%s", sw));
        }

    }

    private static String acquireMethodName(PsiMethod method) {
        PsiClass psiClass = method.getContainingClass();
        assert psiClass != null;

        String methodName = method.getName();
        String qualifiedName = psiClass.getQualifiedName();
        return qualifiedName + "." + methodName;

    }

    private static String generateMethodParam(PsiMethod method, POJO2JSONParser parser) {

        List<ParamNameType> paramNameTypes = getMethodBodyParamList(method);

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

        return new GsonBuilder().setPrettyPrinting().create().toJson(params);
    }

    /**
     * 获取方法所有参数
     * @param psiMethod {@link PsiMethod}
     * @return param list {@link ParamNameType}
     */
    public static List<ParamNameType> getMethodBodyParamList(PsiMethod psiMethod) {
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
    private static String getParamAnnotationValue(PsiParameter param) {
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
