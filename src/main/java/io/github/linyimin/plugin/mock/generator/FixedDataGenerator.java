package io.github.linyimin.plugin.mock.generator;

import com.intellij.openapi.project.Project;
import io.github.linyimin.plugin.mock.schema.Field;
import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * @author banzhe
 * @date 2022/11/30 15:56
 **/
public class FixedDataGenerator implements DataGenerator {
    @Override
    public List<Object> generate(Project project, Field field, int rows) {

        List<Object> list = new ArrayList<>(rows);

        String mockParam = field.getMockParam();

        if (field.isNumber()) {

            if (StringUtils.isBlank(mockParam)) {
                mockParam = "0";
            }

            long value = Long.parseLong(mockParam);

            for (int i = 0; i < rows; i++) {
                list.add(value);
            }

            return list;

        }

        if (StringUtils.isBlank(mockParam)) {
            mockParam = "";
        }

        for (int i = 0; i < rows; i++) {
            list.add(mockParam);
        }

        return list;
    }
}
