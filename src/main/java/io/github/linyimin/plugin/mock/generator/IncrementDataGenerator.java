package io.github.linyimin.plugin.mock.generator;

import com.intellij.openapi.project.Project;
import io.github.linyimin.plugin.mock.schema.TableField;
import org.apache.commons.lang3.StringUtils;

/**
 * @author banzhe
 * @date 2022/11/30 16:05
 **/
public class IncrementDataGenerator implements DataGenerator {
    @Override
    public Object generate(Project project, TableField field) {

        String mockParam = field.getMockParam();

        if (StringUtils.isBlank(mockParam)) {
            mockParam = "0";
        }

        long initValue = Long.parseLong(mockParam);

        field.setMockParam(String.valueOf(initValue++));

        if (TableField.isNumber(field.getActualType())) {
            return initValue;
        }

        return String.valueOf(initValue);
    }
}
