package io.github.linyimin.plugin.utils;

import com.google.common.collect.Lists;
import com.intellij.openapi.project.Project;
import com.intellij.psi.*;
import com.intellij.psi.impl.compiled.ClsFileImpl;
import com.intellij.psi.search.GlobalSearchScope;
import io.github.linyimin.plugin.cache.MybatisXmlContentCache;
import io.github.linyimin.plugin.dom.Constant;
import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.lang.StringUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.stream.Collectors;

/**
 * @author yiminlin
 * @date 2022/01/24 12:17 上午
 **/
public class JavaUtils {

    /**
     * 判断element是否属于接口文件
     * @param psiElement {@link PsiElement}
     * @return true: 属于接口文件，false: 不属于接口文件
     */
    public static boolean isElementWithinMapperInterface(PsiElement psiElement) {

        if (isMapperInterface(psiElement)) {
            return true;
        }

        return isMapperMethod(psiElement);
    }

    /**
     * 判断元素是否是mapper接口
     * @param psiElement {@link PsiElement}
     * @return true: 是mapper接口，false: 不是mapper接口
     */
    public static boolean isMapperInterface(PsiElement psiElement) {

        if (!(psiElement instanceof PsiClass) || !((PsiClass) psiElement).isInterface()) {
            return false;
        }

        List<String> namespaces = MybatisXmlContentCache.acquireByNamespace(psiElement.getProject());

        PsiClass psiClass = (PsiClass) psiElement;
        String qualifiedName = psiClass.getQualifiedName();

        return namespaces.contains(qualifiedName);

    }

    /**
     * 判断元素是否是mapper 接口中定义的方法
     * @param psiElement ${@link PsiElement}
     * @return true: 是Mapper接口中定义的方法，false: 不是Mapper接口中定义的方法
     */
    public static boolean isMapperMethod(PsiElement psiElement) {
        if (!(psiElement instanceof PsiMethod)) {
            return false;
        }

        PsiClass psiClass = ((PsiMethod) psiElement).getContainingClass();
        return isMapperInterface(psiClass);
    }

    /**
     * 获取mapper方法对应的PsiMethod
     * @param project {@link Project}
     * @param clazzName class qualified name
     * @param methodName method name
     * @return {@link PsiMethod}
     */
    public static List<PsiMethod> findMethod(@NotNull Project project, @Nullable String clazzName, @Nullable String methodName) {
        if (StringUtils.isBlank(clazzName) && StringUtils.isBlank(methodName)) {
            return Collections.emptyList();
        }
        PsiClass psiClass = findClazz(project, clazzName);
        if (Objects.isNull(psiClass)) {
            return Collections.emptyList();
        }
        PsiMethod[] methods = psiClass.findMethodsByName(methodName, true);
        return ArrayUtils.isEmpty(methods) ? Collections.emptyList() : Lists.newArrayList(methods);
    }

    /**
     * 查找class对应的PsiClass
     * @param project {@link Project}
     * @param clazzName class qualified name
     * @return {@link Optional}
     */
    public static PsiClass findClazz(Project project, String clazzName) {
        GlobalSearchScope scope = GlobalSearchScope.allScope(project);
        JavaPsiFacade instance = JavaPsiFacade.getInstance(project);
        return instance.findClass(clazzName, scope);
    }

    public static List<PsiMethod> findMethod(Project project, String methodQualifiedName) {
        String className = StringUtils.substring(methodQualifiedName, 0, StringUtils.lastIndexOf(methodQualifiedName, "."));
        String methodName = StringUtils.substring(methodQualifiedName, StringUtils.lastIndexOf(methodQualifiedName, ".") + 1);

        return findMethod(project, className, methodName);
    }

    private static Set<PsiClass> getAllDependencies(PsiClass psiClass) {
        PsiFile psiFile = psiClass.getContainingFile();

        if (psiFile instanceof ClsFileImpl && !psiFile.getContainingFile().getVirtualFile().getPath().contains("jre")) {
            psiFile = ((ClsFileImpl) psiFile).getDecompiledPsiFile();
        }

        Set<PsiClass> importPsiClassSet = new HashSet<>();

        psiFile.acceptChildren(new JavaRecursiveElementVisitor() {
            @Override
            public void visitImportList(PsiImportList list) {
                for (PsiImportStatement statement : list.getImportStatements()) {
                    if (Objects.isNull(statement.getImportReference())) {
                        continue;
                    }

                    PsiElement psiElement = statement.getImportReference().resolve();
                    if (psiElement instanceof PsiClass) {
                        PsiClass importPsiClass = (PsiClass) psiElement;
                        importPsiClassSet.add(importPsiClass);
                    }
                }

                for (PsiImportStaticStatement statement : list.getImportStaticStatements()) {
                    if (Objects.isNull(statement.getImportReference())) {
                        continue;
                    }

                    PsiElement psiElement = statement.getImportReference().resolve();
                    if (psiElement instanceof PsiClass) {
                        PsiClass importPsiClass = (PsiClass) psiElement;
                        importPsiClassSet.add(importPsiClass);
                    }
                }
            }
        });

        return importPsiClassSet;
    }

    public static Set<String> getAllDependenciesRecursive(Project project) {
        List<String> namespaces = MybatisXmlContentCache.acquireByNamespace(project);

        List<PsiClass> psiClassList = namespaces
                .stream()
                .map(namespace -> JavaUtils.findClazz(project, namespace))
                .filter(Objects::nonNull)
                .collect(Collectors.toList());

        Set<PsiClass> allDependencies = new HashSet<>();

        for (PsiClass psiClass : psiClassList) {

            Set<PsiClass> visited = new HashSet<>();

            Queue<PsiClass> queue = new ArrayDeque<>();
            queue.add(psiClass);

            while (!queue.isEmpty()) {
                PsiClass clazz = queue.remove();

                visited.add(clazz);

                Set<PsiClass> dependencies = getAllDependencies(clazz);
                dependencies.removeAll(visited);
                queue.addAll(dependencies);
            }

            visited.remove(psiClass);
            allDependencies.addAll(visited);
        }

        return allDependencies.stream()
                .map(item -> item.getContainingFile().getVirtualFile().getCanonicalPath())
                .filter(path -> StringUtils.isNotEmpty(path) && path.contains("jar!"))
                .map(path -> StringUtils.substring(path, 0, path.lastIndexOf(Constant.JAR_FILE_END)))
                .collect(Collectors.toSet());
    }
}
