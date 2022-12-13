package io.github.linyimin.plugin.mock.generator;

import com.intellij.openapi.project.Project;
import io.github.linyimin.plugin.mock.schema.TableField;

/**
 * @author banzhe
 * @date 2022/11/30 11:06
 **/
public interface DataGenerator<T> {
    T generate(Project project, TableField field) throws Exception;
}
