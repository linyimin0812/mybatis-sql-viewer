package io.github.linyimin.plugin.provider;

import com.google.common.collect.Lists;
import com.intellij.psi.PsiClass;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiMethod;
import com.intellij.psi.impl.source.xml.XmlTagImpl;
import com.intellij.psi.xml.XmlAttribute;
import com.intellij.psi.xml.XmlTag;
import io.github.linyimin.plugin.dom.Constant;
import io.github.linyimin.plugin.utils.JavaUtils;
import org.apache.commons.lang3.StringUtils;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

/**
 * @author yiminlin
 * @date 2022/01/31 10:03 上午
 **/
public class MapperXmlProcessor {

    /**
     * 针对mapper interface的处理，返回满足要求的XmlTag列表
     * @param psiElement {@link PsiElement}
     * @return 满足要求的XmlTag列表
     */
    public static List<PsiClass> processMapperInterface(PsiElement psiElement) {

        XmlTag xmlTag = (XmlTag) psiElement;

        if (!StringUtils.equals(Constant.MAPPER, xmlTag.getName())) {
            return Collections.emptyList();
        }

        XmlAttribute attribute = xmlTag.getAttribute("namespace");

        if (attribute == null) {
            return Collections.emptyList();
        }

        PsiClass psiClass = JavaUtils.findClazz(psiElement.getProject(), attribute.getValue());

        return Objects.isNull(psiClass) ? Collections.emptyList() : Lists.newArrayList(psiClass);
    }

    /**
     * 针对mapper method的处理，返回满足要求的PsiMethod列表
     * @param element {@link PsiElement}
     * @return 满足要求的PsiMethod列表
     */
    public static List<PsiMethod> processMapperMethod(PsiElement element) {
        XmlTag xmlTag = (XmlTag) element;

        if (!Constant.MYBATIS_OPS.contains(xmlTag.getName())) {
            return Collections.emptyList();
        }

        XmlAttribute attribute = xmlTag.getAttribute("id");
        if (attribute == null) {
            return Collections.emptyList();
        }

        String methodName = attribute.getValue();

        PsiElement parent = xmlTag.getParent();

        if (parent == null) {
            return Collections.emptyList();
        }

        XmlAttribute parentAttribute = ((XmlTagImpl) parent).getAttribute("namespace");

        if (parentAttribute == null) {
            return Collections.emptyList();
        }

        String qualifiedName = parentAttribute.getValue();

        return JavaUtils.findMethod(element.getProject(), qualifiedName, methodName);

    }
}
