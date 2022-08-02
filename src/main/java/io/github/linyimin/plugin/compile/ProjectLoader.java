package io.github.linyimin.plugin.compile;

import java.net.URL;
import java.net.URLClassLoader;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author banzhe
 * @date 2022/07/27 16:12
 **/
public class ProjectLoader extends URLClassLoader {


    private final Map<String /* class qualifier */, Class<?> /*loaded class*/> loadedClassMap = new ConcurrentHashMap<>();


    public ProjectLoader(URL[] urls, ClassLoader parent) {
        super(urls, parent);
    }

    private Class<?> findLoadedClass0(String name) {
        return loadedClassMap.get(name);
    }

    @Override
    protected Class<?> loadClass(String name, boolean resolve) throws ClassNotFoundException {

        Class<?> clazz = findLoadedClass0(name);

        if (Objects.nonNull(clazz)) {
            return clazz;
        }

        clazz = findLoadedClass(name);

        if (Objects.nonNull(clazz)) {
            return clazz;
        }

        try {
            clazz = findClass(name);
            if (Objects.nonNull(clazz)) {
                if (resolve) {
                    resolveClass(clazz);
                }
                loadedClassMap.put(name, clazz);
                return clazz;
            }
        } catch (ClassNotFoundException | SecurityException ignored) {

        }

        return super.loadClass(name, resolve);
    }
}
