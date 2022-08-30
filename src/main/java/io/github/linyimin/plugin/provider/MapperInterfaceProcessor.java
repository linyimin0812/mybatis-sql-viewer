package io.github.linyimin.plugin.provider;

import com.intellij.psi.PsiClass;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiMethod;
import com.intellij.psi.xml.XmlTag;
import io.github.linyimin.plugin.cache.MybatisXmlContentCache;
import java.util.Collections;
import java.util.Objects;
import java.util.Set;

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
    public static Set<XmlTag> processMapperInterface(PsiElement psiElement) {
        if (!(psiElement instanceof PsiClass)) {
            return Collections.emptySet();
        }

        PsiClass psiClass = (PsiClass) psiElement;

        return MybatisXmlContentCache.acquireByNamespace(psiClass.getProject(), psiClass.getQualifiedName());

    }

    /**
     * 针对mapper method的处理，返回满足要求的XmlTag列表
     * @param element {@link PsiElement}
     * @return 满足要求的XmlTag列表
     */
    public static Set<XmlTag> processMapperMethod(PsiElement element) {
        if (!(element instanceof PsiMethod)) {
            return Collections.emptySet();
        }

        PsiMethod psiMethod = (PsiMethod) element;
        PsiClass psiClass = ((PsiMethod) element).getContainingClass();

        if (Objects.isNull(psiClass)) {
            return Collections.emptySet();
        }

        String qualifiedName = psiClass.getQualifiedName();
        String methodId = qualifiedName + "." + psiMethod.getName();

        return MybatisXmlContentCache.acquireByMethodName(psiMethod.getProject(), methodId);

    }
}
