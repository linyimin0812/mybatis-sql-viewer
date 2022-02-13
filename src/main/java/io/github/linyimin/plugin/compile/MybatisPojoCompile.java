package io.github.linyimin.plugin.compile;

import com.intellij.compiler.impl.ProjectCompileScope;
import com.intellij.ide.plugins.cl.PluginClassLoader;
import com.intellij.openapi.compiler.CompilerManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.roots.OrderEnumerator;
import com.intellij.openapi.roots.ProjectRootManager;
import com.intellij.openapi.ui.Messages;
import com.intellij.openapi.util.io.FileUtil;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.util.lang.UrlClassLoader;
import io.github.linyimin.plugin.dom.Constant;
import io.github.linyimin.plugin.utils.JavaUtils;
import org.apache.commons.collections.CollectionUtils;

import java.io.File;
import java.lang.reflect.Field;
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

    public static UrlClassLoader classLoader;
    public static List<String> preDependencies;

    public static void compile(Project project) {

        ProjectCompileScope scope = new ProjectCompileScope(project);

        CompilerManager.getInstance(project).make(scope, null);

        setClassLoader(project);
    }

    public static void setClassLoader(Project project) {

        List<String> dependencies = getProjectDependencies(project);

        List<URL> urls = new ArrayList<>();
        for (String path : dependencies) {
            try {
                urls.add(new File(FileUtil.toSystemIndependentName(path)).toURI().toURL());
            }
            catch (MalformedURLException e) {
                Messages.showErrorDialog(e.getMessage(), "MalformedURLException");
            }
        }

        if (Objects.nonNull(classLoader)) {
            changeLoaderUrls(preDependencies, dependencies);
        } else {
            createProjectLoader(urls);
        }
        preDependencies = dependencies;
    }

    private static List<String> getProjectDependencies(Project project) {

        Set<String> mapperDependencies = JavaUtils.getAllDependenciesRecursive(project);

        List<String> list = OrderEnumerator
                .orderEntries(project)
                .recursively()
                .runtimeOnly()
                .withoutSdk()
                .getPathsList()
                .getPathList()
                .stream()
                .filter(path -> path.contains(Constant.MYBATIS_LOGGING_LOG4J)
                        || path.contains(Constant.MYBATIS_LOGGING_SLF4J)
                        || isProjectModule(project, path))
                .collect(Collectors.toList());


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
        classLoader = UrlClassLoader.build().urls(urls).parent(ClassLoader.getSystemClassLoader()).get();
        attachPluginParentLoader(classLoader);
    }

    private static void attachPluginParentLoader(UrlClassLoader classLoader) {
        PluginClassLoader pluginClassLoader = (PluginClassLoader) MybatisPojoCompile.class.getClassLoader();

        Class<? extends PluginClassLoader> pluginClassLoaderClass = pluginClassLoader.getClass();

        Field field = null;

        try {
            field = pluginClassLoaderClass.getDeclaredField(Constant.PLUGIN_CLASS_LOADER_PARENTS);
        } catch (NoSuchFieldException ignored) {
        }

        try {
            field = pluginClassLoaderClass.getDeclaredField(Constant.PLUGIN_CLASS_LOADER_MY_PARENTS);
        } catch (NoSuchFieldException ignored) {
        }

        if (Objects.isNull(field)) {
            Messages.showInfoMessage("Unsupported this version", Constant.APPLICATION_NAME);
            return;
        }

        try {
            field.setAccessible(true);

            ClassLoader[] parents = (ClassLoader[]) field.get(pluginClassLoader);
            if (Objects.isNull(parents)) {
                parents = new ClassLoader[] {classLoader};
                field.set(pluginClassLoader, parents);
            } else {
                ClassLoader[] newParents = new ClassLoader[parents.length + 1];
                newParents[parents.length] = classLoader;
                System.arraycopy(parents, 0, newParents, 0, parents.length);
                field.set(pluginClassLoader, newParents);
            }
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }

    }

    private static void changeLoaderUrls(List<String> preDependencies, List<String> dependencies) {

        List<String> list = new ArrayList<>(dependencies);

        for (String preDependency : preDependencies) {
            list.remove(preDependency);
        }

        if (CollectionUtils.isEmpty(list)) {
            return;
        }

        for (String path : list) {
            try {
                URL url = new File(FileUtil.toSystemIndependentName(path)).toURI().toURL();
                classLoader.addURL(url);
            }
            catch (MalformedURLException e) {
                Messages.showErrorDialog(e.getMessage(), "MalformedURLException");
            }
        }
    }

}
