package io.github.linyimin.plugin.provider.generate;

import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiMethod;
import com.intellij.psi.xml.XmlTag;
import com.intellij.util.Function;


/**
 * @author yiminlin
 * @date 2022/01/31 10:36 上午
 **/
public class FunctionTooltip implements Function<PsiElement, String> {

    private String msg = "Generate Sql and params for ";
    PsiElement psiElement;

    public FunctionTooltip() {}

    public FunctionTooltip(PsiElement psiElement) {
        this.psiElement = psiElement;
    }

    @Override
    public String fun(PsiElement psiElement) {
        if (psiElement instanceof PsiMethod) {
            PsiMethod psiMethod = (PsiMethod) psiElement;
            return msg + psiMethod.getName();
        }
        if (psiElement instanceof XmlTag) {
            XmlTag xmlTag = (XmlTag) psiElement;
            return msg + xmlTag.getName();
        }
        return null;
    }
}
