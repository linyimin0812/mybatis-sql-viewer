package io.github.linyimin.plugin.mock.generator;

import com.intellij.openapi.project.Project;
import com.mifmif.common.regex.Generex;
import io.github.linyimin.plugin.mock.schema.Field;

import java.util.ArrayList;
import java.util.List;

/**
 * @author banzhe
 * @date 2022/11/30 16:45
 **/
public class RegexDataGenerator implements DataGenerator {
    @Override
    public List<String> generate(Project project, Field field, int rows) {

        String mockParams = field.getMockParam();

        List<String> list = new ArrayList<>(rows);
        Generex generex = new Generex(mockParams);

        for (int i = 0; i < rows; i++) {
            String randomStr = generex.random();
            list.add(randomStr);
        }

        return list;
    }
}
