package io.github.linyimin.plugin.mock.generator;

import com.intellij.openapi.project.Project;
import io.github.linyimin.plugin.mock.schema.TableField;
import org.apache.commons.lang3.StringUtils;

/**
 * @author banzhe
 * @date 2022/11/30 15:49
 **/
public class DefaultValueGenerator implements DataGenerator {

    @Override
    public Object generate(Project project, TableField field) {

        String defaultValue = field.getDefaultValue();

        if (TableField.isNumber(field.getActualType())) {

            if (StringUtils.isBlank(defaultValue)) {
                defaultValue = "0";
            }

            long initValue = Long.parseLong(defaultValue);

            field.setDefaultValue(String.valueOf(initValue++));

            return initValue;
        }

        if (StringUtils.isBlank(defaultValue)) {
            defaultValue = "";
        }

        return defaultValue;
    }
}
