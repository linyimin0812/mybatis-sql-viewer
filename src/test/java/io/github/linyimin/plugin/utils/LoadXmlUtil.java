package io.github.linyimin.plugin.utils;

import java.io.InputStream;

/**
 * @author banzhe
 * @date 2022/11/18 22:07
 **/
public class LoadXmlUtil {
    public static InputStream load(String path) {
        ClassLoader classloader = Thread.currentThread().getContextClassLoader();
        return classloader.getResourceAsStream(path);
    }
}
