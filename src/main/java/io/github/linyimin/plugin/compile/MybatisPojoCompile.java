package io.github.linyimin.plugin.compile;

import com.intellij.compiler.impl.ProjectCompileScope;
import com.intellij.openapi.compiler.CompilerManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.roots.OrderEnumerator;
import com.intellij.openapi.ui.Messages;
import com.intellij.openapi.util.io.FileUtil;
import com.intellij.util.lang.UrlClassLoader;

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

        ProjectCompileScope scope = new ProjectCompileScope(project);

        CompilerManager.getInstance(project).make(scope, null);

        setClassLoader(project);
    }

    public static void setClassLoader(Project project) {
        final List<URL> urls = new ArrayList<>();
        final List<String> list = OrderEnumerator.orderEntries(project).recursively().runtimeOnly().getPathsList().getPathList();
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
