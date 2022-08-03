package io.github.linyimin.plugin.compile;

import com.intellij.compiler.impl.ProjectCompileScope;
import com.intellij.openapi.compiler.CompilerManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.roots.OrderEnumerator;
import com.intellij.openapi.ui.Messages;
import com.intellij.openapi.util.io.FileUtil;
import io.github.linyimin.plugin.utils.JavaUtils;
import org.apache.commons.lang3.StringUtils;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;


/**
 * @author yiminlin
 * @date 2022/02/07 1:49 上午
 **/
public class MybatisPojoCompile {

    private static final Map<Project, ProjectLoader> projectClassLoaderMap = new ConcurrentHashMap<>();

    public static ProjectLoader getClassLoader(Project project) {

        return projectClassLoaderMap.get(project);
    }

    public static void compile(Project project) {

        ProjectCompileScope scope = new ProjectCompileScope(project);

        CompilerManager.getInstance(project).make(scope, null);

        setClassLoader(project);
    }

    public static void setClassLoader(Project project) {

        List<String> dependencies = getProjectDependencies(project);

        List<URL> urls = pathToURL(dependencies);

        ProjectLoader classLoader = projectClassLoaderMap.get(project);

        try {
            classLoader.close();
        } catch (Throwable ignored) {
        }

        classLoader = createProjectLoader(urls);

        projectClassLoaderMap.put(project, classLoader);
    }

    private static List<URL> pathToURL(List<String> paths) {
        List<URL> urls = new ArrayList<>();

        try {
            for (String path : paths) {
                urls.add(new File(FileUtil.toSystemIndependentName(path)).toURI().toURL());
            }

            // 添加本地获取消息类
            URL url = MybatisPojoCompile.class.getClassLoader().getResource("io/github/linyimin/plugin/utils/MybatisSqlUtils.class");
            if (Objects.nonNull(url)) {
                String fileName = MybatisPojoCompile.class.getClassLoader().getResource("io/github/linyimin/plugin/utils/MybatisSqlUtils.class").getFile();

                fileName = StringUtils.substring(fileName, 0, fileName.indexOf("jar!") + 3);

                urls.add(new URL(fileName));
            }

        } catch (MalformedURLException e) {
            Messages.showErrorDialog(e.getMessage(), "MalformedURLException");
        }

        return urls;
    }
    private static List<String> getProjectDependencies(Project project) {

        Set<String> mapperDependencies = JavaUtils.getAllDependenciesRecursive(project);

        List<String> list = OrderEnumerator
                .orderEntries(project)
                .recursively()
                .runtimeOnly()
                .withoutSdk()
                .getPathsList()
                .getPathList();

        list.addAll(mapperDependencies);

        return list;
    }

    private static ProjectLoader createProjectLoader(List<URL> urls) {

        URL[] urlArr = urls.toArray(new URL[0]);

        return new ProjectLoader(urlArr, MybatisPojoCompile.class.getClassLoader());
    }

}
