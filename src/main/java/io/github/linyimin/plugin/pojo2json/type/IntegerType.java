package io.github.linyimin.plugin.pojo2json.type;

import org.apache.commons.lang3.RandomUtils;

/**
 * @author banzhe
 * @date 2022/11/21 20:27
 **/
public class IntegerType implements SpecifyType {
    @Override
    public Object def() {
        return 0;
    }

    @Override
    public Object random() {
        return RandomUtils.nextInt(0, 100);
    }
}
