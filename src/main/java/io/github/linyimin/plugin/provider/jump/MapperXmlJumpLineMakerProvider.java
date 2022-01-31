package io.github.linyimin.plugin.provider.jump;

import com.intellij.codeInsight.daemon.RelatedItemLineMarkerInfo;
import com.intellij.codeInsight.daemon.RelatedItemLineMarkerProvider;
import com.intellij.codeInsight.navigation.NavigationGutterIconBuilder;
import com.intellij.openapi.editor.markup.GutterIconRenderer;
import com.intellij.psi.PsiClass;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiMethod;
import com.intellij.psi.xml.XmlTag;
import io.github.linyimin.plugin.provider.MapperXmlProcessor;
import io.github.linyimin.plugin.utils.IconUtils;
import io.github.linyimin.plugin.utils.MapperDomUtils;
import org.apache.commons.collections.CollectionUtils;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author yiminlin
 * @date 2022/01/31 1:03 上午
 * @description mapper interface method jump to xml
 **/
public class MapperXmlJumpLineMakerProvider extends RelatedItemLineMarkerProvider {

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

        result.add(builder.createLineMarkerInfo(xmlTag.getNavigationElement()));
    }

    private List<PsiElement> acquireTarget(PsiElement element) {

        List<PsiElement> classXmlTags = MapperXmlProcessor.processMapperInterface(element)
                .stream()
                .map(PsiClass::getNavigationElement)
                .collect(Collectors.toList());
        List<PsiElement> methodXmlTags = MapperXmlProcessor.processMapperMethod(element)
                .stream()
                .map(PsiMethod::getNavigationElement)
                .collect(Collectors.toList());

        List<PsiElement> target = new ArrayList<>();
        target.addAll(classXmlTags);
        target.addAll(methodXmlTags);

        return target;
    }

}
