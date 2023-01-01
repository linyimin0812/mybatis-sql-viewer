package io.github.linyimin.plugin.cache;

import com.google.common.collect.Lists;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.roots.ProjectRootManager;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiFileSystemItem;
import com.intellij.psi.util.PsiUtil;
import com.intellij.psi.xml.XmlAttribute;
import com.intellij.psi.xml.XmlFile;
import com.intellij.psi.xml.XmlTag;
import io.github.linyimin.plugin.utils.MapperDomUtils;

import java.util.*;

/**
 * @author banzhe
 * @date 2022/08/10 23:28
 **/
public class MybatisXmlContentCache {

    private static final List<String> SUB_TAGS = Lists.newArrayList("insert", "update", "delete", "select");

    private static final Map<Project, Map<String /* namespace */, List<String> /* method name list */>> projectMybatisMapperMap = new HashMap<>();

    private static final Map<Project, Map<String /* namespace */, Set<XmlTag>>> projectMapperNamespaceMap = new HashMap<>();

    private static final Map<Project, Map<String /* method qualified name */, Set<XmlTag>>> projectMapperMethodMap = new HashMap<>();

    private static final Map<Project, Map<String /* namespace */, Set<XmlTag> /* method xmlTag */>> projectNamespaceMethodMap = new HashMap<>();

    public static List<String> acquireByNamespace(Project project, boolean forceUpdate) {

        Set<String> namespaces = projectMybatisMapperMap.getOrDefault(project, new HashMap<>()).keySet();

        if (forceUpdate) {
            addXmlCache(project);
            return new ArrayList<>(projectMybatisMapperMap.getOrDefault(project, new HashMap<>()).keySet());
        }

        if (!namespaces.isEmpty()) {
            return new ArrayList<>(namespaces);
        }

        addXmlCache(project);

        return new ArrayList<>(projectMybatisMapperMap.getOrDefault(project, new HashMap<>()).keySet());
    }

    public static Set<XmlTag> acquireByNamespace(Project project, String namespace) {

        Map<String /* namespace */, Set<XmlTag>> cache = projectMapperNamespaceMap.getOrDefault(project, new HashMap<>());

        if (cache.containsKey(namespace)) {
            return cache.getOrDefault(namespace, new HashSet<>());
        }

        addXmlCache(project);

       return projectMapperNamespaceMap.getOrDefault(project, new HashMap<>()).getOrDefault(namespace, new HashSet<>());
    }

    public static Set<XmlTag> acquireByMethodName(Project project, String methodQualifiedName) {

        Map<String /* namespace */, Set<XmlTag>> cache = projectMapperMethodMap.getOrDefault(project, new HashMap<>());

        if (cache.containsKey(methodQualifiedName)) {
            return cache.get(methodQualifiedName);
        }

        addXmlCache(project);

        return projectMapperMethodMap.getOrDefault(project, new HashMap<>()).getOrDefault(methodQualifiedName, new HashSet<>());
    }

    public static Set<XmlTag> acquireMethodsByNamespace(Project project, String namespace) {

        Map<String /* namespace */, Set<XmlTag>> cache = projectNamespaceMethodMap.getOrDefault(project, new HashMap<>());

        if (cache.containsKey(namespace)) {
            return cache.get(namespace);
        }

        addXmlCache(project);

        return projectNamespaceMethodMap.getOrDefault(project, new HashMap<>()).getOrDefault(namespace, new HashSet<>());
    }

    private static void addXmlCache(Project project) {

        ProjectRootManager.getInstance(project).getFileIndex().iterateContent(fileOrDir -> {

            PsiFileSystemItem item = PsiUtil.findFileSystemItem(project, fileOrDir);

            if (item == null) {
                return true;
            }

            PsiFile psiFile = item.getContainingFile();

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

            addMethodXmlTagCache(project, namespace, id, subTag);

            addNamespaceCache(project, namespace, id);

            addNamespaceMethodCache(project, namespace, subTag);

        }

    }

    private static void addNamespaceMethodCache(Project project, String namespace, XmlTag subTag) {

        Map<String, Set<XmlTag>> map = projectNamespaceMethodMap.getOrDefault(project, new HashMap<>());

        Set<XmlTag> xmlTags = map.getOrDefault(namespace, new HashSet<>());

        xmlTags.add(subTag);
        map.put(namespace, xmlTags);

        projectNamespaceMethodMap.put(project, map);

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
}
