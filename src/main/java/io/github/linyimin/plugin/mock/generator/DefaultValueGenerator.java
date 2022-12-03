package io.github.linyimin.plugin.mock.generator;

import com.intellij.openapi.project.Project;
import io.github.linyimin.plugin.mock.schema.Field;
import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * @author banzhe
 * @date 2022/11/30 15:49
 **/
public class DefaultValueGenerator implements DataGenerator {

    @Override
    public List<Object> generate(Project project, Field field, int rows) {

        List<Object> list = new ArrayList<>(rows);

        String defaultValue = field.getDefaultValue();

        if (field.isNumber()) {

            if (StringUtils.isBlank(defaultValue)) {
                defaultValue = "0";
            }

            long initValue = Long.parseLong(defaultValue);

            for (int i = 0; i < rows; i++) {
                list.add(initValue);
            }

            return list;
        }

        if (StringUtils.isBlank(defaultValue)) {
            defaultValue = "";
        }

        for (int i = 0; i < rows; i++) {
            list.add(defaultValue);
        }

        return list;
    }
}
