package io.github.linyimin.plugin.mock.generator;

import io.github.linyimin.plugin.mock.enums.MockTypeEnum;

import java.util.HashMap;
import java.util.Map;

/**
 * @author banzhe
 * @date 2022/11/30 16:58
 **/
public class DataGeneratorFactory {

    private static final Map<MockTypeEnum, DataGenerator> GENERATOR_MAP = new HashMap<MockTypeEnum, DataGenerator>() {{
        put(MockTypeEnum.database, new DatabaseDataGenerator());
        put(MockTypeEnum.regex, new RegexDataGenerator());
        put(MockTypeEnum.lexicon, new LexiconDataGenerator());
        put(MockTypeEnum.fixed, new FixedDataGenerator());
        put(MockTypeEnum.increment, new IncrementDataGenerator());
        put(MockTypeEnum.random, new RandomDataGenerator());
        put(MockTypeEnum.none, new DefaultValueGenerator());
    }};

    private DataGeneratorFactory() {}

    public static DataGenerator getGenerator(MockTypeEnum type) {
        return GENERATOR_MAP.getOrDefault(type, new DefaultValueGenerator());
    }

}
