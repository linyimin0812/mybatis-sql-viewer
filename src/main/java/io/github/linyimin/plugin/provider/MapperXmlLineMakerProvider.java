package io.github.linyimin.plugin.provider;

import com.google.common.collect.Lists;
import com.intellij.codeInsight.daemon.RelatedItemLineMarkerInfo;
import com.intellij.codeInsight.navigation.NavigationGutterIconBuilder;
import com.intellij.openapi.editor.markup.GutterIconRenderer;
import com.intellij.psi.PsiClass;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiMethod;
import com.intellij.psi.xml.XmlTag;
import com.intellij.util.xml.DomElement;
import com.intellij.util.xml.DomUtil;
import io.github.linyimin.plugin.dom.Constant;
import io.github.linyimin.plugin.dom.model.IdDomElement;
import io.github.linyimin.plugin.dom.model.Mapper;
import io.github.linyimin.plugin.utils.IconUtils;
import io.github.linyimin.plugin.utils.JavaUtils;
import io.github.linyimin.plugin.utils.MapperDomUtils;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * @author yiminlin
 * @date 2022/01/31 1:03 上午
 * @description mapper interface method jump to xml
 **/
public class MapperXmlLineMakerProvider extends MapperLineMakerProviderAbstract {

    @Override
    protected void collectNavigationMarkers(@NotNull PsiElement element, @NotNull Collection<? super RelatedItemLineMarkerInfo<?>> result) {

        if (!MapperDomUtils.isElementWithinMapperXml(element)) {
            return;
        }


        List<PsiElement> target = acquireTarget(element);


        if (CollectionUtils.isEmpty(target)) {
            return;
        }

        NavigationGutterIconBuilder<PsiElement> builder = NavigationGutterIconBuilder
                .create(IconUtils.XML_TO_JAVA_ICON)
                .setAlignment(GutterIconRenderer.Alignment.CENTER)
                .setTargets(target)
                .setTooltipTitle("Navigation to Target in Mapper Class");


        XmlTag xmlTag = (XmlTag) element;

        result.add(builder.createLineMarkerInfo(xmlTag));
    }

    @Override
    public List<PsiElement> processMapperInterface(PsiElement psiElement) {

        XmlTag xmlTag = (XmlTag) psiElement;

        if (!StringUtils.equals(Constant.MAPPER, xmlTag.getName())) {
            return Collections.emptyList();
        }

        DomElement domElement = DomUtil.getDomElement(xmlTag);

        if (!(domElement instanceof Mapper)) {
            return Collections.emptyList();
        }

        Mapper mapper = (Mapper) domElement;

        String namespace = mapper.getNamespace().getRawText();
        PsiClass psiClass = JavaUtils.findClazz(psiElement.getProject(), namespace);

        return Objects.isNull(psiClass) ? Collections.emptyList() : Lists.newArrayList(psiClass.getNavigationElement());
    }

    @Override
    public List<PsiElement> processMapperMethod(PsiElement element) {
        XmlTag xmlTag = (XmlTag) element;

        if (!Constant.MYBATIS_OPS.contains(xmlTag.getName())) {
            return Collections.emptyList();
        }

        DomElement domElement = DomUtil.getDomElement(xmlTag);

        if (!(domElement instanceof IdDomElement)) {
            return Collections.emptyList();
        }

        IdDomElement idDomElement = (IdDomElement) domElement;
        Mapper mapper = MapperDomUtils.findMapper(domElement);

        String qualifiedName = mapper.getNamespace().getRawText();
        String methodName = idDomElement.getId().getRawText();

        List<PsiMethod> psiMethods = JavaUtils.findMethod(element.getProject(), qualifiedName, methodName);

        return psiMethods.stream().map(PsiElement::getNavigationElement).collect(Collectors.toList());
    }
}
