package io.github.linyimin.plugin.pojo2json.type;

import java.util.UUID;

/**
 * @author banzhe
 * @date 2022/11/21 20:47
 **/
public class UUIDType implements SpecifyType {
    @Override
    public Object def() {
        return UUID.randomUUID().toString();
    }

    @Override
    public Object random() {
        return UUID.randomUUID().toString();
    }
}
