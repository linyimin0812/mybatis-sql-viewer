package io.github.linyimin.plugin.provider;

import com.google.common.collect.Lists;
import com.intellij.psi.PsiClass;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiMethod;
import com.intellij.psi.xml.XmlTag;
import com.intellij.util.xml.DomElement;
import com.intellij.util.xml.DomUtil;
import io.github.linyimin.plugin.dom.Constant;
import io.github.linyimin.plugin.dom.model.IdDomElement;
import io.github.linyimin.plugin.dom.model.Mapper;
import io.github.linyimin.plugin.utils.JavaUtils;
import io.github.linyimin.plugin.utils.MapperDomUtils;
import org.apache.commons.lang3.StringUtils;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

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

        DomElement domElement = DomUtil.getDomElement(xmlTag);

        if (!(domElement instanceof Mapper)) {
            return Collections.emptyList();
        }

        Mapper mapper = (Mapper) domElement;

        String namespace = mapper.getNamespace().getRawText();
        PsiClass psiClass = JavaUtils.findClazz(psiElement.getProject(), namespace);

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

        DomElement domElement = DomUtil.getDomElement(xmlTag);

        if (!(domElement instanceof IdDomElement)) {
            return Collections.emptyList();
        }

        IdDomElement idDomElement = (IdDomElement) domElement;
        Mapper mapper = MapperDomUtils.findMapper(domElement);

        String qualifiedName = mapper.getNamespace().getRawText();
        String methodName = idDomElement.getId().getRawText();

        return JavaUtils.findMethod(element.getProject(), qualifiedName, methodName);

    }
}
