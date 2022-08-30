package io.github.linyimin.plugin.cache;

import com.google.common.collect.Lists;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.roots.ProjectRootManager;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiFileSystemItem;
import com.intellij.psi.util.PsiUtil;
import com.intellij.psi.xml.XmlAttribute;
import com.intellij.psi.xml.XmlFile;
import com.intellij.psi.xml.XmlTag;
import io.github.linyimin.plugin.utils.MapperDomUtils;
import org.apache.commons.lang3.StringUtils;

import java.util.*;

/**
 * @author banzhe
 * @date 2022/08/10 23:28
 **/
public class MybatisXmlContentCache {

    private static final List<String> SUB_TAGS = Lists.newArrayList("insert", "update", "delete", "select");

    private static final Map<Project, Map<String /* path */, String /* configuration */>> projectMybatisConfigurationMap = new HashMap<>();

    private static final Map<Project, Map<String /* namespace */, List<String> /* method name list */>> projectMybatisMapperMap = new HashMap<>();

    private static final Map<Project, Map<String /* namespace */, Set<XmlTag>>> projectMapperNamespaceMap = new HashMap<>();

    private static final Map<Project, Map<String /* method qualified name */, Set<XmlTag>>> projectMapperMethodMap = new HashMap<>();

    private static final Map<Project, Map<String /* method qualified name */, String /* mapper xml string */>> projectMethodToMapperFilePath = new HashMap<>();


    public static List<String> acquireConfigurations(Project project) {

        addXmlCache(project);

        Map<String, String> cacheMap = projectMybatisConfigurationMap.getOrDefault(project, Collections.emptyMap());

        return new ArrayList<>(cacheMap.values());
    }

    public static List<String> acquireByNamespace(Project project) {

        addXmlCache(project);

        Set<String> namespaces = projectMybatisMapperMap.getOrDefault(project, new HashMap<>()).keySet();
        return new ArrayList<>(namespaces);
    }

    public static String acquireMapperPathByMethodName(Project project, String methodName) {
        addXmlCache(project);

        return projectMethodToMapperFilePath.getOrDefault(project, new HashMap<>()).get(methodName);
    }

    public static Set<XmlTag> acquireByNamespace(Project project, String namespace) {

        addXmlCache(project);

        Map<String /* namespace */, Set<XmlTag>> cache = projectMapperNamespaceMap.getOrDefault(project, new HashMap<>());

        return cache.getOrDefault(namespace, new HashSet<>());
    }

    public static Set<XmlTag> acquireByMethodName(Project project, String methodQualifiedName) {

        addXmlCache(project);

        Map<String /* namespace */, Set<XmlTag>> cache = projectMapperMethodMap.getOrDefault(project, new HashMap<>());

        return cache.getOrDefault(methodQualifiedName, new HashSet<>());
    }

    private static void addXmlCache(Project project) {

        ProjectRootManager.getInstance(project).getFileIndex().iterateContent(fileOrDir -> {

            PsiFileSystemItem item = PsiUtil.findFileSystemItem(project, fileOrDir);

            if (item == null) {
                return true;
            }

            PsiFile psiFile = item.getContainingFile();

            if (MapperDomUtils.isMybatisConfigurationFile(psiFile)) {
                addConfigurationCache(project, fileOrDir, psiFile);
            }

            if (MapperDomUtils.isMybatisMapperFile(psiFile)) {
                addMapperCache(project, psiFile);
            }

            return true;
        });
    }

    private static void addMapperCache(Project project, PsiFile psiFile) {

        XmlTag rootTag = ((XmlFile) psiFile).getRootTag();

        if (rootTag == null || rootTag.getAttribute("namespace") == null) {
            return;
        }


        XmlAttribute namespaceAttribute = rootTag.getAttribute("namespace");

        if (namespaceAttribute == null) {
            return;
        }

        String namespace = namespaceAttribute.getValue();

        addNamespaceXmlTagCache(project, namespace, rootTag);

        XmlTag[] subTags = rootTag.getSubTags();

        for (XmlTag subTag : subTags) {
            if (!SUB_TAGS.contains(subTag.getName())) {
                continue;
            }

            XmlAttribute subAttribute = subTag.getAttribute("id");

            if (subAttribute == null) {
                continue;
            }

            String id = subAttribute.getValue();

            addMethodToMapperCache(project, namespace, id, psiFile);

            addMethodXmlTagCache(project, namespace, id, subTag);

            addNamespaceCache(project, namespace, id);

        }

    }

    private static void addMethodToMapperCache(Project project, String namespace, String id, PsiFile psiFile) {
        Map<String, String> methodCacheMap = projectMethodToMapperFilePath.getOrDefault(project, new HashMap<>());

        String methodQualifiedName = namespace + "." + id;

        String path = psiFile.getVirtualFile().getPath();

        if (StringUtils.isBlank(path)) {
            return;
        }

        path = path.substring(path.indexOf("resources/") + "resources/".length());

        methodCacheMap.put(methodQualifiedName, path);

        projectMethodToMapperFilePath.put(project, methodCacheMap);

    }

    private static void addNamespaceXmlTagCache(Project project, String namespace, XmlTag xmlTag) {

        Map<String, Set<XmlTag>> namespaceCacheMap = projectMapperNamespaceMap.getOrDefault(project, new HashMap<>());

        Set<XmlTag> tags = namespaceCacheMap.getOrDefault(namespace, new HashSet<>());
        tags.add(xmlTag);

        namespaceCacheMap.put(namespace, tags);

        projectMapperNamespaceMap.put(project, namespaceCacheMap);

    }

    private static void addMethodXmlTagCache(Project project, String namespace, String id, XmlTag xmlTag) {

        Map<String, Set<XmlTag>> methodCacheMap = projectMapperMethodMap.getOrDefault(project, new HashMap<>());

        String methodQualifiedName = namespace + "." + id;

        Set<XmlTag> tags = methodCacheMap.getOrDefault(methodQualifiedName, new HashSet<>());
        tags.add(xmlTag);

        methodCacheMap.put(methodQualifiedName, tags);

        projectMapperMethodMap.put(project, methodCacheMap);
    }

    private static void addNamespaceCache(Project project, String namespace, String id) {

        Map<String, List<String>> cacheMap = projectMybatisMapperMap.getOrDefault(project, new HashMap<>());

        List<String> ids = cacheMap.getOrDefault(namespace, new ArrayList<>());
        ids.add(id);

        cacheMap.put(namespace, ids);

        projectMybatisMapperMap.put(project, cacheMap);
    }

    private static void addConfigurationCache(Project project, VirtualFile fileOrDir, PsiFile psiFile) {
        Map<String, String> cacheMap = projectMybatisConfigurationMap.getOrDefault(project, new HashMap<>());

        String path = fileOrDir.getPath();
        String configuration = psiFile.getText();

        cacheMap.put(path, configuration);
        projectMybatisConfigurationMap.put(project, cacheMap);
    }
}
