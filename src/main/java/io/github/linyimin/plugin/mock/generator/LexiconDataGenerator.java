package io.github.linyimin.plugin.mock.generator;

import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.project.Project;
import io.github.linyimin.plugin.configuration.LexiconComponent;
import io.github.linyimin.plugin.configuration.model.Lexicon;
import io.github.linyimin.plugin.mock.schema.Field;
import org.apache.commons.lang3.RandomUtils;
import org.apache.commons.lang3.StringUtils;

import java.util.List;

/**
 * @author banzhe
 * @date 2022/11/30 17:07
 **/
public class LexiconDataGenerator implements DataGenerator {
    @Override
    public Object generate(Project project, Field field) {

        List<Lexicon> lexicons = ApplicationManager.getApplication().getComponent(LexiconComponent.class).getConfig().getLexicons();

        String name = field.getMockParam();

        Lexicon lexicon = lexicons.stream().filter(temp -> StringUtils.equals(name, temp.getName())).findFirst().orElse(null);

        if (lexicon == null) {
            return StringUtils.EMPTY;
        }

        String[] contents = StringUtils.split(lexicon.getContent(), ",");
        int index = RandomUtils.nextInt(0, contents.length);

        if (Field.isNumber(field.getActualType())) {
            return Long.parseLong(contents[index]);
        } else {
            return contents[index];
        }
    }
}
