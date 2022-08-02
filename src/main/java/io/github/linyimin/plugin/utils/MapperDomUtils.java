package io.github.linyimin.plugin.utils;

import com.intellij.openapi.module.Module;
import com.intellij.openapi.module.ModuleUtilCore;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiClass;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiMethod;
import com.intellij.psi.search.GlobalSearchScope;
import com.intellij.psi.xml.XmlFile;
import com.intellij.psi.xml.XmlTag;
import com.intellij.util.xml.DomElement;
import com.intellij.util.xml.DomFileElement;
import com.intellij.util.xml.DomService;
import com.intellij.util.xml.DomUtil;
import io.github.linyimin.plugin.dom.model.MybatisConfiguration;
import io.github.linyimin.plugin.dom.model.IdDomElement;
import io.github.linyimin.plugin.dom.model.Mapper;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * @author yiminlin
 * @date 2022/01/23 3:53 am
 * @description mapper xml dom util
 **/
public final class MapperDomUtils {

    private MapperDomUtils() {
        throw new UnsupportedOperationException();
    }

    public static boolean isMybatisMapperFile(PsiFile file) {
        if (!isXml(file)) {
            return false;
        }

        XmlTag rootTag = ((XmlFile) file).getRootTag();

        return Objects.nonNull(rootTag) && "mapper".equals(rootTag.getName());

    }


    public static boolean isMybatisConfigurationFile(PsiFile file) {
        if (!isXml(file)) {
            return false;
        }

        XmlTag rootTag = ((XmlFile) file).getRootTag();

        return Objects.nonNull(rootTag)
                && "configuration".equals(rootTag.getName()) &&
                StringUtils.contains(rootTag.getText(), "mappers");

    }

    private static boolean isXml(PsiFile file) {
        return file instanceof XmlFile;
    }

    /**
     * 获取所有mapper xml对应的Mapper列表
     * @param project {@link Project}
     * @return mapper xml对应的Mapper列表
     */
    public static List<Mapper> findMappers(Project project) {
        GlobalSearchScope scope = GlobalSearchScope.allScope(project);
        List<DomFileElement<Mapper>> elements = DomService.getInstance().getFileElements(Mapper.class, project, scope);

        return elements.stream()
                .map(DomFileElement::getRootElement)
                .collect(Collectors.toList());
    }

    /**
     * 根据{@link DomElement}查找Mapper
     * @param element {@link DomElement}
     * @return {@link Mapper}
     */
    public static Mapper findMapper(DomElement element) {
        Mapper mapper = DomUtil.getParentOfType(element, Mapper.class, true);
        if (Objects.nonNull(mapper)) {
            return mapper;
        } else {
            throw new IllegalArgumentException("Unknown element");
        }
    }

    /**
     * 获取mapper xml文件中namespace对应的值
     * @param mapper {@link Mapper}
     * @return namespace属性对应的值
     */
    public static String getNamespace(Mapper mapper) {
        return mapper.getNamespace().getRawText();
    }

    /**
     * 获取project中所有的mapper xml中namespace的属性值（Mapper 接口限定符）
     * @param project ${@link Project}
     * @return project中所有的mapper xml中namespace的属性值列表（Mapper 接口限定符）
     */
    public static List<String> getNamespaces(Project project) {
        List<Mapper> mappers = findMappers(project);
        return mappers.stream().map(MapperDomUtils::getNamespace).collect(Collectors.toList());
    }

    /**
     * 根据namespace属性值查找mapper xml文件对应的Mapper
     * @param qualifiedName mapper interface qualified name
     * @param project {@link Project}
     * @return namespace属性值为qualifiedName的mapper xml文件对应的mapper 列表
     */
    public static List<Mapper> findMappersByNamespace(Project project, String qualifiedName) {
        List<Mapper> mappers = findMappers(project);

        return mappers.stream().filter(mapper -> {
            String namespace = getNamespace(mapper);
            return StringUtils.equals(namespace, qualifiedName);
        }).collect(Collectors.toList());
    }

    /**
     * 获取所有xml中增删改查中对应id: class.method
     * @param mapper {@link Mapper}
     * @return 所有id的字符串列表
     */
    public static List<String> getAllIdsFromMapper(Mapper mapper) {

        List<IdDomElement> idDomElements = mapper.getDaoElements();
        String namespace = getNamespace(mapper);

        return idDomElements.stream().map(idDomElement -> {
            String id = idDomElement.getId().getRawText();
            return namespace + "." + id;
        }).collect(Collectors.toList());
    }

    public static String getIdFromMethod(PsiMethod psiMethod) {
        PsiClass psiClass = psiMethod.getContainingClass();
        assert psiClass != null;
        String qualifiedName = psiClass.getQualifiedName();
        String methodName = psiMethod.getName();

        return qualifiedName + "." + methodName;
    }

    /**
     * 判断mapper xml中的psiElement是否是mapper接口中方法的实现
     * @param psiElement {@link PsiElement}
     * @return true: 是mapper接口方法的实现，false: 不是mapper中接口方法的实现
     */
    public static boolean isElementWithinMapperXml(PsiElement psiElement) {
        if (!(psiElement instanceof XmlTag)) {
            return false;
        }

        PsiFile psiFile = psiElement.getContainingFile();
        if (!isMybatisMapperFile(psiFile)) {
            return false;
        }

        DomElement domElement = DomUtil.getDomElement(psiElement);

        if (domElement instanceof Mapper) {
            return true;
        }

        return domElement instanceof IdDomElement;
    }


    /**
     * 获取mybatis configuration
     * @param project {@link Project}
     * @return {@link MybatisConfiguration}
     */
    public static List<MybatisConfiguration> findConfiguration(Project project, PsiMethod psiMethod) {
        GlobalSearchScope scope = GlobalSearchScope.allScope(project);

        Module module = ModuleUtilCore.findModuleForPsiElement(psiMethod);

        List<DomFileElement<MybatisConfiguration>> elements = DomService.getInstance().getFileElements(MybatisConfiguration.class, project, scope);

        List<MybatisConfiguration> mybatisConfigurations = elements.stream()
                .filter(configuration -> {
                    assert module != null;
                    assert configuration.getModule() != null;
                    return StringUtils.equals(module.getName(), configuration.getModule().getName());
                })
                .map(DomFileElement::getRootElement)
                .collect(Collectors.toList());

        if (CollectionUtils.isNotEmpty(mybatisConfigurations)) {
            return mybatisConfigurations;
        }


        return findConfigurationByPsiMethod(project, elements, psiMethod);

    }

    private static List<MybatisConfiguration> findConfigurationByPsiMethod(Project project, List<DomFileElement<MybatisConfiguration>> elements, PsiMethod psiMethod) {

        if (Objects.isNull(psiMethod.getContainingClass())) {
            return null;
        }

        List<Mapper> mappers = MapperDomUtils.findMappersByNamespace(project, psiMethod.getContainingClass().getQualifiedName());

        if (CollectionUtils.isEmpty(mappers)) {
            return null;
        }

        XmlTag xmlTag = mappers.get(0).getXmlTag();
        if (Objects.isNull(xmlTag) || Objects.isNull(xmlTag.getContainingFile())) {
            return null;
        }

        String mapperFileName = xmlTag.getContainingFile().getName();


        return elements.stream()
                .map(DomFileElement::getRootElement)
                .filter(configuration -> {
                    XmlTag tag = configuration.getXmlTag();
                    return Objects.nonNull(tag) && StringUtils.contains(tag.getText(), mapperFileName);
                }).collect(Collectors.toList());
    }
}
