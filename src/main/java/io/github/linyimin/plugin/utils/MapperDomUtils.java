package io.github.linyimin.plugin.utils;

import com.intellij.psi.PsiFile;
import com.intellij.psi.xml.XmlFile;
import com.intellij.psi.xml.XmlTag;
import org.apache.commons.lang3.StringUtils;

import java.util.Objects;

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
}
