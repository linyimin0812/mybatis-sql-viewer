package io.github.linyimin.plugin.provider.jump;

import com.intellij.codeInsight.daemon.RelatedItemLineMarkerInfo;
import com.intellij.codeInsight.daemon.RelatedItemLineMarkerProvider;
import com.intellij.codeInsight.navigation.NavigationGutterIconBuilder;
import com.intellij.openapi.editor.markup.GutterIconRenderer;
import com.intellij.psi.PsiClass;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiMethod;
import com.intellij.psi.xml.XmlToken;
import com.intellij.psi.xml.XmlTokenType;
import io.github.linyimin.plugin.configuration.GlobalConfig;
import io.github.linyimin.plugin.provider.MapperXmlProcessor;
import io.github.linyimin.plugin.settings.SqlViewerSettingsState;
import io.github.linyimin.plugin.utils.IconUtils;
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
    protected void collectNavigationMarkers(@NotNull PsiElement element, @NotNull Collection<? super RelatedItemLineMarkerInfo> result) {

        SqlViewerSettingsState state = SqlViewerSettingsState.getInstance();

        if (!GlobalConfig.isMybatisMode || !state.fileJumpEnable) {
            return;
        }

        // 避免LineMarker is supposed to be registered for leaf elements only, but got: XmlTag:select Warning
        if (!(element instanceof XmlToken) || ((XmlToken)element).getTokenType() != XmlTokenType.XML_START_TAG_START) {
            return;
        }

        PsiElement tag = element.getParent();

        List<PsiElement> target = acquireTarget(tag);

        if (CollectionUtils.isEmpty(target)) {
            return;
        }

        NavigationGutterIconBuilder<PsiElement> builder = NavigationGutterIconBuilder
                .create(IconUtils.XML_TO_JAVA_ICON)
                .setAlignment(GutterIconRenderer.Alignment.CENTER)
                .setTargets(target)
                .setTooltipTitle("Navigation to Target in Mapper Class");

        result.add(builder.createLineMarkerInfo(element));
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
