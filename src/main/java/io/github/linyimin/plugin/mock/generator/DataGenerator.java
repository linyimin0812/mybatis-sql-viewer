package io.github.linyimin.plugin.mock.generator;

import com.intellij.openapi.project.Project;
import io.github.linyimin.plugin.mock.schema.Field;

import java.util.List;

/**
 * @author banzhe
 * @date 2022/11/30 11:06
 **/
public interface DataGenerator<T> {
    List<T> generate(Project project, Field field, int rows) throws Exception;
}
