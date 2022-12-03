package io.github.linyimin.plugin.mock.generator;

import com.intellij.openapi.project.Project;
import io.github.linyimin.plugin.mock.schema.Field;
import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * @author banzhe
 * @date 2022/11/30 16:05
 **/
public class IncrementDataGenerator implements DataGenerator {
    @Override
    public List<Long> generate(Project project, Field field, int rows) {

        List<Long> list = new ArrayList<>();

        String mockParam = field.getMockParam();

        if (StringUtils.isBlank(mockParam)) {
            mockParam = "0";
        }

        long initValue = Long.parseLong(mockParam);

        for (int i = 0; i < rows; i++) {
            list.add(initValue++);
        }

        return list;
    }
}
