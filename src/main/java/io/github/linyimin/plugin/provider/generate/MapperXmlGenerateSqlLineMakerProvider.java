package io.github.linyimin.plugin.provider.generate;

import com.intellij.codeInsight.daemon.LineMarkerInfo;
import com.intellij.codeInsight.daemon.LineMarkerProvider;
import com.intellij.openapi.editor.markup.GutterIconRenderer;
import com.intellij.psi.PsiElement;
import com.intellij.psi.xml.XmlTag;
import io.github.linyimin.plugin.dom.Constant;
import io.github.linyimin.plugin.utils.IconUtils;
import io.github.linyimin.plugin.utils.MapperDomUtils;
import org.jetbrains.annotations.NotNull;

/**
 * @author yiminlin
 * @date 2022/01/31 11:06 上午
 **/
public class MapperXmlGenerateSqlLineMakerProvider implements LineMarkerProvider {
    @Override
    public LineMarkerInfo<?> getLineMarkerInfo(@NotNull PsiElement element) {

        // 只处理Mapper接口中的内容
        if (!MapperDomUtils.isElementWithinMapperXml(element)) {
            return null;
        }

        XmlTag xmlTag = (XmlTag) element;

        if (!Constant.MYBATIS_OPS.contains(xmlTag.getName())) {
            return null;
        }

        FunctionTooltip tooltip = new FunctionTooltip(element);

        return new LineMarkerInfo<>(
                xmlTag.getNavigationElement(),
                xmlTag.getNavigationElement().getTextRange(),
                IconUtils.GENERATE_ICON,
                tooltip,
                null,
                GutterIconRenderer.Alignment.CENTER,
                () -> "mybatis-sql-viewer");
    }
}
