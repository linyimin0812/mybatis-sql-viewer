package io.github.linyimin.plugin.provider;

import com.intellij.psi.PsiClass;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiMethod;
import com.intellij.psi.xml.XmlTag;
import io.github.linyimin.plugin.cache.MybatisXmlContentCache;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

/**
 * @author yiminlin
 * @date 2022/01/31 10:01 上午
 **/
public class MapperInterfaceProcessor {
    /**
     * 针对mapper interface的处理，返回满足要求的XmlTag列表
     * @param psiElement {@link PsiElement}
     * @return 满足要求的XmlTag列表
     */
    public static List<XmlTag> processMapperInterface(PsiElement psiElement) {
        if (!(psiElement instanceof PsiClass)) {
            return Collections.emptyList();
        }

        PsiClass psiClass = (PsiClass) psiElement;

        return MybatisXmlContentCache.acquireByNamespace(psiClass.getProject(), psiClass.getQualifiedName());

    }

    /**
     * 针对mapper method的处理，返回满足要求的XmlTag列表
     * @param element {@link PsiElement}
     * @return 满足要求的XmlTag列表
     */
    public static List<XmlTag> processMapperMethod(PsiElement element) {
        if (!(element instanceof PsiMethod)) {
            return Collections.emptyList();
        }

        PsiMethod psiMethod = (PsiMethod) element;
        PsiClass psiClass = ((PsiMethod) element).getContainingClass();

        if (Objects.isNull(psiClass)) {
            return Collections.emptyList();
        }

        String qualifiedName = psiClass.getQualifiedName();
        String methodId = qualifiedName + "." + psiMethod.getName();

        return MybatisXmlContentCache.acquireByMethodName(psiMethod.getProject(), methodId);

    }
}
