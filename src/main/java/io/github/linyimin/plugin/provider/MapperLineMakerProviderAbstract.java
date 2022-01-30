package io.github.linyimin.plugin.provider;

import com.intellij.codeInsight.daemon.RelatedItemLineMarkerProvider;
import com.intellij.psi.PsiElement;

import java.util.*;

/**
 * @author yiminlin
 * @date 2022/01/31 1:39 上午
 * @description mapper file jump abstract class
 **/
public abstract class MapperLineMakerProviderAbstract extends RelatedItemLineMarkerProvider {
    /**
     * 获取跳转目标
     * @param element {@link PsiElement}
     * @return 跳转目标列表
     */
    public List<PsiElement> acquireTarget(PsiElement element) {

        List<PsiElement> classXmlTags = processMapperInterface(element);
        List<PsiElement> methodXmlTags = processMapperMethod(element);

        List<PsiElement> target = new ArrayList<>();
        target.addAll(classXmlTags);
        target.addAll(methodXmlTags);

        return target;
    }

    /**
     * 针对mapper interface的处理，返回满足要求的XmlTag列表
     * @param psiElement {@link PsiElement}
     * @return 满足要求的XmlTag列表
     */
    public abstract List<PsiElement> processMapperInterface(PsiElement psiElement);

    /**
     * 针对mapper method的处理，返回满足要求的XmlTag列表
     * @param element {@link PsiElement}
     * @return 满足要求的XmlTag列表
     */
    public abstract List<PsiElement> processMapperMethod(PsiElement element);
}
