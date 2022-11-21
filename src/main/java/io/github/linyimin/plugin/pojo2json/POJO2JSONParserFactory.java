package io.github.linyimin.plugin.pojo2json;

/**
 * @author banzhe
 * @date 2022/11/21 21:13
 **/
public class POJO2JSONParserFactory {
    public static final POJO2JSONParser DEFAULT_POJO_2_JSON_PARSER = new DefaultPOJO2JSONParser();
    public static final POJO2JSONParser RANDOM_POJO_2_JSON_PARSER = new RandomPOJO2JSONParser();
}
