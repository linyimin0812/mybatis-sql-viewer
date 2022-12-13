package io.github.linyimin.plugin.mock.generator;

import com.intellij.openapi.project.Project;
import io.github.linyimin.plugin.mock.schema.Field;
import org.apache.commons.lang3.StringUtils;

/**
 * @author banzhe
 * @date 2022/11/30 15:56
 **/
public class FixedDataGenerator implements DataGenerator {
    @Override
    public Object generate(Project project, Field field) {

        String mockParam = field.getMockParam();

        if (Field.isNumber(field.getActualType())) {

            if (StringUtils.isBlank(mockParam)) {
                mockParam = "0";
            }

            return Long.parseLong(mockParam);
        }

        if (StringUtils.isBlank(mockParam)) {
            mockParam = "";
        }

        return mockParam;
    }
}
