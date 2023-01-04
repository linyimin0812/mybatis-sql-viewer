package io.github.linyimin.plugin.provider.generate;

import com.intellij.codeInsight.daemon.LineMarkerInfo;
import com.intellij.codeInsight.daemon.LineMarkerProvider;
import com.intellij.openapi.editor.markup.GutterIconRenderer;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiNameIdentifierOwner;
import io.github.linyimin.plugin.configuration.GlobalConfig;
import io.github.linyimin.plugin.utils.IconUtils;
import io.github.linyimin.plugin.utils.JavaUtils;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import java.util.Objects;

/**
 * @author yiminlin
 * @date 2022/01/31 9:32 上午
 **/
public class MapperInterfaceGenerateSqlLineMakerProvider implements LineMarkerProvider {

    @Override
    public LineMarkerInfo<?> getLineMarkerInfo(@NotNull PsiElement element) {

        if (!GlobalConfig.isMybatisMode) {
            return null;
        }

        // 只处理Mapper接口中的内容
        if (!JavaUtils.isMapperInterface(element) && !JavaUtils.isMapperMethod(element)) {
            return null;
        }

        PsiNameIdentifierOwner identifierOwner = (PsiNameIdentifierOwner) element;

        if (Objects.isNull(identifierOwner.getNameIdentifier())) {
            return null;
        }

        FunctionTooltip tooltip = new FunctionTooltip(element);

        return new LineMarkerInfo<>(
                identifierOwner.getNameIdentifier(),
                identifierOwner.getNameIdentifier().getTextRange(),
                IconUtils.GENERATE_ICON,
                tooltip,
                new SqlGenerateNavigationHandler(),
                GutterIconRenderer.Alignment.CENTER);
    }

}
