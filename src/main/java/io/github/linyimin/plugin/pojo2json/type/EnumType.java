package io.github.linyimin.plugin.pojo2json.type;

import com.intellij.psi.PsiClass;
import com.intellij.psi.PsiEnumConstant;
import com.intellij.psi.PsiField;
import org.apache.commons.lang3.RandomUtils;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author banzhe
 * @date 2022/11/21 20:23
 **/
public class EnumType implements SpecifyType {

    private final PsiClass psiClass;

    public EnumType(PsiClass psiClass) {
        this.psiClass = psiClass;
    }

    @Override
    public Object def() {
        return Arrays.stream(psiClass.getAllFields())
                .filter(psiField -> psiField instanceof PsiEnumConstant)
                .findFirst()
                .map(PsiField::getName);
    }

    @Override
    public Object random() {
        List<String> psiFieldNames = Arrays.stream(psiClass.getAllFields())
                .filter(psiField -> psiField instanceof PsiEnumConstant)
                .map(PsiField::getName)
                .collect(Collectors.toList());

        if (psiFieldNames.isEmpty()) {
            return "";
        }

        return psiFieldNames.get(RandomUtils.nextInt(0, psiFieldNames.size()));
    }
}
