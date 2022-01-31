package io.github.linyimin.plugin.provider;

import com.intellij.psi.PsiClass;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiMethod;
import com.intellij.psi.xml.XmlTag;
import com.intellij.util.xml.DomElement;
import io.github.linyimin.plugin.dom.model.IdDomElement;
import io.github.linyimin.plugin.dom.model.Mapper;
import io.github.linyimin.plugin.utils.MapperDomUtils;
import org.apache.commons.lang3.StringUtils;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * @author yiminlin
 * @date 2022/01/31 10:01 上午
 **/
public class MapperInterfaceProcessor {
    /**
     * 针对mapper interface的处理，返回满足要求的XmlTag列表
     * @param psiElement {@link PsiElement}
     * @return 满足要求的XmlTag列表
     */
    public static List<XmlTag> processMapperInterface(PsiElement psiElement) {
        if (!(psiElement instanceof PsiClass)) {
            return Collections.emptyList();
        }

        PsiClass psiClass = (PsiClass) psiElement;

        List<Mapper> mappers = MapperDomUtils.findMappersByNamespace(psiClass.getProject(), psiClass.getQualifiedName());

        return mappers
                .stream()
                .map(DomElement::getXmlTag)
                .collect(Collectors.toList());

    }

    /**
     * 针对mapper method的处理，返回满足要求的XmlTag列表
     * @param element {@link PsiElement}
     * @return 满足要求的XmlTag列表
     */
    public static List<XmlTag> processMapperMethod(PsiElement element) {
        if (!(element instanceof PsiMethod)) {
            return Collections.emptyList();
        }

        PsiMethod psiMethod = (PsiMethod) element;
        PsiClass psiClass = ((PsiMethod) element).getContainingClass();

        if (Objects.isNull(psiClass)) {
            return Collections.emptyList();
        }

        String qualifiedName = psiClass.getQualifiedName();
        String methodId = qualifiedName + "." + psiMethod.getName();

        List<Mapper> mappers = MapperDomUtils.findMappersByNamespace(psiClass.getProject(), qualifiedName);

        return mappers.stream()
                .filter(mapper -> {
                    List<String> ids = MapperDomUtils.getAllIdsFromMapper(mapper);
                    return ids.contains(methodId);
                })
                .map(Mapper::getDaoElements)
                .flatMap(Collection::stream)
                .filter(idDomElement -> {
                    String id = idDomElement.getId().getRawText();
                    return StringUtils.equals(methodId, qualifiedName + "." + id);
                })
                .map(IdDomElement::getXmlTag)
                .collect(Collectors.toList());
    }
}
