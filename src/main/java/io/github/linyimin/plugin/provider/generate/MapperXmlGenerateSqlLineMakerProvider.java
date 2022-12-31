package io.github.linyimin.plugin.provider.generate;

import com.intellij.codeInsight.daemon.LineMarkerInfo;
import com.intellij.codeInsight.daemon.LineMarkerProvider;
import com.intellij.openapi.editor.markup.GutterIconRenderer;
import com.intellij.psi.PsiElement;
import com.intellij.psi.xml.XmlTag;
import com.intellij.psi.xml.XmlToken;
import com.intellij.psi.xml.XmlTokenType;
import io.github.linyimin.plugin.configuration.GlobalConfig;
import io.github.linyimin.plugin.constant.Constant;
import io.github.linyimin.plugin.utils.IconUtils;
import org.jetbrains.annotations.NotNull;

/**
 * @author yiminlin
 * @date 2022/01/31 11:06 上午
 **/
public class MapperXmlGenerateSqlLineMakerProvider implements LineMarkerProvider {
    @Override
    public LineMarkerInfo<?> getLineMarkerInfo(@NotNull PsiElement element) {

        if (!GlobalConfig.isMybatisMode) {
            return null;
        }

        // 避免LineMarker is supposed to be registered for leaf elements only, but got: XmlTag:select Warning
        if (!(element instanceof XmlToken) || ((XmlToken)element).getTokenType() != XmlTokenType.XML_START_TAG_START) {
            return null;
        }

        PsiElement tag = element.getParent();

        XmlTag xmlTag = (XmlTag) tag;

        if (!Constant.MYBATIS_OPS.contains(xmlTag.getName())) {
            return null;
        }

        FunctionTooltip tooltip = new FunctionTooltip(tag);

        return new LineMarkerInfo<>(
                element,
                element.getTextRange(),
                IconUtils.GENERATE_ICON,
                tooltip,
                new SqlGenerateNavigationHandler(),
                GutterIconRenderer.Alignment.CENTER);
    }
}
