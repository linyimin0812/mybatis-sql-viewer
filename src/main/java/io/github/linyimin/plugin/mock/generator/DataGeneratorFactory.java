package io.github.linyimin.plugin.mock.generator;

import io.github.linyimin.plugin.mock.enums.MockTypeEnum;

import java.util.HashMap;
import java.util.Map;

/**
 * @author banzhe
 * @date 2022/11/30 16:58
 **/
public class DataGeneratorFactory {
    
    static {
        GENERATOR_MAP.put(MockTypeEnum.database, new DatabaseDataGenerator());
        GENERATOR_MAP.put(MockTypeEnum.regex, new RegexDataGenerator());
        GENERATOR_MAP.put(MockTypeEnum.lexicon, new LexiconDataGenerator());
        GENERATOR_MAP.put(MockTypeEnum.fixed, new FixedDataGenerator());
        GENERATOR_MAP.put(MockTypeEnum.increment, new IncrementDataGenerator());
        GENERATOR_MAP.put(MockTypeEnum.random, new RandomDataGenerator());
        GENERATOR_MAP.put(MockTypeEnum.none, new DefaultValueGenerator());
    }

    private static final Map<MockTypeEnum, DataGenerator> GENERATOR_MAP = new HashMap<MockTypeEnum, DataGenerator>();

    private DataGeneratorFactory() {}

    public static DataGenerator getGenerator(MockTypeEnum type) {
        return GENERATOR_MAP.getOrDefault(type, new DefaultValueGenerator());
    }

}
