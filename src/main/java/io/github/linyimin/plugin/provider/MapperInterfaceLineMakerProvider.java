package io.github.linyimin.plugin.provider;

import com.intellij.codeInsight.daemon.RelatedItemLineMarkerInfo;
import com.intellij.codeInsight.navigation.NavigationGutterIconBuilder;
import com.intellij.openapi.editor.markup.GutterIconRenderer;
import com.intellij.psi.PsiClass;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiMethod;
import com.intellij.psi.PsiNameIdentifierOwner;
import com.intellij.psi.xml.XmlTag;
import com.intellij.util.xml.DomElement;
import io.github.linyimin.plugin.dom.model.IdDomElement;
import io.github.linyimin.plugin.dom.model.Mapper;
import io.github.linyimin.plugin.utils.IconUtils;
import io.github.linyimin.plugin.utils.JavaUtils;
import io.github.linyimin.plugin.utils.MapperDomUtils;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.NotNull;

import java.util.*;
import java.util.stream.Collectors;

/**
 * @author yiminlin
 * @date 2022/01/23 4:52 pm
 * @description mapper interface method jump to xml
 **/
public class MapperInterfaceLineMakerProvider extends MapperLineMakerProviderAbstract {

    @Override
    protected void collectNavigationMarkers(@NotNull PsiElement element, @NotNull Collection<? super RelatedItemLineMarkerInfo<?>> result) {

        // 只处理Mapper接口中的内容
        if (!JavaUtils.isElementWithinMapperInterface(element)) {
            return;
        }

        List<PsiElement> target = acquireTarget(element);


        if (CollectionUtils.isEmpty(target)) {
            return;
        }

        NavigationGutterIconBuilder<PsiElement> builder = NavigationGutterIconBuilder
                .create(IconUtils.JAVA_TO_XML_ICON)
                .setAlignment(GutterIconRenderer.Alignment.CENTER)
                .setTargets(target)
                .setTooltipTitle("Navigation to Target in Mapper Xml");


        PsiNameIdentifierOwner identifierOwner = (PsiNameIdentifierOwner) element;
        if (Objects.isNull(identifierOwner.getNameIdentifier())) {
            return;
        }

        result.add(builder.createLineMarkerInfo(identifierOwner.getNameIdentifier()));

    }

    /**
     * 针对mapper interface的处理，返回满足要求的XmlTag列表
     * @param psiElement {@link PsiElement}
     * @return 满足要求的XmlTag列表
     */
    @Override
    public List<PsiElement> processMapperInterface(PsiElement psiElement) {
        if (!(psiElement instanceof PsiClass)) {
            return Collections.emptyList();
        }

        PsiClass psiClass = (PsiClass) psiElement;

        List<Mapper> mappers = MapperDomUtils.findMappersByNamespace(psiClass.getProject(), psiClass.getQualifiedName());

        return mappers
                .stream()
                .map(DomElement::getXmlTag)
                .filter(Objects::nonNull)
                .map(XmlTag::getNavigationElement)
                .collect(Collectors.toList());

    }

    /**
     * 针对mapper method的处理，返回满足要求的XmlTag列表
     * @param element {@link PsiElement}
     * @return 满足要求的XmlTag列表
     */
    @Override
    public List<PsiElement> processMapperMethod(PsiElement element) {
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
                .filter(Objects::nonNull)
                .map(XmlTag::getNavigationElement)
                .collect(Collectors.toList());
    }
}
