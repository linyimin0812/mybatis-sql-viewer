package io.github.linyimin.plugin.provider.generate;

import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiIdentifier;
import com.intellij.psi.PsiMethod;
import com.intellij.psi.xml.XmlAttribute;
import com.intellij.psi.xml.XmlTag;
import com.intellij.util.Function;


/**
 * @author yiminlin
 * @date 2022/01/31 10:36 上午
 **/
public class FunctionTooltip implements Function<PsiElement, String> {

    private final String MSG = "Generate Sql and params for ";
    PsiElement psiElement;

    public FunctionTooltip() {}

    public FunctionTooltip(PsiElement psiElement) {
        this.psiElement = psiElement;
    }

    @Override
    public String fun(PsiElement psiElement) {
        if (psiElement instanceof PsiIdentifier && psiElement.getParent() instanceof PsiMethod) {
            PsiMethod psiMethod = (PsiMethod) psiElement.getParent();
            return MSG + psiMethod.getName();
        }
        if (psiElement instanceof XmlTag) {

            XmlAttribute attribute = ((XmlTag) psiElement).getAttribute("id");

            if (attribute == null) {
                return null;
            }

            return MSG + attribute.getValue();
        }
        return null;
    }
}
