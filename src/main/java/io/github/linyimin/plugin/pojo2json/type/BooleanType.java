package io.github.linyimin.plugin.pojo2json.type;

import org.apache.commons.lang3.RandomUtils;

/**
 * @author banzhe
 * @date 2022/11/21 20:20
 **/
public class BooleanType implements SpecifyType {
    @Override
    public Object def() {
        return false;
    }

    @Override
    public Object random() {
        return RandomUtils.nextBoolean();
    }
}
