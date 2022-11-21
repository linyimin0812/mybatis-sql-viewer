package io.github.linyimin.plugin.pojo2json;

import io.github.linyimin.plugin.pojo2json.type.SpecifyType;

/**
 * @author banzhe
 * @date 2022/11/21 21:13
= **/
public class RandomPOJO2JSONParser extends POJO2JSONParser {
    @Override
    protected Object getFakeValue(SpecifyType specifyType) {
        return specifyType.random();
    }
}
