package io.github.linyimin.plugin.provider.generate;

import com.intellij.openapi.wm.ToolWindowManager;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiIdentifier;
import com.intellij.psi.PsiMethod;
import com.intellij.psi.xml.XmlTag;
import com.intellij.util.Function;
import com.intellij.util.xml.DomElement;
import com.intellij.util.xml.DomUtil;
import io.github.linyimin.plugin.dom.model.IdDomElement;

import java.util.Objects;


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
        if (psiElement instanceof PsiIdentifier && psiElement.getParent() instanceof PsiMethod) {
            PsiMethod psiMethod = (PsiMethod) psiElement.getParent();
            return msg + psiMethod.getName();
        }
        if (psiElement instanceof XmlTag) {
            DomElement domElement = DomUtil.getDomElement(psiElement);
            if (Objects.isNull(domElement)) {
                return null;
            }
            String id = ((IdDomElement)domElement).getId().getRawText();
            return msg + id;
        }
        return null;
    }
}
