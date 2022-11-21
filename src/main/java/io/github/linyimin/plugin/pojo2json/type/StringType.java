package io.github.linyimin.plugin.pojo2json.type;

import java.util.UUID;

/**
 * @author banzhe
 * @date 2022/11/21 20:45
 **/
public class StringType implements SpecifyType {

    @Override
    public Object def() {
        return "";
    }

    @Override
    public Object random() {
        String uuid = UUID.randomUUID().toString();
        return String.format("test_%s", uuid.substring(uuid.lastIndexOf("-") + 1));
    }
}
