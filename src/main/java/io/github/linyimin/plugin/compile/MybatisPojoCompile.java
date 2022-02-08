package io.github.linyimin.plugin.compile;

import com.intellij.compiler.impl.ModuleCompileScope;
import com.intellij.openapi.compiler.CompilerManager;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.module.ModuleUtilCore;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.roots.OrderEnumerator;
import com.intellij.openapi.ui.Messages;
import com.intellij.openapi.util.io.FileUtil;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiClass;
import com.intellij.psi.PsiMethod;
import com.intellij.psi.util.ClassUtil;
import com.intellij.util.lang.UrlClassLoader;
import io.github.linyimin.plugin.service.MybatisSqlStateComponent;
import io.github.linyimin.plugin.service.model.MybatisSqlConfiguration;
import io.github.linyimin.plugin.utils.JavaUtils;
import org.apache.commons.lang3.StringUtils;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;


/**
 * @author yiminlin
 * @date 2022/02/07 1:49 上午
 **/
public class MybatisPojoCompile {

    // TODO: 需要优化成只编译Mybatis需要的类

    public static ClassLoader classLoader;

    public static void compile(Project project) {

        MybatisSqlConfiguration sqlConfig = project.getService(MybatisSqlStateComponent.class).getState();
        assert sqlConfig != null;

        String methodName = sqlConfig.getMethod();
        String clazzName = StringUtils.substring(methodName,0, StringUtils.lastIndexOf(methodName, "."));

        PsiClass psiClass = JavaUtils.findClazz(project, clazzName);
        final Module module = ModuleUtilCore.findModuleForPsiElement(psiClass);
        final VirtualFile virtualFile = psiClass.getContainingFile().getVirtualFile();
        ModuleCompileScope scope = new ModuleCompileScope(project, new Module[]{module}, false);
//        new FileSetCompileScope(Collections.singletonList(virtualFile), new Module[]{module})

        CompilerManager.getInstance(project).make(scope, null);

        setClassLoader(module);
    }

    public static void setClassLoader(Module module) {
        final List<URL> urls = new ArrayList<>();
        final List<String> list = OrderEnumerator.orderEntries(module).recursively().runtimeOnly().getPathsList().getPathList();
        for (String path : list) {
            try {
                urls.add(new File(FileUtil.toSystemIndependentName(path)).toURI().toURL());
            }
            catch (MalformedURLException e) {
                Messages.showErrorDialog(e.getMessage(), "MalformedURLException");
            }
        }
        classLoader = UrlClassLoader.build().urls(urls).parent(ClassLoader.getSystemClassLoader()).get();
    }
}
