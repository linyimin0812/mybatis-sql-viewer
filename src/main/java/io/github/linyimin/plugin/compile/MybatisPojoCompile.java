package io.github.linyimin.plugin.compile;

import com.intellij.compiler.impl.ProjectCompileScope;
import com.intellij.openapi.compiler.CompilerManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.roots.OrderEnumerator;
import com.intellij.openapi.roots.ProjectRootManager;
import com.intellij.openapi.ui.Messages;
import com.intellij.openapi.util.io.FileUtil;
import com.intellij.openapi.vfs.VirtualFile;
import io.github.linyimin.plugin.utils.JavaUtils;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;


/**
 * @author yiminlin
 * @date 2022/02/07 1:49 上午
 **/
public class MybatisPojoCompile {

    // TODO: 需要优化成只编译Mybatis需要的类

    public static ProjectLoader classLoader;
    public static List<String> preDependencies;

    public static void compile(Project project) {

        ProjectCompileScope scope = new ProjectCompileScope(project);

        CompilerManager.getInstance(project).make(scope, null);

        setClassLoader(project);
    }

    public static void setClassLoader(Project project) {

        List<String> dependencies = getProjectDependencies(project);

        List<URL> urls = pathToURL(dependencies);

        if (Objects.nonNull(classLoader)) {
            changeLoaderUrls(preDependencies, dependencies);
        } else {
            createProjectLoader(urls);
        }
        preDependencies = dependencies;
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

    private static boolean isProjectModule(Project project, String path) {
        VirtualFile[] vFiles = ProjectRootManager.getInstance(project).getContentRootsFromAllModules();
        Set<String> moduleNames = Stream.of(vFiles).map(VirtualFile::getName).collect(Collectors.toSet());

        moduleNames.add(project.getName());
        moduleNames.add(project.getBasePath());

        return moduleNames.stream().anyMatch(path::contains);
    }

    private static void createProjectLoader(List<URL> urls) {

        URL[] urlArr = urls.toArray(new URL[0]);

        classLoader = new ProjectLoader(urlArr, MybatisPojoCompile.class.getClassLoader());
    }

    private static void changeLoaderUrls(List<String> preDependencies, List<String> dependencies) {

        List<String> list = new ArrayList<>(dependencies);

        for (String preDependency : preDependencies) {
            list.remove(preDependency);
        }

        if (CollectionUtils.isEmpty(list)) {
            return;
        }

        try {
            classLoader.close();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        List<URL> urls = pathToURL(dependencies);
        classLoader = new ProjectLoader(urls.toArray(new URL[0]), MybatisPojoCompile.class.getClassLoader());

    }

}
