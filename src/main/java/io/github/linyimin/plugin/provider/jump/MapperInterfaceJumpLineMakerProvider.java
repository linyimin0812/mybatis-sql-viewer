package io.github.linyimin.plugin.provider.jump;

import com.intellij.codeInsight.daemon.RelatedItemLineMarkerInfo;
import com.intellij.codeInsight.daemon.RelatedItemLineMarkerProvider;
import com.intellij.codeInsight.navigation.NavigationGutterIconBuilder;
import com.intellij.openapi.editor.markup.GutterIconRenderer;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiNameIdentifierOwner;
import com.intellij.psi.xml.XmlTag;
import io.github.linyimin.plugin.provider.MapperInterfaceProcessor;
import io.github.linyimin.plugin.utils.IconUtils;
import io.github.linyimin.plugin.utils.JavaUtils;
import org.apache.commons.collections.CollectionUtils;
import org.jetbrains.annotations.NotNull;

import java.util.*;
import java.util.stream.Collectors;

/**
 * @author yiminlin
 * @date 2022/01/23 4:52 pm
 * @description mapper interface method jump to xml
 **/
public class MapperInterfaceJumpLineMakerProvider extends RelatedItemLineMarkerProvider {

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
     * 获取跳转目标
     * @param element {@link PsiElement}
     * @return 跳转目标列表
     */
    private List<PsiElement> acquireTarget(PsiElement element) {

        List<PsiElement> classTags = MapperInterfaceProcessor.processMapperInterface(element)
                .stream()
                .map(XmlTag::getNavigationElement)
                .collect(Collectors.toList());

        List<PsiElement> methodTags = MapperInterfaceProcessor.processMapperMethod(element)
                .stream()
                .map(XmlTag::getNavigationElement)
                .collect(Collectors.toList());

        List<PsiElement> target = new ArrayList<>();
        target.addAll(classTags);
        target.addAll(methodTags);

        return target;
    }
}
