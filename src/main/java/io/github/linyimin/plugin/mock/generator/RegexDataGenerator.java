package io.github.linyimin.plugin.mock.generator;

import com.intellij.openapi.project.Project;
import com.mifmif.common.regex.Generex;
import io.github.linyimin.plugin.mock.schema.TableField;

/**
 * @author banzhe
 * @date 2022/11/30 16:45
 **/
public class RegexDataGenerator implements DataGenerator {
    @Override
    public String generate(Project project, TableField field) {

        String mockParams = field.getMockParam();

        Generex generex = new Generex(mockParams);

        return generex.random();
    }
}
