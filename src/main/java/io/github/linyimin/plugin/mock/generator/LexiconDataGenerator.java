package io.github.linyimin.plugin.mock.generator;

import com.intellij.openapi.project.Project;
import io.github.linyimin.plugin.configuration.LexiconComponent;
import io.github.linyimin.plugin.configuration.model.Lexicon;
import io.github.linyimin.plugin.mock.schema.Field;
import org.apache.commons.lang3.RandomUtils;
import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * @author banzhe
 * @date 2022/11/30 17:07
 **/
public class LexiconDataGenerator implements DataGenerator {
    @Override
    public List<Object> generate(Project project, Field field, int rows) {

        List<Object> list = new ArrayList<>(rows);

        List<Lexicon> lexicons = project.getComponent(LexiconComponent.class).getConfig().getLexicons();

        String name = field.getMockParam();

        if (StringUtils.isBlank(name)) {
            return list;
        }

        Lexicon lexicon = lexicons.stream().filter(temp -> StringUtils.equals(name, temp.getName())).findFirst().orElse(null);

        if (lexicon == null) {
            return list;
        }

        for (int i = 0; i < rows; i++) {
            String[] contents = StringUtils.split(lexicon.getContent(), ",");
            int index = RandomUtils.nextInt(0, contents.length);

            if (field.isNumber()) {
                list.add(Long.parseLong(contents[index]));
            } else {
                list.add(contents[index]);
            }
        }

        return list;
    }
}
